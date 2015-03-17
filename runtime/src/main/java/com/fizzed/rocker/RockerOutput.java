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
package com.fizzed.rocker;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * 
 * @param <T>
 * @author joelauer
 */
public interface RockerOutput<T extends RockerOutput> {
    
    public Charset getCharset();
    
    /**
     * Writes a String to the output. Implementations are responsible for handling
     * the conversion to the correct charset.
     * 
     * Note that underlying implementations may be optimized to handle Strings
     * vs. Bytes. 
     * 
     * @param string The string to write
     * @return This output (so builder pattern can be used)
     * @throws IOException Thrown on exception.
     */
    public T w(String string) throws IOException;
    
    public T w(byte[] bytes) throws IOException;
    
    //public T w(byte[] bytes, int offset, int length) throws IOException;
    
    public int getByteLength();
    
    @Override
    public String toString();
    
}
