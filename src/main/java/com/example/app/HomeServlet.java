package com.example.app;

import java.io.IOException;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "HomeServlet", urlPatterns = {"/home"}) public class HomeServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String ctxPath = req.getContextPath();
        Map<String, String> vals = Map.of("ctx", TemplateRenderer.escapeHtml(ctxPath));
        String html = TemplateRenderer.render(req.getServletContext(), "/WEB-INF/templates/home.html", vals);
        resp.setContentType("text/html;charset=UTF-8");
        resp.getWriter().write(html);
    }

}
