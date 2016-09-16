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
package com.fizzed.rocker.model;

/**
 *
 * @author jensfendler
 */
public class PostProcessorException extends Exception {

    /**
     * Create a new instance of a {@link PostProcessorException} with the given
     * message.
     *
     * @param message the error message
     */
    public PostProcessorException(String message) {
        super(message);
    }
    
    /**
     * Create a new instance of a {@link PostProcessorException} with the given
     * message and the causing Throwable.
     *
     * @param message the error message
     * @param cause the {@link Throwable} which caused this exception
     */
    public PostProcessorException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
