/*
 * Copyright 2015 Fizzed Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.fizzed.rocker.compiler;

import com.fizzed.rocker.RenderingException;
import com.fizzed.test.User;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Assert;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author joelauer
 */
public class CompiledTemplateTest {
    static private final Logger log = LoggerFactory.getLogger(CompiledTemplateTest.class);
    
    @Test
    public void noHeader() throws Exception {
        String html = new rocker.NoHeader().render().toString();
        
        Assert.assertEquals("<h1>Hello @ World!</h1>", html);
    }
    
    @Test
    public void renderOnlyAllowedOnce() throws Exception {
        rocker.NoHeader template = new rocker.NoHeader();
        String html = template.render().toString();
        
        Assert.assertEquals("<h1>Hello @ World!</h1>", html);
        
        try {
            template.render();
            Assert.fail("Should not have allowed render twice");
        } catch (RenderingException e) {
            // expected
        }
    }
    
    @Test
    public void javaImport() throws Exception {
        String html = new rocker.JavaImport()
            .strings(Arrays.asList("first", "second"))
            .render()
            .toString()
            .trim();
        
        Assert.assertEquals("first\nsecond", html);
    }
    
    @Test
    public void args() throws Exception {
        String html = new rocker.Args()
            .s("string")
            .i(10)
            .render()
            .toString()
            .trim();
        
        Assert.assertEquals("string\n10", html);
    }
    
    @Test
    public void argsOnMoreThanOneLine() throws Exception {
        String html = new rocker.ArgsOnMoreThanOneLine()
            .s("string")
            .i(10)
            .render()
            .toString()
            .trim();
        
        Assert.assertEquals("string\n10", html);
    }
    
    @Test
    public void argsEmptyButWithSpace() throws Exception {
        String html = new rocker.ArgsEmptyButWithSpace()
            .render()
            .toString();
        
        assertThat(html, is(""));
    }
    
    @Test
    public void argsComplexTypes() throws Exception {
        Map<String,Integer> map = new HashMap<String,Integer>();
        map.put("a", 1);
        List<Map<String,Integer>> listOfMaps = new ArrayList<Map<String,Integer>>();
        listOfMaps.add(map);
        
        List[] arrayOfLists = new List[] {
            Arrays.asList("a", "b", "c")
        };
        
        String html = new rocker.ArgsComplexTypes()
            .listOfMaps(listOfMaps)
            .stringArray(new String[] { "first", "second" })
            .arrayOfLists(arrayOfLists)
            .render()
            .toString()
            .trim();
        
        Assert.assertEquals("1\nfirst\na", html);
    }
    
    @Test
    public void renderingException() throws Exception {
        // chained call on user will trigger NPE during rendering
        try {
            String html = new rocker.ValueWithChainedCall()
                .user(null)
                .render()
                .toString();
            Assert.fail("Exception expected");
        } catch (RenderingException e) {
            // expected and details should be filled in
            Assert.assertEquals(3, e.getSourceLine());
            // underlying cause should be an NPE
            Assert.assertEquals(NullPointerException.class, e.getCause().getClass());
        }
    }
    
    @Test
    public void utf8() throws Exception {
        String html = rocker.UTF8
            .template("\u20AC")
            .render()
            .toString();
        
        String expected = "\n\u20AC\n\u20AC";
        
        Assert.assertEquals(expected, html);
    }
    
    @Test
    public void escaped() throws Exception {
        String html = new rocker.Escaped()
            .s("str<>&ing")
            .render()
            .toString()
            .trim();
        
        Assert.assertEquals("str&lt;&gt;&amp;ing", html);
    }
    
    @Test
    public void raw() throws Exception {
        // normally these chars would be escaped, but with raw feature they
        // will skip the content type escape mechanism
        String html = new rocker.Raw()
            .s("str<>&ing")
            .render()
            .toString()
            .trim();
        
        Assert.assertEquals("str<>&ing", html);
    }
    
