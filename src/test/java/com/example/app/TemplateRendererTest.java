package com.example.app;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import jakarta.servlet.ServletContext;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
/**
 * Parasoft Jtest UTA: Test class for TemplateRenderer
 *
 * @see com.example.app.TemplateRenderer
 * @author bmcmullin
 */
public class TemplateRendererTest
{

    /**
     * Parasoft Jtest UTA: Test for escapeHtml(String)
     *
     * @see com.example.app.TemplateRenderer#escapeHtml(String)
     * @author bmcmullin
     */
    @Test
    public void testEscapeHtml() throws Throwable
    {
        // When
        String s = null; // UTA: configured value
        String result = TemplateRenderer.escapeHtml(s);

    }

    /**
     * Parasoft Jtest UTA: Test for escapeHtml(String)
     *
     * @see com.example.app.TemplateRenderer#escapeHtml(String)
     * @author bmcmullin
     */
    @Test
    public void testEscapeHtml2() throws Throwable
    {
        // When
        String s = "s"; // UTA: default value
        String result = TemplateRenderer.escapeHtml(s);

    }

    /**
     * Parasoft Jtest UTA: Test for render(ServletContext, String, Map)
     *
     * @see com.example.app.TemplateRenderer#render(ServletContext, String, Map)
     * @author bmcmullin
     */
    @Test
    public void testRender() throws Throwable
    {
        // When
        ServletContext ctx = mock(ServletContext.class);
        InputStream getResourceAsStreamResult = null; // UTA: configured value
        when(ctx.getResourceAsStream(nullable(String.class))).thenReturn(getResourceAsStreamResult);
        String templatePath = "templatePath"; // UTA: default value
        Map<String, String> values = new HashMap<String, String>(); // UTA: default value
        String key = "key"; // UTA: default value
        String value = "value"; // UTA: default value
        values.put(key, value);
        assertThrows(IOException.class, () -> {
            TemplateRenderer.render(ctx, templatePath, values);
        });

    }
}
