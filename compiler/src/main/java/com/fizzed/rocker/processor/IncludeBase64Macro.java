/*
 * Copyright 2015 Fendler Consulting cc.
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
package com.fizzed.rocker.processor;

import java.io.File;
import java.io.IOException;
import java.util.Base64;

/**
 * Includes the contents of a file as a Base-64 encoded string.
 * 
 * @author jensfendler
 */
public class IncludeBase64Macro extends AbstractMacroProcessor {

    public IncludeBase64Macro() {
        super("includeBase64");
    }

    /**
     * @see com.fizzed.rocker.processor.AbstractMacroProcessor#processMacro(java.lang.String)
     */
    @Override
    protected String processMacro(String filename) throws MacroException {
        if (filename == null) {
            throw new MacroException("Arguments for includeBase64 macro must not be null");
        }

        try {
            byte[] data = readFileToByteArray(new File(filename));
            return Base64.getEncoder().encodeToString(data);
        } catch (IOException e) {
            throw new MacroException("Could not include file " + filename + " with base-64 encoding.", e);
        }
    }

}