    @Test
    public void largeLargeContent() throws Exception {
        // render large large content into a string
        String html = new rocker.LargeLargeContent()
            .s("hello")
            .t("world")
            .render()
            .toString();
        
        // grab correct resource to compare it to
        InputStream is = this.getClass().getResourceAsStream("/rocker/LargeLargeContent.output");
        
        // IOUtils is ALSO inserting \r\n when I just want \n on windows
        // make this test more portable across OSes
        String expectedHtml = IOUtils.toString(is, "UTF-8").replace("\r\n", "\n");
        
        Assert.assertEquals(expectedHtml, html);
    }
    
    @Test
    public void discardLogicWhitespace1() throws Exception {
        // verify that nothing gets chomped if something on same line as args
        
        String html = rocker.DiscardLogicWhitespace1
            .template("World")
            .render()
            .toString();
        
        String expected = " Hello World!";
        
        Assert.assertEquals(expected, html);
    }
    
    @Test
    public void discardLogicWhitespace2() throws Exception {
        // verify that nothing gets chomped if something on same line as args
        
        String html = rocker.DiscardLogicWhitespace2
            .template("World")
            .render()
            .toString();
        
        String expected = " Hello World!";
        
        Assert.assertEquals(expected, html);
    }
    
    @Test
    public void discardLogicWhitespace3() throws Exception {
        // verify that nothing gets chomped if something on same line as args
        
        String html = rocker.DiscardLogicWhitespace3
            .template("World")
            .render()
            .toString();
        
        String expected = " Hello World!";
        
        Assert.assertEquals(expected, html);
    }
    
    @Test
    public void discardLogicWhitespaceAfterArgsRetain() throws Exception {
        // verify that nothing gets chomped if something on same line as args
        
        String html = rocker.DiscardLogicWhitespaceAfterArgsRetain
            .template("Hello")
            .render()
            .toString();
        
        String expected = "Hello!";
        
        Assert.assertEquals(expected, html);
    }
    
    @Test
    public void discardLogicWhitespaceIfElse() throws Exception {
        String html = rocker.DiscardLogicWhitespaceIfElse
            .template("\u20AC")
            .render()
            .toString();
        
        String expected = 
            "Hello\n" +
            "  if-block-true\n" +
            "  €\n" +
            " if-block-true-on line ";
        
        Assert.assertEquals(expected, html);
    }
    
    @Test
    public void discardLogicWhitespaceContentClosure() throws Exception {
        String html = rocker.DiscardLogicWhitespaceContentClosureA
            .template("Hello")
            .render()
            .toString();
        
        // newline in content block then newline template that renders it
        // thus the extra newline
        String expected =
            "Header\n" +
            "    Hello!\n" +
            "\n" +
            "Footer";
        
        Assert.assertEquals(expected, html);
    }
    
    @Test
    public void valueWithChainedCall() throws Exception {
        
        User user = new User();
        user.setCreatedAt(new DateTime(2015, 3, 15, 0, 0, 0, 0, DateTimeZone.UTC));
        
        String html = new rocker.ValueWithChainedCall()
            .user(user)
            .render()
            .toString()
            .trim();
        
        Assert.assertEquals("03/15/2015", html);
    }
    
    @Test
    public void plainTextIncludesJavaScript() throws Exception {

        String html = new rocker.PlainTextIncludesJavaScript()
            .render()
            .toString()
            .trim();
        
        String expected = 
            "<script>\n" +
            "    if (true) {\n" +
            "        // this should do nothing\n" +
            "    }\n" +
            "</script>";
        
        Assert.assertEquals(expected, html);
    }
    
    @Test
    public void plainTextIncludesJavaScriptWithinBlock() throws Exception {

        String html = new rocker.PlainTextIncludesJavaScriptWithinBlock()
            .b(true)
            .render()
            .toString()
            .trim();
        
        String expected = 
            "<script>\n" +
            "    if (true) {\n" +
            "        // this should do nothing\n" +
            "    }\n" +
            "</script>";
        
        Assert.assertEquals(expected, html);
        
        String html2 = new rocker.PlainTextIncludesJavaScriptWithinBlock()
            .b(false)
            .render()
            .toString()
            .trim();
        
        Assert.assertEquals("", html2);
    }
    
