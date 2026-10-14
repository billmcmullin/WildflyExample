package com.app.util;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "LogoutServlet", urlPatterns = {"/logout"})
public class LogoutServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final String REMEMBER_COOKIE = "remember";
    private static final String ERROR_REDIRECT_FAILED = "Redirect failed";
    private static final String CLASS_NAME = "LogoutServlet";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
        RequestParamContext requestContext = new RequestParamContext(req);

        // remove remember cookie and delete tokens for this user
        HttpSession s = req.getSession(false);
        String user = null;
        if (s != null) {
            Object u = s.getAttribute("user");
            if (u != null) {
                user = u.toString();
            }
            s.invalidate();
        }

        // remove remember cookie from client and DB
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if (REMEMBER_COOKIE.equals(c.getName())) {
                    // delete from DB if we know user
                    try {
                        // If token present and user unknown, also attempt to delete by token
                        UserDataService.deleteRememberToken(req.getServletContext(), c.getValue());
                    } catch (Exception ignored) {
                    }
                    Cookie deleteCookie = new Cookie(REMEMBER_COOKIE, "");
                    deleteCookie.setPath(requestContext.contextPath().isEmpty() ? AppWebConstants.ROOT_PATH : requestContext.contextPath());
                    deleteCookie.setMaxAge(0);
                    deleteCookie.setHttpOnly(true);
                    deleteCookie.setSecure(req.isSecure());
                    resp.addCookie(deleteCookie);
                }
            }
        }

        // Also delete any tokens for the user (in case they logged out)
        if (user != null) {
            try {
                UserDataService.deleteTokensForUser(req.getServletContext(), user);
            } catch (Exception ignored) {
            }
        }

        try {
            resp.sendRedirect(AppWebConstants.LOGIN_PATH);
        } catch (IOException ex) {
            ServletResponseUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ERROR_REDIRECT_FAILED);
        }
    }

    private final void writeObject(ObjectOutputStream out) throws IOException {
        throw new NotSerializableException(CLASS_NAME);
    }

    private final void readObject(ObjectInputStream in) throws IOException {
        throw new NotSerializableException(CLASS_NAME);
    }
}
