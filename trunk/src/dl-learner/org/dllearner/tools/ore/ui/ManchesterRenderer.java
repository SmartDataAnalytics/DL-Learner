package org.dllearner.tools.ore.ui;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.ui.renderer.OWLEntityRenderer;
import org.protege.editor.owl.ui.renderer.OWLObjectRendererImpl;
import org.protege.editor.owl.ui.renderer.OWLRendererPreferences;
import org.semanticweb.owl.model.OWLAntiSymmetricObjectPropertyAxiom;
import org.semanticweb.owl.model.OWLAxiomAnnotationAxiom;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLClassAssertionAxiom;
import org.semanticweb.owl.model.OWLConstant;
import org.semanticweb.owl.model.OWLConstantAnnotation;
import org.semanticweb.owl.model.OWLDataAllRestriction;
import org.semanticweb.owl.model.OWLDataCardinalityRestriction;
import org.semanticweb.owl.model.OWLDataComplementOf;
import org.semanticweb.owl.model.OWLDataExactCardinalityRestriction;
import org.semanticweb.owl.model.OWLDataMaxCardinalityRestriction;
import org.semanticweb.owl.model.OWLDataMinCardinalityRestriction;
import org.semanticweb.owl.model.OWLDataOneOf;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owl.model.OWLDataPropertyExpression;
import org.semanticweb.owl.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owl.model.OWLDataRange;
import org.semanticweb.owl.model.OWLDataRangeFacetRestriction;
import org.semanticweb.owl.model.OWLDataRangeRestriction;
import org.semanticweb.owl.model.OWLDataSomeRestriction;
import org.semanticweb.owl.model.OWLDataSubPropertyAxiom;
import org.semanticweb.owl.model.OWLDataType;
import org.semanticweb.owl.model.OWLDataValueRestriction;
import org.semanticweb.owl.model.OWLDataVisitor;
import org.semanticweb.owl.model.OWLDeclarationAxiom;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owl.model.OWLDisjointClassesAxiom;
import org.semanticweb.owl.model.OWLDisjointDataPropertiesAxiom;
import org.semanticweb.owl.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owl.model.OWLDisjointUnionAxiom;
import org.semanticweb.owl.model.OWLEntity;
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
import org.semanticweb.owl.model.OWLObjectCardinalityRestriction;
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
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.model.OWLReflexiveObjectPropertyAxiom;
import org.semanticweb.owl.model.OWLRestriction;
import org.semanticweb.owl.model.OWLSameIndividualsAxiom;
import org.semanticweb.owl.model.OWLSubClassAxiom;
import org.semanticweb.owl.model.OWLSymmetricObjectPropertyAxiom;
import org.semanticweb.owl.model.OWLTransitiveObjectPropertyAxiom;
import org.semanticweb.owl.model.OWLTypedConstant;
import org.semanticweb.owl.model.OWLUntypedConstant;
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
import org.semanticweb.owl.util.OWLDescriptionComparator;
import org.semanticweb.owl.util.OWLDescriptionVisitorAdapter;
import org.semanticweb.owl.util.ShortFormProvider;
import org.semanticweb.owl.util.SimpleShortFormProvider;
import org.semanticweb.owl.vocab.OWLRestrictedDataRangeFacetVocabulary;
import org.semanticweb.owl.vocab.XSDVocabulary;

public class ManchesterRenderer implements OWLObjectVisitor{
	private static final Logger logger = Logger.getLogger(OWLObjectRendererImpl.class);

    private OWLOntologyManager owlModelManager;

    private StringBuilder buffer;

    private BracketWriter bracketWriter;

    private Map<OWLRestrictedDataRangeFacetVocabulary, String> facetMap;

    private Map<URI, Boolean> simpleRenderDatatypes;

    private OWLObject focusedObject;

    private OWLEntityRenderer entityRenderer;
    
    private ShortFormProvider shortFomProvider;
    
    private OWLDescriptionComparator comparator = new OWLDescriptionComparator(shortFomProvider);


