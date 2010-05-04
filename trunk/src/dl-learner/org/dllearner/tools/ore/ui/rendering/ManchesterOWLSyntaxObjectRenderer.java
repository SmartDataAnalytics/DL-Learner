package org.dllearner.tools.ore.ui.rendering;

import static org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax.AND;
import static org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax.ANNOTATION_PROPERTY;
import static org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax.ASYMMETRIC;
import static org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax.CHARACTERISTICS;
import static org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax.CLASS;
import static org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax.COMMA;
import static org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax.DATA_PROPERTY;
import static org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax.DIFFERENT_FROM;
import static org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax.DIFFERENT_INDIVIDUALS;
import static org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax.DISJOINT_CLASSES;
import static org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax.DISJOINT_PROPERTIES;
import static org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax.DISJOINT_UNION_OF;
import static org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax.DISJOINT_WITH;
import static org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax.DOMAIN;
import static org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax.EQUIVALENT_CLASSES;
import static org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax.EQUIVALENT_PROPERTIES;
import static org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax.EQUIVALENT_TO;
import static org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax.EXACTLY;
import static org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax.FACET_RESTRICTION_SEPARATOR;
import static org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax.FUNCTIONAL;
import static org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax.HAS_KEY;
import static org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax.INDIVIDUAL;
import static org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax.INVERSE;
import static org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax.INVERSE_FUNCTIONAL;
import static org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax.INVERSE_OF;
import static org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax.IRREFLEXIVE;
import static org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax.MAX;
import static org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax.MIN;
import static org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax.NOT;
import static org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax.OBJECT_PROPERTY;
import static org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax.ONE_OF_DELIMETER;
import static org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax.ONLY;
import static org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax.ONTOLOGY;
import static org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax.OR;
import static org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax.RANGE;
import static org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax.REFLEXIVE;
import static org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax.SAME_AS;
import static org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax.SAME_INDIVIDUAL;
import static org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax.SELF;
import static org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax.SOME;
import static org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax.SUBCLASS_OF;
import static org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax.SUB_PROPERTY_OF;
import static org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax.SYMMETRIC;
import static org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax.TRANSITIVE;
import static org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax.TYPE;
import static org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax.VALUE;

import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLAnonymousClassExpression;
import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLAsymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLCardinalityRestriction;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataAllValuesFrom;
import org.semanticweb.owlapi.model.OWLDataComplementOf;
import org.semanticweb.owlapi.model.OWLDataExactCardinality;
import org.semanticweb.owlapi.model.OWLDataHasValue;
import org.semanticweb.owlapi.model.OWLDataIntersectionOf;
import org.semanticweb.owlapi.model.OWLDataMaxCardinality;
import org.semanticweb.owlapi.model.OWLDataMinCardinality;
import org.semanticweb.owlapi.model.OWLDataOneOf;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLDataUnionOf;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDatatypeDefinitionAxiom;
import org.semanticweb.owlapi.model.OWLDatatypeRestriction;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointUnionAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLFacetRestriction;
import org.semanticweb.owlapi.model.OWLFunctionalDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLHasKeyAxiom;
import org.semanticweb.owlapi.model.OWLHasValueRestriction;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLInverseFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLIrreflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLNegativeDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLNegativeObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;
import org.semanticweb.owlapi.model.OWLObjectExactCardinality;
import org.semanticweb.owlapi.model.OWLObjectHasSelf;
import org.semanticweb.owlapi.model.OWLObjectHasValue;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectInverseOf;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;
import org.semanticweb.owlapi.model.OWLObjectMinCardinality;
import org.semanticweb.owlapi.model.OWLObjectOneOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLQuantifiedRestriction;
import org.semanticweb.owlapi.model.OWLReflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;
import org.semanticweb.owlapi.model.OWLStringLiteral;
import org.semanticweb.owlapi.model.OWLSubAnnotationPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;
import org.semanticweb.owlapi.model.OWLSymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLTransitiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLTypedLiteral;
import org.semanticweb.owlapi.model.SWRLAtom;
import org.semanticweb.owlapi.model.SWRLBuiltInAtom;
import org.semanticweb.owlapi.model.SWRLClassAtom;
import org.semanticweb.owlapi.model.SWRLDArgument;
import org.semanticweb.owlapi.model.SWRLDataPropertyAtom;
import org.semanticweb.owlapi.model.SWRLDataRangeAtom;
import org.semanticweb.owlapi.model.SWRLDifferentIndividualsAtom;
import org.semanticweb.owlapi.model.SWRLIndividualArgument;
import org.semanticweb.owlapi.model.SWRLLiteralArgument;
import org.semanticweb.owlapi.model.SWRLObjectPropertyAtom;
import org.semanticweb.owlapi.model.SWRLRule;
import org.semanticweb.owlapi.model.SWRLSameIndividualAtom;
import org.semanticweb.owlapi.model.SWRLVariable;
import org.semanticweb.owlapi.util.ShortFormProvider;

