package com.fizzed.rocker.runtime;

public class Elvis {
    
    static public Object op(Object a, Object b) {
        return (a != null ? a : b);
    }
    
}