    @Test
    public void plainTextIncludesStyleWithinBlock() throws Exception {
        
        String html = new rocker.PlainTextIncludesStyleWithinBlock()
            .b(true)
            .size("0px")
            .render()
            .toString()
            .trim();
        
        String expectedHtml = 
            "<html>\n" +
            "    <head>\n" +
            "        <style type=\"text/css\">\n" +
            "            \n" +
            "            .body {\n" +
            "                font-family: \"Times New Roman\", Times, serif;\n" +
            "                font-size: 0px;\n" +
            "            }\n" +
            "            \n" +
            "            .h1 {\n" +
            "                margin: 0px;\n" +
            "            }\n" +
            "        </style>\n" +
            "    </head>\n" +
            "    <body>\n" +
            "        <h1>hello world</h1>\n" +
            "    </body>\n" +
            "</html>";
        
        Assert.assertEquals(expectedHtml, html);
        
    }
    
    @Test
    public void ifElseBlock() throws Exception {

        String html = new rocker.IfElseBlock()
            .b(true)
            .render()
            .toString()
            .trim();
        
        Assert.assertEquals("true-block", html);
        
        html = new rocker.IfElseBlock()
            .b(false)
            .render()
            .toString()
            .trim();
        
        Assert.assertEquals("false-block", html);
    }
        
    @Test
    public void ifElseBlockWithWhitespace() throws Exception {

        String html = new rocker.IfElseBlockWithWhitespace()
            .b(true)
            .render()
            .toString()
            .trim();
        
        Assert.assertEquals("true-block", html);
        
        html = new rocker.IfElseBlockWithWhitespace()
            .b(false)
            .render()
            .toString()
            .trim();
        
        Assert.assertEquals("false-block", html);
    }

    @Test
    public void ifElseBlockMixedJavascript() throws Exception {

        String html = new rocker.IfElseBlockMixedJavascript()
                .b(true)
                .render()
                .toString()
                .trim();
        
        //assertThat(html, containsString("if (time < 100) {"));
        //assertThat(html, containsString("} else if (time < 200) {"));
        assertThat(html, containsString("<script>\n" +
"    if (time < 100) {\n" +
"        greeting = \"Good morning\";\n" +
"    } else if (time < 200) {\n" +
"        greeting = \"Good day\";\n" +
"    } else {\n" +
"        greeting = \"Good evening\";\n" +
"        if (time < 300) {\n" +
"            // more if\n" +
"        } else {\n" +
"            // another embedded else\n" +
"        }\n" +
"    }\n" +
"</script>"));
    }
    
    @Test
    public void forBlock() throws Exception {

        String html = new rocker.ForBlock()
            .strings(Arrays.asList("a", "b", "c"))
            .render()
            .toString()
            .trim();
        
        Assert.assertEquals("abc", html);
        
    }
    
    @Test
    public void forBlockWithWhitespace() throws Exception {

        String html = new rocker.ForBlockWithWhitespace()
            .strings(Arrays.asList("a", "b", "c"))
            .render()
            .toString()
            .trim();
        
        Assert.assertEquals("a\n\nb\n\nc", html);
        
    }
    
    @Test
    public void forBlockGeneral() throws Exception {

        String html = new rocker.ForBlockGeneral()
            .strings(Arrays.asList("a", "b", "c"))
            .render()
            .toString()
            .trim();
        
        // in reverse order
        Assert.assertEquals("cba", html);
        
    }
    
    @Test
    public void forBlockEnhancedTyped() throws Exception {

        String html = new rocker.ForBlockEnhancedTyped()
            .items(Arrays.asList("a", "b", "c"))
            .render()
            .toString()
            .trim();
        
        // in reverse order
        Assert.assertEquals("abc", html);
        
    }
    
    @Test
    public void forBlockEnhancedTypedArray() throws Exception {

        String html = new rocker.ForBlockEnhancedTypedCollection()
            .items(Arrays.asList("a", "b", "c"))
            .render()
            .toString()
            .trim();
        
        // in reverse order
        Assert.assertEquals("abc", html);
        
    }
    
    @Test
    public void forBlockEnhancedTypedCollection() throws Exception {

        String html = new rocker.ForBlockEnhancedTypedCollection()
            .items(Arrays.asList("a", "b", "c"))
            .render()
            .toString()
            .trim();
        
        // in reverse order
        Assert.assertEquals("abc", html);
        
    }
    