    public ManchesterRenderer(OWLOntologyManager manager) {
        this.owlModelManager = manager;
        buffer = new StringBuilder();
        bracketWriter = new BracketWriter();
        facetMap = new HashMap<OWLRestrictedDataRangeFacetVocabulary, String>();
        facetMap.put(OWLRestrictedDataRangeFacetVocabulary.MIN_EXCLUSIVE, ">");
        facetMap.put(OWLRestrictedDataRangeFacetVocabulary.MAX_EXCLUSIVE, "<");
        facetMap.put(OWLRestrictedDataRangeFacetVocabulary.MIN_INCLUSIVE, ">=");
        facetMap.put(OWLRestrictedDataRangeFacetVocabulary.MAX_INCLUSIVE, "<=");
        simpleRenderDatatypes = new HashMap<URI, Boolean>();
        simpleRenderDatatypes.put(XSDVocabulary.INT.getURI(), false);
        simpleRenderDatatypes.put(XSDVocabulary.FLOAT.getURI(), false);
        simpleRenderDatatypes.put(XSDVocabulary.DOUBLE.getURI(), false);
        simpleRenderDatatypes.put(XSDVocabulary.STRING.getURI(), true);
        simpleRenderDatatypes.put(XSDVocabulary.BOOLEAN.getURI(), false);
        
        shortFomProvider = new SimpleShortFormProvider();
    }


    public void setup(OWLModelManager owlModelManager) {
    }


    public void initialise() {
    }


    public void dispose() {
    }


    public OWLObject getFocusedObject() {
        return focusedObject;
    }


//    public void setFocusedObject(OWLObject focusedObject) {
//        this.focusedObject = focusedObject;
//        if(focusedObject instanceof OWLDescription) {
//            comparator.setFocusedDescription((OWLDescription) focusedObject);
//        }
//    }


    protected String getAndKeyWord() {
        return "and";
    }


    protected String getOrKeyWord() {
        return "or";
    }


    protected String getNotKeyWord() {
        return " not";
    }


    protected String getSomeKeyWord() {
        return "some";
    }


    protected String getAllKeyWord() {
        return "only";
    }


    protected String getValueKeyWord() {
        return "value";
    }


    protected String getMinKeyWord() {
        return "min";
    }


    protected String getMaxKeyWord() {
        return "max";
    }


    protected String getExactlyKeyWord() {
        return "exactly";
    }


    public String render(OWLObject object, OWLEntityRenderer entityRenderer) {
        reset();
        this.entityRenderer = entityRenderer;
        try {
            object.accept(this);
            return buffer.toString();
        }
        catch (Exception e) {e.printStackTrace();
            return "<Error! " + e.getMessage() + ">";
        }
    }


    protected String getRendering(OWLEntity entity) {
        if (entityRenderer != null){
            return entityRenderer.render(entity);
        }
        return shortFomProvider.getShortForm(entity);
    }


    int lastNewLineIndex = 0;

    int currentIndex = 0;


    protected void write(String s) {
        int index = s.indexOf('\n');
        if (index != -1) {
            lastNewLineIndex = currentIndex + index;
        }
        currentIndex = currentIndex + s.length();
        buffer.append(s);
    }


    protected int getIndent() {
        return currentIndex - lastNewLineIndex;
    }


    protected void insertIndent(int indent) {
        for (int i = 0; i < indent; i++) {
            write(" ");
        }
    }


    protected void writeAndKeyword() {
        write(getAndKeyWord());
        write(" ");
    }


    public void reset() {
        lastNewLineIndex = 0;
        currentIndex = 0;
        buffer = new StringBuilder();
    }


    public String getText() {
        return buffer.toString();
    }


   


    private List<OWLDescription> sort(Set<OWLDescription> descriptions) {
        List<OWLDescription> sortedDescs = new ArrayList<OWLDescription>(descriptions);
        Collections.sort(sortedDescs);
        return sortedDescs;
    }


