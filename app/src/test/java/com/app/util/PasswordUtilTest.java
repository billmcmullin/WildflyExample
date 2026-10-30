package com.app.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.Timeout.ThreadMode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
/**
 * Parasoft Jtest UTA: Test class for PasswordUtil
 *
 * @see com.example.app.util.PasswordUtil
 * @author bmcmullin
 */
public class PasswordUtilTest
{

    /**
     * Parasoft Jtest UTA: Test for hashPassword(String)
     *
     * @see com.example.app.util.PasswordUtil#hashPassword(String)
     * @author bmcmullin
     */
    @Test
    public void testHashPassword() throws Throwable
    {
        // When
        String password = "password"; // UTA: default value
        String result = PasswordUtil.hashPassword(password);

        // Then - assertions for result of method hashPassword(String)
        assertTrue(result.startsWith("p1$"));
        assertTrue(PasswordUtil.verify(password, result));

    }

    /**
     * Parasoft Jtest UTA: Test for verify(String, String)
     *
     * @see com.example.app.util.PasswordUtil#verify(String, String)
     * @author bmcmullin
     */
    @Test
    public void testVerify() throws Throwable
    {
        // When
        String plain = "plain"; // UTA: default value
        String hash = PasswordUtil.hashPassword(plain);

        // Then
        assertTrue(PasswordUtil.verify(plain, hash));
        assertFalse(PasswordUtil.verify("wrong", hash));

    }

    /**
     * Parasoft Jtest UTA: Test for hashPassword(String)
     *
     * @see com.app.util.PasswordUtil#hashPassword(String)
     * @author bmcmullin
     */
    @Test
    @Timeout(value = 5, threadMode = ThreadMode.SEPARATE_THREAD)
    public void testHashPassword2() throws Throwable
    {
        // When
        String password = "password"; // UTA: default value
        String result = PasswordUtil.hashPassword(password);

        // Then - assertions for result of method hashPassword(String)
        assertEquals("p1$120000$42b2f14edd0914f84d82838133ab7ea6$378f587532ac523d20cca27077fe1959bb130ae1951000b5dec55f17010734fa", result);

    }

    /**
     * Parasoft Jtest UTA: Test for verify(String, String)
     *
     * @see com.app.util.PasswordUtil#verify(String, String)
     * @author bmcmullin
     */
    @Test
    @Timeout(value = 5, threadMode = ThreadMode.SEPARATE_THREAD)
    public void testVerify2() throws Throwable
    {
        // When
        String plain = null; // UTA: configured value
        String hash = "hash"; // UTA: default value
        boolean result = PasswordUtil.verify(plain, hash);

        // Then - assertions for result of method verify(String, String)
        assertFalse(result);

    }

    /**
     * Parasoft Jtest UTA: Test for verify(String, String)
     *
     * @see com.app.util.PasswordUtil#verify(String, String)
     * @author bmcmullin
     */
    @Test
    @Timeout(value = 5, threadMode = ThreadMode.SEPARATE_THREAD)
    public void testVerify3() throws Throwable
    {
        // When
        String plain = "plain"; // UTA: default value
        String hash = "p1$"; // UTA: configured value
        boolean result = PasswordUtil.verify(plain, hash);

        // Then - assertions for result of method verify(String, String)
        assertFalse(result);

    }

}
