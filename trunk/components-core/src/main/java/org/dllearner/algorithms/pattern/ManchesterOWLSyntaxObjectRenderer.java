package org.dllearner.algorithms.pattern;

/*
 * This file is part of the OWL API.
 *
 * The contents of this file are subject to the LGPL License, Version 3.0.
 *
 * Copyright (C) 2011, The University of Manchester
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 *
 * Alternatively, the contents of this file may be used under the terms of the Apache License, Version 2.0
 * in which case, the provisions of the Apache License Version 2.0 are applicable instead of those above.
 *
 * Copyright 2011, University of Manchester
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import static org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax.AND;
import static org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax.ANNOTATIONS;
import static org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax.ANNOTATION_PROPERTY;
import static org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax.ASYMMETRIC;
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
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.ShortFormProvider;



/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 25-Apr-2007<br><br>
 */
@SuppressWarnings("javadoc")
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
        //boolean lastWasNamed = false;
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

            //lastWasNamed = desc instanceof OWLClass;
        }
    }

    private void writeRestriction(OWLQuantifiedDataRestriction restriction, ManchesterOWLSyntax keyword) {
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

    private void writeRestriction(OWLQuantifiedObjectRestriction restriction, ManchesterOWLSyntax keyword) {
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


    private <R extends OWLPropertyRange, P extends OWLPropertyExpression<R, P>, V extends OWLObject> void writeRestriction(OWLHasValueRestriction<R, P, V> restriction) {
        restriction.getProperty().accept(this);
        write(VALUE);
        restriction.getValue().accept(this);
    }


    private <R extends OWLPropertyRange, P extends OWLPropertyExpression<R, P>, F extends OWLPropertyRange> void writeRestriction(OWLCardinalityRestriction<R, P, F> restriction, ManchesterOWLSyntax keyword) {
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
        writeAnnotations(node.getAnnotations());
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


    public void visit(OWLLiteral node) {
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
        else if (node.getDatatype().isBoolean()) {
            write(node.getLiteral());
        }
        else {
            pushTab(getIndent());
            writeLiteral(node.getLiteral());
            if(node.hasLang()) {
                write("@");
                write(node.getLang());
            }
            else if(!node.isRDFPlainLiteral()) {
                write("^^");
                node.getDatatype().accept(this);
            }
            popTab();
        }
    }


    private void writeLiteral(String literal) {
        write("\"");
        for(int i = 0; i < literal.length(); i++) {
            char ch = literal.charAt(i);
            if(ch == '"') {
                write('\\');
            }
            else if(ch == '\\') {
                write('\\');
            }
            write(ch);
        }
        write("\"");
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


//    private void writePropertyCharacteristic(ManchesterOWLSyntax characteristic) {
//        setAxiomWriting();
//        writeSectionKeyword(CHARACTERISTICS);
//        write(characteristic);
//        restore();
//    }


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
        if(node.getPredicate().isAnonymous()) {
            write("(");
        }
        node.getPredicate().accept(this);
        if(node.getPredicate().isAnonymous()) {
            write(")");
        }
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

    @SuppressWarnings("unused")
    public void visit(OWLDatatypeDefinitionAxiom axiom) {

    }

    protected void writeAnnotations(Set<OWLAnnotation> annos) {
        if(annos.isEmpty()) {
            return;
        }
        writeNewLine();
        write(ANNOTATIONS.toString());
        write(": ");
        pushTab(getIndent());
        for (Iterator<OWLAnnotation> annoIt = annos.iterator(); annoIt.hasNext();) {
            OWLAnnotation anno = annoIt.next();
//            if (!anno.getAnnotations().isEmpty()) {
//                writeAnnotations(anno.getAnnotations());
//            }
            anno.accept(this);
            if (annoIt.hasNext()) {
                write(", ");
                writeNewLine();
            }

        }
        writeNewLine();
        writeNewLine();
        popTab();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Ontology
    //
    ///////////////////////////////////////////////////////////////////////////////////////////////

    @SuppressWarnings("unused")
    public void visit(OWLOntology ontology) {

    }
}

