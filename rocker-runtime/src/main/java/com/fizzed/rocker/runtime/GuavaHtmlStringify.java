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
 * Guava-based implementation of HtmlStringify. Guava uses some fancy buffer
 * techniques to achieve 3-5x performance boosts on escaping.  However, its
 * a large library and so its simply an optional dependency.
 */
public class GuavaHtmlStringify extends DefaultHtmlStringify {

    private final com.google.common.escape.Escaper escaper;
    
    public GuavaHtmlStringify() {
        this.escaper = com.google.common.html.HtmlEscapers.htmlEscaper();
    }
    
    @Override
    public String s(String str) {
        // guava escape does not like nulls
        if (str == null) {
            return null;
        }
        return this.escaper.escape(str);
    }
    
    // all primitives require no further escaping
    
}
