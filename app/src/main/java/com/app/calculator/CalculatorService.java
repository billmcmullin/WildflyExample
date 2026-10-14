package com.app.calculator;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class CalculatorService {
    private static final int DIV_SCALE = 10;
    private static final String OP_ADD = "add";
    private static final String OP_SUB = "sub";
    private static final String OP_MUL = "mul";
    private static final String OP_DIV = "div";
    private static final String ERROR_MISSING_OP = "Missing 'op' parameter (add, sub, mul, div)";
    private static final String ERROR_DIVISION_BY_ZERO = "Division by zero";
    private static final String ERROR_UNKNOWN_OPERATION = "Unknown operation: ";
    private static final String CLASS_NAME = "CalculatorService";

    /**
     * Calculate result for the operation.
     * a and b must be non-null BigDecimal (caller may supply 0 if missing).
     * Throws IllegalArgumentException or ArithmeticException for invalid inputs.
     */
    public static BigDecimal calculate(BigDecimal a, BigDecimal b, String op) {
        if (op == null || op.isBlank()) {
            throw new IllegalArgumentException(ERROR_MISSING_OP);
        }
        switch (op) {
            case OP_ADD:
                return a.add(b);
            case OP_SUB:
                return a.subtract(b);
            case OP_MUL:
                return a.multiply(b);
            case OP_DIV:
                if (b.compareTo(BigDecimal.ZERO) == 0) {
                    throw new ArithmeticException(ERROR_DIVISION_BY_ZERO);
                }
                return a.divide(b, DIV_SCALE, RoundingMode.HALF_UP).stripTrailingZeros();
            default:
                throw new IllegalArgumentException(ERROR_UNKNOWN_OPERATION + op);
        }
    }

    private final void writeObject(ObjectOutputStream out) throws IOException {
        throw new NotSerializableException(CLASS_NAME);
    }
}
