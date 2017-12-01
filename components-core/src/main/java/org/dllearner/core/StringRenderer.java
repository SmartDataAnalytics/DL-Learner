/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
 *
 * This file is part of DL-Learner.
 *
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.dllearner.core;

import org.dllearner.utilities.owl.DLSyntaxObjectRendererExt;
import org.dllearner.utilities.owl.ManchesterOWLSyntaxOWLObjectRendererImplExt;
import org.dllearner.utilities.owl.OWLXMLRenderer;
import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.util.ShortFormProvider;
import org.semanticweb.owlapi.util.SimpleRenderer;

/**
 * A renderer for OWL objects. Different syntaxes are supported, e.g., DL syntax or Manchester OWL syntax.
 *
 * @author Simon Bin
 */
public final class StringRenderer {

  	public enum Rendering {
  		OWLAPI_SYNTAX("owlapi", new SimpleRenderer()),
    	DL_SYNTAX("dlsyntax", new DLSyntaxObjectRendererExt()),
    	MANCHESTER_SYNTAX("manchester", new ManchesterOWLSyntaxOWLObjectRendererImplExt(true, false)),
    	MANCHESTER_SYNTAX_NL("manchester_nl", new ManchesterOWLSyntaxOWLObjectRendererImplExt(true, true)),
		OWL_XML_SYNTAX("owl/xml", new OWLXMLRenderer());
    	
    	String name;
    	OWLObjectRenderer renderer;
    	
    	Rendering(String name, OWLObjectRenderer render) {
    		this.name = name;
    		this.renderer = render;
    	}
    	
		public String getName() {
			return name;
		}
    	
    	public OWLObjectRenderer getRenderer() {
    		return renderer;
    	}
    }

  	static OWLObjectRenderer renderer;
  	static {
  		setRenderer(Rendering.MANCHESTER_SYNTAX.getRenderer());
  	}

	public static void setRenderer(Rendering r) {
		setRenderer(r.getRenderer());
	}
	
	@SuppressWarnings("deprecation")
	public static void setRenderer(OWLObjectRenderer r) {
		ToStringRenderer.getInstance().setRenderer(r);
		renderer = r;
	}

	public static void setRenderer(String syntax) {
		for (Rendering r : Rendering.values()) {
			if(syntax.equals(r.getName())) {
				setRenderer(r);
				return;
			}
		}
		throw new IllegalArgumentException("No such Renderer: "+syntax);
	}
	
	public static OWLObjectRenderer getRenderer() {
		return renderer;
	}

	public static void setShortFormProvider(
			ShortFormProvider shortFormProvider) {
		ToStringRenderer.getInstance().setShortFormProvider(shortFormProvider);
	}
}
