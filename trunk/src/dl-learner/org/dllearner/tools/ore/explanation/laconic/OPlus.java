

package org.dllearner.tools.ore.explanation.laconic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owl.model.OWLAntiSymmetricObjectPropertyAxiom;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLAxiomAnnotationAxiom;
import org.semanticweb.owl.model.OWLAxiomVisitorEx;
import org.semanticweb.owl.model.OWLClassAssertionAxiom;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owl.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owl.model.OWLDataSubPropertyAxiom;
import org.semanticweb.owl.model.OWLDeclarationAxiom;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owl.model.OWLDisjointClassesAxiom;
import org.semanticweb.owl.model.OWLDisjointDataPropertiesAxiom;
import org.semanticweb.owl.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owl.model.OWLDisjointUnionAxiom;
import org.semanticweb.owl.model.OWLEntityAnnotationAxiom;
import org.semanticweb.owl.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owl.model.OWLEquivalentDataPropertiesAxiom;
import org.semanticweb.owl.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owl.model.OWLFunctionalDataPropertyAxiom;
import org.semanticweb.owl.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owl.model.OWLImportsDeclaration;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLInverseFunctionalObjectPropertyAxiom;
import org.semanticweb.owl.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owl.model.OWLIrreflexiveObjectPropertyAxiom;
import org.semanticweb.owl.model.OWLNegativeDataPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLNegativeObjectPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLObjectIntersectionOf;
import org.semanticweb.owl.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLObjectPropertyChainSubPropertyAxiom;
import org.semanticweb.owl.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owl.model.OWLObjectPropertyExpression;
import org.semanticweb.owl.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owl.model.OWLObjectSubPropertyAxiom;
import org.semanticweb.owl.model.OWLObjectUnionOf;
import org.semanticweb.owl.model.OWLOntologyAnnotationAxiom;
import org.semanticweb.owl.model.OWLReflexiveObjectPropertyAxiom;
import org.semanticweb.owl.model.OWLSameIndividualsAxiom;
import org.semanticweb.owl.model.OWLSubClassAxiom;
import org.semanticweb.owl.model.OWLSymmetricObjectPropertyAxiom;
import org.semanticweb.owl.model.OWLTransitiveObjectPropertyAxiom;
import org.semanticweb.owl.model.SWRLRule;


/*
 * This class computes the oplus closure provided in 'Laconic and Precise Justifications in OWL' from 
 * Matthew Horridge, Bijan Parsia and Ulrike Sattler. A set of axioms is into smaller and weaker axioms.
 */
