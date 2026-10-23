package com.app.admin;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;

import com.app.util.RequestParamContext;
import com.app.util.AppWebConstants;
import com.app.util.PasswordUtil;
import com.app.util.ServletResponseUtil;
import com.app.util.TemplateRenderer;
import com.app.util.UserDataService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "CreateUserServlet", urlPatterns = {"/admin/users"})
public class CreateUserServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final String ROLE_USER = "USER";
    private static final String TEMPLATE_PATH = "/WEB-INF/templates/createuser.html";
    private static final String MESSAGE_REDIRECT_FAILED = "Redirect failed";
    private static final String MESSAGE_TEMPLATE_RENDER_ERROR = "Template render error";
    private static final String KEY_CTX = com.app.util.AppWebConstants.CONTEXT_KEY;
    private static final String KEY_MESSAGE = "message";
    private static final String KEY_USER = com.app.util.AppWebConstants.USER_KEY;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
        // ensure admin
        HttpSession session = req.getSession(false);
        String user = session != null ? (String) session.getAttribute(CreateUserServlet.KEY_USER) : null;
        if (user == null) {
            try {
                resp.sendRedirect(AppWebConstants.LOGIN_PATH);
            } catch (IOException ex) {
                ServletResponseUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, MESSAGE_REDIRECT_FAILED);
            }
            return;
        }
        try {
            String role = UserDataService.findRoleByUsername(req.getServletContext(), user);
            if (!UserDataService.ROLE_ADMIN.equals(role)) {
                ServletResponseUtil.sendError(resp, HttpServletResponse.SC_FORBIDDEN);
                return;
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
        renderForm(req, resp, user, "");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
        HttpSession session = req.getSession(false);
        String adminUser = session != null ? (String) session.getAttribute(CreateUserServlet.KEY_USER) : null;
        RequestParamContext requestContext = new RequestParamContext(req);
        if (adminUser == null) {
            try {
                resp.sendRedirect(AppWebConstants.LOGIN_PATH);
            } catch (IOException ex) {
                ServletResponseUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, MESSAGE_REDIRECT_FAILED);
            }
            return;
        }

        try {
            String role = UserDataService.findRoleByUsername(req.getServletContext(), adminUser);
            if (!UserDataService.ROLE_ADMIN.equals(role)) {
                ServletResponseUtil.sendError(resp, HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            String username = requestContext.value("username");
            String password = requestContext.value("password");
            String newRole = requestContext.value("role");
            if (username == null || username.isBlank() || password == null || password.isBlank()) {
                renderForm(req, resp, adminUser, "Username and password are required.");
                return;
            }

            String normalizedRole = newRole != null && !newRole.isBlank() ? newRole : ROLE_USER;
            boolean ok = UserDataService.createUser(req.getServletContext(), username.trim(), PasswordUtil.hashPassword(password), normalizedRole);
            if (ok) {
                try {
                    resp.sendRedirect(AppWebConstants.HOME_PATH);
                } catch (IOException ex) {
                    ServletResponseUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, MESSAGE_REDIRECT_FAILED);
                }
            } else {
                renderForm(req, resp, adminUser, "Failed to create user (maybe already exists).");
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private void renderForm(HttpServletRequest req, HttpServletResponse resp, String username, String message) {
        Map<String, String> values = new HashMap<>();
        values.put(KEY_CTX, TemplateRenderer.escapeHtml(req.getContextPath()));
        values.put(KEY_MESSAGE, TemplateRenderer.escapeHtml(message));
        values.put(KEY_USER, TemplateRenderer.escapeHtml(username == null ? "" : username));

        try {
            String html = TemplateRenderer.render(req.getServletContext(), TEMPLATE_PATH, values);
            ServletResponseUtil.writeHtml(resp, html);
        } catch (Exception ex) {
            ServletResponseUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, MESSAGE_TEMPLATE_RENDER_ERROR);
        }
    }

    private void readObject(ObjectInputStream in) throws IOException {
        throw new NotSerializableException("CreateUserServlet");
    }
}
