package com.app.util;

import java.io.IOException;

import javax.sql.DataSource;

import com.app.dao.UserDao;

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletContext;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "LogoutServlet", urlPatterns = {"/logout"})
public class LogoutServlet extends HttpServlet {

    private static final String REMEMBER_COOKIE = "remember";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
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
        UserDao dao = resolveUserDao(req);
        if (cookies != null) {
            for (Cookie c : cookies) {
                if (REMEMBER_COOKIE.equals(c.getName())) {
                    // delete from DB if we know user
                    try {
                        if (dao != null) {
                            // If token present and user unknown, also attempt to delete by token
                            dao.deleteRememberToken(c.getValue());
                        }
                    } catch (Exception ignored) {
                    }
                    Cookie deleteCookie = new Cookie(REMEMBER_COOKIE, "");
                    deleteCookie.setPath(requestContext.contextPath().isEmpty() ? "/" : requestContext.contextPath());
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
                if (dao != null) {
                    dao.deleteTokensForUser(user);
                }
            } catch (Exception ignored) {
            }
        }

        resp.sendRedirect("/login");
    }

    private static UserDao resolveUserDao(HttpServletRequest req) {
        if (req == null) {
            return null;
        }
        ServletContext context = req.getServletContext();
        if (context == null) {
            return null;
        }
        Object dataSource = context.getAttribute("datasource");
        if (!(dataSource instanceof DataSource)) {
            return null;
        }
        return new UserDao((DataSource) dataSource);
    }
}
