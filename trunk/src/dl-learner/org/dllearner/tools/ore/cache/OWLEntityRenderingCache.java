package org.dllearner.tools.ore.cache;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.dllearner.tools.ore.OREManager;
import org.dllearner.tools.ore.OREManagerListener;
import org.dllearner.tools.ore.ui.rendering.OWLEntityRenderer;
import org.semanticweb.owl.model.OWLAxiomChange;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDataType;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLEntityVisitor;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyChange;
import org.semanticweb.owl.model.OWLOntologyChangeListener;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Medical Informatics Group<br>
 * Date: 21-Sep-2006<br><br>
 * <p/>
 * matthew.horridge@cs.man.ac.uk<br>
 * www.cs.man.ac.uk/~horridgm<br><br>
 */
public class OWLEntityRenderingCache{

    private static final Logger logger = Logger.getLogger(OWLEntityRenderingCache.class);

    private Map<String, OWLClass> owlClassMap = new HashMap<String, OWLClass>();

    private Map<String, OWLObjectProperty> owlObjectPropertyMap = new HashMap<String, OWLObjectProperty>();

    private Map<String, OWLDataProperty> owlDataPropertyMap = new HashMap<String, OWLDataProperty>();

    private Map<String, OWLIndividual> owlIndividualMap = new HashMap<String, OWLIndividual>();

    private Map<String, OWLDataType> owlDatatypeMap = new HashMap<String, OWLDataType>();

    private Map<OWLEntity, String> entityRenderingMap = new HashMap<OWLEntity, String>();

    private OREManager oreManager;

    private OWLOntologyChangeListener listener = new OWLOntologyChangeListener() {
        public void ontologiesChanged(List<? extends OWLOntologyChange> changes) {
            processChanges(changes);
        }
    };
    
    private OREManagerListener oreManagerListener = new OREManagerListener() {
		
		@Override
		public void activeOntologyChanged() {
			rebuild();
			
		}
	};


    public OWLEntityRenderingCache() {
    }


    public void setOREManager(OREManager oreManager) {
        this.oreManager = oreManager;
        oreManager.addListener(oreManagerListener);
//        owlModelManager.addOntologyChangeListener(listener);
    }


    private void processChanges(List<? extends OWLOntologyChange> changes) {
        for (OWLOntologyChange change : changes) {
            if (change instanceof OWLAxiomChange) {
                OWLAxiomChange chg = (OWLAxiomChange) change;
                for (OWLEntity ent : chg.getEntities()) {
                    updateRendering(ent);
                }
            }
        }
    }


    public void rebuild() {
        clear();
        OWLEntityRenderer entityRenderer = oreManager.getOWLEntityRenderer();

        OWLClass thing = oreManager.getOWLDataFactory().getOWLThing();
        owlClassMap.put(entityRenderer.render(thing), thing);
        entityRenderingMap.put(thing, entityRenderer.render(thing));
        OWLClass nothing = oreManager.getOWLDataFactory().getOWLNothing();
        entityRenderingMap.put(nothing, entityRenderer.render(nothing));
        owlClassMap.put(entityRenderer.render(nothing), nothing);

        for (OWLOntology ont : oreManager.getLoadedOntologies()) {
            for (OWLClass cls : ont.getClassesInSignature()) {
                addRendering(cls, owlClassMap);
            }
            for (OWLObjectProperty prop : ont.getObjectPropertiesInSignature()) {
                addRendering(prop, owlObjectPropertyMap);
            }
            for (OWLDataProperty prop : ont.getDataPropertiesInSignature()) {
                addRendering(prop, owlDataPropertyMap);
            }
            for (OWLIndividual ind : ont.getIndividualsInSignature()) {
                if (!ind.isAnonymous()){
                    addRendering(ind, owlIndividualMap);
                }
            }
        }


//        // datatypes
//        final OWLDataTypeUtils datatypeUtils = new OWLDataTypeUtils(owlModelManager.getOWLOntologyManager());
//        for (OWLDatatype dt : datatypeUtils.getKnownDatatypes(owlModelManager.getActiveOntologies())) {
//            addRendering(dt, owlDatatypeMap);
//        }
    }


    public void dispose() {
        clear();
//        owlModelManager.removeOntologyChangeListener(listener);
    }


    private void clear() {
        owlClassMap.clear();
        owlObjectPropertyMap.clear();
        owlDataPropertyMap.clear();
        owlIndividualMap.clear();
        owlDatatypeMap.clear();
        entityRenderingMap.clear();
    }


