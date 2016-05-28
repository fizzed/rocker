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

import com.fizzed.rocker.compiler.RockerUtil;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;
import com.fizzed.rocker.compiler.TokenException;
import java.util.HashMap;
import java.util.Map;

public class JavaVariable {
    
    static final public Map<String,Class> PRIMITIVES;
    static {
        PRIMITIVES = new HashMap<>();
        PRIMITIVES.put("boolean", Boolean.class);
        PRIMITIVES.put("byte", Byte.class);
        PRIMITIVES.put("char", Character.class);
        PRIMITIVES.put("short", Short.class);
        PRIMITIVES.put("int", Integer.class);
        PRIMITIVES.put("long", Long.class);
        PRIMITIVES.put("float", Float.class);
        PRIMITIVES.put("double", Double.class);
    }

    private final String type;
    private final String name;

    public JavaVariable(String type, String name) {
        if (type == null) {
            this.type = null;
        } else {
            // remove all whitespace
            this.type = type.replaceAll(" ", "");
        }
        this.name = name;
    }

    public String getType() {
        return type;
    }
    
    public String getTypeAsNonPrimitiveType() {
        if (this.type == null) {
            return null;
        }
        
        if (PRIMITIVES.containsKey(this.type)) {
            return PRIMITIVES.get(this.type).getName();
        }
        
        return this.type;
    }

    public String getName() {
        return name;
    }

    public boolean hasType() {
        return this.type != null && !this.type.equals("");
    }
    
    static public JavaVariable parse(String s) throws TokenException {
        List<JavaVariable> vars = parseList(s);
        
        if (vars.size() != 1) {
            throw new TokenException("Invalid java variable");
        }
        
        return vars.get(0);
    }

    static public List<JavaVariable> parseList(String s) throws TokenException {
        // trim list to remove leading/trailing whitespace first
        s = s.trim();
        
        List<JavaVariable> vars = new ArrayList<>();
        int offset = 0;

        while (offset < s.length()) {
            // first will be either type or name
            String typeOrName = parseToken(s, offset);
            
            offset += typeOrName.length();

            if (offset >= s.length() || s.charAt(offset) == ',') {
                // add name but with empty type
                vars.add(new JavaVariable(null, typeOrName.trim()));
            } else {
                // second will be name
                String name = parseToken(s, offset);
                offset += name.length();

                vars.add(new JavaVariable(typeOrName.trim(), name.trim()));
            }
            
            if (offset < s.length()) {
                // skip comma
                offset++;
            }
        }
        
        return vars;
    }

    static public String parseToken(String s, int offset) throws TokenException {
        int begin = -1;
        int nameEnd = -1;
        boolean generic = false;
        int nestedGeneric = 0;
        boolean array = false;
        int nestedArray = 0;
        int i = offset;

        for (; i < s.length(); i++) {
            char c = s.charAt(i);
            
            if (nestedGeneric > 0) {
                if (c == '<') {
                    nestedGeneric++;
                } else if (c == '>') {
                    nestedGeneric--;
                    if (nestedGeneric <= 0) {
                        // done with generic section
                        generic = true;
                    }
                } else {
                    // everything is fair game inside generic since we don't
                    // really need to validate if its valid (will be checked
                    // when template is compiled later on)
                }
            }
            else if (nestedArray > 0) {
                if (RockerUtil.isWhitespace(c)) {
                    // whitespace is okay inside array delimiters
                } else if (c == ']') {
                    nestedArray--;
                    if (nestedArray <= 0) {
                        // done with generic section
                        array = true;
                    }
                } else {
                    throw new TokenException("Unexpected token '" + c + "' inside array delimiters");
                }
            }
            else {
                if (c == '<') {
                    if (generic) {
                        throw new TokenException("Unexpected token '<': generic already defined");
                    }
                    if (begin < 0) {
                        throw new TokenException("Unexpected token '<': generic before name");
                    }
                    if (array) {
                        throw new TokenException("Unexpected token '<': generic cannot be after array delimiters");
                    }
                    
                    // generic will mark end of name part if its not already set
                    if (nameEnd < 0) {
                        nameEnd = i - 1;
                    }
                    
                    nestedGeneric++;
                }
                else if (c == '[') {
                    if (begin < 0) {
                        throw new TokenException("Unexpected token '[': array before name");
                    }
                    
                    // array will mark end of name part if its not already set
                    if (nameEnd < 0) {
                        nameEnd = i - 1;
                    }
                    
                    nestedArray++;
                }
                else if (RockerUtil.isWhitespace(c)) {
                    // whitespace is generally irrelevant except as marker for end of name part
                    if (begin >= 0 && nameEnd < 0) {
                        nameEnd = i - 1;
                    }
                }
                else if (c == ',') {
                    // comma will mark end of everything
                    if (nameEnd < 0) {
                        nameEnd = i - 1;
                    }
                    break;
                }
                else {
                    // start of name
                    if (begin < 0) {
                        begin = i;
                    }
                    else if (nameEnd >= 0) {
                        // another token must be starting
                        break;
                    }
                    else {
                        // name part
                    }  
                }
            }
        }
        
        // was anything not closed?
        if (nestedGeneric > 0) {
            throw new TokenException("Invalid java variable: closing generic token '>' not found");
        }
        
        if (nestedArray > 0) {
            throw new TokenException("Invalid java variable: closing array token ']' not found");
        }
        
        if (begin < 0) {
            throw new TokenException("Invalid java variable: name part never found");
        }

        if (nameEnd < 0) {
            // entire thing MUST have been a name (since being >= 0 above)
            nameEnd = i;
        }

        return s.substring(offset, i);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + Objects.hashCode(this.type);
        hash = 83 * hash + Objects.hashCode(this.name);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final JavaVariable other = (JavaVariable) obj;
        if (!Objects.equals(this.type, other.type)) {
            return false;
        }
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        if (type == null) {
            return name;
        } else {
            return type + " " + name;
        }
    }

}