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

import com.fizzed.rocker.runtime.DefaultRockerBootstrap;
import com.fizzed.rocker.runtime.RockerBootstrap;

/**
 *
 * @author joelauer
 */
public class RockerRuntime {    
    static public final String KEY_RELOADING = "rocker.reloading";
    
    private static class Holder {
        static final RockerRuntime INSTANCE = new RockerRuntime();
    }
    
    private boolean reloading;
    private RockerBootstrap bootstrap;
    
    private RockerRuntime() {
    
        String reloadingProperty = System.getProperty(KEY_RELOADING, "false");
        if (reloadingProperty.equalsIgnoreCase("true")) {
            setReloading(true);
        } else if (reloadingProperty.equalsIgnoreCase("false")) {
            setReloading(false);
        } else {
            throw new IllegalArgumentException("Illegal value [" + reloadingProperty + "] for rocker.reloading sytem property");
        }
        
    }
    
    static public RockerRuntime getInstance() {
        return Holder.INSTANCE;
    }
    
    public boolean isReloading() {
        return this.reloading;
    }
    
    final public void setReloading(boolean reloading) {
        if (reloading) {
            // build instance using reflection (so if its missing we can provide a better exception message)
            this.bootstrap = buildReloadingRockerBootstrap();
        } else {
            this.bootstrap = new DefaultRockerBootstrap();
        }
    }

    public RockerBootstrap getBootstrap() {
        return bootstrap;
    }
    
    private RockerBootstrap buildReloadingRockerBootstrap() {
        try {
            // can reloading successfully be executed in this runtime?
            Class<?> bootstrapType = Class.forName("com.fizzed.rocker.reload.ReloadingRockerBootstrap");
            return (RockerBootstrap)bootstrapType.newInstance();
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Unable to activate Rocker template reloading. Did you forget to include 'rocker-compiler' as an optional dependency?");
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to activate Rocker template reloading. Unable to create ReloadingRockerBootstrap instance", e);
        }
    }
    
}
