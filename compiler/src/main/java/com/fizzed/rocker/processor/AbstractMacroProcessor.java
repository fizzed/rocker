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
package com.fizzed.rocker.processor;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fizzed.rocker.model.PlainText;
import com.fizzed.rocker.model.PostProcessorException;
import com.fizzed.rocker.model.TemplateModel;
import com.fizzed.rocker.model.TemplateModelPostProcessor;

/**
 * This Post-Processor provides some abstract functionality to provide simple
 * "macro replacements" through subclasses.
 * 
 * It parses static text units ({@see PlainText}) of the model for snippets of
 * 
 * <pre>
 * ##macroName:BEGIN##...##macroName:END##
 * </pre>
 * 
 * and passes all text between the begin-sequence and the end-sequence to the
 * processMacro() method for replacement.
 * 
 * @author jensfendler
 *
 */
public abstract class AbstractMacroProcessor implements TemplateModelPostProcessor {

    private static final Logger log = LoggerFactory.getLogger(AbstractMacroProcessor.class);

    private static final String MACRO_PATTERN = "##MACRONAME:BEGIN##.*##MACRONAME:END##";

    private String macroName;

    private Pattern pattern;

    public AbstractMacroProcessor(String macroName) {
        this.macroName = macroName;
        this.pattern = Pattern.compile(MACRO_PATTERN.replaceAll("MACRONAME", macroName));
        log.debug("Using pattern '{}' for macro '{}'.", pattern.pattern(), macroName);
    }

    /**
     * @see com.fizzed.rocker.model.TemplateModelPostProcessor#process(com.fizzed.rocker.model.TemplateModel,
     *      int)
     */
    @Override
    public TemplateModel process(TemplateModel templateModel, int ppIndex) throws PostProcessorException {
        log.debug("Processing '{}' macros in template {}.", macroName, templateModel.getName());

        for (int i = 0; i < templateModel.getUnits().size(); i++) {

            // consider PlainText units only
            if (templateModel.getUnits().get(i) instanceof PlainText) {
                PlainText pt = (PlainText) templateModel.getUnits().get(i);

                // evaluate any macros in the text unit
                String replacementText;
                try {
                    replacementText = processMacroInternal(pt.getText());
                    if (!pt.getText().equals(replacementText)) {
                        log.debug("Replacing text unit '{}' in template {} with '{}'", pt.getText(),
                                templateModel.getName(), replacementText);

                        // create a replacement unit for the model
                        PlainText replacementPt = new PlainText(pt.getSourceRef(), replacementText);

                        // replace the current PlainText unit
                        templateModel.getUnits().add(i, replacementPt);
                        templateModel.getUnits().remove(i + 1);
                    } else {
                        log.debug("Leaving text unit '{}' in template {} unchanged.", pt.getText(),
                                templateModel.getName());
                    }

                } catch (MacroException e) {
                    throw new PostProcessorException("Replacement failed for macro '" + macroName + "' in template "
                            + templateModel.getName() + ".", e);
                }
            }
        }

        return templateModel;
    }

    /**
     * @param text
     * @return
     */
    private String processMacroInternal(String text) throws MacroException {
        Matcher m = pattern.matcher(text);
        while (m.find()) {
            String macro = m.group();
            log.debug("Found Macro: '{}'", macro);
            String arguments = macro.substring(10 + macroName.length(), macro.length() - (8 + macroName.length()));
            String replacement = processMacro(arguments);
            text = m.replaceFirst(replacement);
            m = pattern.matcher(text);
        }
        return text;
    }

    /**
     * @param arguments
     * @return
     */
    protected abstract String processMacro(String arguments) throws MacroException;

    /**
     * Reads the contents of a file into a single byte array.
     * 
     * @param file
     * @return the contents of the given file as a byte array.
     * @throws IOException
     */
    protected static byte[] readFileToByteArray(File file) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            byte[] buf = new byte[8192];
            int read = -1;
            while ((read = bis.read(buf)) != -1) {
                baos.write(buf, 0, read);
            }
            return baos.toByteArray();
        } finally {
            try {
                bis.close();
            } catch (Exception e2) {
            }
        }
    }

    /**
     * Reads the contents of a file into a single String.
     * 
     * @param file
     * @return the contents of the given file as a String.
     * @throws IOException
     */
    protected static String readFileToString(File file) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        try {
            StringBuffer sb = new StringBuffer();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append(System.getProperty("line.separator"));
            }
            return sb.toString();
        } finally {
            try {
                br.close();
            } catch (Exception e2) {
            }
        }
    }

}
