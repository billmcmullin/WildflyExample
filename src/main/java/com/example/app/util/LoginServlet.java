package com.example.app.util;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import com.example.app.dao.UserDao;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "LoginServlet", urlPatterns = {"/login"})
public class LoginServlet extends HttpServlet {

    private static final int SESSION_TIMEOUT_SECONDS = 30 * 60; // 30 minutes
    private static final int REMEMBER_DAYS = 7; // remember-me token valid for 7 days

    private static final SecureRandom secureRandom = new SecureRandom();

    private String generateToken() {
        return new BigInteger(160, secureRandom).toString(32);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String redirect = req.getParameter("redirect");
        Map<String, String> vals = new HashMap<>();
        vals.put("ctx", TemplateRenderer.escapeHtml(req.getContextPath()));
        vals.put("message", "");
        vals.put("redirect", redirect != null ? TemplateRenderer.escapeHtml(redirect) : "");
        String html;
        try {
            html = TemplateRenderer.render(req.getServletContext(), "/WEB-INF/templates/login.html", vals);
        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Template render error");
            return;
        }
        resp.setContentType("text/html;charset=UTF-8");
        resp.getWriter().write(html);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String user = req.getParameter("username");
        String pass = req.getParameter("password");
        String redirect = req.getParameter("redirect");
        String remember = req.getParameter("remember"); // "on" if checked

        DataSource ds = (DataSource) req.getServletContext().getAttribute("datasource");
        try {
            UserDao dao = new UserDao(ds);
            String storedHash = dao.findPasswordHashByUsername(user);
            if (storedHash != null && PasswordUtil.verify(pass, storedHash)) {
                HttpSession session = req.getSession(true);
                session.setAttribute("user", user);
                session.setMaxInactiveInterval(SESSION_TIMEOUT_SECONDS);

                // if remember checked -> create token
                if ("on".equalsIgnoreCase(remember)) {
                    String token = generateToken();
                    long expiry = System.currentTimeMillis() + REMEMBER_DAYS * 24L * 3600L * 1000L;
                    dao.createRememberToken(token, user, expiry);

                    Cookie cookie = new Cookie("remember", token);
                    cookie.setHttpOnly(true);
                    cookie.setPath(req.getContextPath().isEmpty() ? "/" : req.getContextPath());
                    cookie.setMaxAge((int) (expiry - System.currentTimeMillis()) / 1000);
                    // cookie.setSecure(true); // enable when using HTTPS
                    resp.addCookie(cookie);
                }

                // redirect to requested resource if present
                if (redirect != null && !redirect.isBlank()) {
                    resp.sendRedirect(redirect);
                } else {
                    resp.sendRedirect(req.getContextPath() + "/home");
                }
                return;
            } else {
                // authentication failed -> re-render login with message
                Map<String, String> vals = new HashMap<>();
                vals.put("ctx", TemplateRenderer.escapeHtml(req.getContextPath()));
                vals.put("message", "Invalid username or password.");
                vals.put("redirect", redirect != null ? TemplateRenderer.escapeHtml(redirect) : "");
                String html = TemplateRenderer.render(req.getServletContext(), "/WEB-INF/templates/login.html", vals);
                resp.setContentType("text/html;charset=UTF-8");
                resp.getWriter().write(html);
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
