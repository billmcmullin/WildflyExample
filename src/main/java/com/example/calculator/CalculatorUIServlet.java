package com.example.calculator;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "CalculatorUIServlet", urlPatterns = {"/calculator"})
public class CalculatorUIServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // If query params present, compute and show result; otherwise show empty form
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

        resp.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = resp.getWriter()) {
            out.println("<!doctype html>");
            out.println("<html lang=\"en\"><head><meta charset=\"utf-8\"/>");
            out.println("<title>Calculator</title>");
            out.println("<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\"/>");
            out.println("<style>body{font-family:Arial, sans-serif;max-width:720px;margin:24px;}label,input,select,button{display:block;margin:8px 0}.result{margin-top:12px;padding:8px;background:#f6f6f6;border:1px solid #ddd}</style>");
            out.println("</head><body>");
            out.println("<h1>Calculator</h1>");
            out.println("<form method=\"get\" action=\"/calculator\">");
            out.printf("<label>A: <input name=\"a\" value=\"%s\"/></label>%n", a != null ? escapeHtml(a) : "2");
            out.printf("<label>B: <input name=\"b\" value=\"%s\"/></label>%n", b != null ? escapeHtml(b) : "3");
            out.println("<label>Operation: <select name=\"op\">");
            out.printf("<option value=\"add\"%s>add</option>%n", "add".equals(op) ? " selected" : "");
            out.printf("<option value=\"sub\"%s>sub</option>%n", "sub".equals(op) ? " selected" : "");
            out.printf("<option value=\"mul\"%s>mul</option>%n", "mul".equals(op) ? " selected" : "");
            out.printf("<option value=\"div\"%s>div</option>%n", "div".equals(op) ? " selected" : "");
            out.println("</select></label>");
            out.println("<button type=\"submit\">Calculate</button>");
            out.println("</form>");

            if (message != null) {
                out.printf("<div class=\"result\">Error: %s</div>%n", escapeHtml(message));
            } else if (result != null) {
                out.printf("<div class=\"result\">Result: %s</div>%n", escapeHtml(result));
            }

            out.println("<p><a href=\"/home\">Back to Home</a></p>");
            out.println("</body></html>");
        }
    }

    // Simple HTML escaper
    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }
}
