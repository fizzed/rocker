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
 * Callbacks for a RockerModel.
 * 
 * @author joelauer
 */
public interface RockerModelCallback {
    
    /**
     * Called after model creates underlying Template it will render to and
     * immediately before it calls Template.render().  Offers one last chance
     * to inject variables into Template before it executes rendering.
     * 
     * @param template The template that will be rendered by the model.
     */
    void onRender(RockerTemplate template);
    
}
