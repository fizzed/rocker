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

import com.fizzed.rocker.runtime.ParserException;
import com.fizzed.rocker.ContentType;
import com.fizzed.rocker.model.Argument;
import com.fizzed.rocker.model.Comment;
import com.fizzed.rocker.model.ContentClosureBegin;
import com.fizzed.rocker.model.ContentClosureEnd;
import com.fizzed.rocker.model.ElseBlockBegin;
import com.fizzed.rocker.model.ForBlockBegin;
import com.fizzed.rocker.model.ForBlockEnd;
import com.fizzed.rocker.model.ForStatement;
import com.fizzed.rocker.model.IfBlockBegin;
import com.fizzed.rocker.model.IfBlockEnd;
import com.fizzed.rocker.model.JavaVariable;
import com.fizzed.rocker.model.JavaVersion;
import com.fizzed.rocker.model.PlainText;
import com.fizzed.rocker.model.TemplateModel;
import com.fizzed.rocker.model.ValueClosureBegin;
import com.fizzed.rocker.model.ValueClosureEnd;
import com.fizzed.rocker.model.ValueExpression;
import java.io.File;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author joelauer
 */
public class TemplateParserTest {
    static private final Logger log = LoggerFactory.getLogger(TemplateParserTest.class);
    
    File baseDir = new File("src/test/resources"); 
   
    public TemplateParser createParser() {
        RockerConfiguration configuration = new RockerConfiguration();
        configuration.setTemplateDirectory(baseDir);
        
        // set this globally since we really care about preciseness when validating
        // if the parser is doing its job as expected
        configuration.getOptions().setDiscardLogicWhitespace(Boolean.FALSE);
        
        return new TemplateParser(configuration);
    }
    
    public File findTemplate(String name) {
        return baseDir.toPath().resolve(name).toFile();
    }
    
    @Test
    public void optionJavaVersion7() throws Exception {
        TemplateParser parser = createParser();
        File f = findTemplate("rocker/parser/OptionJavaVersion7.rocker.html");
        
        TemplateModel model = parser.parse(f);
        
        Assert.assertEquals(JavaVersion.v1_7, model.getOptions().getJavaVersion());
    }
    
    @Test
    public void optionJavaVersion8() throws Exception {
        TemplateParser parser = createParser();
        File f = findTemplate("rocker/parser/OptionJavaVersion8.rocker.html");
        
        TemplateModel model = parser.parse(f);
        
        Assert.assertEquals(JavaVersion.v1_8, model.getOptions().getJavaVersion());
    }
    
    @Test
    public void unescapeAtAt() throws Exception {
        TemplateParser parser = createParser();
        File f = findTemplate("rocker/parser/UnescapeAtAt.rocker.html");
        
        TemplateModel model = parser.parse(f);
        
        Assert.assertEquals("@", model.getUnit(0, PlainText.class).getText());
    }
    
    @Test
    public void unescapeAtRightCurly() throws Exception {
        TemplateParser parser = createParser();
        File f = findTemplate("rocker/parser/UnescapeAtRightCurly.rocker.html");
        
        TemplateModel model = parser.parse(f);
        
        Assert.assertEquals("}", model.getUnit(0, PlainText.class).getText());
    }

    @Test
    public void noHeader() throws Exception {
        TemplateParser parser = createParser();
        File f = findTemplate("rocker/parser/NoHeader.rocker.html");
        
        TemplateModel model = parser.parse(f);
        
        Assert.assertEquals(0, model.getImports().size());
        Assert.assertEquals(0, model.getArguments().size());
        
        //<h1>no header with @val</h1>
        Assert.assertEquals("<h1>no header with ", model.getUnit(0, PlainText.class).getText());
        Assert.assertEquals("val", model.getUnit(1, ValueExpression.class).getExpression());
        Assert.assertEquals("</h1>", model.getUnit(2, PlainText.class).getText());
    }
    
