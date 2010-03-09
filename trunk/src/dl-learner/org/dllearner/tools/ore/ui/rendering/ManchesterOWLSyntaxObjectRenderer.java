package org.dllearner.tools.ore.ui.rendering;

import static org.coode.manchesterowlsyntax.ManchesterOWLSyntax.AND;
import static org.coode.manchesterowlsyntax.ManchesterOWLSyntax.ANTI_SYMMETRIC;
import static org.coode.manchesterowlsyntax.ManchesterOWLSyntax.DIFFERENT_FROM;
import static org.coode.manchesterowlsyntax.ManchesterOWLSyntax.DISJOINT_UNION_OF;
import static org.coode.manchesterowlsyntax.ManchesterOWLSyntax.DISJOINT_WITH;
import static org.coode.manchesterowlsyntax.ManchesterOWLSyntax.DOMAIN;
import static org.coode.manchesterowlsyntax.ManchesterOWLSyntax.EQUIVALENT_TO;
import static org.coode.manchesterowlsyntax.ManchesterOWLSyntax.EXACTLY;
import static org.coode.manchesterowlsyntax.ManchesterOWLSyntax.FACET_RESTRICTION_SEPARATOR;
import static org.coode.manchesterowlsyntax.ManchesterOWLSyntax.FUNCTIONAL;
import static org.coode.manchesterowlsyntax.ManchesterOWLSyntax.INVERSE;
import static org.coode.manchesterowlsyntax.ManchesterOWLSyntax.INVERSE_FUNCTIONAL;
import static org.coode.manchesterowlsyntax.ManchesterOWLSyntax.IRREFLEXIVE;
import static org.coode.manchesterowlsyntax.ManchesterOWLSyntax.MAX;
import static org.coode.manchesterowlsyntax.ManchesterOWLSyntax.MIN;
import static org.coode.manchesterowlsyntax.ManchesterOWLSyntax.NOT;
import static org.coode.manchesterowlsyntax.ManchesterOWLSyntax.ONE_OF_DELIMETER;
import static org.coode.manchesterowlsyntax.ManchesterOWLSyntax.ONLY;
import static org.coode.manchesterowlsyntax.ManchesterOWLSyntax.OR;
import static org.coode.manchesterowlsyntax.ManchesterOWLSyntax.RANGE;
import static org.coode.manchesterowlsyntax.ManchesterOWLSyntax.REFLEXIVE;
import static org.coode.manchesterowlsyntax.ManchesterOWLSyntax.SAME_AS;
import static org.coode.manchesterowlsyntax.ManchesterOWLSyntax.SELF;
import static org.coode.manchesterowlsyntax.ManchesterOWLSyntax.SOME;
import static org.coode.manchesterowlsyntax.ManchesterOWLSyntax.SUBCLASS_OF;
import static org.coode.manchesterowlsyntax.ManchesterOWLSyntax.SUB_PROPERTY_OF;
import static org.coode.manchesterowlsyntax.ManchesterOWLSyntax.SYMMETRIC;
import static org.coode.manchesterowlsyntax.ManchesterOWLSyntax.THAT;
import static org.coode.manchesterowlsyntax.ManchesterOWLSyntax.TRANSITIVE;
import static org.coode.manchesterowlsyntax.ManchesterOWLSyntax.TYPES;
import static org.coode.manchesterowlsyntax.ManchesterOWLSyntax.VALUE;

