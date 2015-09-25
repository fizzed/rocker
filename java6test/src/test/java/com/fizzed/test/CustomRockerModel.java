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
package com.fizzed.test;

import com.fizzed.rocker.runtime.DefaultRockerModel;

/**
 *
 * @param <T>
 * @author joelauer
 */
public class CustomRockerModel<T extends CustomRockerModel> extends DefaultRockerModel<T> {

    // implicit variables/functions
    protected String implicit;
    
    public T implicit(String s) {
        this.implicit = s;
        return (T)this;
    }
    
    /**
    public Integer i() {
        return 1;
    }
    */

    /**
    @Override
    protected <T> void __configure(T other) throws RenderingException {
        super.__configure(other);
        if (other instanceof CustomRockerModel) {
            CustomRockerModel otherTemplate = (CustomRockerModel)other;
            this.implicit = otherTemplate.implicit;
        }
        else {
            throw new RenderingException("Unable to configure template (not an instance of " + this.getClass().getName() + ")");
        }
    }

    // example of render() providing its own output to tie into specific framework
    @Override
    protected RockerOutput __newOutput() {
        return new CustomRockerOutput();
    }
    */
    
}
