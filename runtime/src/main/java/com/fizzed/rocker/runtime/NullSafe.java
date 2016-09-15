package com.fizzed.rocker.runtime;

/**
 * Thrown for '?' operator when a value expression throws various exceptions
 * or evaluates to a null.  Example exceptions are NullPointerException
 */
public class NullSafe {
    
    static public interface Supplier<V> {
        V apply() throws BreakException;
    }
    
    static public <V> V of(Supplier<V> supplier) {
        try {
            V v = supplier.apply();
            
            if (v == null) {
                throw new BreakException();
            }
            
            return v;
        } catch (NullPointerException e) {
            throw new BreakException();
        }
    }
    
}
