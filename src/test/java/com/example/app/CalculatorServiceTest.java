package com.example.app;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

class CalculatorServiceTest {

    @Test
    void testAdd() {
        BigDecimal a = new BigDecimal("2");
        BigDecimal b = new BigDecimal("3");
        BigDecimal r = CalculatorService.calculate(a, b, "add");
        assertEquals(new BigDecimal("5"), r);
    }

    @Test
    void testSub() {
        BigDecimal a = new BigDecimal("10");
        BigDecimal b = new BigDecimal("7");
        BigDecimal r = CalculatorService.calculate(a, b, "sub");
        assertEquals(new BigDecimal("3"), r);
    }

    @Test
    void testMul() {
        BigDecimal a = new BigDecimal("2.5");
        BigDecimal b = new BigDecimal("4");
        BigDecimal r = CalculatorService.calculate(a, b, "mul");
        assertEquals(new BigDecimal("10.0").stripTrailingZeros(), r.stripTrailingZeros());
    }

    @Test
    void testDiv() {
        BigDecimal a = new BigDecimal("5");
        BigDecimal b = new BigDecimal("2");
        BigDecimal r = CalculatorService.calculate(a, b, "div");
        // 5 / 2 = 2.5
        assertEquals(new BigDecimal("2.5"), r.stripTrailingZeros());
    }

    @Test
    void testDivByZeroThrows() {
        BigDecimal a = new BigDecimal("1");
        BigDecimal b = BigDecimal.ZERO;
        assertThrows(ArithmeticException.class, () -> CalculatorService.calculate(a, b, "div"));
    }

    @Test
    void testUnknownOpThrows() {
        BigDecimal a = new BigDecimal("1");
        BigDecimal b = new BigDecimal("1");
        assertThrows(IllegalArgumentException.class, () -> CalculatorService.calculate(a, b, "pow"));
    }

    @Test
    void testNullOpThrows() {
        BigDecimal a = BigDecimal.ONE;
        BigDecimal b = BigDecimal.ONE;
        assertThrows(IllegalArgumentException.class, () -> CalculatorService.calculate(a, b, null));
    }

}
