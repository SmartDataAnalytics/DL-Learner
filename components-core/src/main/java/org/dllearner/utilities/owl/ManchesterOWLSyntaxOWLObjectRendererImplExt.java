/*
 * This file is part of the OWL API.
 *
 * The contents of this file are subject to the LGPL License, Version 3.0.
 *
 * Copyright (C) 2011, The University of Manchester
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 *
 * Alternatively, the contents of this file may be used under the terms of the Apache License, Version 2.0
 * in which case, the provisions of the Apache License Version 2.0 are applicable instead of those above.
 *
 * Copyright 2011, University of Manchester
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dllearner.utilities.owl;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import org.jetbrains.annotations.NotNull;
import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.util.ShortFormProvider;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;

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
        public void write(@NotNull char[] cbuf, int off, int len) throws IOException {
            delegate.write(cbuf, off, len);
        }
    }
}