    public OWLClass getOWLClass(String rendering) {
        return owlClassMap.get(rendering);
    }


    public OWLObjectProperty getOWLObjectProperty(String rendering) {
        return owlObjectPropertyMap.get(rendering);
    }


    public OWLDataProperty getOWLDataProperty(String rendering) {
        return owlDataPropertyMap.get(rendering);
    }



    public OWLIndividual getOWLIndividual(String rendering) {
        return owlIndividualMap.get(rendering);
    }


    public OWLDataType getOWLDatatype(String rendering) {
        return owlDatatypeMap.get(rendering);
    }


    public String getRendering(OWLEntity owlEntity) {
        return entityRenderingMap.get(owlEntity);
    }


    public OWLEntity getOWLEntity(String rendering) {
        // Examine in the order of class, property, individual
        OWLEntity entity = getOWLClass(rendering);
        if (entity != null) {
            return entity;
        }
        entity = getOWLObjectProperty(rendering);
        if (entity != null) {
            return entity;
        }
        entity = getOWLDataProperty(rendering);
        if (entity != null) {
            return entity;
        }
        entity = getOWLIndividual(rendering);
        if (entity != null) {
            return entity;
        }
        entity = getOWLDatatype(rendering);
        if (entity != null) {
            return entity;
        }
        return null;
    }


    public void addRendering(OWLEntity owlEntity) {
        owlEntity.accept(new OWLEntityVisitor() {
            public void visit(OWLDataProperty entity) {
                addRendering(entity, owlDataPropertyMap);
            }

            public void visit(OWLObjectProperty entity) {
                addRendering(entity, owlObjectPropertyMap);
            }

            public void visit(OWLIndividual entity) {
                addRendering(entity, owlIndividualMap);
            }

            public void visit(OWLClass entity) {
                addRendering(entity, owlClassMap);
            }

            public void visit(OWLDataType entity) {
                addRendering(entity, owlDatatypeMap);
            }
        });
    }


    private <T extends OWLEntity> void addRendering(T entity, Map<String, T> map) {
        if (!entityRenderingMap.containsKey(entity)) {
            String rendering = oreManager.getRendering(entity);
            map.put(rendering, entity);
            entityRenderingMap.put(entity, rendering);
        }
    }


    public void removeRendering(OWLEntity owlEntity) {
        final String oldRendering = entityRenderingMap.get(owlEntity);
        entityRenderingMap.remove(owlEntity);

        owlEntity.accept(new OWLEntityVisitor() {

            public void visit(OWLClass entity) {
                owlClassMap.remove(oldRendering);
            }

            public void visit(OWLDataProperty entity) {
                owlDataPropertyMap.remove(oldRendering);
            }

            public void visit(OWLObjectProperty entity) {
                owlObjectPropertyMap.remove(oldRendering);
            }


            public void visit(OWLIndividual entity) {
                owlIndividualMap.remove(oldRendering);
            }

            public void visit(OWLDataType entity) {
                owlDatatypeMap.remove(oldRendering);
            }
        });
    }


    public void updateRendering(final OWLEntity ent) {
        boolean updateRendering = false;
        for (OWLOntology ont : oreManager.getLoadedOntologies()) {
            if (ont.containsEntityReference(ent)) {
                updateRendering = true;
                break;
            }
        }
        removeRendering(ent); // always remove the old rendering
        if (updateRendering) {
            addRendering(ent);
        }
    }


    public Set<String> getOWLClassRenderings() {
        return owlClassMap.keySet();
    }


    public Set<String> getOWLObjectPropertyRenderings() {
        return owlObjectPropertyMap.keySet();
    }


    public Set<String> getOWLDataPropertyRenderings() {
        return owlDataPropertyMap.keySet();
    }


    public Set<String> getOWLIndividualRenderings() {
        return owlIndividualMap.keySet();
    }


    public Set<String> getOWLDatatypeRenderings() {
        return owlDatatypeMap.keySet();
    }


    public Set<String> getOWLEntityRenderings() {
        Set<String> renderings = new HashSet<String>(owlClassMap.size() +
                                                     owlObjectPropertyMap.size() +
                                                     owlDataPropertyMap.size() +
                                                     owlIndividualMap.size() +
                                                     owlDatatypeMap.size());
        renderings.addAll(owlClassMap.keySet());
        renderings.addAll(owlObjectPropertyMap.keySet());
        renderings.addAll(owlDataPropertyMap.keySet());
        renderings.addAll(owlIndividualMap.keySet());
        renderings.addAll(owlDatatypeMap.keySet());
        return renderings;
    }
}
