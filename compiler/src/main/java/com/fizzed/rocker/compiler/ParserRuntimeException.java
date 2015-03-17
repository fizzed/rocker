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

import com.fizzed.rocker.model.SourceRef;

/**
 *
 * @author joelauer
 */
public class ParserRuntimeException extends RuntimeException {

    private final int line;
    private final int posInLine;

    public ParserRuntimeException(int line, int posInLine, String msg) {
        this(line, posInLine, msg, null);
    }
    
    public ParserRuntimeException(int line, int posInLine, String msg, Throwable cause) {
        super(msg, cause);
        this.line = line;
        this.posInLine = posInLine;
    }
    
    public ParserRuntimeException(SourceRef sourceRef, String msg, Throwable cause) {
        this(sourceRef.getBegin().getLineNumber(),
                sourceRef.getBegin().getPosInLine(),
                msg, cause);
    }

    public int getLine() {
        return line;
    }

    public int getPosInLine() {
        return posInLine;
    }
    
}