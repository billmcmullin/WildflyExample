package com.example.app;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "LoginServlet", urlPatterns = {"/login"}) public class LoginServlet extends HttpServlet {

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

        DataSource ds = (DataSource) req.getServletContext().getAttribute("datasource");
        try {
            UserDao dao = new UserDao(ds);
            String storedHash = dao.findPasswordHashByUsername(user);
            if (storedHash != null && PasswordUtil.verify(pass, storedHash)) {
                HttpSession session = req.getSession(true);
                session.setAttribute("user", user);
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
