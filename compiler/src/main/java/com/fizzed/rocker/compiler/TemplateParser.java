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

import com.fizzed.rocker.antlr4.RockerLexer;
import com.fizzed.rocker.antlr4.RockerParser;
import com.fizzed.rocker.antlr4.RockerParserBaseListener;
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
import com.fizzed.rocker.model.JavaImport;
import com.fizzed.rocker.model.JavaVariable;
import com.fizzed.rocker.model.JavaVersion;
import com.fizzed.rocker.model.Option;
import com.fizzed.rocker.model.Options;
import com.fizzed.rocker.model.PlainText;
import com.fizzed.rocker.model.SourcePosition;
import com.fizzed.rocker.model.SourceRef;
import com.fizzed.rocker.model.TemplateModel;
import com.fizzed.rocker.model.TemplateUnit;
import com.fizzed.rocker.model.ValueClosureBegin;
import com.fizzed.rocker.model.ValueClosureEnd;
import com.fizzed.rocker.model.ValueExpression;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author joelauer
 */
public class TemplateParser {
    static private final Logger log = LoggerFactory.getLogger(TemplateParser.class);
    
    private File baseDirectory;
    private Options defaultOptions;
    
    public TemplateParser() {
        this.baseDirectory = RockerConfigurationKeys.getParserBaseDirectory();
        this.defaultOptions = RockerConfigurationKeys.getParserOptions();
    }

    public File getBaseDirectory() {
        return baseDirectory;
    }

