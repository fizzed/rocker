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

import com.fizzed.rocker.compiler.RockerUtil;
import com.fizzed.rocker.compiler.TokenException;

/**
 *
 * @author joelauer
 */
public class SourceRef {
    
    private final SourcePosition begin;
    private final int charLength;
    //private final SourcePosition end;
    private final String text;

    public SourceRef(SourcePosition begin, int charLength, String text) {
        this.begin = begin;
        this.charLength = charLength;
        this.text = text;
    }
    
    public SourceRef combineAdjacent(SourceRef other) throws TokenException {
        // are they actually adjacent?
        //if ((this.begin.getPosInFile() + this.charLength) != other.begin.getPosInFile()) {
        //    throw new TokenException("Unable to combine non-adjacent tokens");
        //}
        
        return new SourceRef(
            this.begin,
            this.charLength + other.charLength,
            this.text + other.text);
    }

    public SourcePosition getBegin() {
        return begin;
    }

    public int getCharLength() {
        return charLength;
    }

    public String getText() {
        return text;
    }
    
    public String getConsoleFriendlyText() {
        return RockerUtil.consoleFriendlyText(text);
    }

    @Override
    public String toString() {
        return new StringBuilder(20)
            .append(begin)
            .append(" for ")
            .append(charLength)
            .append(" chars")
            .toString();
    }
}