    @Test
    public void kitchenSink() throws Exception {
        TemplateParser parser = createParser();
        File f = findTemplate("rocker/parser/KitchenSink.rocker.html");
        
        TemplateModel model = parser.parse(f);
        
        Assert.assertEquals("KitchenSink", model.getName());
        Assert.assertEquals(ContentType.HTML, model.getContentType());
        Assert.assertEquals("rocker.parser", model.getPackageName());
        Assert.assertEquals("KitchenSink.rocker.html", model.getTemplateName());
        
        Assert.assertEquals("java.util.*", model.getImports().get(0).getStatement());
        Assert.assertEquals("java.util.concurrent.*", model.getImports().get(1).getStatement());
        
        // String title, Date now, Map<String,String> map
        Assert.assertEquals(new Argument(null, "String", "title"), model.getArguments().get(0));
        Assert.assertEquals(new Argument(null, "Date", "now"), model.getArguments().get(1));
        Assert.assertEquals(new Argument(null, "Map<String,String>", "map"), model.getArguments().get(2));
        
        // exact first plain text from template
        String plainText1 = "\n" +
            "\n" +
            "<html>\n" +
            "    <head>\n" +
            "        <title>";
        
        Assert.assertEquals("Example file containing many features of Rocker templates", model.getUnit(0, Comment.class).getText().trim());
        Assert.assertEquals(plainText1, model.getUnit(1, PlainText.class).getText());
        Assert.assertEquals("title", model.getUnit(2, ValueExpression.class).getExpression());
        
        // @@ should be unescaped correctly
        String plainText2 = 
            "</title>\n" +
            "    </head>\n" +
            "    <body>\n" +
            "        <h1>Hello World @ millis ";
        Assert.assertEquals(plainText2, model.getUnit(3, PlainText.class).getText());
        
        Assert.assertEquals("now.getTime()", model.getUnit(4, ValueExpression.class).getExpression());
        
        String plainText3 = "</h1>\n" +
"        ";
        Assert.assertEquals(plainText3, model.getUnit(5, PlainText.class).getText());
        
        Assert.assertEquals("(String s : list)", model.getUnit(6, ForBlockBegin.class).getExpression());
        
        String plainText4 = "\n" +
            "            for-block\n" +
            "            ";
        Assert.assertEquals(plainText4, model.getUnit(7, PlainText.class).getText());
        
        Assert.assertEquals("(valueForIfTrue)", model.getUnit(8, IfBlockBegin.class).getExpression());
        
        String plainText5 = "\n" +
            "                if-block\n" +
            "            ";
        Assert.assertEquals(plainText5, model.getUnit(9, PlainText.class).getText());
        
        Assert.assertEquals(ElseBlockBegin.class, model.getUnit(10, ElseBlockBegin.class).getClass());
        
        String plainText6 = "\n" +
            "                else-block\n" +
            "            ";
        Assert.assertEquals(plainText6, model.getUnit(11, PlainText.class).getText());
        
        Assert.assertEquals(IfBlockEnd.class, model.getUnit(12, IfBlockEnd.class).getClass());
        
        String plainText7 = "\n" +
"        ";
        Assert.assertEquals(plainText7, model.getUnit(13, PlainText.class).getText());
        
        Assert.assertEquals(ForBlockEnd.class, model.getUnit(14, ForBlockEnd.class).getClass());
        
        String plainText8 = "\n" +
            "    </body>\n" +
            "</html>";
        Assert.assertEquals(plainText8, model.getUnit(15, PlainText.class).getText());
    }
    
    @Test
    public void plainInHeaderThrowsException() throws Exception {
        TemplateParser parser = createParser();
        File f = findTemplate("rocker/parser/PlainTextInHeaderError.rocker.html");
        
        try {
            parser.parse(f);
            Assert.fail("Expected parsing failure");
        } catch (ParserException e) {
            //log.error("", e);
            // confirm position of error
            Assert.assertEquals(5, e.getLineNumber());
            Assert.assertEquals(0, e.getColumnNumber());
        }
    }
    
    
    