public class OPlus
    implements OWLAxiomVisitorEx<Set<OWLAxiom>>
{
	private OWLDataFactory dataFactory;
    private Beta beta;
    private Tau tau;
    private BottomTester bottomChecker;
    private TopTester topChecker;
    private Map<OWLAxiom,Set<OWLAxiom>> axiomsMap;

    public OPlus(OWLDataFactory dataFactory)
    {
        axiomsMap = new HashMap<OWLAxiom,Set<OWLAxiom>>();
        this.dataFactory = dataFactory;
        beta = new Beta(dataFactory);
        tau = new Tau(dataFactory);
        bottomChecker = new BottomTester();
        topChecker = new TopTester();
    }

    public boolean isNothing(OWLDescription desc)
    {
        return ((Boolean)desc.accept(bottomChecker)).booleanValue();
    }

    public boolean isThing(OWLDescription desc)
    {
        return ((Boolean)desc.accept(topChecker)).booleanValue();
    }

    public void reset()
    {
        axiomsMap.clear();
    }

    public Map<OWLAxiom,Set<OWLAxiom>> getAxiomsMap()
    {
        return axiomsMap;
    }

    public Set<OWLAxiom> archive(OWLAxiom source, Set<OWLAxiom> axioms)
    {
        for(OWLAxiom axiom : axioms){
        	
//        	Set<OWLAxiom> existing = (Set<OWLAxiom>)axiomsMap.get(axiom);
//            if(existing == null)
//            {
//                existing = new HashSet<OWLAxiom>();
//                axiomsMap.put(axiom, existing);
//            }
//            existing.add(source);     
            if(!axiom.equals(source))
            {
                Set<OWLAxiom> existing = (Set<OWLAxiom>)axiomsMap.get(axiom);
                if(existing == null)
                {
                    existing = new HashSet<OWLAxiom>();
                    axiomsMap.put(axiom, existing);
                }
                existing.add(source);
            }
        } 
        return axioms;
    }

    public Set<OWLAxiom> archive(OWLAxiom source)
    {
        return archive(source, Collections.singleton(source));
    }
    
    

    
	@Override
	public Set<OWLAxiom> visit(OWLSubClassAxiom axiom) {
		Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
        Set<OWLDescription> tauAxioms = new HashSet<OWLDescription>();
        if(axiom.getSuperClass() instanceof OWLObjectIntersectionOf)
        {
        	tauAxioms = new HashSet<OWLDescription>();
            for(OWLDescription desc : ((OWLObjectIntersectionOf)axiom.getSuperClass()).getOperands())
            {
                
                if(desc.isAnonymous()){
                	tauAxioms.addAll(desc.accept(tau));
                }
                else{
                	tauAxioms.add(desc);
                }
            }

            tauAxioms.add(dataFactory.getOWLThing());
        } else
        if(axiom.getSuperClass() instanceof OWLObjectUnionOf)
        {
            boolean allNamed = true;
            for(OWLDescription desc : ((OWLObjectUnionOf)axiom.getSuperClass()).getOperands()){
                           
                if(!desc.isAnonymous())
                    continue;
                allNamed = false;
                break;
            }
            if(allNamed){
            	tauAxioms.add(axiom.getSuperClass());
            } else {
            	tauAxioms = axiom.getSuperClass().accept(tau);
            }
        } else {
        	tauAxioms = axiom.getSuperClass().accept(tau);
        }
        Set<OWLDescription> betaAxioms;
        if(axiom.getSubClass() instanceof OWLObjectUnionOf)
        {
        	betaAxioms = new HashSet<OWLDescription>();
            for(OWLDescription desc : ((OWLObjectUnionOf)axiom.getSubClass()).getOperands()){
            
                if(desc.isAnonymous()) { 
                	betaAxioms.addAll(desc.accept(beta));
                } else {
                	betaAxioms.add(desc);
                }
            }

            betaAxioms.add(dataFactory.getOWLNothing());
        } else {
        	betaAxioms = axiom.getSubClass().accept(beta);
        }
        for(OWLDescription tauDesc : tauAxioms){
        
            if(!isThing(tauDesc)){
            	for(OWLDescription betaDesc : betaAxioms){
                      
                    if(!isNothing(betaDesc) && !(tauDesc instanceof OWLObjectIntersectionOf) && !(betaDesc instanceof OWLObjectUnionOf))
                    {
                        axioms.add(dataFactory.getOWLSubClassAxiom(betaDesc, tauDesc));
                    }
                }
            }
        }

        return archive(axiom, axioms);
	}

	@Override
	public Set<OWLAxiom> visit(OWLNegativeObjectPropertyAssertionAxiom axiom) {
		 return archive(axiom);
	}

	@Override
	public Set<OWLAxiom> visit(OWLAntiSymmetricObjectPropertyAxiom axiom) {
		 return archive(axiom);
	}

	@Override
	public Set<OWLAxiom> visit(OWLReflexiveObjectPropertyAxiom axiom) {
		 return archive(axiom);
	}

	@Override
	public Set<OWLAxiom> visit(OWLDisjointClassesAxiom axiom) {
		boolean containAnonDescriptions = false;
       for(OWLDescription desc : axiom.getDescriptions()){
           
            if(!desc.isAnonymous())
                continue;
            containAnonDescriptions = true;
            break;
        }
        if(!containAnonDescriptions)
            return archive(axiom);
        Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
        List<OWLDescription> descs = new ArrayList<OWLDescription>(axiom.getDescriptions());
        for(int i = 0; i < descs.size(); i++)
        {
            for(int j = i + 1; j < descs.size(); j++)
                axioms.addAll(dataFactory.getOWLSubClassAxiom(descs.get(i), dataFactory.getOWLObjectComplementOf(descs.get(j))).accept(this));

        }

        return archive(axiom, axioms);
	}

	@Override
	public Set<OWLAxiom> visit(OWLDataPropertyDomainAxiom axiom) {
		 return archive(axiom);
	}

	@Override
	public Set<OWLAxiom> visit(OWLImportsDeclaration axiom) {
		 return archive(axiom);
	}

	@Override
	public Set<OWLAxiom> visit(OWLAxiomAnnotationAxiom axiom) {
		 return archive(axiom);
	}

	@Override
	public Set<OWLAxiom> visit(OWLObjectPropertyDomainAxiom axiom) {
		Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
        for(OWLDescription desc : (Set<OWLDescription>)axiom.getDomain().accept(tau)){
		
           
            if(!isThing(desc)){
                axioms.add(dataFactory.getOWLObjectPropertyDomainAxiom((OWLObjectPropertyExpression)axiom.getProperty(), desc));
            }
        }
        return archive(axiom, axioms);
	}

	@Override
	public Set<OWLAxiom> visit(OWLEquivalentObjectPropertiesAxiom axiom) {
		Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
        for(OWLObjectPropertyExpression prop1 : axiom.getProperties()){
            for(OWLObjectPropertyExpression prop2 : axiom.getProperties()){
                if(!prop1.equals(prop2)){
                    axioms.add(dataFactory.getOWLSubObjectPropertyAxiom(prop1, prop2));
                }
            }
        }

        return archive(axiom, axioms);
	}

	@Override
	public Set<OWLAxiom> visit(OWLNegativeDataPropertyAssertionAxiom axiom) {
		 return archive(axiom);
	}

	@Override
	public Set<OWLAxiom> visit(OWLDifferentIndividualsAxiom axiom) {
		 Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
	        for(OWLIndividual ind1 : axiom.getIndividuals()){
	        	for(OWLIndividual ind2 : axiom.getIndividuals()){
	                if(!ind1.equals(ind2)){
	                    axioms.add(dataFactory.getOWLDifferentIndividualsAxiom(new OWLIndividual[] {
	                        ind1, ind2}));
	                }
	            }
	        }

	        return archive(axiom, axioms);
	}

	@Override
	public Set<OWLAxiom> visit(OWLDisjointDataPropertiesAxiom axiom) {
		 return archive(axiom);
	}

	@Override
	public Set<OWLAxiom> visit(OWLDisjointObjectPropertiesAxiom axiom) {
		 return archive(axiom);
	}

	@Override
	public Set<OWLAxiom> visit(OWLObjectPropertyRangeAxiom axiom) {
		Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
		for(OWLDescription range : axiom.getRange().accept(tau)){
           if(!isThing(range)){
                axioms.add(dataFactory.getOWLObjectPropertyRangeAxiom((OWLObjectPropertyExpression)axiom.getProperty(), range));
           }
        }
        return archive(axiom, axioms);
	}

	@Override
	public Set<OWLAxiom> visit(OWLObjectPropertyAssertionAxiom axiom) {
		 return archive(axiom);
	}

	@Override
	public Set<OWLAxiom> visit(OWLFunctionalObjectPropertyAxiom axiom) {
		 return archive(axiom);
	}

	@Override
	public Set<OWLAxiom> visit(OWLObjectSubPropertyAxiom axiom) {
		 return archive(axiom);
	}

	@Override
	public Set<OWLAxiom> visit(OWLDisjointUnionAxiom axiom) {
		 return archive(axiom);
	}

	@Override
	public Set<OWLAxiom> visit(OWLDeclarationAxiom axiom) {
		return Collections.singleton((OWLAxiom)axiom);
	}

	@Override
	public Set<OWLAxiom> visit(OWLEntityAnnotationAxiom axiom) {
		return Collections.singleton((OWLAxiom)axiom);
	}

	@Override
	public Set<OWLAxiom> visit(OWLOntologyAnnotationAxiom axiom) {
		return Collections.singleton((OWLAxiom)axiom);
	}

	@Override
	public Set<OWLAxiom> visit(OWLSymmetricObjectPropertyAxiom axiom) {
		return archive(axiom);
	}

	@Override
	public Set<OWLAxiom> visit(OWLDataPropertyRangeAxiom axiom) {
		return archive(axiom);
	}

	@Override
	public Set<OWLAxiom> visit(OWLFunctionalDataPropertyAxiom axiom) {
		return archive(axiom);
	}

	@Override
	public Set<OWLAxiom> visit(OWLEquivalentDataPropertiesAxiom axiom) {
		return archive(axiom);
	}

	@Override
	public Set<OWLAxiom> visit(OWLClassAssertionAxiom axiom) {
		Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
		for(OWLDescription desc : axiom.getDescription().accept(tau)){
            if(!isThing(desc)){
                axioms.add(dataFactory.getOWLClassAssertionAxiom(axiom.getIndividual(), desc));
            }
        } 
        return archive(axiom, axioms);
	}

	@Override
	public Set<OWLAxiom> visit(OWLEquivalentClassesAxiom axiom) {
		Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
		for(OWLDescription desc1 : axiom.getDescriptions()){
			for(OWLDescription desc2 : axiom.getDescriptions()){
				if(!desc1.equals(desc2)){
					axioms.addAll(dataFactory.getOWLSubClassAxiom(desc1, desc2).accept(this));
				}
			}
		}
       
        return archive(axiom, axioms);
	}

	@Override
	public Set<OWLAxiom> visit(OWLDataPropertyAssertionAxiom axiom) {
		return archive(axiom);
	}

	@Override
	public Set<OWLAxiom> visit(OWLTransitiveObjectPropertyAxiom axiom) {
		return archive(axiom);
	}

	@Override
	public Set<OWLAxiom> visit(OWLIrreflexiveObjectPropertyAxiom axiom) {
		return archive(axiom);
	}

	@Override
	public Set<OWLAxiom> visit(OWLDataSubPropertyAxiom axiom) {
		return archive(axiom);
	}

	@Override
	public Set<OWLAxiom> visit(OWLInverseFunctionalObjectPropertyAxiom axiom) {
		return archive(axiom);
	}

	@Override
	public Set<OWLAxiom> visit(OWLSameIndividualsAxiom axiom) {
		return archive(axiom);
	}

	@Override
	public Set<OWLAxiom> visit(OWLObjectPropertyChainSubPropertyAxiom axiom) {
		return archive(axiom);
	}

	@Override
	public Set<OWLAxiom> visit(OWLInverseObjectPropertiesAxiom axiom) {
		Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
	    axioms.add(dataFactory.getOWLSubObjectPropertyAxiom(axiom.getFirstProperty(), dataFactory.getOWLObjectPropertyInverse(axiom.getSecondProperty())));
	    axioms.add(dataFactory.getOWLSubObjectPropertyAxiom(axiom.getSecondProperty(), dataFactory.getOWLObjectPropertyInverse(axiom.getFirstProperty())));
	    return archive(axiom, axioms);
	}

	@Override
	public Set<OWLAxiom> visit(SWRLRule axiom) {
		// TODO Auto-generated method stub
		return null;
	}
}
