package com.example.calculator;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * The calculator logic. Plain Java, no web code, so it is easy to unit test.
 */
public class Calculator {

    public BigDecimal add(BigDecimal a, BigDecimal b) {
        return a.add(b);
    }

    public BigDecimal subtract(BigDecimal a, BigDecimal b) {
        return a.subtract(b);
    }

    public BigDecimal multiply(BigDecimal a, BigDecimal b) {
        return a.multiply(b);
    }

    public BigDecimal divide(BigDecimal a, BigDecimal b) {
        if (b.signum() == 0) {
            throw new ArithmeticException("Division by zero is not allowed");
        }
        // DECIMAL64 = 16 significant digits, so 1/3 does not throw "non-terminating decimal"
        return a.divide(b, MathContext.DECIMAL64);
    }
}
