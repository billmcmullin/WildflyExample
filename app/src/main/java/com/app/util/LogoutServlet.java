package com.app.util;

import java.io.IOException;

import javax.sql.DataSource;

import com.app.dao.UserDao;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "LogoutServlet", urlPatterns = {"/logout"})
public class LogoutServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
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
                if ("remember".equals(c.getName())) {
                    // delete from DB if we know user
                    try {
                        DataSource ds = (DataSource) req.getServletContext().getAttribute("datasource");
                        if (ds != null) {
                            UserDao dao = new UserDao(ds);
                            // If token present and user unknown, also attempt to delete by token
                            dao.deleteRememberToken(c.getValue());
                        }
                    } catch (Exception ignored) {
                    }
                    c.setValue("");
                    c.setPath(req.getContextPath().isEmpty() ? "/" : req.getContextPath());
                    c.setMaxAge(0);
                    resp.addCookie(c);
                }
            }
        }

        // Also delete any tokens for the user (in case they logged out)
        if (user != null) {
            try {
                DataSource ds = (DataSource) req.getServletContext().getAttribute("datasource");
                if (ds != null) {
                    UserDao dao = new UserDao(ds);
                    dao.deleteTokensForUser(user);
                }
            } catch (Exception ignored) {
            }
        }

        resp.sendRedirect(req.getContextPath() + "/login");
    }
}
