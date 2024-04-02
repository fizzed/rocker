package com.fizzed.rocker.runtime;

import com.fizzed.rocker.BindableRockerModel;
import com.fizzed.rocker.ContentType;
import com.fizzed.rocker.RenderingException;
import com.fizzed.rocker.RockerOutput;
import com.fizzed.rocker.RockerStringify;
import com.fizzed.rocker.RockerContent;
import java.io.IOException;
import com.fizzed.rocker.RockerTemplate;

/**
 * Internal state of a template.
 * 
 * Simple way to hide internal variables from template (which of course
 * are subclasses of this class).
 * 
 * This is an internal API and it may radically change over time. Using
 * this for workaround, etc. is not recommended.
 */
public class Internal {

    // shared vars (e.g. template A calls template B)
    private String charsetName;
    private ContentType contentType;
    private RockerStringify stringify;
    private RockerOutput out;

    private boolean rendered;
    // counters for where we are in original source (helps provide better runtime
    // exceptions)
    private int sourceLine;
    private int sourcePosInLine;
    private String templateName;
    private String templatePackageName;

    Internal() {
        this.sourceLine = -1;
        this.sourcePosInLine = -1;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getTemplatePackageName() {
        return templatePackageName;
    }

    public void setTemplatePackageName(String templatePackageName) {
        this.templatePackageName = templatePackageName;
    }

    public String getCharsetName() {
        return charsetName;
    }

    public void setCharsetName(String charsetName) {
        this.charsetName = charsetName;
    }

    public void aboutToExecutePosInTemplate(int line, int posInLine) {
        this.sourceLine = line;
        this.sourcePosInLine = posInLine;
    }

    public ContentType getContentType() {
        return this.contentType;
    }

    public void setContentType(ContentType contentType) {
        // set default stringify by content type of template
        this.setContentType(contentType, ContentType.stringify(contentType));
    }

    public void setContentType(ContentType contentType, RockerStringify stringify) {
        this.contentType = contentType;
        this.stringify = stringify;
    }

    public RockerStringify getStringify() {
        return stringify;
    }

    public void setStringify(RockerStringify stringify) {
        this.stringify = stringify;
    }

    public RockerOutput getOut() {
        return out;
    }

    public int getSourceLine() {
        return sourceLine;
    }

    public int getSourcePosInLine() {
        return sourcePosInLine;
    }

    public void setOut(RockerOutput out) {
        this.out = out;
    }

    protected void verifyOkToBeginRendering() {
        if (this.rendered) {
            throw new RenderingException("Template already rendered (templates are single use only!)");
        }
        this.rendered = true;
    }

    //
    // break, continue support
    //
    public void throwBreakException() throws BreakException {
        throw new BreakException();
    }

    public void throwContinueException() throws ContinueException {
        throw new ContinueException();
    }

    //
    // method for write raw expressions
    //

    public void writeValue(String s) throws IOException {
        out.w(s);
    }

    public void writeValue(byte[] bytes) throws IOException {
        out.w(bytes);
    }

    //
    // methods for rendering value expressions
    //
    public boolean renderValue(RockerContent c, boolean nullSafe) throws RenderingException, IOException {
        if (nullSafe && c == null) {
            return false;
        }

        // delegating rendering this chunk of content to itself
        c.render();

        return true;
    }

    public boolean renderValue(DefaultRockerModel model, boolean nullSafe, RockerTemplate template) throws RenderingException, IOException {
        if (template instanceof DefaultRockerTemplate) {
            DefaultRockerTemplate defaultTemplate = (DefaultRockerTemplate) template;
            model.doRender(defaultTemplate, null, null);
            return true;
        } else {
            throw new IllegalArgumentException("Template must be an instance of DefaultRockerTemplate");
        }
    }    

    public boolean renderValue(BindableRockerModel model, boolean nullSafe) throws RenderingException, IOException {
        // delegating rendering this model to itself BUT under a context
        DefaultRockerModel underlyingModel = (DefaultRockerModel) model.getModel();

        return this.renderValue(underlyingModel, nullSafe);
    }

    public boolean renderValue(Raw raw, boolean nullSafe) throws RenderingException, IOException {
        if (nullSafe && raw.getValue() == null) {
            return false;
        }

        // no stringify for raws
        out.w(raw.toString());

        return true;
    }

    public boolean renderValue(String value, boolean nullSafe) throws IOException {
        if (nullSafe && value == null) {
            return false;
        }

        String s = stringify.s(value);

        // also, null safe protects against a toString() that returns a null
        if (nullSafe && s == null) {
            return false;
        }

        out.w(s);

        return true;
    }

    public boolean renderValue(Object value, boolean nullSafe) throws IOException {
        if (nullSafe && value == null) {
            return false;
        }

        String s = stringify.s(value);

        // also, null safe protects against a toString() that returns a null
        if (nullSafe && s == null) {
            return false;
        }

        out.w(s);

        return true;
    }

    public boolean renderValue(byte v, boolean nullSafe) throws IOException {
        // ignore nullSafe since a primitive cannot be null
        out.w(stringify.s(v));

        return true;
    }

    public boolean renderValue(short v, boolean nullSafe) throws IOException {
        // ignore nullSafe since a primitive cannot be null
        out.w(stringify.s(v));

        return true;
    }

    public boolean renderValue(int v, boolean nullSafe) throws IOException {
        // ignore nullSafe since a primitive cannot be null
        out.w(stringify.s(v));

        return true;
    }

    public boolean renderValue(long v, boolean nullSafe) throws IOException {
        // ignore nullSafe since a primitive cannot be null
        out.w(stringify.s(v));

        return true;
    }

    public boolean renderValue(float v, boolean nullSafe) throws IOException {
        // ignore nullSafe since a primitive cannot be null
        out.w(stringify.s(v));

        return true;
    }

    public boolean renderValue(double v, boolean nullSafe) throws IOException {
        // ignore nullSafe since a primitive cannot be null
        out.w(stringify.s(v));

        return true;
    }

    public boolean renderValue(char v, boolean nullSafe) throws IOException {
        // ignore nullSafe since a primitive cannot be null
        out.w(stringify.s(v));

        return true;
    }

    public boolean renderValue(boolean v, boolean nullSafe) throws IOException {
        // ignore nullSafe since a primitive cannot be null
        out.w(stringify.s(v));

        return true;
    }

}
