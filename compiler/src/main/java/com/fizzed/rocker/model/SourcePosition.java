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
package com.fizzed.rocker.model;

/**
 *
 * @author joelauer
 */
public class SourcePosition {
    
    private final int lineNumber;
    private final int posInLine;
    private final int posInFile;

    public SourcePosition(int lineNumber, int posInLine, int posInFile) {
        this.lineNumber = lineNumber;
        this.posInLine = posInLine;
        this.posInFile = posInFile;
    }

    public int getLineNumber() {
        return this.lineNumber;
    }

    public int getPosInLine() {
        return this.posInLine;
    }

    public int getPosInFile() {
        return this.posInFile;
    }

    @Override
    public String toString() {
        return new StringBuilder(10)
            .append(lineNumber)
            .append(':')
            .append(posInLine)
            .toString();
    }
    
}
