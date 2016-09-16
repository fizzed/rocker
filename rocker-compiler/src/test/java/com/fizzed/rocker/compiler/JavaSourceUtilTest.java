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

import com.fizzed.rocker.model.SourcePosition;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author joelauer
 */
public class JavaSourceUtilTest {
    
    @Test
    public void findSourcePositionInComment() {
        SourcePosition pos;
        
        pos = JavaSourceUtil.findSourcePositionInComment("    // argument @ [4:6]\n");
        
        Assert.assertEquals(4, pos.getLineNumber());
        Assert.assertEquals(6, pos.getPosInLine());
    }
    
    @Test
    public void findSourcePosition() throws URISyntaxException, IOException {
        
        URL url = this.getClass().getResource("/generated/Args.java.txt");
        File f = new File(url.toURI());
        
        SourcePosition pos;
        
        pos = JavaSourceUtil.findSourcePosition(f, 1, 1);
        
        Assert.assertNull(pos);
        
        
        // // argument @ [4:6]
        // private String s;
        pos = JavaSourceUtil.findSourcePosition(f, 24, 13);
        
        Assert.assertEquals(4, pos.getLineNumber());
        Assert.assertEquals(6, pos.getPosInLine());
        
        // // ValueExpression @ [5:1]
        // __internal.aboutToExecutePosInTemplate(5, 1);
        // __internal.renderValue(s);
        pos = JavaSourceUtil.findSourcePosition(f, 90, 36);
        
        Assert.assertEquals(5, pos.getLineNumber());
        Assert.assertEquals(1, pos.getPosInLine());
        
    }
    
}
