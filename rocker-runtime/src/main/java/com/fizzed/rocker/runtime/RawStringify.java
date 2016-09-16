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

import com.fizzed.rocker.RockerStringify;

/**
 *
 * @author joelauer
 */
public class RawStringify implements RockerStringify {

    @Override
    public String s(String str) {
        // do nothing by default
        return str;
    }

    @Override
    public String s(Object obj) {
        // what to do with null objects?
        if (obj == null) {
            throw new NullPointerException();
        }
        // do nothing by default
        return obj.toString();
    }
    
    @Override
    public String s(boolean v) {
        return Boolean.toString(v);
    }
    
    @Override
    public String s(byte v) {
        return Byte.toString(v);
    }

    @Override
    public String s(char v) {
        return Character.toString(v);
    }
    
    @Override
    public String s(short v) {
        return Short.toString(v);
    }

    @Override
    public String s(int v) {
        return Integer.toString(v);
    }
    
    @Override
    public String s(long v) {
        return Long.toString(v);
    }

    @Override
    public String s(float v) {
        return Float.toString(v);
    }

    @Override
    public String s(double v) {
        return Double.toString(v);
    }

    
    
}
