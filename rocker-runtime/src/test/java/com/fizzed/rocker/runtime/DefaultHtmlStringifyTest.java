/*
 * Copyright 2016 Fizzed, Inc.
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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class DefaultHtmlStringifyTest {
    
    @Test
    public void escape() {
        DefaultHtmlStringify stringify = new DefaultHtmlStringify();
        assertThat(stringify.s(null), is(nullValue()));
        assertThat(stringify.s(""), is(""));
        assertThat(stringify.s("&"), is("&amp;"));
        assertThat(stringify.s("a&b"), is("a&amp;b"));
        assertThat(stringify.s("ab"), is("ab"));
        assertThat(stringify.s("&b"), is("&amp;b"));
        assertThat(stringify.s("a&"), is("a&amp;"));
        assertThat(stringify.s("a&b<c>d'e\"f"), is("a&amp;b&lt;c&gt;d&#39;e&quot;f"));
    }
    
}