import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.coode.manchesterowlsyntax.ManchesterOWLSyntax;
import org.semanticweb.owl.model.OWLAnonymousDescription;
import org.semanticweb.owl.model.OWLAntiSymmetricObjectPropertyAxiom;
import org.semanticweb.owl.model.OWLAxiomAnnotationAxiom;
import org.semanticweb.owl.model.OWLCardinalityRestriction;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLClassAssertionAxiom;
import org.semanticweb.owl.model.OWLConstantAnnotation;
import org.semanticweb.owl.model.OWLDataAllRestriction;
import org.semanticweb.owl.model.OWLDataComplementOf;
import org.semanticweb.owl.model.OWLDataExactCardinalityRestriction;
import org.semanticweb.owl.model.OWLDataMaxCardinalityRestriction;
import org.semanticweb.owl.model.OWLDataMinCardinalityRestriction;
import org.semanticweb.owl.model.OWLDataOneOf;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owl.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owl.model.OWLDataRangeFacetRestriction;
import org.semanticweb.owl.model.OWLDataRangeRestriction;
import org.semanticweb.owl.model.OWLDataSomeRestriction;
import org.semanticweb.owl.model.OWLDataSubPropertyAxiom;
import org.semanticweb.owl.model.OWLDataType;
import org.semanticweb.owl.model.OWLDataValueRestriction;
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
import org.semanticweb.owl.model.OWLObject;
import org.semanticweb.owl.model.OWLObjectAllRestriction;
import org.semanticweb.owl.model.OWLObjectAnnotation;
import org.semanticweb.owl.model.OWLObjectComplementOf;
import org.semanticweb.owl.model.OWLObjectExactCardinalityRestriction;
import org.semanticweb.owl.model.OWLObjectIntersectionOf;
import org.semanticweb.owl.model.OWLObjectMaxCardinalityRestriction;
import org.semanticweb.owl.model.OWLObjectMinCardinalityRestriction;
import org.semanticweb.owl.model.OWLObjectOneOf;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLObjectPropertyChainSubPropertyAxiom;
import org.semanticweb.owl.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owl.model.OWLObjectPropertyExpression;
import org.semanticweb.owl.model.OWLObjectPropertyInverse;
import org.semanticweb.owl.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owl.model.OWLObjectSelfRestriction;
import org.semanticweb.owl.model.OWLObjectSomeRestriction;
import org.semanticweb.owl.model.OWLObjectSubPropertyAxiom;
import org.semanticweb.owl.model.OWLObjectUnionOf;
import org.semanticweb.owl.model.OWLObjectValueRestriction;
import org.semanticweb.owl.model.OWLObjectVisitor;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyAnnotationAxiom;
import org.semanticweb.owl.model.OWLPropertyExpression;
import org.semanticweb.owl.model.OWLQuantifiedRestriction;
import org.semanticweb.owl.model.OWLReflexiveObjectPropertyAxiom;
import org.semanticweb.owl.model.OWLRestriction;
import org.semanticweb.owl.model.OWLSameIndividualsAxiom;
import org.semanticweb.owl.model.OWLSubClassAxiom;
import org.semanticweb.owl.model.OWLSymmetricObjectPropertyAxiom;
import org.semanticweb.owl.model.OWLTransitiveObjectPropertyAxiom;
import org.semanticweb.owl.model.OWLTypedConstant;
import org.semanticweb.owl.model.OWLUntypedConstant;
import org.semanticweb.owl.model.OWLValueRestriction;
import org.semanticweb.owl.model.SWRLAtom;
import org.semanticweb.owl.model.SWRLAtomConstantObject;
import org.semanticweb.owl.model.SWRLAtomDObject;
import org.semanticweb.owl.model.SWRLAtomDVariable;
import org.semanticweb.owl.model.SWRLAtomIVariable;
import org.semanticweb.owl.model.SWRLAtomIndividualObject;
import org.semanticweb.owl.model.SWRLBuiltInAtom;
import org.semanticweb.owl.model.SWRLClassAtom;
import org.semanticweb.owl.model.SWRLDataRangeAtom;
import org.semanticweb.owl.model.SWRLDataValuedPropertyAtom;
import org.semanticweb.owl.model.SWRLDifferentFromAtom;
import org.semanticweb.owl.model.SWRLObjectPropertyAtom;
import org.semanticweb.owl.model.SWRLRule;
import org.semanticweb.owl.model.SWRLSameAsAtom;
import org.semanticweb.owl.vocab.XSDVocabulary;

import uk.ac.manchester.cs.owl.mansyntaxrenderer.AbstractRenderer;



/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 25-Apr-2007<br><br>
 */
public class ManchesterOWLSyntaxObjectRenderer extends AbstractRenderer implements OWLObjectVisitor {

    public static final int LINE_LENGTH = 70;

    private boolean wrap = true;

    private DescriptionComparator descriptionComparator;


    public ManchesterOWLSyntaxObjectRenderer(Writer writer) {
        super(writer);
        descriptionComparator = new DescriptionComparator();
    }


    public void setWrap(boolean wrap) {
        this.wrap = wrap;
    }

    protected List<? extends OWLObject> sort(Collection<? extends OWLObject> objects) {
        List<? extends OWLObject> sortedDescriptions = new ArrayList<OWLObject>(objects);
        Collections.sort(sortedDescriptions, descriptionComparator);
        return sortedDescriptions;
    }