    public void visit(OWLObjectIntersectionOf node) {

        int indent = getIndent();
        List<OWLDescription> ops = sort(node.getOperands());
        for (int i = 0; i < ops.size(); i++) {
            OWLDescription curOp = ops.get(i);
            curOp.accept(this);
            if (i < ops.size() - 1) {
                write("\n");
                insertIndent(indent);
                if (curOp instanceof OWLClass && ops.get(i + 1) instanceof OWLRestriction && OWLRendererPreferences.getInstance().isUseThatKeyword()) {
                    write("that ");
                }
                else {
                    writeAndKeyword();
                }
            }
        }
    }


    public void visit(OWLTypedConstant node) {
        if (simpleRenderDatatypes.containsKey(node.getDataType().getURI())) {
            boolean renderQuotes = simpleRenderDatatypes.get(node.getDataType().getURI());
            if (renderQuotes) {
                write("\"");
            }
            write(node.getLiteral());
            if (renderQuotes) {
                write("\"");
            }
        }
        else {
            write("\"");
            write(node.getLiteral());
            write("\"^^");
            node.getDataType().accept(this);
        }
    }


    public void visit(OWLUntypedConstant node) {
        write("\"");
        write(node.getLiteral());
        write("\"");
        if (node.hasLang()) {
            write("@");
            write(node.getLang());
        }
    }


    public void visit(OWLDataType node) {
        write(getRendering(node));
    }


    public void visit(OWLDataOneOf node) {
        write("{");
        for (Iterator<OWLConstant> it = node.getValues().iterator(); it.hasNext();) {
            it.next().accept(this);
            if (it.hasNext()) {
                write(", ");
            }
        }
        write("}");
    }


    public void visit(OWLDataRangeRestriction node) {
//        writeOpenBracket(node);
        node.getDataRange().accept(this);
        write("[");
        for (Iterator<OWLDataRangeFacetRestriction> it = node.getFacetRestrictions().iterator(); it.hasNext();) {
            it.next().accept(this);
            if (it.hasNext()) {
                write(", ");
            }
        }
        write("]");
//        writeCloseBracket(node);
    }


    public void visit(OWLDataComplementOf owlDataComplementOf) {
        write("not(");
        owlDataComplementOf.getDataRange().accept(this);
        write(")");
    }


    public void visit(OWLDataRangeFacetRestriction node) {
        String rendering = facetMap.get(node.getFacet());
        if (rendering == null) {
            rendering = node.getFacet().getShortName();
        }
        write(rendering);
        write(" ");
        node.getFacetValue().accept(this);
    }


    public void visit(OWLObjectSelfRestriction desc) {
        desc.getProperty().accept(this);
        write(" ");
        write(getSomeKeyWord());
        write(" Self");
    }


    public void visit(OWLDataAllRestriction node) {
        node.getProperty().accept(this);
        write(" ");
        write(getAllKeyWord());
        write(" ");
        node.getFiller().accept(this);
    }


    public void visit(OWLDataProperty node) {
        write(getRendering(node));
    }


    public void visit(OWLDataSomeRestriction node) {
        node.getProperty().accept(this);
        write(" ");
        write(getSomeKeyWord());
        write(" ");
        node.getFiller().accept(this);
    }


    public void visit(OWLDataValueRestriction node) {
        node.getProperty().accept(this);
        write(" ");
        write(getValueKeyWord());
        write(" ");
        node.getValue().accept(this);
    }


    public void visit(OWLIndividual node) {
        if (node.isAnonymous()) {
            write("Anonymous : [");
            for (OWLOntology ont : owlModelManager.getOntologies()) {
                for (OWLDescription desc : node.getTypes(ont)) {
                    write(" ");
                    desc.accept(this);
                }
            }
            write(" ]");
        }
        else {
            write(getRendering(node));
        }
    }


    public void visit(OWLObjectAllRestriction node) {
        node.getProperty().accept(this);
        write(" ");
        write(getAllKeyWord());
        write(" ");
        writeOpenBracket(node.getFiller());
        node.getFiller().accept(this);
        writeCloseBracket(node.getFiller());
    }


    public void visit(OWLObjectMinCardinalityRestriction desc) {
        writeCardinality(desc, getMinKeyWord());
    }


