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
package com.fizzed.rocker.runtime;

/**
 * Default implementation of HtmlStringify.
 */
public class DefaultHtmlStringify extends RawStringify {

    @Override
    public String s(String str) {
        return escape(str);
    }

    @Override
    public String s(Object obj) {
        // what to do with null objects?
        if (obj == null) {
            throw new NullPointerException();
        }
        return s(obj.toString());
    }
    
    // all primitives require no further escaping
    
    static public final String escape(String str) {
        if (str == null) {
            return str;
        }
        
        int size = str.length();
        
        // if empty string immediately return it
        if (size == 0) {
            return str;
        }
        
        char c;
        String r;
        int lastPos = 0;
        StringBuilder sb = null;
        
        // switches are one of fastest ways to do replacements in java
        for (int pos = 0; pos < size; pos++) {
            c = str.charAt(pos);
            switch (c) {
                case '"':
                    r = "&quot;";
                    break;
                case '\'':
                    r = "&#39;"; // Note: "&apos;" is not defined in HTML 4.01.
                    break;
                case '&':
                    r = "&amp;";
                    break;
                case '<':
                    r = "&lt;";
                    break;
                case '>':
                    r = "&gt;";
                    break;
                default:
                    r = null;
                    break;
            }
            
            if (r != null) {
                // lazily instantiate just in case no replacements are needed
                if (sb == null) {
                    // room for the replacement at least
                    sb = new StringBuilder(size + r.length() - 1);
                }
                sb.append(str, lastPos, pos);
                sb.append(r);
                lastPos = pos + 1;
            }
        }
        
        if (sb == null) {
            return str;
        }
        
        // anything not appended?
        if (lastPos < size) {
            sb.append(str, lastPos, size);
        }
        
        return sb.toString();
    }   
}