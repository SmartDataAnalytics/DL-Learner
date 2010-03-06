package org.dllearner.tools.ore.cache;

import java.util.LinkedHashMap;
import java.util.Map;

import org.dllearner.tools.ore.OREManager;
import org.dllearner.tools.ore.OREManagerListener;
import org.protege.editor.core.Disposable;
import org.semanticweb.owl.io.OWLObjectRenderer;
import org.semanticweb.owl.model.OWLObject;

/**
 * Author: drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p/>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Jun 27, 2008<br><br>
 */
public class OWLObjectRenderingCache implements Disposable {

    Map<OWLObject, String> cache = new MyLRUMap<OWLObject, String>(50, 1, 50);

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


    public OWLObjectRenderingCache(OREManager oreManager) {
        this.mngr = oreManager;
        oreManager.addListener(l);
    }


    public void clear() {
        cache.clear();
    }


    public String getRendering(OWLObject object, OWLObjectRenderer owlObjectRenderer) {
        String s = null;
//        if (object instanceof OWLClassExpression){
//            String userRendering = OWLExpressionUserCache.getInstance(mngr).getRendering((OWLClassExpression) object);
//            if (userRendering != null){
//                s = userRendering;
//                cache.put(object, s);
//            }
//        }
        if (s == null){
            s = cache.get(object);
            if (s == null){
                s = owlObjectRenderer.render(object);
                cache.put(object, s);
            }
        }
        return s;
    }


    public void dispose() {
        clear();
        mngr.removeListener(l);
    }


    class MyLRUMap<K,V> extends LinkedHashMap<K,V> {
        private int maxCapacity;

        public MyLRUMap(int initialCapacity, float loadFactor, int maxCapacity) {
            super(initialCapacity, loadFactor, true);
            this.maxCapacity = maxCapacity;
        }

        protected boolean removeEldestEntry(Map.Entry<K,V> eldest) {
            return size() >= this.maxCapacity;
        }
    }
}
