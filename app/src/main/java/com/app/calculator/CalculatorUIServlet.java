package com.app.calculator;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import com.app.dao.UserDao;
import com.app.util.RequestParamContext;
import com.app.util.ServletResponseUtil;
import com.app.util.TemplateRenderer;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "CalculatorUIServlet", urlPatterns = {"/calculator"})
public class CalculatorUIServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        RequestParamContext requestContext = new RequestParamContext(req);

        String a = requestContext.value("a");
        String b = requestContext.value("b");
        String op = requestContext.value("op");
        String message = null;
        String result = null;

        if (op != null && !op.isBlank()) {
            try {
                BigDecimal aVal = new BigDecimal((a != null) ? a.trim() : "0");
                BigDecimal bVal = new BigDecimal((b != null) ? b.trim() : "0");
                BigDecimal r = CalculatorService.calculate(aVal, bVal, op);
                result = r.toPlainString();
            } catch (NumberFormatException ex) {
                message = "Invalid number format";
            } catch (IllegalArgumentException | ArithmeticException ex) {
                message = ex.getMessage();
            } catch (Exception ex) {
                message = "Server error";
            }
        }

        String ctx = requestContext.contextPath();

        // Get the signed-in username from session (if any)
        String user = "";
        HttpSession session = req.getSession(false);
        if (session != null) {
            Object u = session.getAttribute("user");
            if (u != null) {
                user = u.toString();
            }
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
                // ignore and do not show admin link
            }
        }

        Map<String, String> vals = new HashMap<>();
        vals.put("ctx", TemplateRenderer.escapeHtml(ctx));
        vals.put("user", TemplateRenderer.escapeHtml(user));
        vals.put("admin_href", TemplateRenderer.escapeHtml(requestContext.appPath("/admin/users")));
        vals.put("admin_style", showAdminLink ? "" : " style=\"display:none\"");

        vals.put("a", TemplateRenderer.escapeHtml(a != null ? a : "2"));
        vals.put("b", TemplateRenderer.escapeHtml(b != null ? b : "3"));

        vals.put("op_add", "add".equals(op) ? " selected" : "");
        vals.put("op_sub", "sub".equals(op) ? " selected" : "");
        vals.put("op_mul", "mul".equals(op) ? " selected" : "");
        vals.put("op_div", "div".equals(op) ? " selected" : "");

        String resultText = "";
        if (message != null) {
            resultText = "Error: " + message;
        } else if (result != null) {
            resultText = "Result: " + result;
        }
        vals.put("result_text", TemplateRenderer.escapeHtml(resultText));

        String html;
        try {
            html = TemplateRenderer.render(req.getServletContext(), "/WEB-INF/templates/calculator.html", vals);
        } catch (IOException ex) {
            throw ex;
        }
        ServletResponseUtil.writeHtml(resp, html);
    }
}
