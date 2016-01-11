package org.dllearner.core;

import org.dllearner.utilities.owl.DLSyntaxObjectRenderer;
import org.dllearner.utilities.owl.ManchesterOWLSyntaxOWLObjectRendererImplExt;
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
    	DL_SYNTAX("dlsyntax", new DLSyntaxObjectRenderer()),
    	MANCHESTER_SYNTAX("manchester", new ManchesterOWLSyntaxOWLObjectRendererImplExt(true, false)),
    	MANCHESTER_SYNTAX_NL("manchester_nl", new ManchesterOWLSyntaxOWLObjectRendererImplExt(true, true));
    	
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