import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.AbstractRenderer;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 25-Apr-2007<br><br>
 */
public class ManchesterOWLSyntaxObjectRenderer extends AbstractRenderer implements OWLObjectVisitor {

    public static final int LINE_LENGTH = 70;


    public ManchesterOWLSyntaxObjectRenderer(Writer writer, ShortFormProvider entityShortFormProvider) {
        super(writer, entityShortFormProvider);
    }

    protected List<? extends OWLObject> sort(Collection<? extends OWLObject> objects) {
        List<? extends OWLObject> sortedObjects = new ArrayList<OWLObject>(objects);
        Collections.sort(sortedObjects);
        return sortedObjects;
    }


    protected void write(Set<? extends OWLObject> objects, ManchesterOWLSyntax delimeter, boolean newline) {
        int tab = getIndent();
        pushTab(tab);
        for (Iterator<? extends OWLObject> it = sort(objects).iterator(); it.hasNext();) {
            it.next().accept(this);
            if (it.hasNext()) {
                if (newline && isUseWrapping()) {
                    writeNewLine();
                }
                write(delimeter);
            }
        }
        popTab();
    }

    protected void writeCommaSeparatedList(Set<? extends OWLObject> objects) {
        for (Iterator<OWLObject> it = new TreeSet<OWLObject>(objects).iterator(); it.hasNext();) {
            it.next().accept(this);
            if (it.hasNext()) {
                write(", ");
            }
        }
    }

    protected void write(Set<? extends OWLClassExpression> objects, boolean newline) {
        boolean lastWasNamed = false;
        boolean first = true;

        for (Iterator<? extends OWLObject> it = sort(objects).iterator(); it.hasNext();) {
            OWLObject desc = it.next();
            if (!first) {
                if (newline && isUseWrapping()) {
                    writeNewLine();
                }
                write(" ", AND, " ");
            }

            first = false;
            if (desc instanceof OWLAnonymousClassExpression) {
                write("(");
            }
            desc.accept(this);
            if (desc instanceof OWLAnonymousClassExpression) {
                write(")");
            }

            lastWasNamed = desc instanceof OWLClass;
        }
    }


    private void writeRestriction(OWLQuantifiedRestriction restriction, ManchesterOWLSyntax keyword) {
        restriction.getProperty().accept(this);
        write(keyword);
        boolean conjunctionOrDisjunction = false;
        if (restriction.getFiller() instanceof OWLAnonymousClassExpression) {
            if (restriction.getFiller() instanceof OWLObjectIntersectionOf || restriction.getFiller() instanceof OWLObjectUnionOf) {
                conjunctionOrDisjunction = true;
                incrementTab(4);
                writeNewLine();
            }
            write("(");
        }
        restriction.getFiller().accept(this);
        if (restriction.getFiller() instanceof OWLAnonymousClassExpression) {
            write(")");
            if (conjunctionOrDisjunction) {
                popTab();
            }
        }
    }


