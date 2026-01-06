package com.example.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import jakarta.servlet.ServletContext;

public final class TemplateRenderer {

    private TemplateRenderer() {
    }

// Read template file from WEB-INF/templates and perform simple placeholder replacement.
    public static String render(ServletContext ctx, String templatePath, Map<String, String> values) throws IOException {
        // templatePath example: "/WEB-INF/templates/home.html"
        try (InputStream is = ctx.getResourceAsStream(templatePath)) {
            if (is == null) {
                throw new IOException("Template not found: " + templatePath);
            }
            StringBuilder sb = new StringBuilder();
            try (BufferedReader r = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while ((line = r.readLine()) != null) {
                    sb.append(line).append('\n');
                }
            }
            String out = sb.toString();
            if (values != null) {
                for (Map.Entry<String, String> e : values.entrySet()) {
                    out = out.replace("{{" + e.getKey() + "}}", e.getValue() == null ? "" : e.getValue());
                }
            }
            return out;
        }
    }

// Basic HTML escaper
    public static String escapeHtml(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }

}