    public void visit(OWLObjectExactCardinalityRestriction desc) {
        writeCardinality(desc, getExactlyKeyWord());
    }


    public void visit(OWLObjectMaxCardinalityRestriction desc) {
        writeCardinality(desc, getMaxKeyWord());
    }


    private void writeCardinality(OWLObjectCardinalityRestriction desc, String keyword) {
        desc.getProperty().accept(this);
        write(" ");
        write(keyword);
        write(" ");
        write(Integer.toString(desc.getCardinality()));
        write(" ");
        writeOpenBracket(desc.getFiller());
        desc.getFiller().accept(this);
        writeCloseBracket(desc.getFiller());
    }


    public void visit(OWLDataMinCardinalityRestriction desc) {
        writeCardinality(desc, getMinKeyWord());
    }


    public void visit(OWLDataExactCardinalityRestriction desc) {
        writeCardinality(desc, getExactlyKeyWord());
    }


    public void visit(OWLDataMaxCardinalityRestriction desc) {
        writeCardinality(desc, getMaxKeyWord());
    }


    private void writeCardinality(OWLDataCardinalityRestriction desc, String keyword) {
        desc.getProperty().accept(this);
        write(" ");
        write(keyword);
        write(" ");
        write(Integer.toString(desc.getCardinality()));
        write(" ");
        writeOpenBracket(desc.getFiller());
        desc.getFiller().accept(this);
        writeCloseBracket(desc.getFiller());
    }


    public void visit(OWLObjectProperty node) {
        write(getRendering(node));
    }


    public void visit(OWLObjectSomeRestriction node) {
        node.getProperty().accept(this);
        write(" ");
        write(getSomeKeyWord());
        write(" ");
        writeOpenBracket(node.getFiller());
        node.getFiller().accept(this);
        writeCloseBracket(node.getFiller());
    }


    public void visit(OWLObjectValueRestriction node) {
        node.getProperty().accept(this);
        write(" ");
        write(getValueKeyWord());
        write(" ");
        node.getValue().accept(this);
    }


    public void visit(OWLObjectComplementOf node) {
        writeNotKeyword();
        write(" ");
        writeOpenBracket(node.getOperand());
        node.getOperand().accept(this);
        writeCloseBracket(node.getOperand());
    }


    protected void writeNotKeyword() {
        write(getNotKeyWord());
    }


    public void visit(OWLObjectUnionOf node) {
        int indent = getIndent();
        for (Iterator<OWLDescription> it = sort(node.getOperands()).iterator(); it.hasNext();) {
            OWLDescription curOp = it.next();
            writeOpenBracket(curOp);
            curOp.accept(this);
            writeCloseBracket(curOp);
            if (it.hasNext()) {
                write("\n");
                insertIndent(indent);
                writeOrKeyword();
            }
        }
    }


    private void writeOrKeyword() {
        write(getOrKeyWord());
        write(" ");
    }


    public void visit(OWLClass node) {
        write(getRendering(node));
    }


    public void visit(OWLObjectPropertyInverse property) {
        write("inv(");
        property.getInverse().accept(this);
        write(")");
    }


    public void visit(OWLObjectOneOf node) {
        write("{");
        for (Iterator<OWLIndividual> it = node.getIndividuals().iterator(); it.hasNext();) {
            it.next().accept(this);
            if (it.hasNext()) {
                write(", ");
            }
        }
        write("}");
    }


    public void visit(OWLDisjointClassesAxiom node) {
        for (Iterator<OWLDescription> it = sort(node.getDescriptions()).iterator(); it.hasNext();) {
            it.next().accept(this);
            if (it.hasNext()) {
                write(" disjointWith ");
            }
        }
    }


    public void visit(OWLEquivalentClassesAxiom node) {
        List<OWLDescription> orderedDescs = sort(node.getDescriptions());
        for(Iterator<OWLDescription> it = orderedDescs.iterator(); it.hasNext(); ) {
            OWLDescription desc = it.next();
            if(orderedDescs.get(0).isOWLNothing()) {
                it.remove();
                orderedDescs.add(desc);
                break;
            }
        }

        for (Iterator<OWLDescription> it = orderedDescs.iterator(); it.hasNext();) {
            it.next().accept(this);
            if (it.hasNext()) {
                write(" equivalentTo ");
            }
        }
    }


