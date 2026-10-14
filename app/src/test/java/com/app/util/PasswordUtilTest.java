package com.app.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
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

}
