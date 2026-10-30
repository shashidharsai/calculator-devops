package com.example.calculator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class CalculatorTest {

    private final Calculator calculator = new Calculator();

    private static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }

    /** compareTo ignores scale, so 5 and 5.0 are treated as equal. */
    private static void assertNumber(String expected, BigDecimal actual) {
        assertEquals(0, bd(expected).compareTo(actual),
                "expected " + expected + " but was " + actual);
    }

    @Test
    void addsTwoNumbers() {
        assertNumber("5", calculator.add(bd("2"), bd("3")));
    }

    @Test
    void addsDecimalsAndNegatives() {
        assertNumber("-1.25", calculator.add(bd("-3.5"), bd("2.25")));
    }

    @Test
    void subtractsTwoNumbers() {
        assertNumber("-1", calculator.subtract(bd("2"), bd("3")));
    }

    @Test
    void multipliesTwoNumbers() {
        assertNumber("6", calculator.multiply(bd("2"), bd("3")));
    }

    @Test
    void multipliesByZeroGivesZero() {
        assertNumber("0", calculator.multiply(bd("12345"), BigDecimal.ZERO));
    }

    @Test
    void dividesTwoNumbers() {
        assertNumber("2.5", calculator.divide(bd("10"), bd("4")));
    }

    @Test
    void divisionByZeroThrows() {
        ArithmeticException ex = assertThrows(ArithmeticException.class,
                () -> calculator.divide(bd("1"), BigDecimal.ZERO));
        assertEquals("Division by zero is not allowed", ex.getMessage());
    }

    @Test
    void nonTerminatingDivisionDoesNotThrow() {
        assertNumber("0.3333333333333333", calculator.divide(bd("1"), bd("3")));
    }
}
