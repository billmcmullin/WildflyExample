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

@WebServlet(name = "HomeServlet", urlPatterns = {"/home"})
public class HomeServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String ctxPath = req.getContextPath(); // e.g. "/app"

        // Safely obtain username from session (may be null)
        String user = "";
        HttpSession session = req.getSession(false);
        if (session != null) {
            Object u = session.getAttribute("user");
            if (u != null) user = u.toString();
        }

        // Determine admin link if the user is an ADMIN
        String adminLink = "";
        if (!user.isEmpty()) {
            try {
                DataSource ds = (DataSource) req.getServletContext().getAttribute("datasource");
                if (ds != null) {
                    UserDao dao = new UserDao(ds);
                    String role = dao.findRoleByUsername(user);
                    if ("ADMIN".equalsIgnoreCase(role)) {
                        adminLink = "<a href=\"" + req.getContextPath() + "/admin/users\">Manage Users</a>";
                    }
                }
            } catch (Exception ignored) {
                // On error, don't expose admin link
            }
        }

        Map<String, String> vals = new HashMap<>();
        vals.put("ctx", TemplateRenderer.escapeHtml(ctxPath));
        vals.put("user", TemplateRenderer.escapeHtml(user));
        // admin_link contains HTML; do not escape here so template renders link markup
        vals.put("admin_link", adminLink);

        String html = TemplateRenderer.render(req.getServletContext(), "/WEB-INF/templates/home.html", vals);
        resp.setContentType("text/html;charset=UTF-8");
        resp.getWriter().write(html);
    }
}
