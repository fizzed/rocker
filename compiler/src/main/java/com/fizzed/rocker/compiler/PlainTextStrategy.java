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

/**
 *
 * @author joelauer
 */
public enum PlainTextStrategy {
    
    // as strings (chunked to get around java length limits)
    STATIC_STRINGS,
    
    // as byte arrays (loaded at runtime via an unloaded class to prevent
    // both the string constant in the class file + the byte array from
    // using heap/permgen memory)
    STATIC_BYTE_ARRAYS_VIA_UNLOADED_CLASS

}
