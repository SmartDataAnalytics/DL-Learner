package org.dllearner.tools.ore.cache;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.dllearner.tools.ore.OREManager;
import org.dllearner.tools.ore.OREManagerListener;
import org.dllearner.tools.ore.ui.rendering.OWLEntityRenderer;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLEntityVisitor;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;


public class OWLEntityRenderingCache{

    private Map<String, OWLClass> owlClassMap = new HashMap<String, OWLClass>();

    private Map<String, OWLObjectProperty> owlObjectPropertyMap = new HashMap<String, OWLObjectProperty>();

    private Map<String, OWLDataProperty> owlDataPropertyMap = new HashMap<String, OWLDataProperty>();

    private Map<String, OWLNamedIndividual> owlIndividualMap = new HashMap<String, OWLNamedIndividual>();

    private Map<String, OWLDatatype> owlDatatypeMap = new HashMap<String, OWLDatatype>();
    
    private Map<String, OWLAnnotationProperty> owlAnnotationPropertyMap = new HashMap<String, OWLAnnotationProperty>();

    private Map<OWLEntity, String> entityRenderingMap = new HashMap<OWLEntity, String>();

    private OREManager oreManager;

//    private OWLOntologyChangeListener listener = new OWLOntologyChangeListener() {
//        public void ontologiesChanged(List<? extends OWLOntologyChange> changes) {
//            processChanges(changes);
//        }
//    };
    
//    private void processChanges(List<? extends OWLOntologyChange> changes) {
//        for (OWLOntologyChange change : changes) {
//            if (change instanceof OWLAxiomChange) {
//                OWLAxiomChange chg = (OWLAxiomChange) change;
//                for (OWLEntity ent : chg.getEntities()) {
//                    updateRendering(ent);
//                }
//            }
//        }
//    }
    
    private OREManagerListener oreManagerListener = new OREManagerListener() {
		
		@Override
		public void activeOntologyChanged() {
			rebuild();
			
		}
	};


    public OWLEntityRenderingCache(OREManager oreManager) {
    	this.oreManager = oreManager;
        oreManager.addListener(oreManagerListener);
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
            for (OWLNamedIndividual ind : ont.getIndividualsInSignature()) {
                if (!ind.isAnonymous()){
                    addRendering(ind, owlIndividualMap);
                }
            }
            for (OWLDatatype dt : ont.getDatatypesInSignature()){
            	addRendering(dt, owlDatatypeMap);
            }
            
            for (OWLAnnotationProperty prop : ont.getAnnotationPropertiesInSignature()){
            	addRendering(prop, owlAnnotationPropertyMap);
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
        oreManager.removeListener(oreManagerListener);
    }


    private void clear() {
        owlClassMap.clear();
        owlObjectPropertyMap.clear();
        owlDataPropertyMap.clear();
        owlIndividualMap.clear();
        owlDatatypeMap.clear();
        owlAnnotationPropertyMap.clear();
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



    public OWLNamedIndividual getOWLIndividual(String rendering) {
        return owlIndividualMap.get(rendering);
    }


    public OWLDatatype getOWLDatatype(String rendering) {
        return owlDatatypeMap.get(rendering);
    }
    
    
    public OWLAnnotationProperty getOWLAnnotationProperty(String rendering) {
        return owlAnnotationPropertyMap.get(rendering);
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
        entity = getOWLAnnotationProperty(rendering);
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

            public void visit(OWLClass entity) {
                addRendering(entity, owlClassMap);
            }

            public void visit(OWLDatatype entity) {
                addRendering(entity, owlDatatypeMap);
            }

			@Override
			public void visit(OWLNamedIndividual entity) {
				  addRendering(entity, owlIndividualMap);
			}

			@Override
			public void visit(OWLAnnotationProperty entity) {
				addRendering(entity, owlAnnotationPropertyMap);
				
			}
        });
    }


    private <T extends OWLEntity> void addRendering(T entity, Map<String, T> map) {
        if (!entityRenderingMap.containsKey(entity)) {
            String rendering = oreManager.getManchesterSyntaxRendering(entity);
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

            public void visit(OWLDatatype entity) {
                owlDatatypeMap.remove(oldRendering);
            }

			@Override
			public void visit(OWLNamedIndividual individual) {
				owlIndividualMap.remove(oldRendering);
			}

			@Override
			public void visit(OWLAnnotationProperty property) {
				owlAnnotationPropertyMap.remove(property);
				
			}
        });
    }


    public void updateRendering(final OWLEntity ent) {
        boolean updateRendering = false;
        for (OWLOntology ont : oreManager.getLoadedOntologies()) {
            if (ont.containsEntityInSignature(ent)) {
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
    
    public Set<String> getOWLAnnotationPropertyRenderings() {
        return owlAnnotationPropertyMap.keySet();
    }


    public Set<String> getOWLEntityRenderings() {
        Set<String> renderings = new HashSet<String>(owlClassMap.size() +
                                                     owlObjectPropertyMap.size() +
                                                     owlDataPropertyMap.size() +
                                                     owlIndividualMap.size() +
                                                     owlDatatypeMap.size() +
                                                     owlAnnotationPropertyMap.size());
        renderings.addAll(owlClassMap.keySet());
        renderings.addAll(owlObjectPropertyMap.keySet());
        renderings.addAll(owlDataPropertyMap.keySet());
        renderings.addAll(owlIndividualMap.keySet());
        renderings.addAll(owlDatatypeMap.keySet());
        renderings.addAll(owlAnnotationPropertyMap.keySet());
        return renderings;
    }
}
