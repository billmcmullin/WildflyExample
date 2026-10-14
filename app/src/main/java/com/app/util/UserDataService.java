package com.app.util;

import javax.sql.DataSource;

import com.app.dao.UserDao;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public final class UserDataService {

    public static final String CONTEXT_DATASOURCE = "datasource";
    public static final String SESSION_USER = "user";
    public static final String ROLE_ADMIN = "ADMIN";

    private UserDataService() {
    }

    public static String currentUser(HttpServletRequest request) {
        if (request == null) {
            return "";
        }
        HttpSession session = request.getSession(false);
        if (session == null) {
            return "";
        }
        Object user = session.getAttribute(SESSION_USER);
        return user == null ? "" : user.toString();
    }

    public static boolean isAdmin(ServletContext context, String username) {
        if (username == null || username.isBlank()) {
            return false;
        }
        try {
            String role = findRoleByUsername(context, username);
            return ROLE_ADMIN.equalsIgnoreCase(role);
        } catch (Exception ignored) {
            return false;
        }
    }

    public static String findPasswordHashByUsername(ServletContext context, String username) throws Exception {
        UserDao dao = resolveUserDao(context);
        return dao == null ? null : dao.findPasswordHashByUsername(username);
    }

    public static String findRoleByUsername(ServletContext context, String username) throws Exception {
        UserDao dao = resolveUserDao(context);
        return dao == null ? null : dao.findRoleByUsername(username);
    }

    public static boolean createUser(ServletContext context, String username, String passwordHash, String role) throws Exception {
        UserDao dao = resolveUserDao(context);
        return dao != null && dao.createUser(username, passwordHash, role);
    }

    public static boolean createRememberToken(ServletContext context, String token, String username, long expiryMs) throws Exception {
        UserDao dao = resolveUserDao(context);
        return dao != null && dao.createRememberToken(token, username, expiryMs);
    }

    public static String findUsernameByToken(ServletContext context, String token) throws Exception {
        UserDao dao = resolveUserDao(context);
        return dao == null ? null : dao.findUsernameByToken(token);
    }

    public static boolean deleteRememberToken(ServletContext context, String token) throws Exception {
        UserDao dao = resolveUserDao(context);
        return dao != null && dao.deleteRememberToken(token);
    }

    public static void deleteTokensForUser(ServletContext context, String username) throws Exception {
        UserDao dao = resolveUserDao(context);
        if (dao != null) {
            dao.deleteTokensForUser(username);
        }
    }

    private static UserDao resolveUserDao(ServletContext context) {
        if (context == null) {
            throw new IllegalStateException("Servlet context is required");
        }
        Object dataSource = context.getAttribute(CONTEXT_DATASOURCE);
        if (!(dataSource instanceof DataSource)) {
            throw new IllegalStateException("DataSource is not configured");
        }
        return new UserDao((DataSource) dataSource);
    }
}