    @Test
    public void plainTextIncludesStyle() throws Exception {
        TemplateParser parser = createParser();
        File f = findTemplate("rocker/parser/PlainTextIncludesStyle.rocker.html");
        
        TemplateModel model = parser.parse(f);
        
        String expectedHtml =
            "\n<html>\n" +
            "    <head>\n" +
            "        <style type=\"text/css\">\n" +
            "            .body {\n" +
            "                font-family: \"Times New Roman\", Times, serif;\n" +
            "            }\n" +
            "            .h1 {\n" +
            "                margin: 0px;\n" +
            "            }\n" +
            "        </style>\n" +
            "    </head>\n" +
            "    <body>\n" +
            "        <h1>hello world</h1>\n" +
            "    </body>\n" +
            "</html>";
        
        Assert.assertEquals(expectedHtml, model.getUnit(1, PlainText.class).getText());
        
        //Assert.assertEquals("ValueClosureB.template(s, i)", model.getUnit(1, ValueClosureBegin.class).getExpression());
        //Assert.assertEquals("\ninside-closure\n", model.getUnit(2, PlainText.class).getText());
        //Assert.assertNotNull(model.getUnit(3, ValueClosureEnd.class));
    }
    
    @Test
    public void plainTextIncludesStyleWithinBlock() throws Exception {
        TemplateParser parser = createParser();
        File f = findTemplate("rocker/parser/PlainTextIncludesStyleWithinBlock.rocker.html");
        
        TemplateModel model = parser.parse(f);
        
        String expectedHtml1 =
            "\n<html>\n" +
            "    <head>\n" +
            "        <style type=\"text/css\">\n" +
            "            ";

        String expectedHtml2 =
            "\n" +
"            .body {\n" +
"                font-family: \"Times New Roman\", Times, serif;\n" +
"                font-size: ";
        
        String expectedHtml3 =
            ";\n" +
            "            }\n" +
            "            ";
        
        String expectedHtml4 =
            "\n" +
            "            .h1 {\n" +
            "                margin: 0px;\n" +
            "            }\n" +
            "        </style>\n" +
            "    </head>\n" +
            "    <body>\n" +
            "        <h1>hello world</h1>\n" +
            "    </body>\n" +
            "</html>";
        
        Assert.assertEquals(expectedHtml1, model.getUnit(1, PlainText.class).getText());
        
        Assert.assertEquals("(b)", model.getUnit(2, IfBlockBegin.class).getExpression());
        
        Assert.assertEquals(expectedHtml2, model.getUnit(3, PlainText.class).getText());
        
        Assert.assertEquals("size", model.getUnit(4, ValueExpression.class).getExpression());
        
        Assert.assertEquals(expectedHtml3, model.getUnit(5, PlainText.class).getText());
        
        Assert.assertEquals(true, model.getUnit(6, IfBlockEnd.class).isBlockLevel());
        
        Assert.assertEquals(expectedHtml4, model.getUnit(7, PlainText.class).getText());
    }
    
    @Test
    public void plainTextIncludesStyleWithNoMatchingRightCurly() throws Exception {
        // if user chooses to NOT use left/right curlies and escapes then then
        // they actually need to escape both
        try {
            TemplateParser parser = createParser();
            File f = findTemplate("rocker/parser/PlainTextIncludesStyleWithNoMatchingRightCurly.rocker.html");
            TemplateModel model = parser.parse(f);
            
            Assert.fail("expected exception");
        }
        catch (Exception e) {
            // expected
        }
    }
    
    @Test
    public void plainTextIncludesStyleWithEscapedCurlies() throws Exception {
        TemplateParser parser = createParser();
        File f = findTemplate("rocker/parser/PlainTextIncludesStyleWithEscapedCurlies.rocker.html");
        
        TemplateModel model = parser.parse(f);
        
        String expectedHtml = 
            "<style type=\"text/css\">\n" +
            "    .body {\n" +
            "        font-family: \"Times New Roman\", Times, serif;\n" +
            "    }\n" +
            "    .h1 {\n" +
            "        margin: 0px;\n" +
            "    }\n" +
            "</style>";
        
        Assert.assertEquals(expectedHtml, model.getUnit(0, PlainText.class).getText());
    }
    
