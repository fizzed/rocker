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
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author joelauer
 */
public class ArrayOfByteArraysOutputTest {
    
    @Test
    public void toStringTest() throws Exception {
        ArrayOfByteArraysOutput out = new ArrayOfByteArraysOutput(ContentType.HTML, "UTF-8");
        out.w("T");
        out.w("E");
        
        Assert.assertEquals("TE", out.toString());
    }
    
    @Test
    public void asReadableByteChannel() throws Exception {
        
        ArrayOfByteArraysOutput out = new ArrayOfByteArraysOutput(ContentType.HTML, "UTF-8");
        out.w("T");
        out.w("E");
        
        ReadableByteChannel rbc;
        ByteBuffer buf;
        int bytesRead;
        
        //
        // allocate too much capacity...
        //
        rbc = out.asReadableByteChannel();
        
        Assert.assertTrue(rbc.isOpen());
        
        buf = ByteBuffer.allocate(5);
        
        bytesRead = rbc.read(buf);
        
        Assert.assertEquals(2, bytesRead);
        Assert.assertEquals((byte)'T', buf.get(0));
        Assert.assertEquals((byte)'E', buf.get(1));
        
        // end of stream
        Assert.assertEquals(-1, rbc.read(buf));
        
        //
        // allocate too little capacity...
        //
        rbc = out.asReadableByteChannel();
        
        buf = ByteBuffer.allocate(1);
        
        bytesRead = rbc.read(buf);
        
        Assert.assertEquals(1, bytesRead);
        Assert.assertEquals((byte)'T', buf.get(0));
        
        buf.flip();
        
        bytesRead = rbc.read(buf);
        
        Assert.assertEquals(1, bytesRead);
        Assert.assertEquals((byte)'E', buf.get(0));
        
        // end of stream
        Assert.assertEquals(-1, rbc.read(buf));
        
        
        //
        // allocate just enough capacity
        //
        rbc = out.asReadableByteChannel();
        
        buf = ByteBuffer.allocate(2);
        
        bytesRead = rbc.read(buf);
        
        Assert.assertEquals(2, bytesRead);
        Assert.assertEquals((byte)'T', buf.get(0));
        Assert.assertEquals((byte)'E', buf.get(1));
        
        // end of stream
        Assert.assertEquals(-1, rbc.read(buf));
    }
    
}
