package com.app.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import jakarta.servlet.ServletContext;

public final class TemplateRenderer {

    private static final String TEMPLATE_PREFIX = "{{";
    private static final String TEMPLATE_SUFFIX = "}}";
    private static final String EMPTY = "";
    private static final String ERROR_TEMPLATE_NOT_FOUND = "Template not found: ";
    private static final String HTML_ESCAPE_AMP = "&amp;";
    private static final String HTML_ESCAPE_LT = "&lt;";
    private static final String HTML_ESCAPE_GT = "&gt;";
    private static final String HTML_ESCAPE_QUOTE = "&quot;";
    private static final String HTML_ESCAPE_APOS = "&#39;";

    private TemplateRenderer() {
    }

// Read template file from WEB-INF/templates and perform simple placeholder replacement.
    public static String render(ServletContext ctx, String templatePath, Map<String, String> values) throws IOException {
        // templatePath example: "/WEB-INF/templates/home.html"
        try (InputStream is = ctx.getResourceAsStream(templatePath)) {
            if (is == null) {
                throw new IOException(ERROR_TEMPLATE_NOT_FOUND + templatePath);
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
                    String placeholder = TEMPLATE_PREFIX + e.getKey() + TEMPLATE_SUFFIX;
                    out = out.replace(placeholder, e.getValue() == null ? EMPTY : e.getValue());
                }
            }
            return out;
        }
    }

// Basic HTML escaper
    public static String escapeHtml(String s) {
        if (s == null) {
            return EMPTY;
        }
        StringBuilder out = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '&':
                    out.append(HTML_ESCAPE_AMP);
                    break;
                case '<':
                    out.append(HTML_ESCAPE_LT);
                    break;
                case '>':
                    out.append(HTML_ESCAPE_GT);
                    break;
                case '"':
                    out.append(HTML_ESCAPE_QUOTE);
                    break;
                case '\'':
                    out.append(HTML_ESCAPE_APOS);
                    break;
                default:
                    out.append(c);
            }
        }
        return out.toString();
    }

}