    protected void write(Set<? extends OWLObject> objects, ManchesterOWLSyntax delimeter, boolean newline) {
        int tab = getIndent();
        pushTab(tab);
        for (Iterator<? extends OWLObject> it = sort(objects).iterator(); it.hasNext();) {
            it.next().accept(this);
            if (it.hasNext()) {
                if (newline && wrap) {
                    writeNewLine();
                }
                write(delimeter);
            }
        }
        popTab();
    }


    protected void write(Set<? extends OWLDescription> objects, boolean newline) {
        boolean lastWasNamed = false;
        boolean first = true;

        for (Iterator<? extends OWLObject> it = sort(objects).iterator(); it.hasNext();) {
            OWLObject desc = it.next();
            if (!first) {
                if (newline) {
                    writeNewLine();
                }
                if (lastWasNamed && desc instanceof OWLRestriction) {
                    write(" ", THAT, " ");
                }
                else {
                    write(" ", AND, " ");
                }
            }

            first = false;
            desc.accept(this);

            lastWasNamed = desc instanceof OWLClass;
        }
    }


    private void writeRestriction(OWLQuantifiedRestriction restriction, ManchesterOWLSyntax keyword) {
        restriction.getProperty().accept(this);
        write(keyword);
        if (restriction.getFiller() instanceof OWLAnonymousDescription) {
            write("(");
        }
        restriction.getFiller().accept(this);
        if (restriction.getFiller() instanceof OWLAnonymousDescription) {
            write(")");
        }
    }


    private void writeRestriction(OWLValueRestriction restriction) {
        restriction.getProperty().accept(this);
        write(VALUE);
        restriction.getValue().accept(this);
    }


