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

import java.util.BitSet;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author joelauer
 */
public class DescriptiveErrorListener extends BaseErrorListener {
    static private final Logger log = LoggerFactory.getLogger(DescriptiveErrorListener.class);
    
    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object o, int line, int posInLine, String msg, RecognitionException e) {        
        //String sourceName = recognizer.getInputStream().getSourceName();
        //sourceName = !sourceName.isEmpty() ? sourceName+": " : "";
        //log.error(sourceName + " line " + line + ":" + posInLine + " " + msg);
        throw new ParserRuntimeException(line, posInLine, msg, e);
    }

    @Override
    public void reportAmbiguity(Parser parser, DFA dfa, int i, int i1, boolean bln, BitSet bitset, ATNConfigSet atncs) {
        log.trace("reportAmbiguity");
    }

    @Override
    public void reportAttemptingFullContext(Parser parser, DFA dfa, int line, int posInLine, BitSet bitset, ATNConfigSet atncs) {
        log.trace("reportAttemptingFullContext: line=" + line + ", posInLine=" + posInLine + " dfa=" + dfa.toLexerString());
    }

    @Override
    public void reportContextSensitivity(Parser parser, DFA dfa, int i, int i1, int i2, ATNConfigSet atncs) {
        log.trace("reportContextSensitivity");
    }

}
