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
package com.fizzed.rocker;

/**
 *
 * @author joelauer
 */
public enum ContentType {
    
    RAW,
    HTML;
    
    static public boolean removeWhitespace(ContentType contentType) {
        switch (contentType) {
            case RAW:
                return false;
            case HTML:
                return true;
        }
        throw new IllegalArgumentException("Unsupported content type " + contentType);
    }
    
    static public boolean discardLogicWhitespace(ContentType contentType) {
        switch (contentType) {
            case RAW:
                return false;
            case HTML:
                return false;
        }
        throw new IllegalArgumentException("Unsupported content type " + contentType);
    }
    
    static public RockerStringify stringify(ContentType contentType) {
        switch (contentType) {
            case RAW:
                return RockerStringify.RAW;
            case HTML:
                return RockerStringify.HTML;
        }
        throw new IllegalArgumentException("Unsupported content type " + contentType);
    }
    
}