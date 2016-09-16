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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 *
 * @author joelauer
 */
public class JavaSourceUtil {
    
    /**
     * Finds the template source reference embedded within the the prior two
     * lines from the generate Java source.  Allows matching compilation errors
     * from the generate Java source to the underlying Rocker template.
     * 
     * @param javaFile
     * @param lineNumber
     * @param columnNumber
     * @return 
     * @throws IOException 
     */
    static public SourcePosition findSourcePosition(File javaFile, int lineNumber, int columnNumber) throws IOException {
        
        // keep a buffer of the previous two lines before the target line
        String[] lines = new String[3];
        
        int currentLineNumber = 1;
        
        try (BufferedReader br = new BufferedReader(new FileReader(javaFile))) {
            for (String line; (line = br.readLine()) != null; ) {
                
                // cycle lines down one
                lines[0] = lines[1];
                lines[1] = lines[2];
                lines[2] = line;
                
                if (lineNumber == currentLineNumber) {
                    break;  // found it
                }
                
                currentLineNumber++;
            }
        }
        
        // did we find it?
        if (lineNumber != currentLineNumber) {
            return null;
        }
        
        SourcePosition pos0 = findSourcePositionInComment(lines[1]);
        
        if (pos0 != null) {
            return pos0;
        }
        
        SourcePosition pos1 = findSourcePositionInComment(lines[0]);
        
        // value will either be in the previous 2nd line or null if we'll never find it
        return pos1;
    }
    
    static public SourcePosition findSourcePositionInComment(String line) {
        if (line == null) {
            return null;
        }
        
        // e.g. '    // argument @ [5,6]' -> '// argument @ [5,6]'
        line = line.trim();
        
        // check if the line represents a comment
        if (!line.startsWith("// ")) {
            return null;
        }
        
        // search for '@ [' marker to indicate a source position
        int pos = line.indexOf(" @ [");
        
        if (pos < 0) {
            return null;
        }
        
        int end = line.indexOf("]", pos);
        
        if (end < 0) {
            return null;
        }
        
        String sourceRef = line.substring(pos+4, end);
        
        String[] sourceRefs = sourceRef.split(":");
        
        if (sourceRefs.length != 2) {
            return null;
        }
        
        try {
            int lineNumber = Integer.valueOf(sourceRefs[0]);
            int columnNumber = Integer.valueOf(sourceRefs[1]);
            
            return new SourcePosition(lineNumber, columnNumber, -1);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
}
