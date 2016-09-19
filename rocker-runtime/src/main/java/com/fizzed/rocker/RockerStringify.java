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

import com.fizzed.rocker.runtime.RawStringify;
import com.fizzed.rocker.runtime.RockerRuntime;

public interface RockerStringify {
    
    static public final RockerStringify RAW = new RawStringify();
    static public final RockerStringify HTML = RockerRuntime.createDefaultHtmlStringify();
    
    String s(String str);
    
    String s(Object obj);
    
    String s(boolean b);
    
    String s(byte b);
    
    String s(char c);
    
    String s(short s);
    
    String s(int i);
    
    String s(long l);
    
    String s(float f);
    
    String s(double d);
    
}
