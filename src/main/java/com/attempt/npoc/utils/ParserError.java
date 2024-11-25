package com.attempt.npoc.utils;

public class ParserError extends Exception {
    public ParserError(String message) {
        super(message);
    }

    public static ParserError newWithFmt(String format, Object... args) {
        return new ParserError(String.format(format, args));
    }
}