    private void writeRestriction(OWLCardinalityRestriction restriction, ManchesterOWLSyntax keyword) {
        restriction.getProperty().accept(this);
        write(keyword);
        write(Integer.toString(restriction.getCardinality()));
//        if(restriction.isQualified()) {
        writeSpace();
        restriction.getFiller().accept(this);
//        }

    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Class descriptions
    //
    ///////////////////////////////////////////////////////////////////////////////////////////////


    public void visit(OWLClass desc) {
        write(getShortFormProvider().getShortForm(desc));
    }


    public void visit(OWLObjectIntersectionOf desc) {
        write(desc.getOperands(), false);
    }


    public void visit(OWLObjectUnionOf desc) {
        write(desc.getOperands(), OR, false);
    }


    public void visit(OWLObjectComplementOf desc) {
        write("", NOT, desc.isAnonymous() ? " " : "");
        if (desc.isAnonymous()) {
            write("(");
        }
        desc.getOperand().accept(this);
        if (desc.isAnonymous()) {
            write(")");
        }
    }


    public void visit(OWLObjectSomeRestriction desc) {
        writeRestriction(desc, SOME);
    }


    public void visit(OWLObjectAllRestriction desc) {
        writeRestriction(desc, ONLY);
    }


    public void visit(OWLObjectValueRestriction desc) {
        writeRestriction(desc);
    }


    public void visit(OWLObjectMinCardinalityRestriction desc) {
        writeRestriction(desc, MIN);
    }


    public void visit(OWLObjectExactCardinalityRestriction desc) {
        writeRestriction(desc, EXACTLY);
    }


    public void visit(OWLObjectMaxCardinalityRestriction desc) {
        writeRestriction(desc, MAX);
    }


    public void visit(OWLObjectSelfRestriction desc) {
        desc.getProperty().accept(this);
        write(SOME);
        write(SELF);
    }


    public void visit(OWLObjectOneOf desc) {
        write("{");
        write(desc.getIndividuals(), ONE_OF_DELIMETER, false);
        write("}");
    }


    public void visit(OWLDataSomeRestriction desc) {
        writeRestriction(desc, SOME);
    }


    public void visit(OWLDataAllRestriction desc) {
        writeRestriction(desc, ONLY);
    }


    public void visit(OWLDataValueRestriction desc) {
        writeRestriction(desc);
    }


    public void visit(OWLDataMinCardinalityRestriction desc) {
        writeRestriction(desc, MIN);
    }


    public void visit(OWLDataExactCardinalityRestriction desc) {
        writeRestriction(desc, EXACTLY);
    }


    public void visit(OWLDataMaxCardinalityRestriction desc) {
        writeRestriction(desc, MAX);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Entities stuff
    //
    ///////////////////////////////////////////////////////////////////////////////////////////////


    public void visit(OWLObjectProperty property) {
        write(getShortFormProvider().getShortForm(property));
    }


    public void visit(OWLDataProperty property) {
        write(getShortFormProvider().getShortForm(property));
    }


    public void visit(OWLIndividual individual) {
        write(getShortFormProvider().getShortForm(individual));
    }


    public void visit(OWLDataType dataType) {
        write(dataType.getURI().getFragment());
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Data stuff
    //
    ///////////////////////////////////////////////////////////////////////////////////////////////


    public void visit(OWLDataComplementOf node) {
        write(NOT);
        node.getDataRange().accept(this);
    }


    public void visit(OWLDataOneOf node) {
        write("{");
        write(node.getValues(), ONE_OF_DELIMETER, false);
        write("}");
    }


    public void visit(OWLDataRangeRestriction node) {
        node.getDataRange().accept(this);
        write("[");
        write(node.getFacetRestrictions(), FACET_RESTRICTION_SEPARATOR, false);
        write("]");
    }


    public void visit(OWLTypedConstant node) {
        if (node.getDataType().getURI().equals(XSDVocabulary.DOUBLE.getURI())) {
            write(node.getLiteral());
        }
        else if (node.getDataType().getURI().equals(XSDVocabulary.STRING.getURI())) {
            write("\"");
            write(node.getLiteral());
            write("\"");
        }
        else if (node.getDataType().getURI().equals(XSDVocabulary.FLOAT.getURI())) {
            write(node.getLiteral());
            write("f");
        }
        else if (node.getDataType().getURI().equals(XSDVocabulary.INT.getURI())) {
            write(node.getLiteral());
        }
        else if (node.getDataType().getURI().equals(XSDVocabulary.INTEGER.getURI())) {
            write(node.getLiteral());
        }
        else {
            write("\"");
            pushTab(getIndent());
            write(node.getLiteral(), wrap ? LINE_LENGTH : Integer.MAX_VALUE);
            popTab();
            write("\"^^");
            write(node.getDataType().getURI());
        }
    }


    public void visit(OWLUntypedConstant node) {
        write("\"");
        pushTab(getIndent());
        write(node.getLiteral(), wrap ? LINE_LENGTH : Integer.MAX_VALUE);
        popTab();
        write("\"");
        if (node.hasLang()) {
            write("@");
            write(node.getLang());
        }
    }


    public void visit(OWLDataRangeFacetRestriction node) {
        write(node.getFacet().getSymbolicForm());
        writeSpace();
        node.getFacetValue().accept(this);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Property expression stuff
    //
    ///////////////////////////////////////////////////////////////////////////////////////////////


    public void visit(OWLObjectPropertyInverse property) {
        write(INVERSE);
        write("(");
        property.getInverse().accept(this);
        write(")");
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Annotation stuff
    //
    ///////////////////////////////////////////////////////////////////////////////////////////////


    public void visit(OWLObjectAnnotation annotation) {
        write(annotation.getAnnotationURI());
        writeSpace();
        annotation.getAnnotationValue().accept(this);
    }


    public void visit(OWLConstantAnnotation annotation) {
        write(annotation.getAnnotationURI());
        writeSpace();
        annotation.getAnnotationValue().accept(this);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Stand alone axiom representation
    //
    ///////////////////////////////////////////////////////////////////////////////////////////////


    public void visit(OWLSubClassAxiom axiom) {
        axiom.getSubClass().accept(this);
        write(SUBCLASS_OF);
        axiom.getSuperClass().accept(this);
    }


    public void visit(OWLNegativeObjectPropertyAssertionAxiom axiom) {
        write(NOT);
        write("(");
        axiom.getSubject().accept(this);
        write(" ");
        axiom.getProperty().accept(this);
        write(" ");
        axiom.getObject().accept(this);
        write(")");
    }


    private void writePropertyCharacteristic(ManchesterOWLSyntax characteristic, OWLPropertyExpression prop) {
        write(characteristic);
        write("(");
        prop.accept(this);
        write(")");
    }


    public void visit(OWLAntiSymmetricObjectPropertyAxiom axiom) {
        writePropertyCharacteristic(ANTI_SYMMETRIC, axiom.getProperty());
    }


    public void visit(OWLReflexiveObjectPropertyAxiom axiom) {
        writePropertyCharacteristic(REFLEXIVE, axiom.getProperty());
    }


    public void visit(OWLDisjointClassesAxiom axiom) {
        write(axiom.getDescriptions(), DISJOINT_WITH, wrap);
    }


    public void visit(OWLDataPropertyDomainAxiom axiom) {
        axiom.getProperty().accept(this);
        write(DOMAIN);
        axiom.getDomain().accept(this);
    }


    public void visit(OWLImportsDeclaration axiom) {
    }


    public void visit(OWLAxiomAnnotationAxiom axiom) {
    }


    public void visit(OWLObjectPropertyDomainAxiom axiom) {
        axiom.getProperty().accept(this);
        write(DOMAIN);
        axiom.getDomain().accept(this);
    }


    public void visit(OWLEquivalentObjectPropertiesAxiom axiom) {
        write(axiom.getProperties(), EQUIVALENT_TO, wrap);
    }


    public void visit(OWLNegativeDataPropertyAssertionAxiom axiom) {
        write(NOT);
        write("(");
        axiom.getSubject().accept(this);
        write(" ");
        axiom.getProperty().accept(this);
        write(" ");
        axiom.getObject().accept(this);
        write(")");
    }


    public void visit(OWLDifferentIndividualsAxiom axiom) {
        write(axiom.getIndividuals(), DIFFERENT_FROM, wrap);
    }


    public void visit(OWLDisjointDataPropertiesAxiom axiom) {
        write(axiom.getProperties(), DISJOINT_WITH, wrap);
    }


    public void visit(OWLDisjointObjectPropertiesAxiom axiom) {
        write(axiom.getProperties(), DISJOINT_WITH, wrap);
    }


    public void visit(OWLObjectPropertyRangeAxiom axiom) {
        axiom.getProperty().accept(this);
        write(RANGE);
        axiom.getRange().accept(this);
    }


    public void visit(OWLObjectPropertyAssertionAxiom axiom) {
        axiom.getSubject().accept(this);
        write(" ");
        axiom.getProperty().accept(this);
        write(" ");
        axiom.getObject().accept(this);
    }


    public void visit(OWLFunctionalObjectPropertyAxiom axiom) {
        writePropertyCharacteristic(FUNCTIONAL, axiom.getProperty());
    }


    public void visit(OWLObjectSubPropertyAxiom axiom) {
        axiom.getSubProperty().accept(this);
        write(SUB_PROPERTY_OF);
        axiom.getSuperProperty().accept(this);
    }


    public void visit(OWLDisjointUnionAxiom axiom) {
        axiom.getOWLClass().accept(this);
        write(DISJOINT_UNION_OF);
        for(Iterator<OWLDescription> it = axiom.getDescriptions().iterator(); it.hasNext(); ) {
            it.next().accept(this);
            if(it.hasNext()) {
                write(", ");
            }
        }
    }


    public void visit(OWLDeclarationAxiom axiom) {
        
    }


    public void visit(OWLEntityAnnotationAxiom axiom) {
    }


    public void visit(OWLOntologyAnnotationAxiom axiom) {
    }


    public void visit(OWLSymmetricObjectPropertyAxiom axiom) {
        writePropertyCharacteristic(SYMMETRIC, axiom.getProperty());
    }


    public void visit(OWLDataPropertyRangeAxiom axiom) {
        axiom.getProperty().accept(this);
        write(RANGE);
        axiom.getRange().accept(this);
    }


    public void visit(OWLFunctionalDataPropertyAxiom axiom) {
        writePropertyCharacteristic(FUNCTIONAL, axiom.getProperty());
    }


    public void visit(OWLEquivalentDataPropertiesAxiom axiom) {
        write(axiom.getProperties(), EQUIVALENT_TO, wrap);
    }


    public void visit(OWLClassAssertionAxiom axiom) {
        axiom.getIndividual().accept(this);
        write(TYPES);
        axiom.getDescription().accept(this);
    }


    public void visit(OWLEquivalentClassesAxiom axiom) {
        write(axiom.getDescriptions(), EQUIVALENT_TO, wrap);
    }


    public void visit(OWLDataPropertyAssertionAxiom axiom) {
        axiom.getSubject().accept(this);
        write(" ");
        axiom.getProperty().accept(this);
        write(" ");
        axiom.getObject().accept(this);
    }


    public void visit(OWLTransitiveObjectPropertyAxiom axiom) {
        writePropertyCharacteristic(TRANSITIVE, axiom.getProperty());
    }


    public void visit(OWLIrreflexiveObjectPropertyAxiom axiom) {
        writePropertyCharacteristic(IRREFLEXIVE, axiom.getProperty());
    }


    public void visit(OWLDataSubPropertyAxiom axiom) {
        axiom.getSubProperty().accept(this);
        write(SUB_PROPERTY_OF);
        axiom.getSuperProperty().accept(this);
    }


    public void visit(OWLInverseFunctionalObjectPropertyAxiom axiom) {
        writePropertyCharacteristic(INVERSE_FUNCTIONAL, axiom.getProperty());
    }


    public void visit(OWLSameIndividualsAxiom axiom) {
        write(axiom.getIndividuals(), SAME_AS, wrap);
    }


    public void visit(OWLObjectPropertyChainSubPropertyAxiom axiom) {
        for (Iterator<OWLObjectPropertyExpression> it = axiom.getPropertyChain().iterator(); it.hasNext();) {
            it.next().accept(this);
            if (it.hasNext()) {
                write(" o ");
            }
        }
        write(SUB_PROPERTY_OF);
        axiom.getSuperProperty().accept(this);
    }


    public void visit(OWLInverseObjectPropertiesAxiom axiom) {
        axiom.getFirstProperty().accept(this);
        write(INVERSE);
        axiom.getSecondProperty().accept(this);
    }


    public void visit(SWRLRule rule) {
        for (Iterator<SWRLAtom> it = rule.getBody().iterator(); it.hasNext();) {
            it.next().accept(this);
            if (it.hasNext()) {
                write(", ");
            }
        }
        write(" -> ");
        for (Iterator<SWRLAtom> it = rule.getHead().iterator(); it.hasNext();) {
            it.next().accept(this);
            if (it.hasNext()) {
                write(", ");
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //
    // SWRL
    //
    ///////////////////////////////////////////////////////////////////////////////////////////////


    public void visit(SWRLClassAtom node) {
        node.getPredicate().accept(this);
        write("(");
        node.getArgument().accept(this);
        write(")");
    }


    public void visit(SWRLDataRangeAtom node) {
        node.getPredicate().accept(this);
        write("(");
        node.getArgument().accept(this);
        write(")");
    }


    public void visit(SWRLObjectPropertyAtom node) {
        node.getPredicate().accept(this);
        write("(");
        node.getFirstArgument().accept(this);
        write(", ");
        node.getSecondArgument().accept(this);
        write(")");
    }


    public void visit(SWRLDataValuedPropertyAtom node) {
        node.getPredicate().accept(this);
        write("(");
        node.getFirstArgument().accept(this);
        write(", ");
        node.getSecondArgument().accept(this);
        write(")");
    }


    public void visit(SWRLBuiltInAtom node) {
        write(node.getPredicate().getShortName());
        write("(");
        for(Iterator<SWRLAtomDObject> it = node.getArguments().iterator(); it.hasNext(); ) {
            it.next().accept(this);
            if(it.hasNext()) {
                write(", ");
            }
        }
        write(")");
    }


    public void visit(SWRLAtomDVariable node) {
        write("?");
        write(node.getURI().getFragment());
    }


    public void visit(SWRLAtomIVariable node) {
        write("?");
        write(node.getURI().getFragment());
    }


    public void visit(SWRLAtomIndividualObject node) {
        node.getIndividual().accept(this);
    }


    public void visit(SWRLAtomConstantObject node) {
        node.getConstant().accept(this);
    }


    public void visit(SWRLSameAsAtom node) {
        write(SAME_AS);
        write("(");
        node.getFirstArgument().accept(this);
        write(", ");
        node.getSecondArgument().accept(this);
        write(")");
    }


    public void visit(SWRLDifferentFromAtom node) {
        write(DIFFERENT_FROM);
        write("(");
        node.getFirstArgument().accept(this);
        write(", ");
        node.getSecondArgument().accept(this);
        write(")");
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Ontology
    //
    ///////////////////////////////////////////////////////////////////////////////////////////////


    public void visit(OWLOntology ontology) {

    }
}
