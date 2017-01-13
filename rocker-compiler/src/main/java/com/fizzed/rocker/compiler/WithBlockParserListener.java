package com.fizzed.rocker.compiler;

import com.fizzed.rocker.antlr4.WithBlockParser;
import com.fizzed.rocker.antlr4.WithBlockParserBaseListener;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.List;

/**
 * WithBlock listener, gathers the arguments in a list.
 */
public class WithBlockParserListener extends WithBlockParserBaseListener {

    private final List<String> arguments = new ArrayList<>();

    @Override
    public void enterWithArguments(WithBlockParser.WithArgumentsContext ctx) {
         for(final TerminalNode node : ctx.ARGUMENT_COMMA()) {
             final String text = node.getText();
             arguments.add(text.substring(0, text.length()-1)); // Chop off the , at the end.
         }
         arguments.add(ctx.ARGUMENT().getText());
    }

    public List<String> getArguments() {
        return arguments;
    }

    public void clear() {
        arguments.clear();
    }
}
