package com.app.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;

import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.jupiter.api.Test;

import com.app.support.JdbcTestSupport;

class UserDaoTest {

    @Test
    void findPasswordHashAndRoleByUsername() throws Exception {
        JDBCDataSource ds = JdbcTestSupport.newDataSource();
        JdbcTestSupport.insertUser(ds, "alice", "hash1", "ADMIN");

        UserDao dao = new UserDao(ds);
        assertEquals("hash1", dao.findPasswordHashByUsername("alice"));
        assertEquals("ADMIN", dao.findRoleByUsername("alice"));
        assertNull(dao.findPasswordHashByUsername("missing"));
        assertNull(dao.findRoleByUsername("missing"));
    }

    @Test
    void createUserDefaultsRoleToUser() throws Exception {
        JDBCDataSource ds = JdbcTestSupport.newDataSource();
        UserDao dao = new UserDao(ds);

        assertTrue(dao.createUser("bob", "hash2", null));
        assertEquals("USER", JdbcTestSupport.roleByUsername(ds, "bob"));
    }

    @Test
    void createRememberTokenAndFindUsernameByToken() throws Exception {
        JDBCDataSource ds = JdbcTestSupport.newDataSource();
        UserDao dao = new UserDao(ds);
        long future = Instant.now().toEpochMilli() + 60_000L;

        assertTrue(dao.createRememberToken("tok1", "alice", future));
        assertEquals("alice", dao.findUsernameByToken("tok1"));
    }

    @Test
    void findUsernameByTokenDeletesExpiredToken() throws Exception {
        JDBCDataSource ds = JdbcTestSupport.newDataSource();
        UserDao dao = new UserDao(ds);

        JdbcTestSupport.insertToken(ds, "expired", "alice", 1L);

        assertNull(dao.findUsernameByToken("expired"));
        assertFalse(dao.deleteRememberToken("expired"));
    }

    @Test
    void deleteAndCleanupTokenMethods() throws Exception {
        JDBCDataSource ds = JdbcTestSupport.newDataSource();
        UserDao dao = new UserDao(ds);
        long now = Instant.now().toEpochMilli();

        JdbcTestSupport.insertToken(ds, "tokA", "alice", now + 100_000L);
        JdbcTestSupport.insertToken(ds, "tokB", "alice", now - 100_000L);

        assertTrue(dao.deleteRememberToken("tokA"));
        dao.deleteTokensForUser("alice");
        assertEquals(0, JdbcTestSupport.countTokensForUser(ds, "alice"));

        JdbcTestSupport.insertToken(ds, "tokC", "alice", now - 100_000L);
        dao.cleanExpiredTokens();
        assertEquals(0, JdbcTestSupport.countTokensForUser(ds, "alice"));
    }
}
