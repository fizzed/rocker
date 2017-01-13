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
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * Output that wraps an <code>OutputStream</code>.  Optimized for writing bytes
 * vs. Strings. Strings are converted to bytes using the specified charset on
 * each write.
 * 
 * @author joelauer
 */
public class OutputStreamOutput extends AbstractRockerOutput<OutputStreamOutput> {
    
    private final OutputStream stream;

    public OutputStreamOutput(ContentType contentType, OutputStream stream, String charsetName) {
        super(contentType, charsetName, 0);
        this.stream = stream;
    }
    
    public OutputStreamOutput(ContentType contentType, OutputStream stream, Charset charset) {
        super(contentType, charset, 0);
        this.stream = stream;
    }

    public OutputStream getStream() {
        return stream;
    }
    
    @Override
    public OutputStreamOutput w(String string) throws IOException {
        byte[] bytes = string.getBytes(charset);
        stream.write(bytes);
        this.byteLength += bytes.length;
        return this;
    }

    @Override
    public OutputStreamOutput w(byte[] bytes) throws IOException {
        stream.write(bytes);
        this.byteLength += bytes.length;
        return this;
    }
    
}
