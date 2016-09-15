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
package com.fizzed.rocker.compiler;

import com.fizzed.rocker.ContentType;
import org.apache.commons.lang3.text.translate.*;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import javax.xml.bind.DatatypeConverter;

/**
 *
 * @author joelauer
 */
public class RockerUtil {
    
    private static final Pattern VALID_JAVA_IDENTIFIER = Pattern
            .compile("\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*");

    public static boolean isJavaIdentifier(String identifier) {
        return VALID_JAVA_IDENTIFIER.matcher(identifier).matches();
    }
    
    static public String pathToPackageName(Path path) {
        if (path == null) {
            return "";
        }
        // path.toString() uses File.seperator between components
        return path.toString().replace(File.separator, ".");
    }
    
    static public Path packageNameToPath(String packageName) {
        if (packageName == null || packageName.equals("")) {
            return null;
        }
        return Paths.get(packageName.replace('.', '/'));
    }
    
    static public boolean isRelativePath(Path baseDir, Path file) {
        return file.startsWith(baseDir);
    }
    
    static public boolean isWhitespaceNoLineBreak(char c) {
        // switch statements are always lightning fast
        switch (c) {
            case ' ':
            case '\t':
            case '\r':
                return true;
            default:
                return false;
        }
    }
    
    static public boolean isWhitespace(char c) {
        // switch statements are always lightning fast
        switch (c) {
            case ' ':
            case '\t':
            case '\r':
            case '\n':
                return true;
            default:
                return false;
        }
    }
    
    static public boolean isWhitespace(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (!isWhitespace(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }
    
    static public String consoleFriendlyText(String s) {
        if (s == null) {
            return "<NULL>";
        }
        s = s.replace("\n", "\\n");
        s = s.replace("\r", "\\r");
        return s;
    }
    
    static public String templateNameToName(String templateName) {
        int pos = templateName.indexOf('.');
        // must be at least 1 char
        if (pos < 2) {
            throw new IllegalArgumentException("Invalid template name format (unable find first dot character)");
        }
        return templateName.substring(0, pos);
    }
    
    static public ContentType templateNameToContentType(String templateName) {
        int pos = templateName.lastIndexOf('.');
        // must be at least 1 char
        if (pos < 0) {
            throw new IllegalArgumentException("Invalid template name format (unable find last dot character)");
        }
        
        String ext = templateName.substring(pos+1);
        
        for (ContentType type : ContentType.values()) {
            if (type.toString().equalsIgnoreCase(ext)) {
                return type;
            }
        }
        
        throw new IllegalArgumentException("Unsupported content type for extension [" + ext + "] for template name [" + templateName + "]");
    }
    
    public static Collection<File> listFileTree(File dir) {
        Set<File> fileTree = new HashSet<>();
        
        for (File entry : dir.listFiles()) {
            if (entry.isFile()) fileTree.add(entry);
            else fileTree.addAll(listFileTree(entry));
        }
        
        return fileTree;
    }
    
    public static String qualifiedClassName(Object obj) {
        return qualifiedClassName(obj.getClass());
    }
    
    public static String qualifiedClassName(Class<?> type) {
        return type.getName().replace('$', '.');
    }
    
    public static String unqualifiedClassName(Object obj) {
        return unqualifiedClassName(obj.getClass());
    }
    
    public static String unqualifiedClassName(Class<?> type) {
        String name = type.getName();
        if (name != null && name.lastIndexOf('.') > 0) {
          name = name.substring(name.lastIndexOf('.') + 1); // Map$Entry
          name = name.replace('$', '.');      // Map.Entry
        }
        return name;
    }
    
    // removes " -> {" at end of string
    public static String chompClosureOpen(String expr) {
        if (!expr.endsWith("{")) {
            return expr;
        }

        int closurePos = expr.lastIndexOf("->");
        if (closurePos < 0) {
            return expr;
        }
        
        return expr.substring(0, closurePos).trim();
    }
    
    // removes " => {" at end of string
    public static String chompClosureAssignmentOpen(String expr) {
        if (!expr.endsWith("{")) {
            return expr;
        }

        int closureAssignmentPos = expr.lastIndexOf("=>");
        if (closureAssignmentPos < 0) {
            return expr;
        }
        
        return expr.substring(0, closureAssignmentPos).trim();
    }
    
    static public List<String> stringIntoChunks(String s, int chunkSize) {
        // most likely case
        if (s.length() <= chunkSize) {
            return Arrays.asList(s);
        }
        
        List<String> strings = new ArrayList<>();
        
        for (int offset = 0; offset < s.length(); ) {
            
            int chunkLength = chunkSize;
            
            if ((offset + chunkLength) > s.length()) {
                chunkLength = s.length() - offset;
            }
            
            String chunk = s.substring(offset, offset+chunkLength);
            
            strings.add(chunk);
            
            offset += chunkLength;
        }
        
        return strings;
    }
    
    static public List<String> getTextAsJavaByteArrayInitializer(String text, String charsetName, int maxArraySize) throws UnsupportedEncodingException {
        byte[] bytes = text.getBytes(charsetName);
        
        List<String> arrays = new ArrayList<>();
        
        for (int length = 0; length < bytes.length; ) {
            
            StringBuilder sb = new StringBuilder();
        
            sb.append("new byte[] { ");

            for (int chunk = 0; length < bytes.length && chunk < maxArraySize; chunk++) {
                
                byte b = bytes[length];
                
                if (chunk != 0) {
                    sb.append(", ");
                }

                appendByteAsJavaByteInitializer(sb, b);

                chunk++;
                length++;
            }

            sb.append(" };");
            
            arrays.add(sb.toString());
        }
        
        return arrays;
    }
    
    static public void appendByteAsJavaByteInitializer(StringBuilder sb, byte b) {
        if (b >= 0 && b < 128) {
            if (Character.isAlphabetic(b)) {
                sb.append("'").append((char)b).append("'");
            } else {
                // no cast needed
                sb.append("0x").append(javax.xml.bind.DatatypeConverter.printHexBinary(new byte[] { b }));
            }
        } else {
            // cast needed
            sb.append("(byte)0x").append(javax.xml.bind.DatatypeConverter.printHexBinary(new byte[] { b }));
        }
    }
    
    static public String md5(File f) throws IOException {
        try {
            byte[] b = Files.readAllBytes(f.toPath());
            byte[] hash = MessageDigest.getInstance("MD5").digest(b);
            return DatatypeConverter.printHexBinary(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    /**
     * copied from commons lang, but without Unicode escaper
     * anyway whether you have unicode in your template or not
     * in last case you would like to have a pretty-generated comments for debug
     */
    public static final CharSequenceTranslator ESCAPE_JAVA =
            new LookupTranslator(
                    new String[][] {
                            {"\"", "\\\""},
                            {"\\", "\\\\"},
                    }).with(
                    new LookupTranslator(EntityArrays.JAVA_CTRL_CHARS_ESCAPE())
            );
    
}