    private void writeRestriction(OWLHasValueRestriction restriction) {
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
        if (restriction.getFiller() instanceof OWLAnonymousClassExpression) {
            write("(");
        }
        restriction.getFiller().accept(this);
        if (restriction.getFiller() instanceof OWLAnonymousClassExpression) {
            write(")");
        }
//        }

    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Class expressions
    //
    ///////////////////////////////////////////////////////////////////////////////////////////////


    public void visit(OWLClass desc) {
        write((getShortFormProvider().getShortForm(desc)));
    }


    public void visit(OWLObjectIntersectionOf desc) {
        write(desc.getOperands(), true);
    }


    public void visit(OWLObjectUnionOf desc) {
        boolean first = true;
        for (Iterator<? extends OWLClassExpression> it = desc.getOperands().iterator(); it.hasNext();) {
            OWLClassExpression op = it.next();
            if (!first) {
                if (isUseWrapping()) {
                    writeNewLine();
                }
                write(" ", OR, " ");
            }

            first = false;
            if (op.isAnonymous()) {
                write("(");
            }
            op.accept(this);
            if (op.isAnonymous()) {
                write(")");
            }

        }
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


    public void visit(OWLObjectSomeValuesFrom desc) {
        writeRestriction(desc, SOME);
    }


    public void visit(OWLObjectAllValuesFrom desc) {
        writeRestriction(desc, ONLY);
    }


    public void visit(OWLObjectHasValue desc) {
        writeRestriction(desc);
    }


    public void visit(OWLObjectMinCardinality desc) {
        writeRestriction(desc, MIN);
    }


    public void visit(OWLObjectExactCardinality desc) {
        writeRestriction(desc, EXACTLY);
    }


    public void visit(OWLObjectMaxCardinality desc) {
        writeRestriction(desc, MAX);
    }


    public void visit(OWLObjectHasSelf desc) {
        desc.getProperty().accept(this);
        write(SOME);
        write(SELF);
    }


    public void visit(OWLObjectOneOf desc) {
        write("{");
        write(desc.getIndividuals(), ONE_OF_DELIMETER, false);
        write("}");
    }


    public void visit(OWLDataSomeValuesFrom desc) {
        writeRestriction(desc, SOME);
    }


    public void visit(OWLDataAllValuesFrom desc) {
        writeRestriction(desc, ONLY);
    }


    public void visit(OWLDataHasValue desc) {
        writeRestriction(desc);
    }


    public void visit(OWLDataMinCardinality desc) {
        writeRestriction(desc, MIN);
    }


    public void visit(OWLDataExactCardinality desc) {
        writeRestriction(desc, EXACTLY);
    }


    public void visit(OWLDataMaxCardinality desc) {
        writeRestriction(desc, MAX);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Entities stuff
    //
    ///////////////////////////////////////////////////////////////////////////////////////////////


    public void visit(OWLObjectProperty property) {
        write((getShortFormProvider().getShortForm(property)));
    }


    public void visit(OWLDataProperty property) {
        write((getShortFormProvider().getShortForm(property)));
    }


    public void visit(OWLNamedIndividual individual) {
        write((getShortFormProvider().getShortForm(individual)));
    }

    public void visit(OWLAnnotationProperty property) {
        write((getShortFormProvider().getShortForm(property)));
    }

    public void visit(OWLDatatype datatype) {
        write((getShortFormProvider().getShortForm(datatype)));
    }

    public void visit(OWLAnonymousIndividual individual) {
        write(individual.toString());
    }

    public void visit(IRI iri) {
        write(iri.toQuotedString());
    }

    public void visit(OWLAnnotation node) {
        node.getProperty().accept(this);
        writeSpace();
        node.getValue().accept(this);
    }

//    private String escape(String s) {
//        for(int i = 0; i < s.length(); i++) {
//            char ch = s.charAt(i);
//            if(i == 0 && ch == '\'') {
//                return s;
//            }
//            if(" [](){},^<>?@".indexOf(ch) != -1) {
//                StringBuilder sb = new StringBuilder();
//                sb.append("'");
//                sb.append(s);
//                sb.append("'");
//                return sb.toString();
//            }
//        }
//        return s;
//    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Data stuff
    //
    ///////////////////////////////////////////////////////////////////////////////////////////////


    public void visit(OWLDataComplementOf node) {
        write(NOT);
        if(node.getDataRange().isDatatype()) {
            node.getDataRange().accept(this);
        }
        else {
            write("(");
            node.getDataRange().accept(this);
            write(")");
        }

    }


    public void visit(OWLDataOneOf node) {
        write("{");
        write(node.getValues(), ONE_OF_DELIMETER, false);
        write("}");
    }


    public void visit(OWLDataIntersectionOf node) {
        write("(");
        write(node.getOperands(), AND, false);
        write(")");
    }

    public void visit(OWLDataUnionOf node) {
        write("(");
        write(node.getOperands(), OR, false);
        write(")");
    }

    public void visit(OWLDatatypeRestriction node) {
        node.getDatatype().accept(this);
        write("[");
        write(node.getFacetRestrictions(), FACET_RESTRICTION_SEPARATOR, false);
        write("]");
    }


    public void visit(OWLTypedLiteral node) {
        if (node.getDatatype().isDouble()) {
            write(node.getLiteral());
        }
        else if (node.getDatatype().isFloat()) {
            write(node.getLiteral());
            write("f");
        }
        else if (node.getDatatype().isInteger()) {
            write(node.getLiteral());
        }
        else if (node.getDatatype().isString()) {
            writeLiteral(node.getLiteral());
        }
        else if (node.getDatatype().isBoolean()) {
            write(node.getLiteral());
        }
        else {
            pushTab(getIndent());
            writeLiteral(node.getLiteral());
            popTab();
            write("^^");
            node.getDatatype().accept(this);
        }
    }


    public void visit(OWLStringLiteral node) {
        pushTab(getIndent());
        writeLiteral(node.getLiteral());
            write("@");
        if (node.getLang() != null) {
            write(node.getLang());
        }
        popTab();
    }

    private void writeLiteral(String literal) {
        write("\"");
        if (literal.indexOf("\"") == -1 && literal.indexOf("\\") != -1) {
            write(literal);
        }
        else {

            literal = literal.replace("\\", "\\\\");
            literal = literal.replace("\"", "\\\"");
            write(literal);
        }
        write("\"");
//        if(literal.indexOf('\"') != -1) {
//            write("\"\"\"");
//            write(literal);
//            write("\"\"\"");
//        }
//        else {
//            write("\"");
//            write(literal);
//            write("\"");
//        }
    }


    public void visit(OWLFacetRestriction node) {
        write(node.getFacet().getSymbolicForm());
        writeSpace();
        node.getFacetValue().accept(this);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Property expression stuff
    //
    ///////////////////////////////////////////////////////////////////////////////////////////////


    public void visit(OWLObjectInverseOf property) {
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

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Stand alone axiom representation
    //
    // We render each axiom as a one line frame
    //
    ///////////////////////////////////////////////////////////////////////////////////////////////

    private boolean wrapSave;

    private boolean tabSave;

    private void setAxiomWriting() {
        wrapSave = isUseWrapping();
        tabSave = isUseTabbing();
        setUseWrapping(false);
        setUseTabbing(false);
    }

    private void restore() {
        setUseTabbing(tabSave);
        setUseWrapping(wrapSave);
    }

    public void visit(OWLSubClassOfAxiom axiom) {
        setAxiomWriting();
        axiom.getSubClass().accept(this);
        write(SUBCLASS_OF);
        axiom.getSuperClass().accept(this);
        restore();
    }


    public void visit(OWLNegativeObjectPropertyAssertionAxiom axiom) {
        setAxiomWriting();
        write(NOT);
        write("(");
        axiom.getSubject().accept(this);
        write(" ");
        axiom.getProperty().accept(this);
        write(" ");
        axiom.getObject().accept(this);
        write(")");
        restore();
    }


    private void writePropertyCharacteristic(ManchesterOWLSyntax characteristic) {
        setAxiomWriting();
        writeSectionKeyword(CHARACTERISTICS);
        write(characteristic);
        restore();
    }


    public void visit(OWLAsymmetricObjectPropertyAxiom axiom) {
        setAxiomWriting();
        writeSectionKeyword(ASYMMETRIC);
        axiom.getProperty().accept(this);
        restore();
    }


    public void visit(OWLReflexiveObjectPropertyAxiom axiom) {
        setAxiomWriting();
        writeSectionKeyword(REFLEXIVE);
        axiom.getProperty().accept(this);
        restore();
    }

    private void writeBinaryOrNaryList(ManchesterOWLSyntax binaryKeyword, Set<? extends OWLObject> objects, ManchesterOWLSyntax naryKeyword) {
        if(objects.size() == 2) {
            Iterator<? extends OWLObject> it = objects.iterator();
            it.next().accept(this);
            write(binaryKeyword);
            it.next().accept(this);
        }
        else {
            writeSectionKeyword(naryKeyword);
            writeCommaSeparatedList(objects);
        }
    }

    public void visit(OWLDisjointClassesAxiom axiom) {
        setAxiomWriting();
        writeBinaryOrNaryList(DISJOINT_WITH, axiom.getClassExpressions(), DISJOINT_CLASSES);
        restore();
    }


    public void visit(OWLDataPropertyDomainAxiom axiom) {
        setAxiomWriting();
        axiom.getProperty().accept(this);
        write(DOMAIN);
        axiom.getDomain().accept(this);
        restore();
    }

    public void visit(OWLObjectPropertyDomainAxiom axiom) {
        setAxiomWriting();
        axiom.getProperty().accept(this);
        write(DOMAIN);
        axiom.getDomain().accept(this);
        restore();
    }


    public void visit(OWLEquivalentObjectPropertiesAxiom axiom) {
        setAxiomWriting();
        writeBinaryOrNaryList(EQUIVALENT_TO, axiom.getProperties(), EQUIVALENT_PROPERTIES);
        restore();
    }


    public void visit(OWLNegativeDataPropertyAssertionAxiom axiom) {
        setAxiomWriting();
        write(NOT);
        write("(");
        axiom.getSubject().accept(this);
        write(" ");
        axiom.getProperty().accept(this);
        write(" ");
        axiom.getObject().accept(this);
        write(")");
        restore();
    }


    public void visit(OWLDifferentIndividualsAxiom axiom) {
        setAxiomWriting();
        writeBinaryOrNaryList(DIFFERENT_FROM, axiom.getIndividuals(), DIFFERENT_INDIVIDUALS);
        restore();
    }


    public void visit(OWLDisjointDataPropertiesAxiom axiom) {
        setAxiomWriting();
        writeBinaryOrNaryList(DISJOINT_WITH, axiom.getProperties(), DISJOINT_PROPERTIES);
        restore();
    }


    public void visit(OWLDisjointObjectPropertiesAxiom axiom) {
        setAxiomWriting();
        writeBinaryOrNaryList(DISJOINT_WITH, axiom.getProperties(), DISJOINT_PROPERTIES);
        restore();
    }


    public void visit(OWLObjectPropertyRangeAxiom axiom) {
        setAxiomWriting();
        axiom.getProperty().accept(this);
        write(RANGE);
        axiom.getRange().accept(this);
        restore();
    }


    public void visit(OWLObjectPropertyAssertionAxiom axiom) {
        setAxiomWriting();
        axiom.getSubject().accept(this);
        write(" ");
        axiom.getProperty().accept(this);
        write(" ");
        axiom.getObject().accept(this);
        restore();
    }


    public void visit(OWLFunctionalObjectPropertyAxiom axiom) {
        setAxiomWriting();
        writeSectionKeyword(FUNCTIONAL);
        axiom.getProperty().accept(this);
        restore();
    }


    public void visit(OWLSubObjectPropertyOfAxiom axiom) {
        setAxiomWriting();
        axiom.getSubProperty().accept(this);
        write(SUB_PROPERTY_OF);
        axiom.getSuperProperty().accept(this);
        restore();
    }


    public void visit(OWLDisjointUnionAxiom axiom) {
        setAxiomWriting();
        axiom.getOWLClass().accept(this);
        write(DISJOINT_UNION_OF);
        writeCommaSeparatedList(axiom.getClassExpressions());
        restore();
    }

    private void writeFrameType(OWLObject object) {
        setAxiomWriting();
        if (object instanceof OWLOntology) {
            writeFrameKeyword(ONTOLOGY);
            OWLOntology ont = (OWLOntology) object;
            if (!ont.isAnonymous()) {
                write("<");
                write(ont.getOntologyID().getOntologyIRI().toString());
                write(">");
            }
        }
        else {
            if (object instanceof OWLClassExpression) {
                writeFrameKeyword(CLASS);
            }
            else if (object instanceof OWLObjectPropertyExpression) {
                writeFrameKeyword(OBJECT_PROPERTY);
            }
            else if (object instanceof OWLDataPropertyExpression) {
                writeFrameKeyword(DATA_PROPERTY);
            }
            else if (object instanceof OWLIndividual) {
                writeFrameKeyword(INDIVIDUAL);
            }
            else if (object instanceof OWLAnnotationProperty) {
                writeFrameKeyword(ANNOTATION_PROPERTY);
            }
        }
        object.accept(this);
    }

    public void visit(OWLDeclarationAxiom axiom) {
        setAxiomWriting();
        writeFrameType(axiom.getEntity());
        restore();
    }


    public void visit(OWLAnnotationAssertionAxiom axiom) {
        setAxiomWriting();
        axiom.getSubject().accept(this);
        write(" ");
        axiom.getAnnotation().accept(this);
        restore();
    }

    public void visit(OWLAnnotationPropertyDomainAxiom axiom) {
        setAxiomWriting();
        axiom.getProperty().accept(this);
        write(DOMAIN);
        axiom.getDomain().accept(this);
    }

    public void visit(OWLAnnotationPropertyRangeAxiom axiom) {
        setAxiomWriting();
        axiom.getProperty().accept(this);
        write(RANGE);
        axiom.getRange().accept(this);
    }

    public void visit(OWLSubAnnotationPropertyOfAxiom axiom) {
        setAxiomWriting();
        axiom.getSubProperty().accept(this);
        write(SUB_PROPERTY_OF);
        axiom.getSuperProperty().accept(this);
    }

    public void visit(OWLSymmetricObjectPropertyAxiom axiom) {
        setAxiomWriting();
        writeSectionKeyword(SYMMETRIC);
        axiom.getProperty().accept(this);
        restore();
    }


    public void visit(OWLDataPropertyRangeAxiom axiom) {
        setAxiomWriting();
        axiom.getProperty().accept(this);
        writeSectionKeyword(RANGE);
        axiom.getRange().accept(this);
        restore();
    }


    public void visit(OWLFunctionalDataPropertyAxiom axiom) {
        setAxiomWriting();
        writeSectionKeyword(FUNCTIONAL);
        axiom.getProperty().accept(this);
        restore();
    }


    public void visit(OWLEquivalentDataPropertiesAxiom axiom) {
        setAxiomWriting();
        writeFrameKeyword(EQUIVALENT_PROPERTIES);
        writeCommaSeparatedList(axiom.getProperties());
        restore();
    }


    public void visit(OWLClassAssertionAxiom axiom) {
        setAxiomWriting();
        axiom.getIndividual().accept(this);
        write(TYPE);
        axiom.getClassExpression().accept(this);
        restore();
    }


    public void visit(OWLEquivalentClassesAxiom axiom) {
        setAxiomWriting();
        writeBinaryOrNaryList(EQUIVALENT_TO, axiom.getClassExpressions(), EQUIVALENT_CLASSES);
        restore();
    }


    public void visit(OWLDataPropertyAssertionAxiom axiom) {
        setAxiomWriting();
        axiom.getSubject().accept(this);
        write(" ");
        axiom.getProperty().accept(this);
        write(" ");
        axiom.getObject().accept(this);
        restore();
    }


    public void visit(OWLTransitiveObjectPropertyAxiom axiom) {
        setAxiomWriting();
        writeSectionKeyword(TRANSITIVE);
        axiom.getProperty().accept(this);
        restore();
    }


    public void visit(OWLIrreflexiveObjectPropertyAxiom axiom) {
        setAxiomWriting();
        writeSectionKeyword(IRREFLEXIVE);
        axiom.getProperty().accept(this);
        restore();
    }


    public void visit(OWLSubDataPropertyOfAxiom axiom) {
        setAxiomWriting();
        axiom.getSubProperty().accept(this);
        writeSectionKeyword(SUB_PROPERTY_OF);
        axiom.getSuperProperty().accept(this);
        restore();
    }


    public void visit(OWLInverseFunctionalObjectPropertyAxiom axiom) {
        setAxiomWriting();
        writeSectionKeyword(INVERSE_FUNCTIONAL);
        axiom.getProperty().accept(this);
        restore();
    }


    public void visit(OWLSameIndividualAxiom axiom) {
        setAxiomWriting();
        writeBinaryOrNaryList(SAME_AS, axiom.getIndividuals(), SAME_INDIVIDUAL);
        restore();
    }


    public void visit(OWLSubPropertyChainOfAxiom axiom) {
        setAxiomWriting();
        for (Iterator<OWLObjectPropertyExpression> it = axiom.getPropertyChain().iterator(); it.hasNext();) {
            it.next().accept(this);
            if (it.hasNext()) {
                write(" o ");
            }
        }
        write(SUB_PROPERTY_OF);
        axiom.getSuperProperty().accept(this);
        restore();
    }


    public void visit(OWLInverseObjectPropertiesAxiom axiom) {
        setAxiomWriting();
        axiom.getFirstProperty().accept(this);
        write(INVERSE_OF);
        axiom.getSecondProperty().accept(this);
        restore();
    }


    public void visit(SWRLRule rule) {
        setAxiomWriting();
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
        restore();
    }

    public void visit(OWLHasKeyAxiom axiom) {
        setAxiomWriting();
        axiom.getClassExpression().accept(this);
        write(HAS_KEY);
        write(axiom.getObjectPropertyExpressions(), COMMA, false);
        write(axiom.getDataPropertyExpressions(), COMMA, false);
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


    public void visit(SWRLDataPropertyAtom node) {
        node.getPredicate().accept(this);
        write("(");
        node.getFirstArgument().accept(this);
        write(", ");
        node.getSecondArgument().accept(this);
        write(")");
    }


    public void visit(SWRLBuiltInAtom node) {
        write(node.getPredicate().toQuotedString());
        write("(");
        for (Iterator<SWRLDArgument> it = node.getArguments().iterator(); it.hasNext();) {
            it.next().accept(this);
            if (it.hasNext()) {
                write(", ");
            }
        }
        write(")");
    }


    public void visit(SWRLVariable node) {
        write("?");
        write(node.getIRI().toQuotedString());
    }

    
    public void visit(SWRLIndividualArgument node) {
        node.getIndividual().accept(this);
    }


    public void visit(SWRLLiteralArgument node) {
        node.getLiteral().accept(this);
    }


    public void visit(SWRLSameIndividualAtom node) {
        write(SAME_AS);
        write("(");
        node.getFirstArgument().accept(this);
        write(", ");
        node.getSecondArgument().accept(this);
        write(")");
    }


    public void visit(SWRLDifferentIndividualsAtom node) {
        write(DIFFERENT_FROM);
        write("(");
        node.getFirstArgument().accept(this);
        write(", ");
        node.getSecondArgument().accept(this);
        write(")");
    }


    public void visit(OWLDatatypeDefinitionAxiom axiom) {
        
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Ontology
    //
    ///////////////////////////////////////////////////////////////////////////////////////////////


    public void visit(OWLOntology ontology) {

    }
}