    public void setBaseDirectory(File baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

    public Options getDefaultOptions() {
        return defaultOptions;
    }

    public void setDefaultOptions(Options defaultOptions) {
        this.defaultOptions = defaultOptions;
    }
   
    public TemplateModel parse(File f) throws IOException, ParserException {
        if (!f.exists() || !f.canRead()) {
            throw new IOException("File cannot read or does not exist [" + f + "]");
        }
        
        ANTLRFileStream input = new ANTLRFileStream(f.getPath(), "UTF-8");
       
        // deduce "package" of file by relativizing it to input directory
        String packageName = RockerUtil.pathToPackageName(f.toPath());
        
        Path p = f.toPath();
        
        if (this.baseDirectory != null) {
            Path bdp = baseDirectory.toPath();
            
            if (!RockerUtil.isRelativePath(bdp, p)) {
                throw new IOException("Template file [" + f + "] not relative to base dir [" + baseDirectory + "]");
            }
            
            Path relativePath = bdp.relativize(p);
            
            // new package name is the parent of this file
            packageName = RockerUtil.pathToPackageName(relativePath.getParent());
        }
        
        return parse(input, packageName, f.getName());
    }
    
    public TemplateModel parse(File f, String packageName) throws IOException, ParserException {
        ANTLRFileStream input = new ANTLRFileStream(f.getPath(), "UTF-8");
        return parse(input, packageName, f.getName());
    }
    
    public TemplateModel parse(String source, String qualifiedName) throws IOException, ParserException {
        ANTLRInputStream input = new ANTLRInputStream(source);
        input.name = qualifiedName;
        return parse(input, "views", qualifiedName);
    }
    
    private TemplateModel parse(ANTLRInputStream input, String packageName, String templateName) throws ParserException {
        // construct path for more helpful error messages
        String templatePath = packageName.replace(".", File.separator) + "/" + templateName;
        
        // get our lexer
        log.trace("Lexer for input stream");
        RockerLexer lexer = new RockerLexer(input);
        lexer.removeErrorListeners();
        lexer.addErrorListener(new DescriptiveErrorListener());

        //
        // lexer
        //
        CommonTokenStream tokens = null;
        try {
            // get a list of matched tokens
            log.trace("Tokenizing lexer");
            tokens = new CommonTokenStream(lexer);
        } catch (ParserRuntimeException e) {
            throw unwrapParserRuntimeException(templatePath, e);
        }
        
        if (log.isTraceEnabled()) {
            // just for debugging lexer
            tokens.fill();
            for (Token token : tokens.getTokens()) {
                log.trace("{}", token);
            }
        }
        
        //
        // parser & new model
        //
        try {
            // pass the tokens to the parser
            log.trace("Parsing tokens");
            RockerParser parser = new RockerParser(tokens);
            parser.removeErrorListeners();
            parser.addErrorListener(new DescriptiveErrorListener());
            
            TemplateModel model = new TemplateModel(packageName, templateName, defaultOptions.copy());
            
            // walk it and attach our listener
            TemplateParserListener listener = new TemplateParserListener(input, model, templatePath);
            ParseTreeWalker walker = new ParseTreeWalker();
            log.trace("Walking parse tree");
            walker.walk(listener, parser.template());
            
            if (model.getOptions().getCombineAdjacentPlain()) {
                combineAdjacentPlain(model);
            }

            // discard whitespace either globally or template-set or also fallsback
            // to the default per content-type
            if (model.getOptions().getDiscardLogicWhitespaceForContentType(model.getContentType())) {
                discardLogicWhitespace(model);
            }
            
            return model;
        } 
        catch (ParserRuntimeException e) {
            throw unwrapParserRuntimeException(templatePath, e);
        }
    }
    
    public ParserException unwrapParserRuntimeException(String templatePath, ParserRuntimeException e) {
        return new ParserException(e.getLine(), e.getPosInLine(), templatePath, e.getMessage(), e.getCause());
    }
    
    public void combineAdjacentPlain(TemplateModel model) throws ParserRuntimeException {
        PlainText lastPlainText = null;
        
        for (int i = 0; i < model.getUnits().size(); ) {
            TemplateUnit unit = model.getUnits().get(i);
            
            if (unit instanceof PlainText) {
   
                PlainText plainText = (PlainText)unit;
                
                if (lastPlainText != null) {
                    
                    try {
                        log.trace("Combining plainText @ {} with {}", lastPlainText.getSourceRef(), plainText.getSourceRef());
                        
                        // combine with last and create new "last"
                        lastPlainText = lastPlainText.combineAdjacent(plainText);
                    }
                    catch (TokenException e) {
                        throw new ParserRuntimeException(plainText.getSourceRef(), e.getMessage(), e);
                    }
                    
                    // replace last unit with the combined version
                    model.getUnits().set(i - 1, lastPlainText);
                    
                    // remove current node
                    model.getUnits().remove(i);
                    
                    // move onto next w/o swapping in last value
                    continue;
                }
                
                lastPlainText = plainText;
            }
            else {
                lastPlainText = null;
            }
            
            i++;
        }
    }
    
    public void discardLogicWhitespace(TemplateModel model) {
        // Discard any lines that are only whitespace up till the first line of non-whitespace
        // text or an expression that would output a value.
        boolean withinContentClosure = false;
        
        for (int i = 0; i < model.getUnits().size(); i++) {
            TemplateUnit unit = model.getUnits().get(i);
            
            // content closures are unique beasts - they are assignments and anything
            // inside of them doesn't count in this calculation
            if (unit instanceof ContentClosureBegin) {
                withinContentClosure = true;
            } else if (unit instanceof ContentClosureEnd) {
                withinContentClosure = false;
            }
            
            if (withinContentClosure) {
                continue;
            }
            
            if (!unit.isBlockLevel()) {
                if (unit instanceof PlainText) {
                    PlainText plain = (PlainText)unit;
                    if (plain.isWhitespace()) {
                        // trim this plain text, but keep searching for next one
                        plain.trim();
                    } else {
                        while (plain.chompLeadingWhitespaceToEndOfLine() > 0) {
                            // keep chomping until we get to the good stuff
                        }
                        // we are done
                        break;
                    }
                } else {
                    break;
                }
            }
        }
        
        // Then discard lines consisting entirely of block-level logic such as if/else
        // blocks, or content/value closures.  Lines that mix non-whitespace text and
        // block-level logic will be skipped.
        // only need to start on second node (since we look behind/forward by 1)
        for (int i = 1; i < model.getUnits().size(); i++) {
            TemplateUnit unit = model.getUnits().get(i);
            if (!unit.isBlockLevel()) {
                continue;
            }
            
            int prevUnitTrailingWhitespaceLengthToStartOfLine = -1;
            PlainText prevPlain = null;
            TemplateUnit prevUnit = model.getUnits().get(i-1);
            if (prevUnit instanceof PlainText) {
                prevPlain = (PlainText)prevUnit;
                prevUnitTrailingWhitespaceLengthToStartOfLine = 
                        prevPlain.trailingWhitespaceLengthToStartOfLine();
            }
            
            boolean lastUnit = ((i+1) == model.getUnits().size());
            
            int nextUnitLeadingWhitespaceLengthToEndOfLine = -1;
            PlainText nextPlain = null;
            if (!lastUnit) {
                TemplateUnit nextUnit = model.getUnits().get(i+1);
                if (nextUnit instanceof PlainText) {
                    nextPlain = (PlainText)nextUnit;
                    nextUnitLeadingWhitespaceLengthToEndOfLine =
                        nextPlain.leadingWhitespaceLengthToEndOfLine();
                }
            }
            
            // do we chop this line?
            if ((prevPlain != null && prevUnitTrailingWhitespaceLengthToStartOfLine >= 0) &&
                    (lastUnit || (nextPlain != null && nextUnitLeadingWhitespaceLengthToEndOfLine >= 0))) {
                
                prevPlain.chompTrailingLength(prevUnitTrailingWhitespaceLengthToStartOfLine);
                if (nextPlain != null && nextUnitLeadingWhitespaceLengthToEndOfLine > 0) {
                    // we actually want to chop the newline char as well!
                    nextPlain.chompLeadingLength(nextUnitLeadingWhitespaceLengthToEndOfLine);
                }
            }
        }
        
        // remove any empty plain text units (since many above may have been chopped down to nothing)
        for (int i = 0; i < model.getUnits().size(); ) {
            TemplateUnit unit = model.getUnits().get(i);
            if (unit instanceof PlainText) {
                PlainText plain = (PlainText)unit;
                if (plain.getText().isEmpty()) {
                    model.getUnits().remove(i);
                    continue;
                }
            }
            i++;
        }
    }
    
    static public class TemplateParserListener extends RockerParserBaseListener {
        
        private final ANTLRInputStream input;
        private final TemplateModel model;
        private final String templatePath;
        
        public TemplateParserListener(ANTLRInputStream input, TemplateModel model, String templatePath) {
            this.input = input;
            this.model = model;
            this.templatePath = templatePath;
        }
        
        public SourceRef createSourceRef(ParserRuleContext rule) {
            return createSourceRef(rule, rule.getStart(), rule.getStop());
        }

        public SourceRef createSourceRef(ParserRuleContext rule, Token start, Token stop) {
            // antlr's position in line is zero-based, but humans think in 1-based
            SourcePosition begin = new SourcePosition(start.getLine(), start.getCharPositionInLine() + 1, start.getStartIndex());
            
            // stop index is inclusive -- we want it exclusive
            int stopIndex = stop.getStopIndex() + 1;
            
            int length = stopIndex - start.getStartIndex();
            
            /**
            // the stop index actually is correct but uses an inclusive value
            // and we want to think in where the next token starts as the end
            int endPosInFile = stop.getStopIndex() + 1;
            stop.getTokenSource().
            SourcePosition end = new SourcePosition(stop.getLine(), stop.getCharPositionInLine(), endPosInFile);
            */

            String text = input.getText(new Interval(begin.getPosInFile(), stop.getStopIndex()));

            return new SourceRef(begin, length, text);
        }
        
        @Override
        public void enterEveryRule(ParserRuleContext ctx) {
            if (log.isTraceEnabled()) {
                log.trace("{} entered with {}", ctx.getClass().getName(), ctx.toStringTree());
            }
        }
        
        @Override
        public void exitEveryRule(ParserRuleContext ctx) {
            if (log.isTraceEnabled()) {
                log.trace("{} exited with {}", ctx.getClass().getName(), ctx.toStringTree());
            }
        }

        public void verifyTemplateHeaderElementOK() {
            // any template units other than whitespace?
            for (TemplateUnit unit : this.model.getUnits()) {
                if (unit instanceof Comment) {
                    // okay
                } else if (unit instanceof PlainText) {
                    PlainText plain = (PlainText)unit;
                    if (plain.isWhitespace()) {
                        // okay
                    } else {
                        // no plain allowed
                        SourcePosition pos = plain.findSourcePositionOfNonWhitespace();
                        throw new ParserRuntimeException(pos.getLineNumber(), pos.getPosInLine(), "plain text not allowed before end of template header");
                    }
                }
            }
            
            // discard whitespace-only plain text
            List<TemplateUnit> units = this.model.getUnits();
            for (int i = 0; i < units.size(); ) {
                TemplateUnit unit = units.get(i);
                if (unit instanceof PlainText) {
                    log.trace("Discarding whitespace-only plain text in template header");
                    units.remove(i);
                } else {
                    i++;
                }
            }
        }

        @Override
        public void enterComment(RockerParser.CommentContext ctx) {
            SourceRef sourceRef = createSourceRef(ctx);
            
            String text = ctx.getText();
            
            // trim leading @* and trailing *@
            String trimmedComment = text.substring(2, text.length()-2);
            
            model.getUnits().add(new Comment(sourceRef, trimmedComment));
        }
        
        @Override
        public void enterImportDeclaration(RockerParser.ImportDeclarationContext ctx) {
            verifyTemplateHeaderElementOK();
            
            SourceRef sourceRef = createSourceRef(ctx);
            
            // we only care about child import statement
            RockerParser.ImportStatementContext statementCtx = ctx.importStatement();
            
            String statement = statementCtx.getText().trim();
            
            model.getImports().add(new JavaImport(sourceRef, statement));
        }

        @Override
        public void enterOptionDeclaration(RockerParser.OptionDeclarationContext ctx) {
            verifyTemplateHeaderElementOK();
            
            SourceRef sourceRef = createSourceRef(ctx);
            
            // we only care about child import statement
            RockerParser.OptionStatementContext statementCtx = ctx.optionStatement();
            
            String statement = statementCtx.getText().trim();
            
            model.getOptions().parseOption(new Option(sourceRef, statement));
        }

        @Override
        public void exitArgumentsDeclaration(RockerParser.ArgumentsDeclarationContext ctx) {
            //log.info("template header completed: line={}", ctx.getStart().getLine());
        }

        @Override
        public void enterArgumentsStatement(RockerParser.ArgumentsStatementContext ctx) {
            verifyTemplateHeaderElementOK();
            
            SourceRef sourceRef = createSourceRef(ctx);
            
            String statement = ctx.getText().trim();
            
            if (!statement.startsWith("(") || !statement.endsWith(")")) {
               throw new ParserException(sourceRef, templatePath, "Arguments for @args must be enclosed with parentheses");
            }
            
            // chomp off parenthese
            statement = statement.substring(1, statement.length() - 1);
            
            try {
                List<JavaVariable> args = JavaVariable.parseList(statement);
                
                // make sure each argument has a type
                for (JavaVariable arg : args) {
                    if (arg.getType() == null) {
                        throw new TokenException("Argument " + arg.getName() + " missing type");
                    }
                    
                    model.getArguments().add(new Argument(sourceRef, arg));
                }
            } catch (TokenException e) {
                throw new ParserException(sourceRef, templatePath, e.getMessage(), e);
            }
            
            // special handling for argument of "RockerBody"
            for (int i = 0; i < model.getArguments().size(); i++) {
                Argument arg = model.getArguments().get(i);
                if (arg.getType().equals("RockerBody")) {
                    // only permitted as the LAST argument
                    if (i != (model.getArguments().size() - 1)) {
                        throw new ParserException(sourceRef, templatePath, "RockerBody type only allowed as last argument");
                    }
                }
            }
            
        }
        
        @Override
        public void enterPlain(RockerParser.PlainContext ctx) {
            SourceRef sourceRef = createSourceRef(ctx);
            
            // is this plain in the context of a plainBlock?
            // we skip processing these since more plains will come that we prefer
            // to process instead
            if (ctx.plainBlock() != null) {
                log.trace("Plain but within context of PlainBlock -- skipping it!");
                return;
            }
            
            String text = ctx.getText();
            
            // unescape it (e.g. @@ -> @)
            String unescaped = PlainText.unescape(text);
            
            model.getUnits().add(new PlainText(sourceRef, unescaped));
        }

        @Override
        public void enterPlainBlock(RockerParser.PlainBlockContext ctx) {
            SourceRef sourceRef = createSourceRef(ctx);
            
            // we simply want to keep the left token for the start of this block
            String text = ctx.LCURLY().getText();
            
            model.getUnits().add(new PlainText(sourceRef, text));
        }
        
        @Override
        public void exitPlainBlock(RockerParser.PlainBlockContext ctx) {
            SourceRef sourceRef = createSourceRef(ctx);
            
            // we simply want to keep the right token for the end of this block
            String text = ctx.RCURLY().getText();
            
            model.getUnits().add(new PlainText(sourceRef, text));
        }

        @Override
        public void enterValueClosure(RockerParser.ValueClosureContext ctx) {
            SourceRef sourceRef = createSourceRef(ctx);
            
            // we only care about the expression
            RockerParser.ValueClosureExpressionContext expressionCtx = ctx.valueClosureExpression();
            
            String expr = expressionCtx.getText();
            
            // we need to chomp off the " -> {" at the end
            expr = RockerUtil.chompClosureOpen(expr);
            
            model.getUnits().add(new ValueClosureBegin(sourceRef, expr));
        }

        @Override
        public void exitValueClosure(RockerParser.ValueClosureContext ctx) {
            SourceRef sourceRef = createSourceRef(ctx);
            
            model.getUnits().add(new ValueClosureEnd(sourceRef));
        }
        
        @Override
        public void enterContentClosure(RockerParser.ContentClosureContext ctx) {
            SourceRef sourceRef = createSourceRef(ctx);
            
            // we only care about the expression
            RockerParser.ContentClosureExpressionContext expressionCtx = ctx.contentClosureExpression();
            
            String expr = expressionCtx.getText();
            
            // we need to chomp off the " => {" at the end
            String identifier = RockerUtil.chompClosureAssignmentOpen(expr);
            
            model.getUnits().add(new ContentClosureBegin(sourceRef, identifier));
        }

        @Override
        public void exitContentClosure(RockerParser.ContentClosureContext ctx) {
            SourceRef sourceRef = createSourceRef(ctx);
            
            model.getUnits().add(new ContentClosureEnd(sourceRef));
        } 

        @Override
        public void enterValue(RockerParser.ValueContext ctx) {
            SourceRef sourceRef = createSourceRef(ctx);
            
            // we only care about the expression
            RockerParser.ValueExpressionContext expressionCtx = ctx.valueExpression();
            
            String expr = expressionCtx.getText();
            
            model.getUnits().add(new ValueExpression(sourceRef, expr));
        }

        @Override
        public void enterForBlock(RockerParser.ForBlockContext ctx) {
            SourceRef sourceRef = createSourceRef(ctx);
            
            // we only care about the expression
            RockerParser.ForStatementContext statementCtx = ctx.forStatement();
            
            String expression = statementCtx.getText();
            
            try {
                ForStatement statement = ForStatement.parse(expression);

                // any Java 1.8+ features used?
                if (!model.getOptions().isGreaterThanOrEqualToJavaVersion(JavaVersion.v1_8) &&
                        statement.hasAnyUntypedArguments()) {
                    throw new TokenException("Untyped variables cannot be used with Java " + model.getOptions().getJavaVersion().getLabel() + " (only allowed with Java 1.8+)");
                }
                
                model.getUnits().add(new ForBlockBegin(sourceRef, expression, statement));
            } catch (TokenException e) {
                throw new ParserException(sourceRef, templatePath, e.getMessage(), e);
            }
        }

        @Override
        public void exitForBlock(RockerParser.ForBlockContext ctx) {
            SourceRef sourceRef = createSourceRef(ctx);
            
            model.getUnits().add(new ForBlockEnd(sourceRef));
        }
        
        @Override
        public void enterIfBlock(RockerParser.IfBlockContext ctx) {
            SourceRef sourceRef = createSourceRef(ctx);
            
            // we only care about the expression
            RockerParser.IfExpressionContext expressionCtx = ctx.ifExpression();
            
            String expr = expressionCtx.getText();
            
            model.getUnits().add(new IfBlockBegin(sourceRef, expr));
        }

        @Override
        public void exitIfBlock(RockerParser.IfBlockContext ctx) {
            SourceRef sourceRef = createSourceRef(ctx);
            
            model.getUnits().add(new IfBlockEnd(sourceRef));
        }

        @Override
        public void enterElseBlock(RockerParser.ElseBlockContext ctx) {
            SourceRef sourceRef = createSourceRef(ctx);
            
            model.getUnits().add(new ElseBlockBegin(sourceRef));
        }
    }   
}