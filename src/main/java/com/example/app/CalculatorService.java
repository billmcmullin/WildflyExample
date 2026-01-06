package com.example.app;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class CalculatorService {
    private static final int DIV_SCALE = 10;

    /**
     * Calculate result for the operation.
     * a and b must be non-null BigDecimal (caller may supply 0 if missing).
     * Throws IllegalArgumentException or ArithmeticException for invalid inputs.
     */
    public static BigDecimal calculate(BigDecimal a, BigDecimal b, String op) {
        if (op == null || op.isBlank()) {
            throw new IllegalArgumentException("Missing 'op' parameter (add, sub, mul, div)");
        }
        switch (op) {
            case "add":
                return a.add(b);
            case "sub":
                return a.subtract(b);
            case "mul":
                return a.multiply(b);
            case "div":
                if (b.compareTo(BigDecimal.ZERO) == 0) {
                    throw new ArithmeticException("Division by zero");
                }
                return a.divide(b, DIV_SCALE, RoundingMode.HALF_UP).stripTrailingZeros();
            default:
                throw new IllegalArgumentException("Unknown operation: " + op);
        }
    }
}
