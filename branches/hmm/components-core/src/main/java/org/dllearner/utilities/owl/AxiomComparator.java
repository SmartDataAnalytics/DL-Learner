package org.dllearner.utilities.owl;

import java.util.Collection;
import java.util.Comparator;

import org.dllearner.algorithms.properties.DataPropertyDomainAxiomLearner;
import org.dllearner.algorithms.properties.DataPropertyRangeAxiomLearner;
import org.dllearner.algorithms.properties.ReflexiveObjectPropertyAxiomLearner;
import org.dllearner.core.owl.AsymmetricObjectPropertyAxiom;
import org.dllearner.core.owl.Axiom;
import org.dllearner.core.owl.ClassAssertionAxiom;
import org.dllearner.core.owl.DatatypeProperty;
import org.dllearner.core.owl.DatatypePropertyAssertion;
import org.dllearner.core.owl.DatatypePropertyDomainAxiom;
import org.dllearner.core.owl.DatatypePropertyRangeAxiom;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.DisjointClassesAxiom;
import org.dllearner.core.owl.DisjointDatatypePropertyAxiom;
import org.dllearner.core.owl.DisjointObjectPropertyAxiom;
import org.dllearner.core.owl.EquivalentClassesAxiom;
import org.dllearner.core.owl.EquivalentDatatypePropertiesAxiom;
import org.dllearner.core.owl.EquivalentObjectPropertiesAxiom;
import org.dllearner.core.owl.FunctionalDatatypePropertyAxiom;
import org.dllearner.core.owl.FunctionalObjectPropertyAxiom;
import org.dllearner.core.owl.InverseFunctionalObjectPropertyAxiom;
import org.dllearner.core.owl.IrreflexiveObjectPropertyAxiom;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.ObjectPropertyAssertion;
import org.dllearner.core.owl.ObjectPropertyDomainAxiom;
import org.dllearner.core.owl.ObjectPropertyRangeAxiom;
import org.dllearner.core.owl.Property;
import org.dllearner.core.owl.ReflexiveObjectPropertyAxiom;
import org.dllearner.core.owl.SubClassAxiom;
import org.dllearner.core.owl.SubDatatypePropertyAxiom;
import org.dllearner.core.owl.SubObjectPropertyAxiom;
import org.dllearner.core.owl.SymmetricObjectPropertyAxiom;
import org.dllearner.core.owl.TransitiveObjectPropertyAxiom;

public class AxiomComparator implements Comparator<Axiom>{
	
	private ConceptComparator conceptComp;
	private RoleComparator roleComp;
	
	public AxiomComparator() {
		conceptComp = new ConceptComparator();
		roleComp = new RoleComparator();
	}

