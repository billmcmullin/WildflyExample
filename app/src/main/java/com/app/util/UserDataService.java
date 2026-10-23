package com.app.util;

import javax.sql.DataSource;

import com.app.dao.UserDao;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public final class UserDataService {
    private static final String EMPTY_TEXT = "";
    private static final String TEXT_SERVLET_CONTEXT_IS_REQUIRED = "Servlet context is required";
    private static final String TEXT_DATA_SOURCE_IS_NOT_CONFIGURED = "DataSource is not configured";


    public static final String CONTEXT_DATASOURCE = "datasource";
    public static final String SESSION_USER = "user";
    public static final String ROLE_ADMIN = "ADMIN";

    private UserDataService() {
    }

    public static String currentUser(HttpServletRequest request) {
        if (request == null) {
            return UserDataService.EMPTY_TEXT;
        }
        HttpSession session = request.getSession(false);
        if (session == null) {
            return UserDataService.EMPTY_TEXT;
        }
        Object user = session.getAttribute(SESSION_USER);
        return user == null ? UserDataService.EMPTY_TEXT : user.toString();
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
        return dao.findPasswordHashByUsername(username);
    }

    public static String findRoleByUsername(ServletContext context, String username) throws Exception {
        UserDao dao = resolveUserDao(context);
        return dao.findRoleByUsername(username);
    }

    public static boolean createUser(ServletContext context, String username, String passwordHash, String role) throws Exception {
        UserDao dao = resolveUserDao(context);
        return dao.createUser(username, passwordHash, role);
    }

    public static boolean createRememberToken(ServletContext context, String token, String username, long expiryMs) throws Exception {
        UserDao dao = resolveUserDao(context);
        return dao.createRememberToken(token, username, expiryMs);
    }

    public static String findUsernameByToken(ServletContext context, String token) throws Exception {
        UserDao dao = resolveUserDao(context);
        return dao.findUsernameByToken(token);
    }

    public static boolean deleteRememberToken(ServletContext context, String token) throws Exception {
        UserDao dao = resolveUserDao(context);
        return dao.deleteRememberToken(token);
    }

    public static void deleteTokensForUser(ServletContext context, String username) throws Exception {
        UserDao dao = resolveUserDao(context);
        dao.deleteTokensForUser(username);
    }

    private static UserDao resolveUserDao(ServletContext context) {
        if (context == null) {
            throw new IllegalStateException(UserDataService.TEXT_SERVLET_CONTEXT_IS_REQUIRED);
        }
        Object dataSource = context.getAttribute(CONTEXT_DATASOURCE);
        if (!(dataSource instanceof DataSource)) {
            throw new IllegalStateException(UserDataService.TEXT_DATA_SOURCE_IS_NOT_CONFIGURED);
        }
        return new UserDao((DataSource) dataSource);
    }
}
