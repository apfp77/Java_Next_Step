package src.calculator;

import static org.junit.jupiter.api.Assertions.*;

class StringCalculatorTest {
    private StringCalculator cal;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        cal = new StringCalculator();
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
    }

    @org.junit.jupiter.api.Test
    void add() throws Exception {
        assertEquals(3, cal.add("1,2"));
        assertThrows(RuntimeException.class, () -> cal.add("-1"));
    }
}