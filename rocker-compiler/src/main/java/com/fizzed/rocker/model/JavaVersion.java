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
package com.fizzed.rocker.model;

import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author joelauer
 */
public enum JavaVersion {

    v1_6 (6, "1.6"),
    v1_7 (7, "1.7"),
    v1_8 (8, "1.8");

    private final int version;
    private final String label;

    JavaVersion(int version, String label) {
        this.version = version;
        this.label = label;
    }

    public int getVersion() {
        return version;
    }

    public String getLabel() {
        return label;
    }

    static public JavaVersion findByLabel(String label) {
        // starting from Java 9, the version is simply a number, i.e. 9, 10, 11, and so on
        // we select the best minimum compatible level for those Java versions
        if (StringUtils.isNumeric(label)) {
            return v1_8;
        }

        for (JavaVersion jv : JavaVersion.values()) {
            if (jv.getLabel().equals(label)) {
                return jv;
            }
        }
        return null;
    }

}
