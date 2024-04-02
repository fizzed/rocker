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
import com.fizzed.rocker.RockerOutput;
import java.nio.charset.Charset;

/**
 * Output that wraps an <code>OutputStream</code>.  Optimized for writing bytes
 * vs. Strings. Strings are converted to bytes using the specified charset on
 * each write.
 * 
 * @param <T>
 * @author joelauer
 */
public abstract class AbstractRockerOutput<T extends AbstractRockerOutput> implements RockerOutput<AbstractRockerOutput> {
    
    protected final ContentType contentType;
    protected final Charset charset;
    // this may not be set if the output is not optimized for bytes (e.g. StringBuilderOutput)
    protected int byteLength;

    public AbstractRockerOutput(ContentType contentType, String charsetName, int byteLength) {
        this.contentType = contentType;
        this.charset = Charset.forName(charsetName);
    }
    
    public AbstractRockerOutput(ContentType contentType, Charset charset, int byteLength) {
        this.contentType = contentType;
        this.charset = charset;
    }

    @Override
    public ContentType getContentType() {
        return contentType;
    }
    
    @Override
    public Charset getCharsetName() {
        return this.charset;
    }

    @Override
    public int getByteLength() {
        return byteLength;
    }
    
}
