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

import java.util.Base64;

/**
 * Replaces a given string with its base-64 encoded equivalent.
 * 
 * @author jensfendler
 */
public class Base64Macro extends AbstractMacroProcessor {

    public Base64Macro() {
        super("base64");
    }

    /**
     * @see com.fizzed.rocker.processor.AbstractMacroProcessor#processMacro(java.lang.String)
     */
    @Override
    protected String processMacro(String arguments) throws MacroException {
        if (arguments == null) {
            throw new MacroException("Arguments for base64 macro must not be null");
        }

        return new String(Base64.getEncoder().encode(arguments.getBytes()));
    }

}
