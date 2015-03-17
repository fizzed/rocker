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

import com.fizzed.rocker.ForIterator;

/**
 * ForIterator implementation that only handles the <code>index</code> and 
 * not the actual iteration of elements.
 * 
 * @author joelauer
 */
public class IndexOnlyForIterator implements ForIterator {

    private int index;
    private final int size;
    
    public IndexOnlyForIterator(int size) {
        this.index = -1;
        this.size = size;
    }
    
    public void increment() {
        this.index++;
    }
    
    @Override
    public int index() {
        return index;
    }

    @Override
    public boolean first() {
        return index == 0;
    }

    @Override
    public boolean last() {
        return index == (size - 1);
    }

}
