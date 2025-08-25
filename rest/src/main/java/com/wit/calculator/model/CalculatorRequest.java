package com.wit.calculator.model;

public record CalculatorRequest(String op, String a, String b) { } // keep numbers as String to avoid float/double precision issues -> the calculator will parse to BigDecimal
