package com.app.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import com.app.dao.UserDao;

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
        String user = "";
        HttpSession session = req.getSession(false);
        if (session != null) {
            Object u = session.getAttribute("user");
            if (u != null) user = u.toString();
        }

        // Determine admin link if the user is an ADMIN
        boolean showAdminLink = false;
        if (!user.isEmpty()) {
            try {
                DataSource ds = (DataSource) req.getServletContext().getAttribute("datasource");
                if (ds != null) {
                    UserDao dao = new UserDao(ds);
                    String role = dao.findRoleByUsername(user);
                    if ("ADMIN".equalsIgnoreCase(role)) {
                        showAdminLink = true;
                    }
                }
            } catch (Exception ignored) {
                // On error, don't expose admin link
            }
        }

        Map<String, String> vals = new HashMap<>();
        vals.put("ctx", TemplateRenderer.escapeHtml(ctxPath));
        vals.put("user", TemplateRenderer.escapeHtml(user));
        vals.put("admin_href", TemplateRenderer.escapeHtml(requestContext.appPath("/admin/users")));
        vals.put("admin_style", showAdminLink ? "" : " style=\"display:none\"");

        String html;
        try {
            html = TemplateRenderer.render(req.getServletContext(), "/WEB-INF/templates/home.html", vals);
        } catch (IOException ex) {
            throw ex;
        }
        ServletResponseUtil.writeHtml(resp, html);
    }
}
