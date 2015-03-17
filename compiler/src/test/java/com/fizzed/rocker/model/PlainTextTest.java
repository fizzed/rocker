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

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author joelauer
 */
public class PlainTextTest {
    
    @Test
    public void unescape() throws Exception {
        Assert.assertEquals("@", PlainText.unescape("@@"));
        Assert.assertEquals("}", PlainText.unescape("@}"));
        Assert.assertEquals("{", PlainText.unescape("@{"));
        
        Assert.assertEquals("@}", PlainText.unescape("@@@}"));
        Assert.assertEquals("h@}t", PlainText.unescape("h@@@}t"));
        Assert.assertEquals("@{", PlainText.unescape("@@@{"));
        Assert.assertEquals("h@{t", PlainText.unescape("h@@@{t"));
        
        // correct \r\n -> \n
        Assert.assertEquals("h\nt", PlainText.unescape("h\r\nt"));
        Assert.assertEquals("h\nt", PlainText.unescape("h\nt"));
        
        // everything together
        Assert.assertEquals("@\n@\n}{\r", PlainText.unescape("@@\r\n@\n@}@{\r"));
    }
    
    @Test
    public void chompLeadingWhitespaceToEndOfLine() {
        
        PlainText plain;
        
        plain = new PlainText(null, "\nt");
        
        plain.chompLeadingWhitespaceToEndOfLine();
        Assert.assertEquals("t", plain.getText());
        
        plain.chompLeadingWhitespaceToEndOfLine();
        Assert.assertEquals("t", plain.getText());
        
        
        plain = new PlainText(null, "\nt\n");
        
        plain.chompLeadingWhitespaceToEndOfLine();
        Assert.assertEquals("t\n", plain.getText());
        
        
        plain = new PlainText(null, "\n     \n");
        
        plain.chompLeadingWhitespaceToEndOfLine();
        Assert.assertEquals("     \n", plain.getText());
        
        int length = plain.leadingWhitespaceLengthToEndOfLine();
        Assert.assertEquals(6, length);
        
        plain.chompLeadingWhitespaceToEndOfLine();
        Assert.assertEquals("", plain.getText());
        
        
        // test short circuit where no whitespace removed if any non-whitespace detected
        plain = new PlainText(null, "     t\n");
        
        plain.chompLeadingWhitespaceToEndOfLine();
        Assert.assertEquals("     t\n", plain.getText());
        
    }
    
    @Test
    public void chompTrailingWhitespaceToStartOfLine() {
        
        PlainText plain;
        
        plain = new PlainText(null, "t\n ");
        
        plain.chompTrailingWhitespaceToStartOfLine();
        Assert.assertEquals("t\n", plain.getText());
        
        plain.chompTrailingWhitespaceToStartOfLine();
        Assert.assertEquals("t\n", plain.getText());
        
        
        plain = new PlainText(null, " ");
        
        plain.chompTrailingWhitespaceToStartOfLine();
        Assert.assertEquals("", plain.getText());

        
        // short circuit - don't touch line if any non-plain text found
        plain = new PlainText(null, "t ");
        
        plain.chompTrailingWhitespaceToStartOfLine();
        Assert.assertEquals("t ", plain.getText());
        
        
        
        plain = new PlainText(null, "\n     ");
        
        plain.chompTrailingWhitespaceToStartOfLine();
        Assert.assertEquals("\n", plain.getText());
    }
    
}
