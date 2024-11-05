package com.fizzed.rocker.compiler;

public class TestHelper {

    static public String normalizeNewlines(String v) {
        if (v != null) {
            return v.replaceAll("\r\n", "\n");
        }
        return null;
    }

}