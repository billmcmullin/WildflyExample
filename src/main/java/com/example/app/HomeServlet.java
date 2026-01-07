package com.example.app;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
            if (u != null) {
                user = u.toString();
            }
        }

        Map<String, String> vals = new HashMap<>();
        vals.put("ctx", TemplateRenderer.escapeHtml(ctxPath));
        vals.put("user", TemplateRenderer.escapeHtml(user));

        String html = TemplateRenderer.render(req.getServletContext(), "/WEB-INF/templates/home.html", vals);
        resp.setContentType("text/html;charset=UTF-8");
        resp.getWriter().write(html);
    }
}
