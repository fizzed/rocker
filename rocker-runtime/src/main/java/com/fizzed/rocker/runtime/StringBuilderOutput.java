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

import com.fizzed.rocker.ContentType;
import com.fizzed.rocker.RockerOutputFactory;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Output that wraps an <code>StringBuilder</code>.  Optimized for writing
 * Strings vs. Bytes. Bytes are converted to Strings using the specified charset
 * on each write.
 * 
 * @author joelauer
 */
public class StringBuilderOutput extends AbstractRockerOutput<StringBuilderOutput> {

    public static RockerOutputFactory<StringBuilderOutput> FACTORY
        = new RockerOutputFactory<StringBuilderOutput>() {
            @Override
            public StringBuilderOutput create(ContentType contentType, String charsetName) {
                return new StringBuilderOutput(contentType, charsetName);
            }
        };
    
    private final StringBuilder buffer;
    
    public StringBuilderOutput(ContentType contentType, String charsetName) {
        super(contentType, charsetName, -1);
        this.buffer = new StringBuilder();
    }
    
    public StringBuilderOutput(ContentType contentType, String charsetName, int intialSize) {
        super(contentType, charsetName, -1);
        this.buffer = new StringBuilder(intialSize);
    }
    
    public StringBuilderOutput(ContentType contentType, Charset charset) {
        super(contentType, charset, -1);
        this.buffer = new StringBuilder();
    }
    
    public StringBuilderOutput(ContentType contentType, Charset charset, int intialSize) {
        super(contentType, charset, -1);
        this.buffer = new StringBuilder(intialSize);
    }
    
    public StringBuilder getBuffer() {
        return this.buffer;
    }
    
    @Override
    public StringBuilderOutput w(String string) throws IOException {
        this.buffer.append(string);
        return this;
    }
    
    @Override
    public StringBuilderOutput w(byte[] bytes) throws IOException {
        String s = new String(bytes, charset);
        this.buffer.append(s);
        return this;
    }

    @Override
    public String toString() {
        return this.buffer.toString();
    }
    
}
