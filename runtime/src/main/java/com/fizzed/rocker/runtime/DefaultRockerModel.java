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

import com.fizzed.rocker.RenderingException;
import com.fizzed.rocker.RockerModel;
import com.fizzed.rocker.RockerModelCallback;
import com.fizzed.rocker.RockerOutput;

public class DefaultRockerModel implements RockerModel {

    private boolean rendered;
    private RockerModelCallback callback;

    /**
     * Renders model and template content to output. Single use only.
     *
     * @return The output of rendering process
     * @throws RenderingException Thrown if any error encountered while rendering
     *                            template. Exception will include underlying cause as well as line
     *                            and position of original template source that triggered exception.
     */
    @Override
    public RockerOutput render() throws RenderingException {
        return render(null);
    }

    protected RockerOutput render(DefaultRockerTemplate context) throws RenderingException {
        // no real need for thread safety since templates should only be used by a single thread
        if (this.rendered) {
            throw new RenderingException("Template already rendered (templates are single use only!");
        }

        DefaultRockerTemplate template = buildTemplate();

        if (this.callback != null) {
            this.callback.onRender(template);
        }

        this.rendered = true;

        return template.__render(context);
    }

    protected DefaultRockerTemplate buildTemplate() throws RenderingException {
        throw new RenderingException("Rocker model does not implement buildTemplate(). Did you forget to implement?");
    }

    public void __callback(RockerModelCallback callback) {
        this.callback = callback;
    }

    @Override
    public String toString() {
        throw new UnsupportedOperationException("toString() not permitted on DefaultRockerTemplateModel. User render() method.");
    }

}