    @Test
    public void sourceCodeLinePositions() throws Exception {
        TemplateParser parser = createParser();
        File f = findTemplate("rocker/parser/PosInFile.rocker.html");
        
        TemplateModel model = parser.parse(f);
        
        Comment firstComment = model.getUnit(0, Comment.class);
        Assert.assertEquals(1, firstComment.getSourceRef().getBegin().getLineNumber());
        Assert.assertEquals(1, firstComment.getSourceRef().getBegin().getPosInLine());
        Assert.assertEquals(0, firstComment.getSourceRef().getBegin().getPosInFile());
        Assert.assertEquals(5, firstComment.getSourceRef().getCharLength());
        
        // euro char and whitespace
        PlainText plainText = model.getUnit(1, PlainText.class);
        //log.info("plain text [{}]", StringEscapeUtils.escapeJava(plainText.getText()));
        Assert.assertEquals(1, plainText.getSourceRef().getBegin().getLineNumber());
        Assert.assertEquals(6, plainText.getSourceRef().getBegin().getPosInLine());
        Assert.assertEquals(5, plainText.getSourceRef().getBegin().getPosInFile());
        
        // length is actually number of chars not bytes!
        // stupid ANTLR treats newlines differently on each platform... not preserving
        // what is actually in the underlying template file (annoying)
        // http://www.antlr3.org/pipermail/stringtemplate-interest/2012-October/004013.html
        // so on nix this will be 7, but on windows it'll be 11
        int charLength = plainText.getSourceRef().getCharLength();
        Assert.assertTrue((charLength == 7 || charLength == 11));
        
        Assert.assertEquals("\n\n\u20AC\n\n  ", plainText.getText());
        
        // @title in file
        ValueExpression valueExpr = model.getUnit(2, ValueExpression.class);
        //log.info("value expr [{}]", StringEscapeUtils.escapeJava(valueExpr.getSourceRef().getText()));
        Assert.assertEquals(5, valueExpr.getSourceRef().getBegin().getLineNumber());
        Assert.assertEquals(3, valueExpr.getSourceRef().getBegin().getPosInLine());
        
        // again, stupid ANTLR and newlines (adding \r's when they don't exist)
        int posInFile = valueExpr.getSourceRef().getBegin().getPosInFile();
        Assert.assertTrue(posInFile == 12 || posInFile == 16);
        
        Assert.assertEquals(6, valueExpr.getSourceRef().getCharLength());
    }
    
    @Test
    public void discardLogicWhitespace() throws Exception {
        TemplateParser parser = createParser();
        File f = findTemplate("rocker/parser/DiscardLogicWhitespace.rocker.html");
        
        // parse w/ discarding block-level whitespace
        TemplateModel model = parser.parse(f);
        
        PlainText plainText = model.getUnit(0, PlainText.class);
        Assert.assertEquals("Hello\n", plainText.getText());
        
        plainText = model.getUnit(2, PlainText.class);
        Assert.assertEquals("  if-block-true\n", plainText.getText());
        
        plainText = model.getUnit(4, PlainText.class);
        Assert.assertEquals("if-block-false\n", plainText.getText());
    }
    
    @Test
    public void discardLogicWhitespaceRemoveEmptyPlain1() throws Exception {
        TemplateParser parser = createParser();
        File f = findTemplate("rocker/parser/DiscardLogicWhitespaceRemoveEmptyPlain1.rocker.html");
        
        // parse w/ discarding block-level whitespace -- this sample will end
        // up with empty plain text -- which we should discard from the model
        TemplateModel model = parser.parse(f);
        
        // first unit should be the value
        Assert.assertEquals(ValueExpression.class, model.getUnits().get(0).getClass());
        
        PlainText plainText = model.getUnit(1, PlainText.class);
        Assert.assertEquals("!", plainText.getText());
    }
    
    
    @Test
    public void forBlockEnhancedTyped() throws Exception {
        TemplateParser parser = createParser();
        File f = findTemplate("rocker/parser/ForBlockEnhancedTyped.rocker.html");
        
        TemplateModel model = parser.parse(f);
        
        ForBlockBegin forBlockBegin = model.getUnit(0, ForBlockBegin.class);
        
        // the expression is the raw statement from the template
        Assert.assertEquals("(String u : items)", forBlockBegin.getExpression());
        
        // test what the parsed statement actually was
        Assert.assertEquals(ForStatement.Form.ENHANCED, forBlockBegin.getStatement().getForm());
        Assert.assertEquals(1, forBlockBegin.getStatement().getArguments().size());
        Assert.assertEquals(new JavaVariable("String", "u"), forBlockBegin.getStatement().getArguments().get(0));
    }
    