    @Test
    public void forBlockEnhancedTypedCollectionWithForIterator() throws Exception {

        String html = new rocker.ForBlockEnhancedTypedCollectionWithForIterator()
            .items(Arrays.asList("a", "b", "c"))
            .render()
            .toString()
            .trim();
        
        // in reverse order
        Assert.assertEquals("first 0a1b last 2c", html);
        
    }
    
    @Test
    public void forBlockEnhancedTypedMap() throws Exception {
        Map<Integer,String> map = new LinkedHashMap<Integer,String>();
        map.put(1, "a");
        map.put(2, "b");
        map.put(3, "c");
        
        String html = new rocker.ForBlockEnhancedTypedMap()
            .items(map)
            .render()
            .toString()
            .trim();
        
        // in reverse order
        Assert.assertEquals("1: a 2: b 3: c", html);
        
    }
    
    @Test
    public void implicits() throws Exception {
        String html = rocker.ImplicitC
            .template()
            .implicit("a")
            .render()
            .toString();
        
        String expected = "1a";
        
        Assert.assertEquals(expected, html);
    }
    
    @Test
    public void implicitsIncludeOtherTemplates() throws Exception {
        // implicit values copied when other templates included
        String html = rocker.ImplicitA
            .template()
            .implicit("a")
            .render()
            .toString();
        
        String expected = "a11a";
        
        Assert.assertEquals(expected, html);
    }
    
    @Test
    public void includeOtherTemplates() throws Exception {
        String html = new rocker.IncludeA()
            .b("b")
            .c("c")
            .render()
            .toString();

        // newlines don't really matter - just verify what it did
        Assert.assertEquals("bac", html.trim().replace("\n", ""));
    }
    
    @Test
    public void contentClosure() throws Exception {
        String html = new rocker.ContentClosureA()
            .render()
            .toString();

        // newlines don't really matter - just verify what it did
        Assert.assertEquals("i am a block of content\n\n\ni am another block of content", html.trim());
    }
    
    @Test
    public void valueClosure() throws Exception {
        String html = new rocker.ValueClosureA()
            .s("a")
            .i(1)
            .render()
            .toString();

        // newlines don't really matter - just verify what it did
        Assert.assertEquals("a\ninside-b-closure\n\nhello\ninside-c-closure\n\n1", html.trim().replace(" ", ""));
    }
    
    @Test
    public void types() throws Exception {
        // normally these chars would be escaped, but with raw feature they
        // will skip the content type escape mechanism
        String html = new rocker.Types()
            .str("Joe")
            .b((byte)127)
            .s((short)32767)
            .i(2147483647)
            .l(21474836471234L)
            .f(2.147483647f)
            .d(2.1474836471234d)
            .bool(true)
            .c('C')
            .render()
            .toString()
            .trim();
        
        // expected values confirmed by doing a System.out.println(value+"")
        Assert.assertEquals("Joe\n127\n32767\n2147483647\n21474836471234\n2.1474836\n2.1474836471234\ntrue\nC", html);
    }
    
    @Test
    public void breakStatement() throws Exception {
        String out = rocker.BreakStatement.template()
            .render()
            .toString()
            .trim();
        
        Assert.assertEquals("012", out);
    }
    
    @Test
    public void continueStatement() throws Exception {
        String out = rocker.ContinueStatement.template()
            .render()
            .toString()
            .trim();
        
        Assert.assertEquals("013", out);
    }

    @Test
    public void longUnicodeString() throws Exception {
        String out = rocker.LongUnicodeString.template()
                .render()
                .toString()
                .trim();

        Assert.assertEquals("!Я помню чудное мгновенье: " +
                "Передо мной явилась ты, " +
                "Как мимолетное виденье, " +
                "Как гений чистой красоты. " +
                "В томленьях грусти безнадежной " +
                "В тревогах шумной суеты, " +
                "Звучал мне долго голос нежный " +
                "И снились милые черты.", out);
    }

    /**
    @Test
    public void customInterface() throws Exception {
        // cast to interface
        rocker.CustomInterface template = new rocker.CustomInterface();
        
        NameSupport nameable = (NameSupport)template;
        
        nameable.name("Joe");
        
        String html = template 
            .render()
            .toString()
            .trim();
        
        Assert.assertEquals("<h1>Hello Joe!</h1>", html);
    }
    */
    
}
