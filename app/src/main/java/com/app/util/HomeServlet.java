package com.app.util;

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
        RequestParamContext requestContext = new RequestParamContext(req);
        String ctxPath = requestContext.contextPath();

        // Safely obtain username from session (may be null)
        String user = UserDataService.currentUser(req);

        // Determine admin link if the user is an ADMIN
        boolean showAdminLink = UserDataService.isAdmin(req.getServletContext(), user);

        Map<String, String> vals = new HashMap<>();
        vals.put("ctx", TemplateRenderer.escapeHtml(ctxPath));
        vals.put("user", TemplateRenderer.escapeHtml(user));
        vals.put("admin_href", TemplateRenderer.escapeHtml(requestContext.appPath("/admin/users")));
        vals.put("admin_style", showAdminLink ? "" : " style=\"display:none\"");

        String html = TemplateRenderer.render(req.getServletContext(), "/WEB-INF/templates/home.html", vals);
        ServletResponseUtil.writeHtml(resp, html);
    }
}
