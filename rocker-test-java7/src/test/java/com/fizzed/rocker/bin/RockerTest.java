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
package com.fizzed.rocker.bin;

import com.fizzed.rocker.BindableRockerModel;
import com.fizzed.rocker.Rocker;
import com.fizzed.rocker.TemplateBindException;
import com.fizzed.rocker.TemplateNotFoundException;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author joelauer
 */
public class RockerTest {
    
    @Test(expected = TemplateNotFoundException.class)
    public void templateNotFound() throws Exception {
        BindableRockerModel template = Rocker.template("rocker/ArgsNotFound.rocker.html");
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void templateWithValidClassNameButInvalidExtension() throws Exception {
        // use a valid class name, but an invalid rocker file extension name
        BindableRockerModel template = Rocker.template("rocker/Args.ftl.html");
    }
    
    @Test
    public void templateWithBindableProperties() throws Exception {
        BindableRockerModel template = Rocker.template("rocker/Args.rocker.html");
        template.bind("s", "Test");
        template.bind("i", 1);
        
        String out = template.render().toString().trim();
        
        Assert.assertEquals("Test\n1", out);
    }

    @Test
    public void templateWithRelaxedBindableProperties() throws Exception {
        BindableRockerModel template = Rocker.template("rocker/Args.rocker.html");

        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("s", "Test");
        properties.put("i", 1);
        // should not be considered
        properties.put("non-existing", null);

        template.relaxedBind(properties);

        String out = template.render().toString().trim();

        Assert.assertEquals("Test\n1", out);
    }

    @Test
    public void templateWithArgumentsInlined() throws Exception {
        BindableRockerModel template = Rocker.template("rocker/Args.rocker.html", "Test", 1);
        
        String out = template.render().toString().trim();
        
        Assert.assertEquals("Test\n1", out);
    }
    
    @Test(expected = TemplateBindException.class)
    public void templateWithPropertyNotFound() throws Exception {
        BindableRockerModel template = Rocker.template("rocker/Args.rocker.html");
        template.bind("snotfound", "Test");
    }
    
    @Test(expected = TemplateBindException.class)
    public void templateWithPropertyInvalidType() throws Exception {
        BindableRockerModel template = Rocker.template("rocker/Args.rocker.html");
        template.bind("s", 1);
    }

    @Test
    public void templateWithEmptyArgumentsNoSpaces() throws Exception {
        BindableRockerModel template = Rocker.template("rocker/ArgsEmptyNoSpaces.rocker.html");
        String out = template.render().toString().trim();
        Assert.assertEquals("Test", out);
    }
    
    @Test
    public void templateWithEmptyArgumentsWithSpaces() throws Exception {
        BindableRockerModel template = Rocker.template("rocker/ArgsEmptyWithSpaces.rocker.html");
        String out = template.render().toString().trim();
        Assert.assertEquals("Test", out);
    }
    
    @Test
    public void templateWithEmptyArgumentsWithSpacesMultiline() throws Exception {
        BindableRockerModel template = Rocker.template("rocker/ArgsEmptyWithSpacesMultiline.rocker.html");
        String out = template.render().toString().trim();
        Assert.assertEquals("Test", out);
    }
    

}
