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
public class PlainText extends TemplateUnit {
    
    // store twice since some text may be escaped (e.g. "@@" -> "@")
    private String text;

    public PlainText(SourceRef sourceRef, String text) {
        super(sourceRef);
        this.text = text;
    }

    public String getText() {
        return text;
    }
    
    public PlainText combineAdjacent(PlainText other)  throws TokenException {
        SourceRef combinedSourceRef = this.getSourceRef().combineAdjacent(other.getSourceRef());
        return new PlainText(
            combinedSourceRef,
            this.text + other.text);
    }
    
    public boolean isWhitespace() {
        return RockerUtil.isWhitespace(text);
    }
    
    public void trim() {
        this.text = this.text.trim();
    }
    
    public int trailingWhitespaceLengthToStartOfLine() {
        int length = 0;
        for (int i = text.length()-1; i >= 0; i--) {
            char c = text.charAt(i);
            if (RockerUtil.isWhitespaceNoLineBreak(c)) {
                length++;
            } else if (c == '\n') {
                // breaks but not included in trailing whitespace
                break;
            } else {
                // something non-whitespace is on line!
                return -1;
            }
        }
        return length;
    }
    
    public int leadingWhitespaceLengthToEndOfLine() {
        int length = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (RockerUtil.isWhitespaceNoLineBreak(c)) {
                length++;
            } else if (c == '\n') {
                length++;
                break;
            } else {
                // something non-whitespace is on line!
                return -1;
            }
        }
        return length;
    }
    
    public int chompTrailingWhitespaceToStartOfLine() {
        int length = trailingWhitespaceLengthToStartOfLine();
        chompTrailingLength(length);
        return length;
    }
    
    public void chompTrailingLength(int length) {
        if (length > 0 && length <= this.text.length()) {
            this.text = this.text.substring(0, this.text.length() - length);
        }
    }
    
    public int chompLeadingWhitespaceToEndOfLine() {
        int length = leadingWhitespaceLengthToEndOfLine();
        chompLeadingLength(length);
        return length;
    }
    
    public void chompLeadingLength(int length) {
        if (length > 0 && length <= this.text.length()) {
            this.text = this.text.substring(length);
        }
    }
    
    public SourcePosition findSourcePositionOfNonWhitespace() {
        int lineNumber = this.getSourceRef().getBegin().getLineNumber();
        int posInLine = this.getSourceRef().getBegin().getPosInLine();
        int posInFile = this.getSourceRef().getBegin().getPosInFile();
        String sourceText = this.getSourceRef().getText();
        for (int i = 0; i < sourceText.length(); i++) {
            char c = sourceText.charAt(i);
            if (!RockerUtil.isWhitespace(c)) {
                return new SourcePosition(lineNumber, posInLine, posInFile);
            } else if (c == '\n') {
                lineNumber++;
                posInLine = -1;
            }
            posInFile++;
            posInLine++;
        }
        return this.getSourceRef().getBegin();
    }
    
    static public String unescape(String s) {
        // unescaping must occur in 1 pass (otherwise @@@{ would end up only as {
        // if you first replaced "@@" with "@" and then 
        StringBuilder sb = new StringBuilder(s.length());
        
        for (int i = 0; i < s.length(); i++) {
            char c1 = s.charAt(i);
            if (c1 == '@' && (i+1) < s.length()) {
                char c2 = s.charAt(i+1);
                if (c2 == '@') {
                    sb.append("@");
                    i++;        // skip 2 chars
                    continue;
                } else if (c2 == '}') {
                    sb.append("}");
                    i++;
                    continue;   // skip 2 chars
                } else if (c2 == '{') {
                    sb.append("{");
                    i++;
                    continue;   // skip 2 chars
                }
            }
            else if (c1 == '\r' && (i+1) < s.length()) {
                char c2 = s.charAt(i+1);
                if (c2 == '\n') {
                    // only append single newline
                    sb.append("\n");
                    i++;        // skip 2 chars
                    continue;
                }
            }
            
            // otherwise just append the char
            sb.append(c1);
        }
        
        return sb.toString();
    }
    
}
