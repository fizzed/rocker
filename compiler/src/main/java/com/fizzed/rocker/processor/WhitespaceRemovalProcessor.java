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

import com.fizzed.rocker.model.PlainText;
import com.fizzed.rocker.model.PostProcessorException;
import com.fizzed.rocker.model.TemplateModel;
import com.fizzed.rocker.model.TemplateModelPostProcessor;
import com.fizzed.rocker.model.TemplateUnit;

/**
 * This post-processor reduces the amount of whitespace in static strings.
 *  
 * @author jensfendler
 */
public class WhitespaceRemovalProcessor implements TemplateModelPostProcessor {

    /**
     * For whitespace removals: matching one or more new-lines after horizontal spaces (e.g. space, tab),
     * i.e. at the end of a line. Will be replaced by a single \n.  
     */
    private static final String LINE_END = "[ \t]+[\n\r]+";
    
    /**
     * For whitespace removals: matching more than one horizontal whitespace in a single line.
     * Will be replaced by a single space. 
     */
    private static final String IN_LINE = "[ \t]{2,}";

    /**
     * For whitespace removals: matching one or more horizontal space (e.g. space, tab) after 
     * one or more new-lines (i.e. at the beginning of a line).
     * Will be replaced by a single new line.
     */
    private static final String LINE_START = "[\n\r]+[ \t]+";
 
    /**
     * @see com.fizzed.rocker.model.TemplateModelPostProcessor#process(com.fizzed.rocker.model.TemplateModel, int)
     */
    @Override
    public TemplateModel process(TemplateModel templateModel, int ppIndex) throws PostProcessorException {
        for (int i = 0; i < templateModel.getUnits().size(); i ++) {
            TemplateUnit tu = templateModel.getUnits().get(i);
            if (tu instanceof PlainText) {
                PlainText pt = (PlainText)tu;
                
                // create a replacement PlainText unit with reduced whitespace
                PlainText replacementPt = new PlainText(pt.getSourceRef(), reduceWhitespace(pt.getText()));
                
                // replace the unit
                templateModel.getUnits().add(i, replacementPt);
                templateModel.getUnits().remove(i+1);
            }
        }
        return templateModel;
    }

    /**
     * Replace a given string with a whitespace-reduced variant of itself.
     * 
     * @param text original string with whitespaces.
     * @return the string with multiple occurences of whitespaces reduced to single occurences.
     */
    private String reduceWhitespace(String text) {
        if ( text == null ) {
            return null;
        }
        return text.replaceAll(LINE_END, "\n").replaceAll(LINE_START, "\n").replaceAll(IN_LINE, " ");
    }

}
