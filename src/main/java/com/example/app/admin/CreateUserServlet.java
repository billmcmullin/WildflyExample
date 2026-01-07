package com.example.app.admin;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import com.example.app.dao.UserDao;
import com.example.app.util.PasswordUtil;
import com.example.app.util.TemplateRenderer;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "CreateUserServlet", urlPatterns = {"/admin/users"})
public class CreateUserServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // ensure admin
        HttpSession session = req.getSession(false);
        String user = session != null ? (String) session.getAttribute("user") : null;
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        try {
            DataSource ds = (DataSource) req.getServletContext().getAttribute("datasource");
            UserDao dao = new UserDao(ds);
            String role = dao.findRoleByUsername(user);
            if (!"ADMIN".equals(role)) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }

        Map<String, String> vals = new HashMap<>();
        vals.put("ctx", TemplateRenderer.escapeHtml(req.getContextPath()));
        vals.put("message", "");
        String html = TemplateRenderer.render(req.getServletContext(), "/WEB-INF/templates/createuser.html", vals);
        resp.setContentType("text/html;charset=UTF-8");
        resp.getWriter().write(html);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        String adminUser = session != null ? (String) session.getAttribute("user") : null;
        if (adminUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        try {
            DataSource ds = (DataSource) req.getServletContext().getAttribute("datasource");
            UserDao dao = new UserDao(ds);
            String role = dao.findRoleByUsername(adminUser);
            if (!"ADMIN".equals(role)) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            String username = req.getParameter("username");
            String password = req.getParameter("password");
            String newRole = req.getParameter("role");
            if (username == null || username.isBlank() || password == null || password.isBlank()) {
                Map<String, String> vals = new HashMap<>();
                vals.put("ctx", TemplateRenderer.escapeHtml(req.getContextPath()));
                vals.put("message", "Username and password are required.");
                String html = TemplateRenderer.render(req.getServletContext(), "/WEB-INF/templates/createuser.html", vals);
                resp.setContentType("text/html;charset=UTF-8");
                resp.getWriter().write(html);
                return;
            }

            boolean ok = dao.createUser(username.trim(), PasswordUtil.hashPassword(password), newRole != null && !newRole.isBlank() ? newRole : "USER");
            if (ok) {
                resp.sendRedirect(req.getContextPath() + "/home");
            } else {
                Map<String, String> vals = new HashMap<>();
                vals.put("ctx", TemplateRenderer.escapeHtml(req.getContextPath()));
                vals.put("message", "Failed to create user (maybe already exists).");
                String html = TemplateRenderer.render(req.getServletContext(), "/WEB-INF/templates/createuser.html", vals);
                resp.setContentType("text/html;charset=UTF-8");
                resp.getWriter().write(html);
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
