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
        
        int replaced = 0;
        StringBuilder sb = new StringBuilder(str.length());
        
        // switches are incredibly fast in java
        int size = str.length();
        for (int i = 0; i < size; i++) {
            char c = str.charAt(i);
            switch (c) {
                case '"':
                    sb.append("&quot;");
                    replaced++;
                    break;
                case '\'':
                    sb.append("&#39;"); // Note: "&apos;" is not defined in HTML 4.01.
                    replaced++;
                    break;
                case '&':
                    sb.append("&amp;");
                    replaced++;
                    break;
                case '<':
                    sb.append("&lt;");
                    replaced++;
                    break;
                case '>':
                    sb.append("&gt;");
                    replaced++;
                    break;
                default:
                    sb.append(c);
                    break;
            }
        }
        
        if (replaced == 0) {
            // more efficient than creating a new string if nothing was actually modified
            return str;
        }
        
        return sb.toString();
    }   
}