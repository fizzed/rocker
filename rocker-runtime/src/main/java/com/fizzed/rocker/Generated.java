/**
 * 
 */
package com.fizzed.rocker;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marks the class as generated for tools like JaCoCo.
 * */
@Documented
@Retention(CLASS)
@Target(TYPE)
public @interface Generated {}
