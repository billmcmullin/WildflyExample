package com.example.calculator;

import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "HomeServlet", urlPatterns = {"/home"})
public class HomeServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = resp.getWriter()) {
            out.println("<!doctype html>");
            out.println("<html lang=\"en\"><head><meta charset=\"utf-8\"/>");
            out.println("<title>App Home</title>");
            out.println("<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\"/>");
            out.println("<style>body{font-family:Arial, sans-serif;max-width:960px;margin:24px}nav a{display:inline-block;margin-right:12px;padding:8px 12px;background:#f0f0f0;border-radius:4px;text-decoration:none;color:#111}</style>");
            out.println("</head><body>");
            out.println("<h1>Welcome to App</h1>");
            out.println("<p>This is the hub page. Use the link below to open the calculator:</p>");
            out.println("<nav><a href=\"/calculator\">Open Calculator</a></nav>");
            out.println("<hr><section><h2>Quick links</h2><ul>");
            out.println("<li><a href=\"/calculator\">Calculator</a></li>");
            out.println("<li><a href=\"/api/calc\">Raw Calculator API (GET example)</a> — try <code>?a=2&b=3&op=add</code></li>");
            out.println("</ul></section>");
            out.println("</body></html>");
        }
    }
}
