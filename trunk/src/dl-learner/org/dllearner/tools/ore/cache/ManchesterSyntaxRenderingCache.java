package org.dllearner.tools.ore.cache;

import java.util.Map;

import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.tools.ore.OREManager;
import org.dllearner.tools.ore.OREManagerListener;
import org.dllearner.tools.ore.ui.rendering.ManchesterOWLSyntaxOWLObjectRendererImpl;
import org.dllearner.utilities.owl.OWLAPIConverter;
import org.dllearner.utilities.owl.OWLAPIDescriptionConvertVisitor;
import org.semanticweb.owl.io.OWLObjectRenderer;
import org.semanticweb.owl.model.OWLObject;
import org.semanticweb.owl.util.SimpleShortFormProvider;

public class ManchesterSyntaxRenderingCache {

	private OWLObjectRenderer renderer;

	Map<OWLObject, String> owlObjectCache = new LRUMap<OWLObject, String>(50, 1, 50);
	Map<Description, String> descriptionCache = new LRUMap<Description, String>(50, 1, 50);
	Map<Individual, String> individualCache = new LRUMap<Individual, String>(50, 1, 50);

	private OREManager mngr;

	private OREManagerListener l = new OREManagerListener() {
		@Override
		public void activeOntologyChanged() {
			clear();
		}
	};

	// private OWLOntologyChangeListener l2 = new OWLOntologyChangeListener(){
	// public void ontologiesChanged(List<? extends OWLOntologyChange>
	// owlOntologyChanges) throws OWLException {
	// clear();
	// }
	// };

	public ManchesterSyntaxRenderingCache(OREManager oreManager) {
		this.mngr = oreManager;
		oreManager.addListener(l);
		renderer = new ManchesterOWLSyntaxOWLObjectRendererImpl();
		renderer.setShortFormProvider(new SimpleShortFormProvider());
	}

	public void clear() {
		owlObjectCache.clear();
		descriptionCache.clear();
	}

	public String getRendering(OWLObject object) {
		String s = null;
		s = owlObjectCache.get(object);
		if (s == null) {
			s = renderer.render(object);
			owlObjectCache.put(object, s);
		}
		return s;
	}

	public String getRendering(Description description) {
		String s = null;
		s = descriptionCache.get(description);
		if (s == null) {
			s = renderer.render(OWLAPIDescriptionConvertVisitor.getOWLDescription(description));
			descriptionCache.put(description, s);
		}
		return s;
	}
	
	public String getRendering(Individual individual) {
		String s = null;
		s = individualCache.get(individual);
		if (s == null) {
			s = renderer.render(OWLAPIConverter.getOWLAPIIndividual(individual));
			individualCache.put(individual, s);
		}
		return s;
	}

	public void dispose() {
		clear();
		mngr.removeListener(l);
	}

}
