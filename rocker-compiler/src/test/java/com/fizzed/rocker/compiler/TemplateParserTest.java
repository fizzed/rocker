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

import com.fizzed.rocker.ContentType;
import com.fizzed.rocker.model.*;
import com.fizzed.rocker.runtime.ParserException;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static com.fizzed.rocker.compiler.TestHelper.normalizeNewlines;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 *
 * @author joelauer
 */
public class TemplateParserTest {

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

        Assert.assertEquals("(valueForElseIfTrue)", model.getUnit(10, IfBlockElseIf.class).getExpression());

        String plainText6 = "\n" +
          "                else-if-block\n" +
          "            ";

        Assert.assertEquals(plainText6, model.getUnit(11, PlainText.class).getText());

        Assert.assertEquals(IfBlockElse.class, model.getUnit(12, IfBlockElse.class).getClass());

        String plainText7 = "\n" +
          "                else-block\n" +
          "            ";
        Assert.assertEquals(plainText7, model.getUnit(13, PlainText.class).getText());

        Assert.assertEquals(IfBlockEnd.class, model.getUnit(14, IfBlockEnd.class).getClass());

        String plainText8 = "\n" +
          "        ";
        Assert.assertEquals(plainText8, model.getUnit(15, PlainText.class).getText());

        Assert.assertEquals(ForBlockEnd.class, model.getUnit(16, ForBlockEnd.class).getClass());

