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
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Output stores a list of references to byte arrays.  Extremely optimized for
 * taking static byte arrays (e.g. plain text) and just saving a pointer to it
 * rather than copying it. Optimized for writing bytes vs. Strings. Strings are
 * converted to bytes using the specified charset on each write and then that
 * byte array is internally stored as part of the underlying list. Thus, the
 * output will consist of reused byte arrays as well as new ones when Strings
 * are written to this output.
 * 
 * @author joelauer
 */
public class ArrayOfByteArraysOutput extends AbstractRockerOutput<ArrayOfByteArraysOutput> {
    
    public static RockerOutputFactory<ArrayOfByteArraysOutput> FACTORY
        = new RockerOutputFactory<ArrayOfByteArraysOutput>() {
            @Override
            public ArrayOfByteArraysOutput create(ContentType contentType, String charsetName) {
                return new ArrayOfByteArraysOutput(contentType, charsetName);
            }
        };
    
    private final List<byte[]> arrays;

    public ArrayOfByteArraysOutput(ContentType contentType, String charsetName) {
        super(contentType, charsetName, 0);
        this.arrays = new ArrayList<byte[]>();
    }
    
    public ArrayOfByteArraysOutput(ContentType contentType, Charset charset) {
        super(contentType, charset, 0);
        this.arrays = new ArrayList<byte[]>();
    }

    public List<byte[]> getArrays() {
        return arrays;
    }
    
    @Override
    public ArrayOfByteArraysOutput w(String string) throws IOException {
        byte[] bytes = string.getBytes(charset);
        arrays.add(bytes);
        this.byteLength += bytes.length;
        return this;
    }

    @Override
    public ArrayOfByteArraysOutput w(byte[] bytes) throws IOException {
        arrays.add(bytes);
        this.byteLength += bytes.length;
        return this;
    }
    
    /**
     * Expensive operation of allocating a byte array to hold the entire contents
     * of this output and then copying each underlying byte array into this new
     * byte array.  Lots of memory copying...
     * 
     * @return 
     */
    public byte[] toByteArray() {
        byte[] bytes = new byte[this.byteLength];
        int offset = 0;
        for (byte[] chunk : arrays) {  
            System.arraycopy(chunk, 0, bytes, offset, chunk.length);
            offset += chunk.length;
        }
        return bytes;
    }

    @Override
    public String toString() {
        // super inneffecient method to convert to string
        // since byte arrays are guaranteed to be split real chars we need to
        // construct the entire array first before doing final convert to string
        byte[] bytes = toByteArray();
        return new String(bytes, this.charset);
    }

    public ReadableByteChannel asReadableByteChannel() {
        return new ReadableByteChannel() {

            private boolean closed = false;
            private int offset = 0;
            private final int length = getByteLength();
            private int chunkIndex = 0;
            private int chunkOffset = 0;
            
            @Override
            public int read(ByteBuffer dst) throws IOException {
                if (closed) {
                    throw new ClosedChannelException();
                }
                
                // end of stream?
                if (arrays.isEmpty() || offset >= length) {
                    return -1;
                }
                
                int readBytes = 0;
                
                // keep trying to fill up buffer while it has capacity and we
                // still have data to fill it up with
                while (dst.hasRemaining() && (offset < length)) {
                
                    byte[] chunk = arrays.get(chunkIndex);
                    int chunkLength = chunk.length - chunkOffset;

                    // number of bytes capable of being read
                    int capacity = dst.remaining();
                    if (capacity < chunkLength) {
                        chunkLength = capacity;
                    }

                    dst.put(chunk, chunkOffset, chunkLength);

                    // update everything
                    offset += chunkLength;
                    chunkOffset += chunkLength;

                    if (chunkOffset >= chunk.length) {
                        // next chunk next time
                        chunkIndex++;
                        chunkOffset = 0;
                    }
                    
                    readBytes += chunkLength;
                }
                
                return readBytes;
            }

            @Override
            public boolean isOpen() {
                return !closed;
            }

            @Override
            public void close() throws IOException {
                closed = true;
            }
        };
    }
    
    /**
    @Override
    public ArrayOfByteArraysOutput w(byte[] bytes, int offset, int length) throws IOException {
        // slightly
        
        stream.write(bytes, offset, length);
        this.byteLength += length;
        return this;
    }
    */
    
}
