package com.app.util;

import java.sql.SQLException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.Timeout.ThreadMode;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
/**
 * Parasoft Jtest UTA: Test class for UserDataService
 *
 * @see com.app.util.UserDataService
 * @author bmcmullin
 */
public class UserDataServiceTest
{

    /**
     * Parasoft Jtest UTA: Test for createRememberToken(ServletContext, String, String, long)
     *
     * @see com.app.util.UserDataService#createRememberToken(ServletContext, String, String, long)
     * @author bmcmullin
     */
    @Test
    @Timeout(value = 5, threadMode = ThreadMode.SEPARATE_THREAD)
    public void testCreateRememberToken() throws Throwable
    {
        // When
        ServletContext context = null; // UTA: configured value
        String token = "token"; // UTA: default value
        String username = "username"; // UTA: default value
        long expiryMs = 1L; // UTA: default value
        assertThrows(IllegalStateException.class, () -> {
            UserDataService.createRememberToken(context, token, username, expiryMs);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for createRememberToken(ServletContext, String, String, long)
     *
     * @see com.app.util.UserDataService#createRememberToken(ServletContext, String, String, long)
     * @author bmcmullin
     */
    @Test
    @Timeout(value = 5, threadMode = ThreadMode.SEPARATE_THREAD)
    public void testCreateRememberToken2() throws Throwable
    {
        // When
        ServletContext context = mock(ServletContext.class);
        Object getAttributeResult = null; // UTA: configured value
        when(context.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        String token = "token"; // UTA: default value
        String username = "username"; // UTA: default value
        long expiryMs = 1L; // UTA: default value
        assertThrows(IllegalStateException.class, () -> {
            UserDataService.createRememberToken(context, token, username, expiryMs);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for currentUser(HttpServletRequest)
     *
     * @see com.app.util.UserDataService#currentUser(HttpServletRequest)
     * @author bmcmullin
     */
    @Test
    @Timeout(value = 5, threadMode = ThreadMode.SEPARATE_THREAD)
    public void testCurrentUser() throws Throwable
    {
        // When
        HttpServletRequest request = null; // UTA: configured value
        String result = UserDataService.currentUser(request);

        // Then - assertions for result of method currentUser(HttpServletRequest)
        assertEquals("", result);

    }

    /**
     * Parasoft Jtest UTA: Test for currentUser(HttpServletRequest)
     *
     * @see com.app.util.UserDataService#currentUser(HttpServletRequest)
     * @author bmcmullin
     */
    @Test
    @Timeout(value = 5, threadMode = ThreadMode.SEPARATE_THREAD)
    public void testCurrentUser2() throws Throwable
    {
        // When
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession getSessionResult = null; // UTA: configured value
        when(request.getSession(anyBoolean())).thenReturn(getSessionResult);
        String result = UserDataService.currentUser(request);

        // Then - assertions for result of method currentUser(HttpServletRequest)
        assertEquals("", result);

    }

    /**
     * Parasoft Jtest UTA: Test for currentUser(HttpServletRequest)
     *
     * @see com.app.util.UserDataService#currentUser(HttpServletRequest)
     * @author bmcmullin
     */
    @Test
    @Timeout(value = 5, threadMode = ThreadMode.SEPARATE_THREAD)
    public void testCurrentUser3() throws Throwable
    {
        // When
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession getSessionResult = mock(HttpSession.class);
        Object getAttributeResult = new Object(); // UTA: default value
        when(getSessionResult.getAttribute(nullable(String.class))).thenReturn(getAttributeResult);
        when(request.getSession(anyBoolean())).thenReturn(getSessionResult);
        String result = UserDataService.currentUser(request);

        // Then - assertions for result of method currentUser(HttpServletRequest)
        assertEquals("java.lang.Object@37aa13b2", result);

    }

    /**
     * Parasoft Jtest UTA: Test for isAdmin(ServletContext, String)
     *
     * @see com.app.util.UserDataService#isAdmin(ServletContext, String)
     * @author bmcmullin
     */
    @Test
    @Timeout(value = 5, threadMode = ThreadMode.SEPARATE_THREAD)
    public void testIsAdmin() throws Throwable
    {
        // When
        ServletContext context = mock(ServletContext.class);
        String username = null; // UTA: configured value
        boolean result = UserDataService.isAdmin(context, username);

        // Then - assertions for result of method isAdmin(ServletContext, String)
        assertFalse(result);

    }

    /**
     * Parasoft Jtest UTA: Test for isAdmin(ServletContext, String)
     *
     * @see com.app.util.UserDataService#isAdmin(ServletContext, String)
     * @author bmcmullin
     */
    @Test
    @Timeout(value = 5, threadMode = ThreadMode.SEPARATE_THREAD)
    public void testIsAdmin2() throws Throwable
    {
        // When
        ServletContext context = mock(ServletContext.class);
        String username = "username"; // UTA: configured value
        boolean result = UserDataService.isAdmin(context, username);

        // Then - assertions for result of method isAdmin(ServletContext, String)
        assertFalse(result);

    }

}
