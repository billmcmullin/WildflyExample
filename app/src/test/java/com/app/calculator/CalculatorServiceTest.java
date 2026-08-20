package com.app.calculator;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
/**
 * Parasoft Jtest UTA: Test class for CalculatorService
 *
 * @see com.example.app.calculator.CalculatorService
 * @author bmcmullin
 */
public class CalculatorServiceTest
{

    /**
     * Parasoft Jtest UTA: Test for calculate(BigDecimal, BigDecimal, String)
     *
     * @see com.example.app.calculator.CalculatorService#calculate(BigDecimal, BigDecimal, String)
     * @author bmcmullin
     */
    @Test
    public void testCalculate() throws Throwable
    {
        // When
        BigDecimal a = BigDecimal.ONE; // UTA: default value
        BigDecimal b = BigDecimal.ONE; // UTA: default value
        String op = null; // UTA: configured value
        assertThrows(IllegalArgumentException.class, () -> {
            CalculatorService.calculate(a, b, op);
        });

    }

    /**
     * Parasoft Jtest UTA: Test for calculate(BigDecimal, BigDecimal, String)
     *
     * @see com.example.app.calculator.CalculatorService#calculate(BigDecimal, BigDecimal, String)
     * @author bmcmullin
     */
    @Test
    public void testCalculate2() throws Throwable
    {
        // When
        BigDecimal a = BigDecimal.ONE; // UTA: default value
        BigDecimal b = BigDecimal.ONE; // UTA: default value
        String op = "op"; // UTA: configured value
        assertThrows(IllegalArgumentException.class, () -> {
            CalculatorService.calculate(a, b, op);
        });

    }

}
