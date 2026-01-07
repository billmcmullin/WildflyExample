package com.example.app.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
/**
 * Parasoft Jtest UTA: Test class for PasswordUtil
 *
 * @see com.example.app.util.PasswordUtil
 * @author bmcmullin
 */
public class PasswordUtilTest
{

    /**
     * Parasoft Jtest UTA: Test for hashPassword(String)
     *
     * @see com.example.app.util.PasswordUtil#hashPassword(String)
     * @author bmcmullin
     */
    @Test
    public void testHashPassword() throws Throwable
    {
        // When
        String password = "password"; // UTA: default value
        String result = PasswordUtil.hashPassword(password);

        // Then - assertions for result of method hashPassword(String)
        assertEquals("5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8", result);

    }

    /**
     * Parasoft Jtest UTA: Test for verify(String, String)
     *
     * @see com.example.app.util.PasswordUtil#verify(String, String)
     * @author bmcmullin
     */
    @Test
    public void testVerify() throws Throwable
    {
        // When
        String plain = "plain"; // UTA: default value
        String hash = "hash"; // UTA: default value
        boolean result = PasswordUtil.verify(plain, hash);

    }

}
