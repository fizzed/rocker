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
package com.fizzed.rocker.compiler;

import com.fizzed.rocker.ContentType;
import com.fizzed.rocker.compiler.RockerUtil;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author joelauer
 */
public class RockerUtilTest {
    
    /**
    @Test
    public void relativePath() throws Exception {
        File bd = new File("src/test/resources");
        String rp;
        
        rp = RockerUtil.relativePath(bd, new File("src/test/resources/templates/KitchenSink.rocker.html"));
        Assert.assertEquals("templates/KitchenSink.rocker.html", rp);
        
        try {
            rp = RockerUtil.relativePath(new File("src/test/java"), new File("src/test/resources/templates/KitchenSink.rocker.html"));
            Assert.fail("Invalid relative path");
        } catch (IOException e) {
            // expected
        }
        
        // no base dir just returns the file path
        File f = new File("src/test/resources/templates/KitchenSink.rocker.html");
        Assert.assertEquals(f.getPath(), RockerUtil.relativePath(null, f));
    }
    */
    
    @Test
    public void pathToPackageName() throws Exception {
        // make sure this works on windows & unix
        Assert.assertEquals("com.fizzed", RockerUtil.pathToPackageName(Paths.get("com", "fizzed")));
    }
    
    @Test
    public void templateNameToName() throws Exception {
        Assert.assertEquals("index", RockerUtil.templateNameToName("index.rocker.html"));
        Assert.assertEquals("index", RockerUtil.templateNameToName("index.html"));
        
        try {
            RockerUtil.templateNameToName("index");
            Assert.fail("Exception expected");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }
    
    @Test
    public void templateNameToContentType() throws Exception {
        Assert.assertEquals(ContentType.HTML, RockerUtil.templateNameToContentType("index.rocker.html"));
        Assert.assertEquals(ContentType.RAW, RockerUtil.templateNameToContentType("index.rocker.raw"));
        Assert.assertEquals(ContentType.HTML, RockerUtil.templateNameToContentType("index.html"));
        
        try {
            RockerUtil.templateNameToContentType("index");
            Assert.fail("Exception expected");
        } catch (IllegalArgumentException e) {
            // expected
        }
        
        try {
            RockerUtil.templateNameToContentType("index.unsupported");
            Assert.fail("Exception expected");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }
    
    @Test
    public void chompClosureOpen() {
        Assert.assertEquals("value()", RockerUtil.chompClosureOpen("value()->{"));
        Assert.assertEquals("value()", RockerUtil.chompClosureOpen("value() -> {"));
        Assert.assertEquals("value()", RockerUtil.chompClosureOpen("value()\t\r\n ->\t\n{"));
    }
    
    private String doAppendByteAsJavaByteInitializer(byte b) {
        StringBuilder sb = new StringBuilder();
        RockerUtil.appendByteAsJavaByteInitializer(sb, b);
        return sb.toString();
    }
    
    @Test
    public void appendByteAsJavaByteInitializer() {
        Assert.assertEquals("'j'", doAppendByteAsJavaByteInitializer((byte)'j'));
        Assert.assertEquals("0x00", doAppendByteAsJavaByteInitializer((byte)0x00));
        Assert.assertEquals("(byte)0x80", doAppendByteAsJavaByteInitializer((byte)0x80));
    }
    
    @Test
    public void getTextAsJavaByteArrayInitializer() throws Exception {
        Assert.assertEquals("new byte[] { 'h', 't', 'm', 'l' };", RockerUtil.getTextAsJavaByteArrayInitializer("html", "UTF-8", 4096).get(0));
        Assert.assertEquals("new byte[] { (byte)0xE2, (byte)0x82, (byte)0xAC };", RockerUtil.getTextAsJavaByteArrayInitializer("\u20AC", "UTF-8", 4096).get(0));
        
        List<String> byteArrays = RockerUtil.getTextAsJavaByteArrayInitializer("\u20AC", "UTF-8", 1);
        Assert.assertEquals("new byte[] { (byte)0xE2 };", byteArrays.get(0));
        Assert.assertEquals("new byte[] { (byte)0x82 };", byteArrays.get(1));
        Assert.assertEquals("new byte[] { (byte)0xAC };", byteArrays.get(2));
    }
    
    @Test
    public void stringIntoChunks() {
        List<String> chunks;
        
        chunks = RockerUtil.stringIntoChunks("sj", 1);
        Assert.assertEquals(2, chunks.size());
        Assert.assertEquals("s", chunks.get(0));
        Assert.assertEquals("j", chunks.get(1));
    }
    
}
