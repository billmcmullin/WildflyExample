package com.example.app;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "CalculatorUIServlet", urlPatterns = {"/calculator"}) public class CalculatorUIServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String a = req.getParameter("a");
        String b = req.getParameter("b");
        String op = req.getParameter("op");
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

        String ctx = req.getContextPath(); // "/app"

        // Get the signed-in username from session (if any)
        String user = "";
        HttpSession session = req.getSession(false);
        if (session != null) {
            Object u = session.getAttribute("user");
            if (u != null) {
                user = u.toString();
            }
        }

        Map<String, String> vals = new HashMap<>();
        vals.put("ctx", TemplateRenderer.escapeHtml(ctx));
        vals.put("user", TemplateRenderer.escapeHtml(user));
        vals.put("a", TemplateRenderer.escapeHtml(a != null ? a : "2"));
        vals.put("b", TemplateRenderer.escapeHtml(b != null ? b : "3"));

        vals.put("op_add", "add".equals(op) ? " selected" : "");
        vals.put("op_sub", "sub".equals(op) ? " selected" : "");
        vals.put("op_mul", "mul".equals(op) ? " selected" : "");
        vals.put("op_div", "div".equals(op) ? " selected" : "");

        String resultBlock = "";
        if (message != null) {
            resultBlock = "<div class=\"result\">Error: " + TemplateRenderer.escapeHtml(message) + "</div>";
        } else if (result != null) {
            resultBlock = "<div class=\"result\">Result: " + TemplateRenderer.escapeHtml(result) + "</div>";
        }
        vals.put("result_block", resultBlock);

        String html = TemplateRenderer.render(req.getServletContext(), "/WEB-INF/templates/calculator.html", vals);
        resp.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = resp.getWriter()) {
            out.write(html);
        }
    }

}
