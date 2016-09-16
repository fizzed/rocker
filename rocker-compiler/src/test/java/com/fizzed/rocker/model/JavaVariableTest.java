package com.fizzed.rocker.model;

import com.fizzed.rocker.compiler.TokenException;
import org.junit.Assert;
import org.junit.Test;
import java.util.List;

/**
 *
 * @author joelauer
 */
public class JavaVariableTest {
    
    @Test
    public void parseToken() throws Exception {

        Assert.assertEquals("User", JavaVariable.parseToken("User", 0));
        Assert.assertEquals(" User ", JavaVariable.parseToken(" User ", 0));
        Assert.assertEquals(" User", JavaVariable.parseToken(" User, ", 0));
        Assert.assertEquals("User[]", JavaVariable.parseToken("User[]", 0));
        Assert.assertEquals("User[ ] ", JavaVariable.parseToken("User[ ] ", 0));
        Assert.assertEquals("List<User>  ", JavaVariable.parseToken("List<User>  ", 0));
        Assert.assertEquals("List<User>[]", JavaVariable.parseToken("List<User>[]", 0));
        Assert.assertEquals("Map< String, Object >", JavaVariable.parseToken("Map< String, Object >", 0));
        Assert.assertEquals("Map< String,Map<String,String> >", JavaVariable.parseToken("Map< String,Map<String,String> >", 0));
        Assert.assertEquals("Map<String,Object> ", JavaVariable.parseToken("Map<String,Object> u", 0));
        Assert.assertEquals("Map<String,Object> ", JavaVariable.parseToken("Map<String,Object> u,", 0));
        
        // newlines & tabs
        String test = " String\tu,\nString s";
        Assert.assertEquals(" String\t", JavaVariable.parseToken(test, 0));
        Assert.assertEquals("u", JavaVariable.parseToken(test, 8));
        Assert.assertEquals("\nString ", JavaVariable.parseToken(test, 10));
        Assert.assertEquals("s", JavaVariable.parseToken(test, 18));
        
        // space between name and generic
        Assert.assertEquals("Map <String,Object> ", JavaVariable.parseToken("Map <String,Object> u,", 0));
        
        // lots and lots of spaces
        Assert.assertEquals("Map  <  String  ,  Object  >   [   ]   [   ] ", JavaVariable.parseToken("Map  <  String  ,  Object  >   [   ]   [   ] u", 0));

        try {
            // generic marker before name
            JavaVariable.parseToken("<User u", 0);
            Assert.fail("expected exception");
        } catch (TokenException e) {
            // expected
        }
        
        try {
            // generic marker after array
            JavaVariable.parseToken("User[<] u", 0);
            Assert.fail("expected exception");
        } catch (TokenException e) {
            // expected
        }
        
        try {
            // no closing > tag
            JavaVariable.parseToken("User < u", 0);
            Assert.fail("expected exception");
        } catch (TokenException e) {
            // expected
        }
        
        try {
            // nothing...
            JavaVariable.parseToken("", 0);
            Assert.fail("expected exception");
        } catch (TokenException e) {
            // expected
        }
        
        try {
            // nothing...
            JavaVariable.parseToken(" ", 0);
            Assert.fail("expected exception");
        } catch (TokenException e) {
            // expected
        }
        
        try {
            // no name
            JavaVariable.parseToken(" <>", 0);
            Assert.fail("expected exception");
        } catch (TokenException e) {
            // expected
        }
    }

    @Test
    public void parseList() throws Exception {

        List<JavaVariable> vars;

        // single space
        vars = JavaVariable.parseList(" ");
        Assert.assertEquals(0, vars.size());
        
        // single arg
        vars = JavaVariable.parseList("User u");
        
        Assert.assertEquals(1, vars.size());
        Assert.assertEquals(new JavaVariable("User", "u"), vars.get(0));
        
        // single arg no type
        vars = JavaVariable.parseList("u");
        
        Assert.assertEquals(1, vars.size());
        Assert.assertEquals(new JavaVariable(null, "u"), vars.get(0));

        // simple types
        vars = JavaVariable.parseList("User u, String s");

	Assert.assertEquals(2, vars.size());
        Assert.assertEquals(new JavaVariable("User", "u"), vars.get(0));
        Assert.assertEquals(new JavaVariable("String", "s"), vars.get(1));

        // nested generic w/ comma
        vars = JavaVariable.parseList("Map<String, Object>  m , List<Map<String,Object>> l ");

        Assert.assertEquals(2, vars.size());
        Assert.assertEquals(new JavaVariable("Map<String, Object>", "m"), vars.get(0));
        Assert.assertEquals(new JavaVariable("List<Map<String,Object>>", "l"), vars.get(1));

        // no types
        vars = JavaVariable.parseList("k,v");

        Assert.assertEquals(2, vars.size());
        Assert.assertEquals(new JavaVariable(null, "k"), vars.get(0));
        Assert.assertEquals(new JavaVariable(null, "v"), vars.get(1));
        
        // fully-qualified type names
        vars = JavaVariable.parseList("java.lang.String s,v");

        Assert.assertEquals(2, vars.size());
        Assert.assertEquals(new JavaVariable("java.lang.String", "s"), vars.get(0));
        Assert.assertEquals(new JavaVariable(null, "v"), vars.get(1));
        
        // fully-qualified type names
        vars = JavaVariable.parseList("java.lang.String s,v");

        Assert.assertEquals(2, vars.size());
        Assert.assertEquals(new JavaVariable("java.lang.String", "s"), vars.get(0));
        Assert.assertEquals(new JavaVariable(null, "v"), vars.get(1));

        // array
        vars = JavaVariable.parseList("java.lang.String[] s,v");

        Assert.assertEquals(2, vars.size());
        Assert.assertEquals(new JavaVariable("java.lang.String[]", "s"), vars.get(0));
        Assert.assertEquals(new JavaVariable(null, "v"), vars.get(1));
        
        // array of arrays
        vars = JavaVariable.parseList("java.lang.String[][] s,v");

        Assert.assertEquals(2, vars.size());
        Assert.assertEquals(new JavaVariable("java.lang.String[][]", "s"), vars.get(0));
        Assert.assertEquals(new JavaVariable(null, "v"), vars.get(1));

        // whitespace cleanup of type
        vars = JavaVariable.parseList("java.lang.String  [  ] [  ] s");

        Assert.assertEquals(1, vars.size());
        Assert.assertEquals(new JavaVariable("java.lang.String  [  ] [  ]", "s"), vars.get(0));
        Assert.assertEquals("java.lang.String[][]", vars.get(0).getType());
        
    }
}