    public void visit(OWLSubClassAxiom node) {
        node.getSubClass().accept(this);
        write(" subClassOf ");
        node.getSuperClass().accept(this);
    }


    public void visit(OWLFunctionalObjectPropertyAxiom axiom) {
        write("Functional: ");
        axiom.getProperty().accept(this);
    }


    public void visit(OWLInverseFunctionalObjectPropertyAxiom axiom) {
        write("InverseFunctional: ");
        axiom.getProperty().accept(this);
    }


    public void visit(OWLIrreflexiveObjectPropertyAxiom axiom) {
        write("Irreflexive: ");
        axiom.getProperty().accept(this);
    }


    public void visit(OWLDataSubPropertyAxiom axiom) {
        axiom.getSubProperty().accept(this);
        write(" subPropertyOf ");
        axiom.getSuperProperty().accept(this);
    }


    public void visit(OWLReflexiveObjectPropertyAxiom axiom) {
        write("Reflexive: ");
        axiom.getProperty().accept(this);
    }


    public void visit(OWLSymmetricObjectPropertyAxiom axiom) {
        write("Symmetric: ");
        axiom.getProperty().accept(this);
    }


    public void visit(OWLTransitiveObjectPropertyAxiom axiom) {
        write("Transitive: ");
        axiom.getProperty().accept(this);
    }


    public void visit(OWLObjectPropertyDomainAxiom axiom) {
        if (!OWLRendererPreferences.getInstance().isRenderDomainAxiomsAsGCIs()) {
            axiom.getProperty().accept(this);
            write(" domain ");
            axiom.getDomain().accept(this);
        }
        else {
            axiom.getProperty().accept(this);
            write(" some ");
            owlModelManager.getOWLDataFactory().getOWLThing().accept(this);
            write(" subClassOf ");
            axiom.getDomain().accept(this);
        }
    }


    public void visit(OWLEquivalentObjectPropertiesAxiom node) {
        for (Iterator<OWLObjectPropertyExpression> it = node.getProperties().iterator(); it.hasNext();) {
            it.next().accept(this);
            if (it.hasNext()) {
                write(" equivalentTo ");
            }
        }
    }


    public void visit(OWLObjectPropertyRangeAxiom axiom) {
        axiom.getProperty().accept(this);
        write(" range ");
        axiom.getRange().accept(this);
    }


    public void visit(OWLClassAssertionAxiom axiom) {
        axiom.getIndividual().accept(this);
        write(" types ");
        axiom.getDescription().accept(this);
    }


    public void visit(OWLFunctionalDataPropertyAxiom axiom) {
        write("Functional: ");
        axiom.getProperty().accept(this);
    }


    public void visit(OWLEquivalentDataPropertiesAxiom node) {
        for (Iterator<OWLDataPropertyExpression> it = node.getProperties().iterator(); it.hasNext();) {
            it.next().accept(this);
            if (it.hasNext()) {
                write(" equivalentTo ");
            }
        }
    }


    public void visit(OWLSameIndividualsAxiom axiom) {
        write("SameIndividuals: [");
        for (Iterator<OWLIndividual> it = axiom.getIndividuals().iterator(); it.hasNext();) {
            it.next().accept(this);
            if (it.hasNext()) {
                write(", ");
            }
        }
        write("]");
    }


    public void visit(OWLDifferentIndividualsAxiom axiom) {
        write("DifferentIndividuals: [");
        for (Iterator<OWLIndividual> it = axiom.getIndividuals().iterator(); it.hasNext();) {
            it.next().accept(this);
            if (it.hasNext()) {
                write(", ");
            }
        }
        write("]");
    }


    public void visit(OWLDisjointDataPropertiesAxiom axiom) {
        for (Iterator<OWLDataPropertyExpression> it = axiom.getProperties().iterator(); it.hasNext();) {
            it.next().accept(this);
            if (it.hasNext()) {
                write(" disjointWith ");
            }
        }
    }


