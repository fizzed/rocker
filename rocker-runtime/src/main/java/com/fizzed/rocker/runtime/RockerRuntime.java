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

import javax.tools.ToolProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author joelauer
 */
public class RockerRuntime {
    private final static Logger log = LoggerFactory.getLogger(RockerRuntime.class.getName());
    
    static public final String KEY_RELOADING = "rocker.reloading";
    static public final String CONF_RESOURCE_NAME = "/rocker-compiler.conf";
    
    private static class Holder {
        static final RockerRuntime INSTANCE = new RockerRuntime();
    }
    
    private Boolean reloading;
    private RockerBootstrap bootstrap;
    
    private RockerRuntime() {
    
        log.info("Rocker version {}", com.fizzed.rocker.Version.getVersion());
        
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
        if (this.reloading != null && this.reloading == reloading) {
            // noop
            return;
        }
        
        if (reloading) {
            // build instance using reflection (so if its missing we can provide a better exception message)
            this.bootstrap = buildReloadingRockerBootstrap();
            log.info("Rocker template reloading activated");
        } else {
            this.bootstrap = new DefaultRockerBootstrap();
            log.info("Rocker template reloading not activated");
        }
        
        this.reloading = reloading;
    }

    public RockerBootstrap getBootstrap() {
        return bootstrap;
    }
    
    public boolean isReloadingPossible() {
        try {
            buildReloadingRockerBootstrap();
            return true;
        } catch (Throwable t) {
            return false;
        }
    }
    
    private RockerBootstrap buildReloadingRockerBootstrap() {
        // is a java compiler available?
        if (ToolProvider.getSystemJavaCompiler() == null) {
            throw new RuntimeException("Unable to activate Rocker template reloading. No system java compiler available. Are you running with a JRE instead of a JDK?");
        }
        
        // is the /rocker.conf file on classpath so we can find & (re)compile templates
        if (getClass().getResource(CONF_RESOURCE_NAME) == null) {
            throw new RuntimeException("Unable to activate Rocker template reloading. Unable to find " + CONF_RESOURCE_NAME + " on classpath. Did one get generated during the build?");
        }
        
        try {
            // can reloading successfully be executed in this runtime?
            Class<?> bootstrapType = Class.forName("com.fizzed.rocker.reload.ReloadingRockerBootstrap");
            return (RockerBootstrap)bootstrapType.newInstance();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Unable to activate Rocker template reloading. Did you forget to include 'rocker-compiler' as an optional/provided dependency?");
        } catch (Exception e) {
            throw new RuntimeException("Unable to activate Rocker template reloading. Unable to create ReloadingRockerBootstrap instance", e);
        }
    }
    
}