        String plainText9 = "\n" +
          "    </body>\n" +
          "</html>";
        Assert.assertEquals(plainText9, model.getUnit(17, PlainText.class).getText());
    }

    @Test
    public void plainInHeaderThrowsException() throws Exception {
        TemplateParser parser = createParser();
        File f = findTemplate("rocker/parser/PlainTextInHeaderError.rocker.html");

        try {
            parser.parse(f);
            fail("Expected parsing failure");
        }
        catch (ParserException e) {
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

            fail("expected exception");
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
            fail("expected exception");
        }
        catch (ParserException e) {
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
            fail();
        }
        catch (ParserException e) {
            Assert.assertEquals(2, e.getLineNumber());
        }
    }

    @Test
    public void breakStatementNotInLoop2() throws Exception {
        TemplateParser parser = createParser();

        File f = findTemplate("rocker/parser/BreakStatementNotInLoop2.rocker.html");

        try {
            TemplateModel model = parser.parse(f);
            fail();
        }
        catch (ParserException e) {
            Assert.assertEquals(1, e.getLineNumber());
        }
    }

    @Test
    public void optionPostProcessing1() throws Exception {
        TemplateParser parser = createParser();
        File f = findTemplate("rocker/parser/OptionPostProcessing1.rocker.html");

        TemplateModel model = parser.parse(f);

        Assert.assertArrayEquals(new String[]{"com.fizzed.rocker.processor.WhitespaceRemovalProcessor",
          "com.fizzed.rocker.processor.LoggingProcessor"}, model.getOptions().getPostProcessing());
    }

    @Test
    public void optionPostProcessing2() throws Exception {
        TemplateParser parser = createParser();
        File f = findTemplate("rocker/parser/OptionPostProcessing2.rocker.html");

        TemplateModel model = parser.parse(f);

        Assert.assertArrayEquals(new String[]{"com.fizzed.rocker.processor.WhitespaceRemovalProcessor",
          "com.fizzed.rocker.processor.LoggingProcessor"}, model.getOptions().getPostProcessing());
    }

    @Test
    public void optionPostProcessing3() throws Exception {
        TemplateParser parser = createParser();
        File f = findTemplate("rocker/parser/OptionPostProcessing3.rocker.html");

        TemplateModel model = parser.parse(f);

        Assert.assertArrayEquals(new String[]{"com.fizzed.rocker.processor.LoggingProcessor",
          "com.fizzed.rocker.processor.WhitespaceRemovalProcessor",
          "com.fizzed.rocker.processor.LoggingProcessor"}, model.getOptions().getPostProcessing());
    }

    @Test
    public void valueNullSafe() throws Exception {
        TemplateParser parser = createParser();
        File f = findTemplate("rocker/parser/ValueNullSafe.rocker.html");

        TemplateModel model = parser.parse(f);

        ValueExpression value = model.getUnit(1, ValueExpression.class);

        assertThat(value.isNullSafe(), is(true));
        assertThat(value.getExpression(), is("s"));
    }

    @Test
    public void eval() throws Exception {
        TemplateParser parser = createParser();
        File f = findTemplate("rocker/parser/Eval.rocker.html");

        TemplateModel model = parser.parse(f);

        EvalExpression eval = model.getUnit(1, EvalExpression.class);

        assertThat(eval.isNullSafe(), is(false));
        assertThat(eval.getExpression(), is("(a)"));

        eval = model.getUnit(3, EvalExpression.class);

        assertThat(eval.isNullSafe(), is(false));
        assertThat(eval.getExpression(), is("(a + \"more\")"));

        eval = model.getUnit(5, EvalExpression.class);

        assertThat(eval.isNullSafe(), is(false));
        assertThat(eval.getExpression(), is("(a + \"more\"+\"even more\"+0)"));
    }

    @Test
    public void nullTernary() throws Exception {
        TemplateParser parser = createParser();
        File f = findTemplate("rocker/parser/NullTernary.rocker.html");

        TemplateModel model = parser.parse(f);

        NullTernaryExpression nullTernary = model.getUnit(1, NullTernaryExpression.class);

        assertThat(nullTernary.getLeftExpression(), is("a"));
        assertThat(nullTernary.getRightExpression(), is("b"));

        nullTernary = model.getUnit(3, NullTernaryExpression.class);

        assertThat(nullTernary.getLeftExpression(), is("a"));
        assertThat(nullTernary.getRightExpression(), is("\"default\""));

        nullTernary = model.getUnit(5, NullTernaryExpression.class);

        assertThat(nullTernary.getLeftExpression(), is("a"));
        assertThat(nullTernary.getRightExpression(), is("0"));

        nullTernary = model.getUnit(7, NullTernaryExpression.class);

        assertThat(nullTernary.getLeftExpression(), is("a"));
        assertThat(nullTernary.getRightExpression(), is("0L"));
    }

    @Test
    public void withStatementMultipleArguments() throws Exception {
        TemplateParser parser = createParser();
        File f = findTemplate("rocker/parser/WithBlockMultipleArguments.rocker.html");

        TemplateModel model = parser.parse(f);

        WithBlockBegin withBlockBegin = model.getUnit(1, WithBlockBegin.class);
        assertNotNull("Must be non null", withBlockBegin);
        assertThat(withBlockBegin.getExpression(), is("(String b = a.get(0), List<String> list = map.get(\"abc\"), Long value = Long.valueOf(a.get(0)))"));

        List<WithStatement.VariableWithExpression> variableWithExpressions = withBlockBegin.getStatement().getVariables();
        WithStatement.VariableWithExpression variableWithExpression = variableWithExpressions.get(0);
        assertThat(variableWithExpression.getVariable().getName(), is("b"));
        assertThat(variableWithExpression.getVariable().getType(), is("String"));
        assertThat(variableWithExpression.getValueExpression(), is("a.get(0)"));

        variableWithExpression = variableWithExpressions.get(1);
        assertThat(variableWithExpression.getVariable().getName(), is("list"));
        assertThat(variableWithExpression.getVariable().getType(), is("List<String>"));
        assertThat(variableWithExpression.getValueExpression(), is("map.get(\"abc\")"));

        variableWithExpression = variableWithExpressions.get(2);
        assertThat(variableWithExpression.getVariable().getName(), is("value"));
        assertThat(variableWithExpression.getVariable().getType(), is("Long"));
        assertThat(variableWithExpression.getValueExpression(), is("Long.valueOf(a.get(0))"));
    }

    @Test
    public void withStatementNullSafeNotAllowedMultipleArguments() throws Exception {
        TemplateParser parser = createParser();
        File f = findTemplate("rocker/parser/WithBlockMultipleArgumentsNullSafeFails.rocker.html");

        try {
            parser.parse(f);
            fail("Expected ParserException");
        }
        catch (ParserException e) {
            assertThat(e.getMessage(), containsString("Nullsafe option not allowed for with block with multiple arguments"));
        }
    }

    @Test
    public void testIfStatement() throws Exception {
        TemplateParser parser = createParser();
        File f = findTemplate("rocker/parser/IfStatement.rocker.html");
        TemplateModel model = parser.parse(f);
        IfBlockBegin unit = model.findUnitByOccurrence(IfBlockBegin.class, 1);
        assertThat(unit.getExpression(), is("(s.equals(\"a\"))"));

        unit = model.findUnitByOccurrence(IfBlockBegin.class, 2);
        assertThat(unit.getExpression(), is("( s.equals(\"b\") )"));
    }

    @Test
    public void testSwitchBlockWithDefault() throws Exception {

        TemplateParser parser = createParser();
        File f = findTemplate("rocker/parser/SwitchBlockWithDefault.rocker.html");
        TemplateModel model = parser.parse(f);
        SwitchBlock switchBlock = model.findUnitByOccurrence(SwitchBlock.class, 1);
        assertThat(switchBlock.getExpression(), is("(s)"));

        SwitchCaseBlock case1 = model.findUnitByOccurrence(SwitchCaseBlock.class, 1);
        assertThat(case1.getExpression(), is("\"test\""));

        SwitchCaseBlock case2 = model.findUnitByOccurrence(SwitchCaseBlock.class, 2);
        assertThat(case2.getExpression(), is("\"test2\""));

        SwitchDefaultBlock defaultBlock = model.findUnitByOccurrence(SwitchDefaultBlock.class, 1);
        assertNull(defaultBlock.getExpression());
    }

    @Test
    public void testSwitchBlockWithDefaultSpaces() throws Exception {

        TemplateParser parser = createParser();
        File f = findTemplate("rocker/parser/SwitchBlockWithDefaultSpaces.rocker.html");
        TemplateModel model = parser.parse(f);
        SwitchBlock switchBlock = model.findUnitByOccurrence(SwitchBlock.class, 1);
        assertThat(switchBlock.getExpression(), is("(s)"));

        SwitchCaseBlock case1 = model.findUnitByOccurrence(SwitchCaseBlock.class, 1);
        assertThat(case1.getExpression(), is("\"test\""));

        SwitchCaseBlock case2 = model.findUnitByOccurrence(SwitchCaseBlock.class, 2);
        assertThat(case2.getExpression(), is("\"test2\""));

        SwitchDefaultBlock defaultBlock = model.findUnitByOccurrence(SwitchDefaultBlock.class, 1);
        assertNull(defaultBlock.getExpression());
    }

    @Test
    public void testSwitchBlockWithoutDefault() throws Exception {
        TemplateParser parser = createParser();
        File f = findTemplate("rocker/parser/SwitchBlockWithoutDefault.rocker.html");
        TemplateModel model = parser.parse(f);
        SwitchBlock switchBlock = model.findUnitByOccurrence(SwitchBlock.class, 1);
        assertThat(switchBlock.getExpression(), is("(s)"));

        SwitchCaseBlock case1 = model.findUnitByOccurrence(SwitchCaseBlock.class, 1);
        assertThat(case1.getExpression(), is("\"test\""));

        SwitchCaseBlock case2 = model.findUnitByOccurrence(SwitchCaseBlock.class, 2);
        assertThat(case2.getExpression(), is("\"test2\""));


    }

    @Test
    public void testIfElseStatement() throws Exception {
        TemplateParser parser = createParser();
        File f = findTemplate("rocker/parser/IfElseStatement.rocker.html");
        TemplateModel model = parser.parse(f);

        IfBlockElse unit = model.findUnitByOccurrence(IfBlockElse.class, 1);
        assertThat(unit.getExpression(), nullValue());
        assertThat(unit.getSourceRef().getText(), is("}else{no spaces}"));

        unit = model.findUnitByOccurrence(IfBlockElse.class, 2);
        assertThat(unit.getExpression(), nullValue());
        assertThat(normalizeNewlines(unit.getSourceRef().getText()), is("}\nelse {\nspacy\n}"));
    }

    @Test
    public void testIfElseIfStatement() throws Exception {
        TemplateParser parser = createParser();
        File f = findTemplate("rocker/parser/IfElseIfStatement.rocker.html");
        TemplateModel model = parser.parse(f);

        IfBlockElseIf unit = model.findUnitByOccurrence(IfBlockElseIf.class, 1);
        assertThat(unit.getExpression(), is("(s.equals(\"b\"))"));

        unit = model.findUnitByOccurrence(IfBlockElseIf.class, 2);
        assertThat(unit.getExpression(), is("( s.equals(\"c\") )"));

        unit = model.findUnitByOccurrence(IfBlockElseIf.class, 3);
        assertThat(unit.getExpression(), is("(s.equals(\"d\"))"));

        // Check that the starting if unit is there
        IfBlockBegin ifUnit = model.findUnitByOccurrence(IfBlockBegin.class, 1);
        assertThat(ifUnit.getExpression(), is("(s.equals(\"a\"))"));

        // Check that else is there as well.
        IfBlockElse elseUnit = model.findUnitByOccurrence(IfBlockElse.class, 1);
        assertThat(elseUnit.getExpression(), nullValue());
        assertThat(normalizeNewlines(elseUnit.getSourceRef().getText()), is("}\nelse {else}"));

        // Finally since we had nested javascript in the template, we should find no more occurrences
        // of if, else if and else.

        try {
            model.findUnitByOccurrence(IfBlockBegin.class, 2);
            fail("Should not have found a second @if occurrence!");
        }
        catch (RuntimeException e) {
            // Expected
        }

        try {
            model.findUnitByOccurrence(IfBlockElse.class, 4);
            fail("Should not have found a fourth 'else if' occurrence!");
        }
        catch (RuntimeException e) {
            // Expected
        }

        try {
            model.findUnitByOccurrence(IfBlockElse.class, 2);
            fail("Should not have found a second 'else' occurrence!");
        }
        catch (RuntimeException e) {
            // Expected
        }
    }
}