    @Test
    public void forBlockEnhancedTypedTuple1() throws Exception {
        TemplateParser parser = createParser();
        File f = findTemplate("rocker/parser/ForBlockEnhancedTypedTuple1.rocker.html");
        
        TemplateModel model = parser.parse(f);
        
        ForBlockBegin forBlockBegin = model.getUnit(0, ForBlockBegin.class);
        
        // the expression is the raw statement from the template
        Assert.assertEquals("((String u) : items)", forBlockBegin.getExpression());
        
        // test what the parsed statement actually was
        Assert.assertEquals(ForStatement.Form.ENHANCED, forBlockBegin.getStatement().getForm());
        Assert.assertEquals(1, forBlockBegin.getStatement().getArguments().size());
        Assert.assertEquals(new JavaVariable("String", "u"), forBlockBegin.getStatement().getArguments().get(0));
    }
    
    @Test
    public void forBlockEnhancedTypedTuple2() throws Exception {
        TemplateParser parser = createParser();
        File f = findTemplate("rocker/parser/ForBlockEnhancedTypedTuple2.rocker.html");
        
        TemplateModel model = parser.parse(f);
        
        ForBlockBegin forBlockBegin = model.getUnit(0, ForBlockBegin.class);
        
        // test what the parsed statement actually was
        Assert.assertEquals(ForStatement.Form.ENHANCED, forBlockBegin.getStatement().getForm());
        Assert.assertEquals(2, forBlockBegin.getStatement().getArguments().size());
        Assert.assertEquals(new JavaVariable("String", "k"), forBlockBegin.getStatement().getArguments().get(0));
        Assert.assertEquals(new JavaVariable("String", "v"), forBlockBegin.getStatement().getArguments().get(1));
    }
    
    @Test
    public void forBlockEnhancedTypedTuple3() throws Exception {
        TemplateParser parser = createParser();
        File f = findTemplate("rocker/parser/ForBlockEnhancedTypedTuple3.rocker.html");
        
        TemplateModel model = parser.parse(f);
        
        ForBlockBegin forBlockBegin = model.getUnit(0, ForBlockBegin.class);
        
        // test what the parsed statement actually was
        Assert.assertEquals(ForStatement.Form.ENHANCED, forBlockBegin.getStatement().getForm());
        Assert.assertEquals(3, forBlockBegin.getStatement().getArguments().size());
        Assert.assertEquals(new JavaVariable("ForIterator", "i"), forBlockBegin.getStatement().getArguments().get(0));
        Assert.assertEquals(new JavaVariable("String", "k"), forBlockBegin.getStatement().getArguments().get(1));
        Assert.assertEquals(new JavaVariable("String", "v"), forBlockBegin.getStatement().getArguments().get(2));
    }
    
    @Test
    public void forBlockEnhancedUntyped() throws Exception {
        // requires java 1.8+ to parse
        TemplateParser parser = createParser();
        
        parser.getConfiguration().getOptions().setJavaVersion(JavaVersion.v1_8);
        
        File f = findTemplate("rocker/parser/ForBlockEnhancedUntyped.rocker.html");
        
        TemplateModel model = parser.parse(f);
        
        ForBlockBegin forBlockBegin = model.getUnit(0, ForBlockBegin.class);
        
        // the expression is the raw statement from the template
        Assert.assertEquals("(u : items)", forBlockBegin.getExpression());
        
        // test what the parsed statement actually was
        Assert.assertEquals(ForStatement.Form.ENHANCED, forBlockBegin.getStatement().getForm());
        Assert.assertEquals(1, forBlockBegin.getStatement().getArguments().size());
        Assert.assertEquals(new JavaVariable(null, "u"), forBlockBegin.getStatement().getArguments().get(0));
    }
    
    @Test
    public void forBlockEnhancedUntypedJava7ThrowsException() throws Exception {
        // requires java 1.8+ to parse
        TemplateParser parser = createParser();
        
        parser.getConfiguration().getOptions().setJavaVersion(JavaVersion.v1_7);
        
        File f = findTemplate("rocker/parser/ForBlockEnhancedUntyped.rocker.html");
        
        try {
            parser.parse(f);
            Assert.fail("expected exception");
        } catch (ParserException e) {
            // expected
        }
    }
    