    public void visit(OWLDisjointObjectPropertiesAxiom axiom) {
        for (Iterator<OWLObjectPropertyExpression> it = axiom.getProperties().iterator(); it.hasNext();) {
            it.next().accept(this);
            if (it.hasNext()) {
                write(" disjointWith ");
            }
        }
    }


    public void visit(OWLObjectPropertyAssertionAxiom axiom) {
        axiom.getSubject().accept(this);
        write(" ");
        axiom.getProperty().accept(this);
        write(" ");
        axiom.getObject().accept(this);
    }


    public void visit(OWLDataPropertyAssertionAxiom axiom) {
        axiom.getSubject().accept(this);
        write(" ");
        axiom.getProperty().accept(this);
        write(" ");
        axiom.getObject().accept(this);
    }


    public void visit(OWLNegativeDataPropertyAssertionAxiom axiom) {
        write("not(");
        axiom.getSubject().accept(this);
        write(" ");
        axiom.getProperty().accept(this);
        write(" ");
        axiom.getObject().accept(this);
        write(")");
    }


    public void visit(OWLNegativeObjectPropertyAssertionAxiom axiom) {
        write("not(");
        axiom.getSubject().accept(this);
        write(" ");
        axiom.getProperty().accept(this);
        write(" ");
        axiom.getObject().accept(this);
        write(")");
    }


    public void visit(OWLInverseObjectPropertiesAxiom axiom) {
        axiom.getFirstProperty().accept(this);
        write(" inverseOf ");
        axiom.getSecondProperty().accept(this);
    }


    public void visit(OWLAntiSymmetricObjectPropertyAxiom axiom) {
        write("AntiSymmetric: ");
        axiom.getProperty().accept(this);
    }


    public void visit(OWLDataPropertyDomainAxiom axiom) {
        axiom.getProperty().accept(this);
        write(" domain ");
        axiom.getDomain().accept(this);
    }


    public void visit(OWLDataPropertyRangeAxiom axiom) {
        axiom.getProperty().accept(this);
        write(" range ");
        axiom.getRange().accept(this);
    }


    public void visit(OWLObjectSubPropertyAxiom axiom) {
        axiom.getSubProperty().accept(this);
        write(" subPropertyOf ");
        axiom.getSuperProperty().accept(this);
    }


    public void visit(OWLDisjointUnionAxiom axiom) {
        axiom.getOWLClass().accept(this);
        write(" disjointUnionOf ");
        write("[");
        int indent = getIndent();
        for (Iterator<OWLDescription> it = axiom.getDescriptions().iterator(); it.hasNext();) {
            it.next().accept(this);
            if (it.hasNext()) {
                write("\n");
                insertIndent(indent);
            }
        }
        write("]");
    }


    public void visit(OWLDeclarationAxiom axiom) {
        OWLEntity entity = axiom.getEntity();
        if (entity.isOWLClass()){
            write("Class(");
        }
        else if (entity.isOWLObjectProperty()){
            write("Object property(");
        }
        else if (entity.isOWLDataProperty()){
            write("Data property(");
        }
        else if (entity.isOWLIndividual()){
            write("Individual(");
        }
        else{
            write("(");
        }
        entity.accept(this);
        write(")");
    }


    public void visit(OWLImportsDeclaration axiom) {
        writeOntologyURI(axiom.getImportedOntologyURI());
        if (owlModelManager.getImportedOntology(axiom) == null) {
            write("      (Not Loaded)");
        }
    }


    public void visit(OWLAxiomAnnotationAxiom axiom) {
        axiom.getSubject().accept(this);
        write(" ");
        axiom.getAnnotation().accept(this);
    }


    private void writeOpenBracket(OWLDescription description) {
        description.accept(bracketWriter);
        if (bracketWriter.writeBrackets()) {
            write("(");
        }
    }


    private void writeOpenBracket(OWLDataRange dataRange) {
        dataRange.accept(bracketWriter);
        if (bracketWriter.writeBrackets()) {
            write("(");
        }
    }


