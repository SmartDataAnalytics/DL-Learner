package org.dllearner.tools.ore.cache;

import java.util.Map;

import org.dllearner.tools.ore.OREManager;
import org.dllearner.tools.ore.OREManagerListener;
import org.semanticweb.owlapi.model.OWLObject;

import uk.ac.manchester.cs.owlapi.dlsyntax.DLSyntaxObjectRenderer;

public class DLSyntaxRenderingCache {
	
	Map<OWLObject, String> cache = new LRUMap<OWLObject, String>(50, 1, 50);
	DLSyntaxObjectRenderer renderer = new DLSyntaxObjectRenderer();

    private OREManagerListener l = new OREManagerListener(){
		@Override
		public void activeOntologyChanged() {
			clear();
		}
    };

//    private OWLOntologyChangeListener l2 = new OWLOntologyChangeListener(){
//        public void ontologiesChanged(List<? extends OWLOntologyChange> owlOntologyChanges) throws OWLException {
//            clear();
//        }
//    };

    private OREManager mngr;


    public DLSyntaxRenderingCache(OREManager oreManager) {
        this.mngr = oreManager;
        oreManager.addListener(l);
    }


    public void clear() {
        cache.clear();
    }


    public String getRendering(OWLObject object) {
        String s = null;
        if (s == null){
            s = cache.get(object);
            if (s == null){
                s = renderer.render(object);
                cache.put(object, s);
            }
        }
        return s;
    }


    public void dispose() {
        clear();
        mngr.removeListener(l);
    }


    
}
