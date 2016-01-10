package org.dllearner.utilities.owl;

import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.util.ShortFormProvider;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

/**
 * An implementation of the OWLObjectRenderer interface. (Renders standalone
 * class class expressions and axioms in the manchester syntax).
 * 
 * @author Matthew Horridge, The University Of Manchester, Bio-Health
 *         Informatics Group, Date: 25-Nov-2007
 */
public class ManchesterOWLSyntaxOWLObjectRendererImplExt implements
        OWLObjectRenderer {

    private ManchesterOWLSyntaxObjectRendererExt ren;
    private WriterDelegate writerDelegate;

    /** default constructor */
    public ManchesterOWLSyntaxOWLObjectRendererImplExt() {
        writerDelegate = new WriterDelegate();
        ren = new ManchesterOWLSyntaxObjectRendererExt(writerDelegate,
                new SimpleShortFormProvider());
    }
    
    /** default constructor */
    public ManchesterOWLSyntaxOWLObjectRendererImplExt(boolean useTabbing, boolean useWrapping) {
        writerDelegate = new WriterDelegate();
        ren = new ManchesterOWLSyntaxObjectRendererExt(writerDelegate,
                new SimpleShortFormProvider());
        ren.setUseTabbing(useTabbing);
        ren.setUseWrapping(useWrapping);
    }

    @Override
    public synchronized String render(OWLObject object) {
        writerDelegate.reset();
        object.accept(ren);
        return writerDelegate.toString();
    }

    @Override
    public synchronized void setShortFormProvider(
            ShortFormProvider shortFormProvider) {
        ren = new ManchesterOWLSyntaxObjectRendererExt(writerDelegate,
                shortFormProvider);
    }
    
    /**
     * @param useTabbing
     *        useTabbing
     */
    public void setUseTabbing(boolean useTabbing) {
    	ren.setUseTabbing(useTabbing);
    }

    /**
     * @param useWrapping
     *        useWrapping
     */
    public void setUseWrapping(boolean useWrapping) {
        ren.setUseWrapping(useWrapping);
    }

    /** @return true if use wrapping */
    public boolean isUseWrapping() {
        return ren.isUseWrapping();
    }

    /** @return true if use tabbing */
    public boolean isUseTabbing() {
        return ren.isUseWrapping();
    }

    private static class WriterDelegate extends Writer {

        private StringWriter delegate;

        /** default constructor */
        public WriterDelegate() {}

        protected void reset() {
            delegate = new StringWriter();
        }

        @Override
        public String toString() {
            return delegate.getBuffer().toString();
        }

        @Override
        public void close() throws IOException {
            delegate.close();
        }

        @Override
        public void flush() throws IOException {
            delegate.flush();
        }

        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            delegate.write(cbuf, off, len);
        }
    }
}