    private void writeCloseBracket(OWLDescription description) {
        description.accept(bracketWriter);
        if (bracketWriter.writeBrackets()) {
            write(")");
        }
    }


    private void writeCloseBracket(OWLDataRange dataRange) {
        dataRange.accept(bracketWriter);
        if (bracketWriter.writeBrackets()) {
            write(")");
        }
    }


    public void visit(OWLOntology ontology) {
        writeOntologyURI(ontology.getURI());
    }


    public void visit(OWLObjectPropertyChainSubPropertyAxiom axiom) {
        for (Iterator<OWLObjectPropertyExpression> it = axiom.getPropertyChain().iterator(); it.hasNext();) {
            it.next().accept(this);
            if (it.hasNext()) {
                write(" o ");
            }
        }
        write(" \u279E ");
        axiom.getSuperProperty().accept(this);
    }


    public void visit(OWLConstantAnnotation annotation) {
        write(annotation.getAnnotationURI().getFragment());
        write(" ");
        write(annotation.getAnnotationValue().toString());
    }


    public void visit(SWRLRule swrlRule) {
        for (Iterator<SWRLAtom> it = swrlRule.getBody().iterator(); it.hasNext();) {
            it.next().accept(this);
            if (it.hasNext()) {
                write(" , ");
            }
        }
        write(" -> ");
        for (Iterator<SWRLAtom> it = swrlRule.getHead().iterator(); it.hasNext();) {
            it.next().accept(this);
            if (it.hasNext()) {
                write(" \u2227 ");
            }
        }
    }


    public void visit(OWLEntityAnnotationAxiom owlEntityAnnotationAxiom) {
        owlEntityAnnotationAxiom.getSubject().accept(this);
        write(" ");
        owlEntityAnnotationAxiom.getAnnotation().accept(this);
    }


    public void visit(OWLOntologyAnnotationAxiom axiom) {
        axiom.getSubject().accept(this);
        write(" ");
        axiom.getAnnotation().accept(this);
    }


    public void visit(OWLObjectAnnotation owlObjectAnnotation) {
        write(owlObjectAnnotation.getAnnotationURI().getFragment());
        write(" ");
        owlObjectAnnotation.getAnnotationValue().accept(this);
    }


    public void visit(SWRLClassAtom swrlClassAtom) {
        OWLDescription desc = swrlClassAtom.getPredicate();
        if (desc.isAnonymous()) {
            write("(");
        }
        desc.accept(this);
        if (desc.isAnonymous()) {
            write(")");
        }
        write("(");
        swrlClassAtom.getArgument().accept(this);
        write(")");
    }


    public void visit(SWRLDataRangeAtom swrlDataRangeAtom) {
        swrlDataRangeAtom.getPredicate().accept(this);
        write("(");
        swrlDataRangeAtom.getArgument().accept(this);
        write(")");
    }


    public void visit(SWRLObjectPropertyAtom swrlObjectPropertyAtom) {
        swrlObjectPropertyAtom.getPredicate().accept(this);
        write("(");
        swrlObjectPropertyAtom.getFirstArgument().accept(this);
        write(", ");
        swrlObjectPropertyAtom.getSecondArgument().accept(this);
        write(")");
    }


    public void visit(SWRLDataValuedPropertyAtom swrlDataValuedPropertyAtom) {
        swrlDataValuedPropertyAtom.getPredicate().accept(this);
        write("(");
        swrlDataValuedPropertyAtom.getFirstArgument().accept(this);
        write(", ");
        swrlDataValuedPropertyAtom.getSecondArgument().accept(this);
        write(")");
    }


    public void visit(SWRLBuiltInAtom swrlBuiltInAtom) {
        write(swrlBuiltInAtom.getPredicate().getShortName());
        write("(");
        Iterator<SWRLAtomDObject> it = swrlBuiltInAtom.getArguments().iterator();
        while (it.hasNext()) {
            SWRLAtomDObject argument = it.next();
            argument.accept(this);
            if (it.hasNext()) {
                write(", ");
            }
        }
        write(")");
    }