    @Test
    public void forBlockEnhancedUntypedTuple1() throws Exception {
        TemplateParser parser = createParser();
        File f = findTemplate("rocker/parser/ForBlockEnhancedUntypedTuple1.rocker.html");
        
        TemplateModel model = parser.parse(f);
        
        ForBlockBegin forBlockBegin = model.getUnit(0, ForBlockBegin.class);
        
        // the expression is the raw statement from the template
        Assert.assertEquals("((u) : items)", forBlockBegin.getExpression());
        
        // test what the parsed statement actually was
        Assert.assertEquals(ForStatement.Form.ENHANCED, forBlockBegin.getStatement().getForm());
        Assert.assertEquals(1, forBlockBegin.getStatement().getArguments().size());
        Assert.assertEquals(new JavaVariable(null, "u"), forBlockBegin.getStatement().getArguments().get(0));
    }
    
    @Test
    public void valueClosure() throws Exception {
        TemplateParser parser = createParser();
        File f = findTemplate("rocker/parser/ValueClosure.rocker.html");
        
        TemplateModel model = parser.parse(f);
        
        Assert.assertEquals("ValueClosureB.template(s, i)", model.getUnit(1, ValueClosureBegin.class).getExpression());
        Assert.assertEquals("\ninside-closure\n", model.getUnit(2, PlainText.class).getText());
        Assert.assertNotNull(model.getUnit(3, ValueClosureEnd.class));
    }
    
    @Test
    public void contentClosure() throws Exception {
        TemplateParser parser = createParser();
        File f = findTemplate("rocker/parser/ContentClosure.rocker.html");
        
        TemplateModel model = parser.parse(f);
        
        Assert.assertEquals("content1", model.getUnit(0, ContentClosureBegin.class).getIdentifier());
        Assert.assertEquals("i am a block of content", model.getUnit(1, PlainText.class).getText().trim());
        Assert.assertNotNull(model.getUnit(2, ContentClosureEnd.class));
        
        Assert.assertEquals("content2", model.getUnit(4, ContentClosureBegin.class).getIdentifier());
        Assert.assertEquals("i am another block of content", model.getUnit(5, PlainText.class).getText().trim());
        Assert.assertNotNull(model.getUnit(6, ContentClosureEnd.class));
    }
    
    @Test
    public void argsWithNamesLikeRockerReservedNames() throws Exception {
        // requires java 1.8+ to parse
        TemplateParser parser = createParser();
        
        parser.getConfiguration().getOptions().setJavaVersion(JavaVersion.v1_8);
        
        File f = findTemplate("rocker/parser/ArgsWithNamesLikeRockerReservedNames.rocker.html");
        
        TemplateModel model = parser.parse(f);
        
        // should all be values
        ValueExpression value;
        
        value = model.getUnit(2, ValueExpression.class);
        Assert.assertEquals("ift", value.getExpression());
        
        value = model.getUnit(4, ValueExpression.class);
        Assert.assertEquals("optiont", value.getExpression());
        
        value = model.getUnit(6, ValueExpression.class);
        Assert.assertEquals("importt", value.getExpression());
        
        value = model.getUnit(8, ValueExpression.class);
        Assert.assertEquals("argst", value.getExpression());
        
        value = model.getUnit(10, ValueExpression.class);
        Assert.assertEquals("format", value.getExpression());
    }
    
    @Test
    public void breakStatementNotInLoop() throws Exception {
        TemplateParser parser = createParser();
        
        File f = findTemplate("rocker/parser/BreakStatementNotInLoop.rocker.html");
        
        try {
            TemplateModel model = parser.parse(f);
            Assert.fail();
        } catch (ParserException e) {
            Assert.assertEquals(2, e.getLineNumber());
        }
    }
    
    @Test
    public void breakStatementNotInLoop2() throws Exception {
        TemplateParser parser = createParser();
        
        File f = findTemplate("rocker/parser/BreakStatementNotInLoop2.rocker.html");
        
        try {
            TemplateModel model = parser.parse(f);
            Assert.fail();
        } catch (ParserException e) {
            Assert.assertEquals(1, e.getLineNumber());
        }
    }
}
