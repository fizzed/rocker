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
package com.fizzed.rocker.reload;

import com.fizzed.rocker.Rocker;
import com.fizzed.rocker.runtime.RockerRuntime;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import org.apache.commons.io.IOUtils;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class ReloadTest {

    static {
        RockerRuntime.getInstance().setReloading(true);
    }
    
    @Test
    public void reload() throws Exception {
        
        // render initial
        String out = Rocker.template("views/index.rocker.html", "Home", "Joe")
            .render()
            .toString();
        
        assertThat(out, containsString("<h1>Hi, Joe!</h1>"));
        
        // to find path reliably of source file, we'll take something on classpath
        // for this project and then do something relative to it
        URL logbackUrl = ReloadTest.class.getResource("/logback.xml");
        File projectDir = new File(logbackUrl.toURI()).getParentFile().getParentFile().getParentFile();
        
        File currentTemplateFile = new File(projectDir, "src/test/java/views/index.rocker.html");
        
        FileInputStream fis = new FileInputStream(currentTemplateFile);
        
        String currentTemplate = IOUtils.toString(fis, "UTF-8");
        
        String newTemplate = currentTemplate.replace("<h1>Hi, @name!</h1>", "<h1>Hi, @name!?!</h1>");
        
        try {
            FileOutputStream fos = new FileOutputStream(currentTemplateFile);
            IOUtils.write(newTemplate, fos, "UTF-8");
            
            out = Rocker.template("views/index.rocker.html", "Home", "Joe")
                .render()
                .toString();

            assertThat(out, containsString("<h1>Hi, Joe!?!</h1>"));
            
        } finally {
            // restore template back...
            FileOutputStream fos = new FileOutputStream(currentTemplateFile);
            IOUtils.write(currentTemplate, fos, "UTF-8");
        }
        
        // since we base reloading on timestamp, need to force something
        // different since these tests run so quickly
        currentTemplateFile.setLastModified(System.currentTimeMillis()+5000);
        
        // try the restored file one more time
        out = Rocker.template("views/index.rocker.html", "Home", "Joe")
            .render()
            .toString();

        assertThat(out, containsString("<h1>Hi, Joe!</h1>"));
    }
    
}