    public void visit(SWRLAtomDVariable swrlAtomDVariable) {
        write("?");
        write(swrlAtomDVariable.getURI().getFragment());
    }


    public void visit(SWRLAtomIVariable swrlAtomIVariable) {
        write("?");
        write(swrlAtomIVariable.getURI().getFragment());
    }


    public void visit(SWRLAtomIndividualObject swrlAtomIndividualObject) {
        swrlAtomIndividualObject.getIndividual().accept(this);
    }


    public void visit(SWRLAtomConstantObject swrlAtomConstantObject) {
        swrlAtomConstantObject.getConstant().accept(this);
    }


    public void visit(SWRLDifferentFromAtom swrlDifferentFromAtom) {
        swrlDifferentFromAtom.getPredicate().accept(this);
        write("(");
        swrlDifferentFromAtom.getFirstArgument().accept(this);
        write(", ");
        swrlDifferentFromAtom.getSecondArgument().accept(this);
        write(")");
    }


    public void visit(SWRLSameAsAtom swrlSameAsAtom) {
        swrlSameAsAtom.getPredicate().accept(this);
        write("(");
        swrlSameAsAtom.getFirstArgument().accept(this);
        write(", ");
        swrlSameAsAtom.getSecondArgument().accept(this);
        write(")");
    }


    private void writeOntologyURI(URI uri) {
        String shortName = uri.getFragment();//owlModelManager.getURIRendering(uri);
        if (shortName != null) {
            write(shortName);
            write(" (");
            write(uri.toString());
            write(")");
        }
        else {
            write(uri.toString());
        }
    }


    private class BracketWriter extends OWLDescriptionVisitorAdapter implements OWLDataVisitor {

        boolean nested = false;


        public boolean writeBrackets() {
            return nested;
        }


        public void visit(OWLObjectIntersectionOf owlAnd) {
            nested = true;
        }


        public void visit(OWLDataAllRestriction owlDataAllRestriction) {
            nested = true;
        }


        public void visit(OWLDataSomeRestriction owlDataSomeRestriction) {
            nested = true;
        }


        public void visit(OWLDataValueRestriction owlDataValueRestriction) {
            nested = true;
        }


        public void visit(OWLObjectAllRestriction owlObjectAllRestriction) {
            nested = true;
        }


        public void visit(OWLObjectSomeRestriction owlObjectSomeRestriction) {
            nested = true;
        }


        public void visit(OWLObjectValueRestriction owlObjectValueRestriction) {
            nested = true;
        }


        public void visit(OWLObjectComplementOf owlNot) {
            nested = true;
        }


        public void visit(OWLObjectUnionOf owlOr) {
            nested = true;
        }


        public void visit(OWLClass owlClass) {
            nested = false;
        }


        public void visit(OWLObjectOneOf owlObjectOneOf) {
            nested = false;
        }


        public void visit(OWLObjectMinCardinalityRestriction desc) {
            nested = true;
        }


        public void visit(OWLObjectExactCardinalityRestriction desc) {
            nested = true;
        }


        public void visit(OWLObjectMaxCardinalityRestriction desc) {
            nested = true;
        }


        public void visit(OWLObjectSelfRestriction desc) {
            nested = true;
        }


        public void visit(OWLDataMinCardinalityRestriction desc) {
            nested = true;
        }


        public void visit(OWLDataExactCardinalityRestriction desc) {
            nested = true;
        }


        public void visit(OWLDataMaxCardinalityRestriction desc) {
            nested = true;
        }


        public void visit(OWLDataType node) {
            nested = false;
        }


        public void visit(OWLDataComplementOf node) {
            nested = false;
        }


        public void visit(OWLDataOneOf node) {
            nested = false;
        }


        public void visit(OWLDataRangeRestriction node) {
            nested = true;
        }


        public void visit(OWLTypedConstant node) {
            nested = false;
        }


        public void visit(OWLUntypedConstant node) {
            nested = false;
        }


        public void visit(OWLDataRangeFacetRestriction node) {
            nested = false;
        }
    }

}