	@Override
	public int compare(Axiom ax1, Axiom ax2) {
		if(ax1 instanceof SubClassAxiom){
			if(ax2 instanceof SubClassAxiom){
				Description sub1 = ((SubClassAxiom)ax1).getSubConcept();
				Description sub2 = ((SubClassAxiom)ax2).getSubConcept();
				if(conceptComp.compare(sub1, sub2) == 0){
					Description sup1 = ((SubClassAxiom)ax1).getSuperConcept();
					Description sup2 = ((SubClassAxiom)ax2).getSuperConcept();
					return conceptComp.compare(sup1, sup2);
				} else {
					return -1;
				}
				
			} else {
				return -1;
			}
		} else if(ax1 instanceof DisjointClassesAxiom){
			if(ax2 instanceof DisjointClassesAxiom){
				Collection<Description> descriptions1 = ((DisjointClassesAxiom)ax1).getDescriptions();
				Collection<Description> descriptions2 = ((DisjointClassesAxiom)ax2).getDescriptions();
				if(descriptions1.size() == descriptions2.size()){
					for(Description d1 : descriptions1){
						boolean existsEqual = false;
						for(Description d2 : descriptions2){
							if(conceptComp.compare(d1, d2) == 0){
								existsEqual = true;
								break;
							}
						}
						if(!existsEqual){
							return -1;
						}
					}
				} else {
					return -1;
				}
			} else {
				return -1;
			}
		} else if(ax1 instanceof EquivalentClassesAxiom){
			if(ax2 instanceof EquivalentClassesAxiom){
				Description eq11 = ((EquivalentClassesAxiom)ax1).getConcept1();
				Description eq21 = ((EquivalentClassesAxiom)ax2).getConcept1();
				Description eq12 = ((EquivalentClassesAxiom)ax1).getConcept2();
				Description eq22 = ((EquivalentClassesAxiom)ax2).getConcept2();
				
				if(conceptComp.compare(eq11, eq21) == 0){
					return conceptComp.compare(eq12, eq22);
				} else if(conceptComp.compare(eq11, eq22) == 0){
					return conceptComp.compare(eq12, eq21);
				} else {
					return -1;
				}
			} else {
				return -1;
			}
		} else if(ax1 instanceof SubObjectPropertyAxiom){
			if(ax2 instanceof SubObjectPropertyAxiom){
				ObjectProperty sub1 = ((SubObjectPropertyAxiom)ax1).getSubRole();
				ObjectProperty sub2 = ((SubObjectPropertyAxiom)ax2).getSubRole();
				if(roleComp.compare(sub1, sub2) == 0){
					ObjectProperty sup1 = ((SubObjectPropertyAxiom)ax1).getRole();
					ObjectProperty sup2 = ((SubObjectPropertyAxiom)ax2).getRole();
					return roleComp.compare(sup1, sup2);
				} else {
					return -1;
				}
			} else {
				return -1;
			}
		} else if(ax1 instanceof DisjointObjectPropertyAxiom){
			if(ax2 instanceof DisjointObjectPropertyAxiom){
				ObjectProperty dis11 = ((DisjointObjectPropertyAxiom)ax1).getRole();
				ObjectProperty dis12 = ((DisjointObjectPropertyAxiom)ax1).getDisjointRole();
				ObjectProperty dis21 = ((DisjointObjectPropertyAxiom)ax2).getRole();
				ObjectProperty dis22 = ((DisjointObjectPropertyAxiom)ax2).getDisjointRole();
				if(roleComp.compare(dis11, dis21) == 0){
					return roleComp.compare(dis12, dis22);
				} else if(roleComp.compare(dis11, dis22) == 0){
					return roleComp.compare(dis12, dis21);
				} else {
					return -1;
				}
			} else {
				return -1;
			}
		} else if(ax1 instanceof EquivalentObjectPropertiesAxiom){
			if(ax2 instanceof EquivalentObjectPropertiesAxiom){
				Collection<ObjectProperty> properties1 = ((EquivalentObjectPropertiesAxiom)ax1).getEquivalentProperties();
				Collection<ObjectProperty> properties2 = ((EquivalentObjectPropertiesAxiom)ax2).getEquivalentProperties();
				if(properties1.size() == properties2.size()){
					for(ObjectProperty p1 : properties1){
						boolean existsEqual = false;
						for(ObjectProperty p2 : properties2){
							if(roleComp.compare(p1, p2) == 0){
								existsEqual = true;
								break;
							}
						}
						if(!existsEqual){
							return -1;
						}
					}
				} else {
					return -1;
				}
			} else {
				return -1;
			}
		} else if(ax1 instanceof ObjectPropertyDomainAxiom){
			if(ax2 instanceof ObjectPropertyDomainAxiom){
				ObjectProperty p1 = ((ObjectPropertyDomainAxiom)ax1).getProperty();
				ObjectProperty p2 = ((ObjectPropertyDomainAxiom)ax2).getProperty();
				if(roleComp.compare(p1, p2) == 0){
					return conceptComp.compare(((ObjectPropertyDomainAxiom)ax1).getDomain(), ((ObjectPropertyDomainAxiom)ax2).getDomain());
				} else {
					return -1;
				}
			} else {
				return -1;
			}
		} else if(ax1 instanceof ObjectPropertyRangeAxiom){
			if(ax2 instanceof ObjectPropertyRangeAxiom){
				Property p1 = ((ObjectPropertyRangeAxiom)ax1).getProperty();
				Property p2 = ((ObjectPropertyRangeAxiom)ax2).getProperty();
				if(roleComp.compare(p1, p2) == 0){
					return conceptComp.compare(((ObjectPropertyRangeAxiom)ax1).getRange(), ((ObjectPropertyRangeAxiom)ax2).getRange());
				} else {
					return -1;
				}
			} else {
				return -1;
			}
		} else if(ax1 instanceof FunctionalObjectPropertyAxiom){
			if(ax2 instanceof FunctionalObjectPropertyAxiom){
				return roleComp.compare(((FunctionalObjectPropertyAxiom)ax1).getRole(), ((FunctionalObjectPropertyAxiom)ax2).getRole());
			} else {
				return -1;
			}
		} else if(ax1 instanceof InverseFunctionalObjectPropertyAxiom){
			if(ax2 instanceof InverseFunctionalObjectPropertyAxiom){
				return roleComp.compare(((InverseFunctionalObjectPropertyAxiom)ax1).getRole(), ((InverseFunctionalObjectPropertyAxiom)ax2).getRole());
			} else {
				return -1;
			}
		} else if(ax1 instanceof ReflexiveObjectPropertyAxiom){
			if(ax2 instanceof ReflexiveObjectPropertyAxiom){
				return roleComp.compare(((ReflexiveObjectPropertyAxiom)ax1).getRole(), ((ReflexiveObjectPropertyAxiom)ax2).getRole());
			} else {
				return -1;
			}
		} else if(ax1 instanceof IrreflexiveObjectPropertyAxiom){
			if(ax2 instanceof IrreflexiveObjectPropertyAxiom){
				return roleComp.compare(((IrreflexiveObjectPropertyAxiom)ax1).getRole(), ((IrreflexiveObjectPropertyAxiom)ax2).getRole());
			} else {
				return -1;
			}
		} else if(ax1 instanceof TransitiveObjectPropertyAxiom){
			if(ax2 instanceof TransitiveObjectPropertyAxiom){
				return roleComp.compare(((TransitiveObjectPropertyAxiom)ax1).getRole(), ((TransitiveObjectPropertyAxiom)ax2).getRole());
			} else {
				return -1;
			}
		} else if(ax1 instanceof SymmetricObjectPropertyAxiom){
			if(ax2 instanceof SymmetricObjectPropertyAxiom){
				return roleComp.compare(((SymmetricObjectPropertyAxiom)ax1).getRole(), ((SymmetricObjectPropertyAxiom)ax2).getRole());
			} else {
				return -1;
			}
		} else if(ax1 instanceof AsymmetricObjectPropertyAxiom){
			if(ax2 instanceof AsymmetricObjectPropertyAxiom){
				return roleComp.compare(((AsymmetricObjectPropertyAxiom)ax1).getRole(), ((AsymmetricObjectPropertyAxiom)ax2).getRole());
			} else {
				return -1;
			}
		} else if(ax1 instanceof SubDatatypePropertyAxiom){
			if(ax2 instanceof SubDatatypePropertyAxiom){
				DatatypeProperty sub1 = ((SubDatatypePropertyAxiom)ax1).getSubRole();
				DatatypeProperty sub2 = ((SubDatatypePropertyAxiom)ax2).getSubRole();
				if(roleComp.compare(sub1, sub2) == 0){
					DatatypeProperty sup1 = ((SubDatatypePropertyAxiom)ax1).getRole();
					DatatypeProperty sup2 = ((SubDatatypePropertyAxiom)ax2).getRole();
					return roleComp.compare(sup1, sup2);
				} else {
					return -1;
				}
			} else {
				return -1;
			}
		} else if(ax1 instanceof DisjointDatatypePropertyAxiom){
			if(ax2 instanceof DisjointDatatypePropertyAxiom){
				DatatypeProperty dis11 = ((DisjointDatatypePropertyAxiom)ax1).getRole();
				DatatypeProperty dis12 = ((DisjointDatatypePropertyAxiom)ax1).getDisjointRole();
				DatatypeProperty dis21 = ((DisjointDatatypePropertyAxiom)ax2).getRole();
				DatatypeProperty dis22 = ((DisjointDatatypePropertyAxiom)ax2).getDisjointRole();
				if(roleComp.compare(dis11, dis21) == 0){
					return roleComp.compare(dis12, dis22);
				} else if(roleComp.compare(dis11, dis22) == 0){
					return roleComp.compare(dis12, dis21);
				} else {
					return -1;
				}
			} else {
				return -1;
			}
		} else if(ax1 instanceof EquivalentDatatypePropertiesAxiom){
			if(ax2 instanceof EquivalentDatatypePropertiesAxiom){
				DatatypeProperty eq11 = ((EquivalentDatatypePropertiesAxiom)ax1).getRole();
				DatatypeProperty eq12 = ((EquivalentDatatypePropertiesAxiom)ax1).getEquivalentRole();
				DatatypeProperty eq21 = ((EquivalentDatatypePropertiesAxiom)ax2).getRole();
				DatatypeProperty eq22 = ((EquivalentDatatypePropertiesAxiom)ax2).getEquivalentRole();
				if(roleComp.compare(eq11, eq21) == 0){
					return roleComp.compare(eq12, eq22);
				} else if(roleComp.compare(eq11, eq22) == 0){
					return roleComp.compare(eq12, eq21);
				} else {
					return -1;
				}
			} else {
				return -1;
			}
		} else if(ax1 instanceof DatatypePropertyDomainAxiom){
			if(ax2 instanceof DatatypePropertyDomainAxiom){
				DatatypeProperty p1 = ((DatatypePropertyDomainAxiom)ax1).getProperty();
				DatatypeProperty p2 = ((DatatypePropertyDomainAxiom)ax2).getProperty();
				if(roleComp.compare(p1, p2) == 0){
					return conceptComp.compare(((DatatypePropertyDomainAxiom)ax1).getDomain(), ((DatatypePropertyDomainAxiom)ax2).getDomain());
				} else {
					return -1;
				}
			} else {
				return -1;
			}
		} else if(ax1 instanceof DatatypePropertyRangeAxiom){
			if(ax2 instanceof DatatypePropertyRangeAxiom){
				Property p1 = ((DatatypePropertyRangeAxiom)ax1).getProperty();
				Property p2 = ((DatatypePropertyRangeAxiom)ax2).getProperty();
				if(roleComp.compare(p1, p2) == 0){
					return ((DatatypePropertyRangeAxiom)ax1).getRange().toString(null, null).compareTo(
							((DatatypePropertyRangeAxiom)ax2).getRange().toString(null, null));
				} else {
					return -1;
				}
			} else {
				return -1;
			}
		} else if(ax1 instanceof FunctionalDatatypePropertyAxiom){
			if(ax2 instanceof FunctionalDatatypePropertyAxiom){
				return roleComp.compare(((FunctionalDatatypePropertyAxiom)ax1).getRole(), ((FunctionalDatatypePropertyAxiom)ax2).getRole());
			} else {
				return -1;
			}
		} else if(ax1 instanceof ClassAssertionAxiom){
			if(ax2 instanceof ClassAssertionAxiom){
				Description d1 = ((ClassAssertionAxiom)ax1).getConcept();
				Description d2 = ((ClassAssertionAxiom)ax2).getConcept();
				if(conceptComp.compare(d1, d2) == 0){
					return ((ClassAssertionAxiom)ax1).getIndividual().compareTo(((ClassAssertionAxiom)ax2).getIndividual());
				} else {
					return -1;
				}
			} else {
				return -1;
			}
		} else if(ax1 instanceof ObjectPropertyAssertion){
			if(ax2 instanceof ObjectPropertyAssertion){
				return -1;//TODO
			} else {
				return -1;
			}
		} else if(ax1 instanceof DatatypePropertyAssertion){
			if(ax2 instanceof DatatypePropertyAssertion){
				return -1;//TODO
			} else {
				return -1;
			}
		} 
		return -1;
	}

}
