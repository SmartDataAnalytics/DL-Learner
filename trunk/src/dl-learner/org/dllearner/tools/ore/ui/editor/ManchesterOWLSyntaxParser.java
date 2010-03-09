package org.dllearner.tools.ore.ui.editor;


import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.coode.manchesterowlsyntax.ManchesterOWLSyntax;
import org.coode.manchesterowlsyntax.ManchesterOWLSyntaxTokenizer;
import org.semanticweb.owl.expression.OWLEntityChecker;
import org.semanticweb.owl.model.AddAxiom;
import org.semanticweb.owl.model.OWLAnnotation;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLClassAxiom;
import org.semanticweb.owl.model.OWLConstant;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDataPropertyExpression;
import org.semanticweb.owl.model.OWLDataRange;
import org.semanticweb.owl.model.OWLDataRangeFacetRestriction;
import org.semanticweb.owl.model.OWLDataType;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLImportsDeclaration;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLObjectPropertyAxiom;
import org.semanticweb.owl.model.OWLObjectPropertyChainSubPropertyAxiom;
import org.semanticweb.owl.model.OWLObjectPropertyExpression;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyChange;
import org.semanticweb.owl.model.OWLOntologyChangeException;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.model.SetOntologyURI;
import org.semanticweb.owl.util.CollectionFactory;
import org.semanticweb.owl.util.NamespaceUtil;
import org.semanticweb.owl.vocab.Namespaces;
import org.semanticweb.owl.vocab.OWLRDFVocabulary;
import org.semanticweb.owl.vocab.OWLRestrictedDataRangeFacetVocabulary;
import org.semanticweb.owl.vocab.XSDVocabulary;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 10-Sep-2007<br><br>
 * <p/>
 * A parser for the Manchester OWL Syntax.
 * All properties must be defined before they are used.  For example,
 * consider the restriction hasPart some Leg.  The parser must know
 * in advance whether or not hasPart is an object property or a data
 * property so that Leg gets parsed correctly.  In a tool, such as an
 * editor, it is expected that hasPart will already exists as either
 * a data property or an object property.  If a complete ontology is
 * being parsed, it is expected that hasPart will have been defined at
 * the top of the file before it is used in any class descriptions or
 * property assertions (e.g.  ObjectProperty: hasPart)
 */
public class ManchesterOWLSyntaxParser {

    // This parser was built by hand!  After stuggling with terrible
    // error messages produced by ANTLR (or JavaCC) I decides to construct
    // this parser by hand.  The error messages that this parser generates
    // are specific to the Manchester OWL Syntax and are such that it should
    // be easy to use this parser in tools such as editors.

    private OWLDataFactory dataFactory;


    private OWLEntityChecker owlEntityChecker;

    private final String EOF = "<EOF>";

    private Set<Character> skip = new HashSet<Character>();

    private Set<Character> delims = new HashSet<Character>();

    private Set<Character> escapeChars = new HashSet<Character>();

    private String base;

    private Set<String> classNames;

    private Set<String> objectPropertyNames;

    private Set<String> dataPropertyNames;

    private Set<String> individualNames;

    private Set<String> dataTypeNames;

    private Set<String> annotationURIs;

    private Set<String> restrictionKeywords;

    private Map<String, URI> namespaceMap;

    private int col = 0;

    private int tokenStartCol = 0;

    private int line = 0;

    private int tokenStartPos;

    private String token;

    private String bufferedToken = null;

    private String buffer;

    private int pos;

    private static final String AND = ManchesterOWLSyntax.AND.toString();

    private static final String OR = ManchesterOWLSyntax.OR.toString();

    private static final String INV = ManchesterOWLSyntax.INVERSE.toString();

    private static final String SOME = ManchesterOWLSyntax.SOME.toString();

    private static final String SELF = ManchesterOWLSyntax.SELF.toString();

    private static final String ONLY = ManchesterOWLSyntax.ONLY.toString();

    private static final String VALUE = ManchesterOWLSyntax.VALUE.toString();

    private static final String MIN = ManchesterOWLSyntax.MIN.toString();

    private static final String MAX = ManchesterOWLSyntax.MAX.toString();

    private static final String EXACTLY = ManchesterOWLSyntax.EXACTLY.toString();

    private static final String ONLYSOME = ManchesterOWLSyntax.ONLYSOME.toString();

    private static final String NOT = ManchesterOWLSyntax.NOT.toString();

    private static final String CLASS = ManchesterOWLSyntax.CLASS.toString() + ":";

    private static final String SUB_CLASS_OF = ManchesterOWLSyntax.SUBCLASS_OF.toString();// + ":";

    private static final String EQUIVALENT_TO = ManchesterOWLSyntax.EQUIVALENT_TO.toString();// + ":";

    private static final String DISJOINT_WITH = ManchesterOWLSyntax.DISJOINT_WITH.toString();// + ":";

    private static final String OBJECT_PROPERTY = ManchesterOWLSyntax.OBJECT_PROPERTY.toString() + ":";

    private static final String DATA_PROPERTY = ManchesterOWLSyntax.DATA_PROPERTY.toString() + ":";

    private static final String SUB_PROPERTY_OF = ManchesterOWLSyntax.SUB_PROPERTY_OF.toString() + ":";

    private static final String DOMAIN = ManchesterOWLSyntax.DOMAIN.toString();// + ":";

    private static final String RANGE = ManchesterOWLSyntax.RANGE.toString();// + ":";

    private static final String CHARACTERISTICS = ManchesterOWLSyntax.CHARACTERISTICS.toString() + ":";

    private static final String INDIVIDUAL = ManchesterOWLSyntax.INDIVIDUAL.toString() + ":";

    private static final String ANNOTATIONS = ManchesterOWLSyntax.ANNOTATIONS.toString() + ":";

    private static final String TYPES = ManchesterOWLSyntax.TYPES.toString() + ":";

    private static final String FACTS = ManchesterOWLSyntax.FACTS.toString() + ":";

    private static final String SAME_AS = ManchesterOWLSyntax.SAME_AS.toString() + ":";

    private static final String DIFFERENT_FROM = ManchesterOWLSyntax.DIFFERENT_FROM.toString() + ":";

    private static final String VALUE_PARTITION = "ValuePartition:";

    private static final String ONTOLOGY = ManchesterOWLSyntax.ONTOLOGY.toString() + ":";

    private static final String NAMESPACE = ManchesterOWLSyntax.NAMESPACE.toString() + ":";

    private static final String IMPORTS = ":";


    private static final String FUNCTIONAL = ManchesterOWLSyntax.FUNCTIONAL.toString();

    private static final String INVERSE_FUNCTIONAL = ManchesterOWLSyntax.INVERSE_FUNCTIONAL.toString();

    private static final String SYMMETRIC = ManchesterOWLSyntax.SYMMETRIC.toString();

    private static final String ANTI_SYMMETRIC = ManchesterOWLSyntax.ANTI_SYMMETRIC.toString();

    private static final String TRANSITIVE = ManchesterOWLSyntax.TRANSITIVE.toString();

    private static final String REFLEXIVE = ManchesterOWLSyntax.REFLEXIVE.toString();

    private static final String IRREFLEXIVE = ManchesterOWLSyntax.IRREFLEXIVE.toString();

    private static final String INVERSE_OF = ManchesterOWLSyntax.INVERSE_OF + ":";

    private Set<String> potentialKeywords;
    
    private List<ManchesterOWLSyntaxTokenizer.Token> tokens;


    public ManchesterOWLSyntaxParser(OWLDataFactory dataFactory, String s) {
        this.dataFactory = dataFactory;
        skip.add(' ');
        skip.add('\n');
        skip.add('\t');
        delims.add('(');
        delims.add(')');
        delims.add('[');
        delims.add(']');
        delims.add(',');
        delims.add('{');
        delims.add('}');
//        delims.add('"');
        delims.add('^');
        delims.add('@');
        escapeChars.add('\'');
        escapeChars.add('\"');
        potentialKeywords = new HashSet<String>();
        restrictionKeywords = new HashSet<String>();
        restrictionKeywords.add(ManchesterOWLSyntax.SOME.toString());
        restrictionKeywords.add(ManchesterOWLSyntax.ONLY.toString());
        restrictionKeywords.add(ManchesterOWLSyntax.MIN.toString());
        restrictionKeywords.add(ManchesterOWLSyntax.MAX.toString());
        restrictionKeywords.add(ManchesterOWLSyntax.EXACTLY.toString());
        restrictionKeywords.add(ManchesterOWLSyntax.VALUE.toString());
        restrictionKeywords.add(ManchesterOWLSyntax.THAT.toString());


        classNames = new HashSet<String>();
        objectPropertyNames = new HashSet<String>();
        dataPropertyNames = new HashSet<String>();
        individualNames = new HashSet<String>();
        dataTypeNames = new HashSet<String>();
        annotationURIs = new HashSet<String>();
        namespaceMap = new HashMap<String, URI>();
        NamespaceUtil u = new NamespaceUtil();

        for (URI uri : OWLRDFVocabulary.BUILT_IN_ANNOTATION_PROPERTIES) {
            String[] res = u.split(uri.toString(), null);
            annotationURIs.add(u.getPrefix(res[0]) + ":" + res[1]);
        }
        pos = 0;
        buffer = s;
        base = "http://www.semanticweb.org#";
        owlEntityChecker = new DefaultEntityChecker();
        
        tokens = new ArrayList<ManchesterOWLSyntaxTokenizer.Token>();
        tokens.addAll(getTokenizer(s).tokenize());
        tokenIndex = 0;
    }


    public String getBase() {
        return base;
    }


    public void setBase(String base) {
        this.base = base;
    }


    public OWLEntityChecker getOWLEntityChecker() {
        return owlEntityChecker;
    }


    public void setOWLEntityChecker(OWLEntityChecker owlEntityChecker) {
        this.owlEntityChecker = owlEntityChecker;
    }


    public boolean isClassName(String name) {
        if (classNames.contains(name)) {
            return true;
        }
        return owlEntityChecker != null && owlEntityChecker.getOWLClass(name) != null;
    }


    public boolean isObjectPropertyName(String name) {
        if (objectPropertyNames.contains(name)) {
            return true;
        }
        return owlEntityChecker != null && owlEntityChecker.getOWLObjectProperty(name) != null;
    }


    public boolean isAnnotationURI(String name) {
        return annotationURIs.contains(name);
    }


    public boolean isDataPropertyName(String name) {
        if (dataPropertyNames.contains(name)) {
            return true;
        }
        return owlEntityChecker != null && owlEntityChecker.getOWLDataProperty(name) != null;
    }


    public boolean isIndividualName(String name) {
        if (individualNames.contains(name)) {
            return true;
        }
        return owlEntityChecker != null && owlEntityChecker.getOWLIndividual(name) != null;
    }


    public boolean isDatatypeName(String name) {
        if (dataTypeNames.contains(name)) {
            return true;
        }
        return owlEntityChecker != null && owlEntityChecker.getOWLDataType(name) != null;
    }


    public OWLClass getOWLClass(String name) {
        return owlEntityChecker.getOWLClass(name);
    }


    public OWLObjectProperty getOWLObjectProperty(String name) {
        return owlEntityChecker.getOWLObjectProperty(name);
    }


    public OWLIndividual getOWLIndividual(String name) {
        return owlEntityChecker.getOWLIndividual(name);
    }


    public OWLDataProperty getOWLDataProperty(String name) {
        return owlEntityChecker.getOWLDataProperty(name);
    }


    public OWLDataType getDataType(String name) {
        return dataFactory.getOWLDataType(URI.create(Namespaces.XSD + name));
    }


    public URI getAnnotationURI(String name) {
        if (name.startsWith("rdfs:")) {
            return URI.create(Namespaces.RDFS + name.substring(5, name.length()));
        }
        else if (name.startsWith("owl:")) {
            return URI.create(Namespaces.RDFS + name.substring(4, name.length()));
        }
        return URI.create(base + name);
    }


    private String getLastToken() {
        return token;
    }


    private String peekToken() {
    	return getToken().getToken();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  Tokenizer
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////


    private String readToken() {
        if (bufferedToken != null) {
            token = bufferedToken;
            bufferedToken = null;
            return token;
        }

        // Skip any skip characters
        while (pos < buffer.length()) {
            char ch = buffer.charAt(pos);
            if (skip.contains(ch)) {
                if (ch == '\n') {
                    line++;
                    col = 0;
                }
            }
            else {
                break;
            }
            col++;
            pos++;
        }

        tokenStartPos = pos;
        tokenStartCol = col;

        if (pos >= buffer.length()) {
            // Past end of buffer
            token = EOF;
        }
        else if (pos+1 == buffer.length()  && escapeChars.contains(buffer.charAt(pos))){
            // single escape character
            token = EOF;
        }
        else if (delims.contains(buffer.charAt(pos))) {
            // Single delim
            pos++;
            col++;
            token = buffer.substring(pos - 1, pos);
        }
        else {
            // Some other token
            int start = pos;
            boolean inEscapedString = false;
            while (pos < buffer.length()) {
                char ch = buffer.charAt(pos);
                if (escapeChars.contains(ch) && !inEscapedString) {
                    inEscapedString = true;
                    start++;
                }
                else {
                    if (escapeChars.contains(ch) && inEscapedString) {
                        if (ch == '\'') {
                            token = buffer.substring(start, pos);
                        }
                        else {
                            token = buffer.substring(start - 1, pos + 1);
                        }
                        inEscapedString = false;
                        pos++;
                        return token;
                    }
                    if (!inEscapedString && (skip.contains(ch) || delims.contains(ch))) {
                        token = buffer.substring(start, pos);
                        return token;
                    }
                }
                col++;
                pos++;
            }
            if (start != pos) {
                token = buffer.substring(start, pos);
            }
        }
        return token;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  Parser
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////


    /**
     * Parses an OWL description that is represented in Manchester OWL Syntax
     * @return The parsed description
     * @throws ParserException If an description could not be parsed.
     */
    public OWLDescription parseDescription() throws ParserException {
        OWLDescription desc = parseIntersection();
        if (!consumeToken().equals(EOF)) {
            throwException(EOF);
        }
        return desc;
    }


    public OWLDescription parseIntersection() throws ParserException {
        Set<OWLDescription> ops = new HashSet<OWLDescription>();
        String kw = AND;
        while (kw.equalsIgnoreCase(AND)) {
            potentialKeywords.remove(AND);
            ops.add(parseUnion());
            potentialKeywords.add(AND);
            kw = peekToken();
            if (kw.equalsIgnoreCase(AND)) {
                kw = consumeToken();
            }
            else if (kw.equalsIgnoreCase("that")) {
                consumeToken();
                kw = AND;
            }
        }
        if (ops.size() == 1) {
            return ops.iterator().next();
        }
        else {
            return dataFactory.getOWLObjectIntersectionOf(ops);
        }
    }


    public OWLDescription parseUnion() throws ParserException {
        Set<OWLDescription> ops = new HashSet<OWLDescription>();
        String kw = OR;
        while (kw.equalsIgnoreCase(OR)) {
            potentialKeywords.remove(OR);
            ops.add(parseNonNaryDescription());
            potentialKeywords.add(OR);
            kw = peekToken();
            if (kw.equalsIgnoreCase(OR)) {
                kw = consumeToken();
            }
        }
        if (ops.size() == 1) {
            return ops.iterator().next();
        }
        else {
            return dataFactory.getOWLObjectUnionOf(ops);
        }
    }


    public OWLObjectPropertyExpression parseObjectPropertyExpression(boolean allowUndeclared) throws ParserException {
        String tok = consumeToken();
        if (tok.equalsIgnoreCase(INV)) {
            String open = consumeToken();
            if (!open.equals("(")) {
                throwException("(");
            }
            OWLObjectPropertyExpression prop = parseObjectPropertyExpression();
            String close = consumeToken();
            if (!close.equals(")")) {
                throwException(")");
            }
            return dataFactory.getOWLObjectPropertyInverse(prop);
        }
        else {
            if (!allowUndeclared && !isObjectPropertyName(tok)) {
                throwException(false, true, false, false, false, INV);
            }
            return getOWLObjectProperty(tok);
        }
    }


    public OWLObjectPropertyExpression parseObjectPropertyExpression() throws ParserException {
        return parseObjectPropertyExpression(false);
    }


    public OWLDescription parseRestriction() throws ParserException {
        String tok = peekToken();
        if (isObjectPropertyName(tok) || tok.equalsIgnoreCase(INV)) {
            return parseObjectRestriction();
        }
        else if (isDataPropertyName(tok)) {
            return parseDataRestriction();
        }
        else {
            consumeToken();
            throwException(false, true, true, false);
        }
        return null;
    }


    /**
     * Parses all class descriptions except ObjectIntersectionOf
     * and ObjectUnionOf
     * @return The description which was parsed
     * @throws ParserException if a non-nary description could not be parsed
     */
    public OWLDescription parseNonNaryDescription() throws ParserException {

        String tok = peekToken();
        if (tok.equalsIgnoreCase(NOT)) {
            consumeToken();
            OWLDescription complemented = parseNestedClassExpression();
            return dataFactory.getOWLObjectComplementOf(complemented);
        }
        else if (isObjectPropertyName(tok) || tok.equalsIgnoreCase(INV)) {
            return parseObjectRestriction();
        }
        else if (isDataPropertyName(tok)) {
            // Data restriction
            return parseDataRestriction();
        }
        else if (tok.equals("{")) {
            return parseObjectOneOf();
        }
        else if (tok.equals("(")) {
            return parseNestedClassExpression();
        }
        else if (isClassName(tok)) {
            consumeToken();
            OWLDescription desc = getOWLClass(tok);
//            if (peekToken().equalsIgnoreCase("that")) {
//                consumeToken();
//                OWLDescription rest = parseRestriction();
//                desc = dataFactory.getOWLObjectIntersectionOf(CollectionFactory.createSet(desc, rest));
//            }
            return desc;
        }
        // Add option for strict class name checking
        else {
            consumeToken();
            throwException(true, true, true, false, false, "(", "{", NOT, INV);
        }
        return null;
    }


    private OWLDescription parseObjectRestriction() throws ParserException {
        OWLObjectPropertyExpression prop = parseObjectPropertyExpression();
        String kw = consumeToken();
        if (kw.equalsIgnoreCase(SOME)) {
            String possSelfToken = peekToken();
            if (possSelfToken.equalsIgnoreCase(SELF)) {
                consumeToken();
                return dataFactory.getOWLObjectSelfRestriction(prop);
            }
            else {
                OWLDescription filler = null;
                try {
                    filler = parseNestedClassExpression();
                }
                catch (ParserException e) {
                    Set<String> keywords = new HashSet<String>();
                    keywords.addAll(e.getExpectedKeywords());
                    keywords.add(SELF);
                    throwException(e.isClassNameExpected(),
                                   e.isObjectPropertyNameExpected(),
                                   e.isDataPropertyNameExpected(),
                                   e.isIndividualNameExpected(),
                                   e.isDatatypeNameExpected(),
                                   keywords.toArray(new String[keywords.size()]));
                }
                return dataFactory.getOWLObjectSomeRestriction(prop, filler);
            }
        }
        else if (kw.equalsIgnoreCase(ONLY)) {
            OWLDescription filler = parseNestedClassExpression();
            return dataFactory.getOWLObjectAllRestriction(prop, filler);
        }
        else if (kw.equalsIgnoreCase(VALUE)) {
            String indName = consumeToken();
            if (!isIndividualName(indName)) {
                throwException(false, false, false, true);
            }
            return dataFactory.getOWLObjectValueRestriction(prop, getOWLIndividual(indName));
        }
        else if (kw.equalsIgnoreCase(MIN)) {
            int card = parseInteger();
            OWLDescription filler = parseNestedClassExpression();
            return dataFactory.getOWLObjectMinCardinalityRestriction(prop, card, filler);
        }
        else if (kw.equalsIgnoreCase(MAX)) {
            int card = parseInteger();
            OWLDescription filler = parseNestedClassExpression();
            return dataFactory.getOWLObjectMaxCardinalityRestriction(prop, card, filler);
        }
        else if (kw.equalsIgnoreCase(EXACTLY)) {
            int card = parseInteger();
            OWLDescription filler = parseNestedClassExpression();
            return dataFactory.getOWLObjectExactCardinalityRestriction(prop, card, filler);
        }
        else if (kw.equalsIgnoreCase(ONLYSOME)) {
            String tok = peekToken();
            Set<OWLDescription> descs = new HashSet<OWLDescription>();
            if (!tok.equals("[")) {
                descs.add(parseIntersection());
            }
            else {
                descs.addAll(parseDescriptionList("[", "]"));
            }
            Set<OWLDescription> ops = new HashSet<OWLDescription>();
            for (OWLDescription desc : descs) {
                ops.add(dataFactory.getOWLObjectSomeRestriction(prop, desc));
            }
            OWLDescription filler;
            if (descs.size() == 1) {
                filler = descs.iterator().next();
            }
            else {
                filler = dataFactory.getOWLObjectUnionOf(descs);
            }
            ops.add(dataFactory.getOWLObjectAllRestriction(prop, filler));
            return dataFactory.getOWLObjectIntersectionOf(ops);
        }
        else {
            // Error!
            throwException(SOME, ONLY, VALUE, MIN, MAX, EXACTLY);
        }
        return null;
    }


    public OWLDescription parseDataRestriction() throws ParserException {
        OWLDataPropertyExpression prop = parseDataProperty();
        String kw = consumeToken();
        if (kw.equalsIgnoreCase(SOME)) {
            OWLDataRange rng = parseDataRange();
            return dataFactory.getOWLDataSomeRestriction(prop, rng);
        }
        else if (kw.equalsIgnoreCase(ONLY)) {
            OWLDataRange rng = parseDataRange();
            return dataFactory.getOWLDataAllRestriction(prop, rng);
        }
        else if (kw.equalsIgnoreCase(VALUE)) {
            OWLConstant con = parseConstant();
            return dataFactory.getOWLDataValueRestriction(prop, con);
        }
        else if (kw.equalsIgnoreCase(MIN)) {
            int card = parseInteger();
            OWLDataRange rng = parseDataRange();
            return dataFactory.getOWLDataMinCardinalityRestriction(prop, card, rng);
        }
        else if (kw.equalsIgnoreCase(EXACTLY)) {
            int card = parseInteger();
            OWLDataRange rng = parseDataRange();
            return dataFactory.getOWLDataExactCardinalityRestriction(prop, card, rng);
        }
        else if (kw.equalsIgnoreCase(MAX)) {
            int card = parseInteger();
            OWLDataRange rng = parseDataRange();
            return dataFactory.getOWLDataMaxCardinalityRestriction(prop, card, rng);
        }
        throwException(SOME, ONLY, VALUE, MIN, EXACTLY, MAX);
        return null;
    }


    public OWLDataRange parseDataRange() throws ParserException {
        String tok = peekToken();
        if (isDatatypeName(tok)) {
            consumeToken();
            OWLDataType dataType = getDataType(tok);
            String next = peekToken();
            if (next.equals("[")) {
                // Restricted data range
                consumeToken();
                String sep = ",";
                Set<OWLDataRangeFacetRestriction> facetRestrictions = new HashSet<OWLDataRangeFacetRestriction>();
                while (sep.equals(",")) {
                    String facet = consumeToken();
                    OWLRestrictedDataRangeFacetVocabulary fv = OWLRestrictedDataRangeFacetVocabulary.getFacetBySymbolicName(
                            facet);
                    if (fv == null) {
                        throwException(OWLRestrictedDataRangeFacetVocabulary.getFacets().toArray(new String[OWLRestrictedDataRangeFacetVocabulary.getFacetURIs().size()]));
                    }
                    OWLConstant con = parseConstant();
                    facetRestrictions.add(dataFactory.getOWLDataRangeFacetRestriction(fv, con.asOWLTypedConstant()));
                    sep = consumeToken();
                }
                if (!sep.equals("]")) {
                    throwException("]");
                }
                return dataFactory.getOWLDataRangeRestriction(dataType, facetRestrictions);
            }
            else {
                return dataType;
            }
        }
        else if (tok.equalsIgnoreCase(NOT)) {
            return parseDataComplementOf();
        }
        else if (tok.equals("{")) {
            return parseDataOneOf();
        }
        else {
            consumeToken();
            throwException(false, false, false, false, true, NOT, "{");
        }
        return null;
    }


    private Set<OWLDataRange> parseDataRangeList() throws ParserException {
        String sep = ",";
        Set<OWLDataRange> ranges = new HashSet<OWLDataRange>();
        while (sep.equals(",")) {
            potentialKeywords.remove(",");
            OWLDataRange rng = parseDataRange();
            ranges.add(rng);
            potentialKeywords.add(",");
            sep = peekToken();
            if (sep.equals(",")) {
                consumeToken();
            }
        }
        return ranges;
    }


    private OWLDataRange parseDataOneOf() throws ParserException {
        consumeToken();
        Set<OWLConstant> cons = new HashSet<OWLConstant>();
        String sep = ",";
        while (sep.equals(",")) {
            OWLConstant con = parseConstant();
            cons.add(con);
            sep = consumeToken();
        }
        if (!sep.equals("}")) {
            throwException(",", "}");
        }
        return dataFactory.getOWLDataOneOf(cons);
    }


    private OWLDataRange parseDataComplementOf() throws ParserException {
        consumeToken();
        String open = consumeToken();
        if (!open.equals("(")) {
            throwException("(");
        }
        OWLDataRange complementedDataRange = parseDataRange();
        String close = consumeToken();
        if (!close.equals(")")) {
            throwException(")");
        }
        return dataFactory.getOWLDataComplementOf(complementedDataRange);
    }


    public OWLConstant parseConstant() throws ParserException {
        String tok = consumeToken();
        if (tok.startsWith("\"")) {
            String lit = tok.substring(1, tok.length() - 1);
            if (peekToken().equals("^")) {
                consumeToken();
                String type = consumeToken();
                String dataType = consumeToken();
                return dataFactory.getOWLTypedConstant(lit, getDataType(dataType));
            }
            else if (peekToken().equals("@")) {
                consumeToken();
                String lang = consumeToken();
                return dataFactory.getOWLUntypedConstant(lit, lang);
            }
            else {
                return dataFactory.getOWLUntypedConstant(lit);
            }
        }
        else {
            try {
                Integer.parseInt(tok);
                return dataFactory.getOWLTypedConstant(tok, dataFactory.getOWLDataType(XSDVocabulary.INT.getURI()));
            }
            catch (NumberFormatException e) {
                // Ignore - not interested
            }
            try {
                Double.parseDouble(tok);
                return dataFactory.getOWLTypedConstant(tok, dataFactory.getOWLDataType(XSDVocabulary.DOUBLE.getURI()));
            }
            catch (NumberFormatException e) {
                // Ignore - not interested
            }
            try {
                Float.parseFloat(tok);
                return dataFactory.getOWLTypedConstant(tok, dataFactory.getOWLDataType(XSDVocabulary.FLOAT.getURI()));
            }
            catch (NumberFormatException e) {
                // Ignore - not interested
            }
        }
        throwException(false,
                       false,
                       false,
                       false,
                       false,
                       "\"<Literal>\"",
                       "\"<Literal>\"^^<datatype>",
                       "\"<Literal>\"@<lang>");
        return null;
    }


    public int parseInteger() throws ParserException {
        String i = consumeToken();
        try {
            return Integer.parseInt(i);
        }
        catch (NumberFormatException e) {
            throw new ParserException(token, tokenStartPos, line, true, tokenStartCol);
        }
    }


    public String getLineCol() {
        return "Encountered " + getLastToken() + " at " + line + ":" + col + " ";
    }


    private OWLDescription parseNestedClassExpression() throws ParserException {
        String tok = peekToken();
        if (tok.equals("(")) {
            consumeToken();
            OWLDescription desc = parseIntersection();
            String closeBracket = consumeToken();
            if (!closeBracket.equals(")")) {
                // Error!
                throwException(")");
            }
            return desc;
        }
        else if (tok.equals("{")) {
            return parseObjectOneOf();
        }
        else if (isClassName(tok)) {
            String name = consumeToken();
            return getOWLClass(name);
        }
        else {
            consumeToken();
            throwException(true, false, false, false, false, "(", "{");
        }
        return null;
    }


    public OWLDescription parseObjectOneOf() throws ParserException {
        String open = consumeToken();
        if (!open.equals("{")) {
            throwException("{");
        }
        String sep = ",";
        Set<OWLIndividual> inds = new HashSet<OWLIndividual>();
        while (sep.equals(",")) {
            OWLIndividual ind = parseIndividual();
            inds.add(ind);
            sep = peekToken();
            if (sep.equals(",")) {
                consumeToken();
            }
        }
        String close = consumeToken();
        if (!close.equals("}")) {
            throwException("}", ",");
        }
        return dataFactory.getOWLObjectOneOf(inds);
    }


    public Set<OWLAxiom> parseFrames() throws ParserException {
        Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
        while (true) {
            String tok = peekToken();
            if (tok.equalsIgnoreCase(CLASS)) {
                axioms.addAll(parseClassFrame());
            }
            else if (tok.equalsIgnoreCase(OBJECT_PROPERTY)) {
                axioms.addAll(parseObjectPropertyFrame());
            }
            else if (tok.equalsIgnoreCase(DATA_PROPERTY)) {
                axioms.addAll(parseDataPropertyFrame());
            }
            else if (tok.equalsIgnoreCase(INDIVIDUAL)) {
                axioms.addAll(parseIndividualFrame());
            }
            else if (tok.equalsIgnoreCase(VALUE_PARTITION)) {
                parseValuePartitionFrame();
            }
            else {
                if (tok.equals(EOF)) {
                    break;
                }
                else {
                    throwException(CLASS, OBJECT_PROPERTY, DATA_PROPERTY, INDIVIDUAL, VALUE_PARTITION);
                }
            }
        }
        return axioms;
    }

    public Set<OWLAnnotation> parseAnnotations() throws ParserException {
        Set<OWLAnnotation> annos = new HashSet<OWLAnnotation>();
        String header = consumeToken();
        if (!header.equals(ANNOTATIONS)) {
            throwException(ANNOTATIONS);
        }
        String sep = ",";
        while (sep.equals(",")) {
            String prop = consumeToken();
            if (!isAnnotationURI(prop)) {
                throwException(annotationURIs.toArray(new String[annotationURIs.size()]));
            }
            String obj = peekToken();
            if (isIndividualName(obj)) {
                OWLIndividual ind = parseIndividual();
                OWLAnnotation anno = dataFactory.getOWLObjectAnnotation(getAnnotationURI(prop), ind);
                annos.add(anno);
            }
            else {
                OWLConstant con = parseConstant();
                OWLAnnotation anno = dataFactory.getOWLConstantAnnotation(getAnnotationURI(prop), con);
                annos.add(anno);
            }
            sep = peekToken();
            if (sep.equals(",")) {
                consumeToken();
            }
        }
        return annos;
    }

    public Set<OWLAxiom> parseAnnotations(OWLEntity subject) throws ParserException {
        Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
        for(OWLAnnotation anno : parseAnnotations()) {
            axioms.add(dataFactory.getOWLEntityAnnotationAxiom(subject, anno));
        }
        return axioms;
    }



    public Set<OWLAxiom> parseClassFrame() throws ParserException {
        return parseClassFrame(false);
    }


    public Set<OWLAxiom> parseClassFrameEOF() throws ParserException {
        return parseClassFrame(true);
    }


    private Set<OWLAxiom> parseClassFrame(boolean eof) throws ParserException {
        String tok = consumeToken();
        Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
        if (!tok.equalsIgnoreCase(CLASS)) {
            throwException(CLASS);
        }
        String subj = consumeToken();
        if (!isClassName(subj)) {
            throwException(true, false, false, false);
        }
        OWLClass cls = getOWLClass(subj);
        while (true) {
            String sect = peekToken();
            if (sect.equalsIgnoreCase(SUB_CLASS_OF)) {
                potentialKeywords.clear();
                consumeToken();
                Set<OWLDescription> descs = parseDescriptionList();
                for (OWLDescription desc : descs) {
                    axioms.add(dataFactory.getOWLSubClassAxiom(cls, desc));
                }
            }
            else if (sect.equalsIgnoreCase(EQUIVALENT_TO)) {
                potentialKeywords.clear();
                consumeToken();
                Set<OWLDescription> descs = parseDescriptionList();
                for (OWLDescription desc : descs) {
                    axioms.add(dataFactory.getOWLEquivalentClassesAxiom(CollectionFactory.createSet(cls, desc)));
                }
            }
            else if (sect.equalsIgnoreCase(DISJOINT_WITH)) {
                potentialKeywords.clear();
                consumeToken();
                Set<OWLDescription> descs = parseDescriptionList();
                for (OWLDescription desc : descs) {
                    axioms.add(dataFactory.getOWLDisjointClassesAxiom(cls, desc));
                }
            }
            else if (sect.equals(ANNOTATIONS)) {
                potentialKeywords.clear();
                axioms.addAll(parseAnnotations(cls));
            }
            else {
                // If force EOF then we need EOF or else everything is o.k.
                if (eof && !sect.equals(EOF)) {
                    throwException(SUB_CLASS_OF, EQUIVALENT_TO, DISJOINT_WITH, ANNOTATIONS);
                }
                else {
                    break;
                }
            }
        }
        return axioms;
    }


    public Set<OWLAxiom> parseObjectPropertyFrame() throws ParserException {
        Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
        OWLObjectPropertyExpression prop = null;
        String tok = consumeToken();
        if (!tok.equalsIgnoreCase(OBJECT_PROPERTY)) {
            throwException(OBJECT_PROPERTY);
        }
        String subj = peekToken();
        objectPropertyNames.add(subj);
        prop = parseObjectPropertyExpression();
        while (true) {
            String sect = peekToken();
            if (sect.equalsIgnoreCase(SUB_PROPERTY_OF)) {
                potentialKeywords.clear();
                consumeToken();
                Set<OWLObjectPropertyExpression> props = parseObjectPropertyList();
                for (OWLObjectPropertyExpression pe : props) {
                    axioms.add(dataFactory.getOWLSubObjectPropertyAxiom(prop, pe));
                }
            }
            else if (sect.equalsIgnoreCase(EQUIVALENT_TO)) {
                potentialKeywords.clear();
                consumeToken();
                Set<OWLObjectPropertyExpression> props = parseObjectPropertyList();
                for (OWLObjectPropertyExpression pe : props) {
                    axioms.add(dataFactory.getOWLEquivalentObjectPropertiesAxiom(CollectionFactory.createSet(prop,
                                                                                                             pe)));
                }
            }
            else if (sect.equalsIgnoreCase(DISJOINT_WITH)) {
                potentialKeywords.clear();
                consumeToken();
                Set<OWLObjectPropertyExpression> props = parseObjectPropertyList();
                for (OWLObjectPropertyExpression pe : props) {
                    axioms.add(dataFactory.getOWLDisjointObjectPropertiesAxiom(CollectionFactory.createSet(prop, pe)));
                }
            }
            else if (sect.equalsIgnoreCase(DOMAIN)) {
                potentialKeywords.clear();
                consumeToken();
                Set<OWLDescription> domains = parseDescriptionList();
                for (OWLDescription dom : domains) {
                    axioms.add(dataFactory.getOWLObjectPropertyDomainAxiom(prop, dom));
                }
            }
            else if (sect.equalsIgnoreCase(RANGE)) {
                potentialKeywords.clear();
                consumeToken();
                Set<OWLDescription> ranges = parseDescriptionList();
                for (OWLDescription rng : ranges) {
                    axioms.add(dataFactory.getOWLObjectPropertyRangeAxiom(prop, rng));
                }
            }
            else if (sect.equalsIgnoreCase(INVERSE_OF)) {
                potentialKeywords.clear();
                consumeToken();
                Set<OWLObjectPropertyExpression> inverses = parseObjectPropertyList();
                for (OWLObjectPropertyExpression inv : inverses) {
                    axioms.add(dataFactory.getOWLInverseObjectPropertiesAxiom(prop, inv));
                }
            }
            else if (sect.equalsIgnoreCase(CHARACTERISTICS)) {
                potentialKeywords.clear();
                consumeToken();
                axioms.addAll(parseObjectPropertyCharacteristicList(prop));
            }
            else if (sect.equalsIgnoreCase(ANNOTATIONS)) {
                potentialKeywords.clear();
                axioms.addAll(parseAnnotations(prop.asOWLObjectProperty()));
            }
            else {
                break;
            }
        }
        return axioms;
    }


    public Set<OWLAxiom> parseDataPropertyFrame() throws ParserException {
        Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
        String tok = consumeToken();
        if (!tok.equalsIgnoreCase(DATA_PROPERTY)) {
            throwException(DATA_PROPERTY);
        }
        String subj = consumeToken();
        dataPropertyNames.add(subj);
        OWLDataProperty prop = getOWLDataProperty(subj);
        while (true) {
            String sect = peekToken();
            if (sect.equalsIgnoreCase(SUB_PROPERTY_OF)) {
                potentialKeywords.clear();
                consumeToken();
                Set<OWLDataProperty> props = parseDataPropertyList();
                for (OWLDataProperty pe : props) {
                    axioms.add(dataFactory.getOWLSubDataPropertyAxiom(prop, pe));
                }
            }
            else if (sect.equalsIgnoreCase(EQUIVALENT_TO)) {
                potentialKeywords.clear();
                consumeToken();
                Set<OWLDataProperty> props = parseDataPropertyList();
                for (OWLDataProperty pe : props) {
                    axioms.add(dataFactory.getOWLEquivalentDataPropertiesAxiom(CollectionFactory.createSet(prop, pe)));
                }
            }
            else if (sect.equalsIgnoreCase(DISJOINT_WITH)) {
                potentialKeywords.clear();
                consumeToken();
                Set<OWLDataProperty> props = parseDataPropertyList();
                for (OWLDataProperty pe : props) {
                    axioms.add(dataFactory.getOWLDisjointDataPropertiesAxiom(CollectionFactory.createSet(prop, pe)));
                }
            }
            else if (sect.equalsIgnoreCase(DOMAIN)) {
                potentialKeywords.clear();
                consumeToken();
                Set<OWLDescription> domains = parseDescriptionList();
                for (OWLDescription dom : domains) {
                    axioms.add(dataFactory.getOWLDataPropertyDomainAxiom(prop, dom));
                }
            }
            else if (sect.equalsIgnoreCase(RANGE)) {
                potentialKeywords.clear();
                consumeToken();
                Set<OWLDataRange> ranges = parseDataRangeList();
                for (OWLDataRange rng : ranges) {
                    axioms.add(dataFactory.getOWLDataPropertyRangeAxiom(prop, rng));
                }
            }
            else if (sect.equalsIgnoreCase(CHARACTERISTICS)) {
                potentialKeywords.clear();
                String characteristic = consumeToken();
                if (!characteristic.equals(FUNCTIONAL)) {
                    throwException(FUNCTIONAL);
                }
                axioms.add(dataFactory.getOWLFunctionalDataPropertyAxiom(prop));
            }
            else if (sect.equalsIgnoreCase(ANNOTATIONS)) {
                potentialKeywords.clear();
                axioms.addAll(parseAnnotations(prop));
            }
            else {
                break;
            }
        }
        return axioms;
    }


    public Set<OWLAxiom> parseIndividualFrame() throws ParserException {
        String tok = consumeToken();
        Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
        if (!tok.equalsIgnoreCase(INDIVIDUAL)) {
            throwException(INDIVIDUAL);
        }
        String subj = consumeToken();
        OWLIndividual ind = getOWLIndividual(subj);
        while (true) {
            String sect = peekToken();
            if (sect.equalsIgnoreCase(TYPES)) {
                potentialKeywords.clear();
                consumeToken();
                Set<OWLDescription> descs = parseDescriptionList();
                for (OWLDescription desc : descs) {
                    axioms.add(dataFactory.getOWLClassAssertionAxiom(ind, desc));
                }
            }
            else if (sect.equalsIgnoreCase(FACTS)) {
                potentialKeywords.clear();
                consumeToken();
                String sep = ",";
                while (sep.equals(",")) {
                    String prop = peekToken();
                    if (isDataPropertyName(prop)) {
                        OWLDataProperty p = parseDataProperty();
                        OWLConstant con = parseConstant();
                        axioms.add(dataFactory.getOWLDataPropertyAssertionAxiom(ind, p, con));
                    }
                    else if (isObjectPropertyName(prop)) {
                        OWLObjectPropertyExpression p = parseObjectPropertyExpression();
                        OWLIndividual obj = parseIndividual();
                        axioms.add(dataFactory.getOWLObjectPropertyAssertionAxiom(ind, p, obj));
                    }
                    else if (isAnnotationURI(prop)) {
                        URI annotationURI = getAnnotationURI(prop);
                        // Object could be an individual or literal
                        String object = peekToken();
                        OWLAnnotation annotation;
                        if (individualNames.contains(object)) {
                            annotation = dataFactory.getOWLObjectAnnotation(annotationURI, getOWLIndividual(object));
                        }
                        else {
                            // Assume constant
                            OWLConstant con = null;
                            try {
                                con = parseConstant();
                            }
                            catch (ParserException e) {
                                throwException(e.isClassNameExpected(),
                                               e.isObjectPropertyNameExpected(),
                                               e.isDataPropertyNameExpected(),
                                               true,
                                               e.isDatatypeNameExpected(),
                                               e.getExpectedKeywords().toArray(new String[e.getExpectedKeywords().size()]));
                            }
                            annotation = dataFactory.getOWLConstantAnnotation(annotationURI, con);
                        }
                        axioms.add(dataFactory.getOWLEntityAnnotationAxiom(ind, annotation));
                    }
                    else {
                        throwException(false, true, true, false, false, ",");
                    }
                    sep = peekToken();
                    if (sep.equals(",")) {
                        consumeToken();
                    }
                }
            }
            else if (sect.equalsIgnoreCase(SAME_AS)) {
                potentialKeywords.clear();
                consumeToken();
                Set<OWLIndividual> inds = parseIndividualList();
                inds.add(ind);
                axioms.add(dataFactory.getOWLSameIndividualsAxiom(inds));
            }
            else if (sect.equalsIgnoreCase(DIFFERENT_FROM)) {
                potentialKeywords.clear();
                consumeToken();
                Set<OWLIndividual> inds = parseIndividualList();
                inds.add(ind);
                axioms.add(dataFactory.getOWLDifferentIndividualsAxiom(inds));
            }
            else if (sect.equalsIgnoreCase(ANNOTATIONS)) {
                potentialKeywords.clear();
                axioms.addAll(parseAnnotations(ind));
            }
            else {
//                // If force EOF then we need EOF or else everything is o.k.
//                if (eof && !sect.equals(EOF)) {
//                    throwException(SUB_CLASS_OF, EQUIVALENT_TO, DISJOINT_WITH);
//                }
//                else {
                break;
//                }
            }
        }
        return axioms;
    }


    public Set<OWLAxiom> parseValuePartitionFrame() throws ParserException {
        String section = consumeToken();
        if (!section.equalsIgnoreCase(VALUE_PARTITION)) {
            throwException(VALUE_PARTITION);
        }
        String name = consumeToken();
        if (name.equals(EOF)) {
            throwException(false, true, false, false, false);
        }
        OWLObjectProperty prop = dataFactory.getOWLObjectProperty(getURI(name));
        String clsName = consumeToken();
        if (clsName.equals(EOF)) {
            throwException(false, true, false, false, false);
        }
        OWLClass cls = getOWLClass(clsName);
        Set<OWLDescription> values = parseDescriptionList("[", "]");

        Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
        for (OWLDescription val : values) {
            axioms.add(dataFactory.getOWLSubClassAxiom(val, cls));
        }
        axioms.add(dataFactory.getOWLSubClassAxiom(cls, dataFactory.getOWLObjectUnionOf(values)));
        axioms.add(dataFactory.getOWLFunctionalObjectPropertyAxiom(prop));
        axioms.add(dataFactory.getOWLObjectPropertyRangeAxiom(prop, cls));
        return axioms;
    }


    public Set<OWLAxiom> parseObjectPropertyCharacteristicList(OWLObjectPropertyExpression prop) throws
            ParserException {
        Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
        String sep = ",";
        while (sep.equals(",")) {
            String characteristic = consumeToken();
            if (characteristic.equalsIgnoreCase(FUNCTIONAL)) {
                axioms.add(dataFactory.getOWLFunctionalObjectPropertyAxiom(prop));
            }
            else if (characteristic.equalsIgnoreCase(INVERSE_FUNCTIONAL)) {
                axioms.add(dataFactory.getOWLInverseFunctionalObjectPropertyAxiom(prop));
            }
            else if (characteristic.equalsIgnoreCase(SYMMETRIC)) {
                axioms.add(dataFactory.getOWLSymmetricObjectPropertyAxiom(prop));
            }
            else if (characteristic.equalsIgnoreCase(ANTI_SYMMETRIC)) {
                axioms.add(dataFactory.getOWLAntiSymmetricObjectPropertyAxiom(prop));
            }
            else if (characteristic.equalsIgnoreCase(TRANSITIVE)) {
                axioms.add(dataFactory.getOWLTransitiveObjectPropertyAxiom(prop));
            }
            else if (characteristic.equalsIgnoreCase(REFLEXIVE)) {
                axioms.add(dataFactory.getOWLReflexiveObjectPropertyAxiom(prop));
            }
            else if (characteristic.equalsIgnoreCase(IRREFLEXIVE)) {
                axioms.add(dataFactory.getOWLIrreflexiveObjectPropertyAxiom(prop));
            }
            else {
                throwException(FUNCTIONAL,
                               INVERSE_FUNCTIONAL,
                               SYMMETRIC,
                               ANTI_SYMMETRIC,
                               TRANSITIVE,
                               REFLEXIVE,
                               IRREFLEXIVE);
            }
            sep = peekToken();
            if (sep.equals(",")) {
                sep = consumeToken();
            }
        }
        return axioms;
    }


    public Set<OWLDescription> parseDescriptionList() throws ParserException {
        Set<OWLDescription> descs = new HashSet<OWLDescription>();
        String sep = ",";
        while (sep.equals(",")) {
            potentialKeywords.remove(",");
            descs.add(parseIntersection());
            potentialKeywords.add(",");
            sep = peekToken();
            if (sep.equals(",")) {
                sep = consumeToken();
            }
        }
        return descs;
    }


    public Set<OWLDescription> parseDescriptionList(String expectedOpen, String expectedClose) throws ParserException {
        String open = consumeToken();
        Set<OWLDescription> descs = new HashSet<OWLDescription>();
        if (!open.equals(expectedOpen)) {
            throwException(expectedOpen);
        }
        String sep = ",";
        while (sep.equals(",")) {
            potentialKeywords.remove(",");
            OWLDescription desc = parseIntersection();
            potentialKeywords.add(",");
            descs.add(desc);
            sep = peekToken();
            if (sep.equals(",")) {
                sep = consumeToken();
            }
        }
        String close = consumeToken();
        if (!close.equals(expectedClose)) {
            throwException(expectedClose);
        }
        return descs;
    }


    public Set<OWLDataProperty> parseDataPropertyList() throws ParserException {
        Set<OWLDataProperty> props = new HashSet<OWLDataProperty>();
        String sep = ",";
        while (sep.equals(",")) {
            sep = peekToken();
            OWLDataProperty prop = parseDataProperty();
            props.add(prop);
            if (sep.equals(",")) {
                consumeToken();
            }
        }
        return props;
    }


    public Set<OWLObjectPropertyExpression> parseObjectPropertyList() throws ParserException {
        Set<OWLObjectPropertyExpression> props = new HashSet<OWLObjectPropertyExpression>();
        String sep = ",";
        while (sep.equals(",")) {
            sep = peekToken();
            OWLObjectPropertyExpression prop = parseObjectPropertyExpression();
            props.add(prop);
            if (sep.equals(",")) {
                consumeToken();
            }
        }
        return props;
    }


    public Set<OWLIndividual> parseIndividualList() throws ParserException {
        Set<OWLIndividual> inds = new HashSet<OWLIndividual>();
        String sep = ",";
        while (sep.equals(",")) {
            inds.add(parseIndividual());
            sep = peekToken();
            if (sep.equals(",")) {
                consumeToken();
            }
        }
        return inds;
    }

    public List<OWLObjectPropertyExpression> parseObjectPropertyChain() throws ParserException {
        String delim = "o";
        List<OWLObjectPropertyExpression> properties = new ArrayList<OWLObjectPropertyExpression>();
        while(delim.equals("o")) {
            OWLObjectPropertyExpression prop = parseObjectPropertyExpression();
            properties.add(prop);
            delim = peekToken();
            if(delim.equals("o")) {
                consumeToken();
            }
        }
        return properties;
    }

    public OWLObjectPropertyChainSubPropertyAxiom parsePropertyChainSubPropertyAxiom() throws ParserException {
        // Chain followed by subPropertyOf
        List<OWLObjectPropertyExpression> props = parseObjectPropertyChain();
        String imp = consumeToken();
        if(!imp.equals("->")) {
            throwException("->", "o");
        }
        OWLObjectPropertyExpression superProp = parseObjectPropertyExpression();
        return dataFactory.getOWLObjectPropertyChainSubPropertyAxiom(props, superProp);
    }

    public OWLClassAxiom parseClassAxiom() throws ParserException {
        OWLDescription lhs = parseDescription();
        // subClassOf
        String kw = consumeToken();
        if (kw.equalsIgnoreCase(ManchesterOWLSyntax.SUBCLASS_OF.toString())) {
            OWLDescription rhs = parseDescription();
            return dataFactory.getOWLSubClassAxiom(lhs, rhs);
        }
        else if (kw.equalsIgnoreCase(ManchesterOWLSyntax.EQUIVALENT_TO.toString())) {
            OWLDescription rhs = parseDescription();
            return dataFactory.getOWLEquivalentClassesAxiom(lhs, rhs);
        }
        else if (kw.equalsIgnoreCase(ManchesterOWLSyntax.DISJOINT_WITH.toString())) {
            OWLDescription rhs = parseDescription();
            return dataFactory.getOWLDisjointClassesAxiom(lhs, rhs);
        }
        throwException(SUB_CLASS_OF, EQUIVALENT_TO, DISJOINT_WITH);
        return null;
    }

    public OWLObjectPropertyAxiom parseObjectPropertyAxiom() throws ParserException {

        String tok = peekToken();
        if (tok.equals(ManchesterOWLSyntax.FUNCTIONAL)) {
            consumeToken();
            String open = consumeToken();
            if (!open.equals("(")) {
                throwException("(");
            }
            OWLObjectPropertyExpression prop = parseObjectPropertyExpression();
            String close = consumeToken();
            if (!close.equals(")")) {
                throwException(")");
            }
            return dataFactory.getOWLFunctionalObjectPropertyAxiom(prop);
        }
        else if (tok.equals(ManchesterOWLSyntax.INVERSE_FUNCTIONAL)) {
            consumeToken();
            String open = consumeToken();
            if (!open.equals("(")) {
                throwException("(");
            }
            OWLObjectPropertyExpression prop = parseObjectPropertyExpression();
            String close = consumeToken();
            if (!close.equals(")")) {
                throwException(")");
            }
            return dataFactory.getOWLInverseFunctionalObjectPropertyAxiom(prop);
        }
        else if (tok.equals(ManchesterOWLSyntax.TRANSITIVE)) {
            consumeToken();
            String open = consumeToken();
            if (!open.equals("(")) {
                throwException("(");
            }
            OWLObjectPropertyExpression prop = parseObjectPropertyExpression();
            String close = consumeToken();
            if (!close.equals(")")) {
                throwException(")");
            }
            return dataFactory.getOWLTransitiveObjectPropertyAxiom(prop);

        }
        else if (tok.equals(ManchesterOWLSyntax.SYMMETRIC)) {
            consumeToken();
            String open = consumeToken();
            if (!open.equals("(")) {
                throwException("(");
            }
            OWLObjectPropertyExpression prop = parseObjectPropertyExpression();
            String close = consumeToken();
            if (!close.equals(")")) {
                throwException(")");
            }
            return dataFactory.getOWLSymmetricObjectPropertyAxiom(prop);

        }
        else if (tok.equals(ManchesterOWLSyntax.REFLEXIVE)) {
            consumeToken();
            String open = consumeToken();
            if (!open.equals("(")) {
                throwException("(");
            }
            OWLObjectPropertyExpression prop = parseObjectPropertyExpression();
            String close = consumeToken();
            if (!close.equals(")")) {
                throwException(")");
            }
            return dataFactory.getOWLReflexiveObjectPropertyAxiom(prop);

        }
        else if (tok.equals(ManchesterOWLSyntax.IRREFLEXIVE)) {
            consumeToken();
            String open = consumeToken();
            if (!open.equals("(")) {
                throwException("(");
            }
            OWLObjectPropertyExpression prop = parseObjectPropertyExpression();
            String close = consumeToken();
            if (!close.equals(")")) {
                throwException(")");
            }
            return dataFactory.getOWLIrreflexiveObjectPropertyAxiom(prop);

        }
        else if (tok.equals(ManchesterOWLSyntax.ANTI_SYMMETRIC)) {
            consumeToken();
            String open = consumeToken();
            if (!open.equals("(")) {
                throwException("(");
            }
            OWLObjectPropertyExpression prop = parseObjectPropertyExpression();
            String close = consumeToken();
            if (!close.equals(")")) {
                throwException(")");
            }
            return dataFactory.getOWLAntiSymmetricObjectPropertyAxiom(prop);
        }
        return null;
    }


    public OWLIndividual parseIndividual() throws ParserException {
        String name = consumeToken();
        if (!isIndividualName(name)) {
            throwException(false, false, false, true);
        }
        return getOWLIndividual(name);
    }


    public OWLDataProperty parseDataProperty() throws ParserException {
        String name = consumeToken();
        if (!isDataPropertyName(name)) {
            throwException(false, false, true, false);
        }
        return getOWLDataProperty(name);
    }


    public Map<String, URI> parseNamespace() throws ParserException {
        String nsTok = consumeToken();
        if (!nsTok.equals(NAMESPACE)) {
            throwException(NAMESPACE);
        }
        // Namespaces are of the form
        //  prefix = <URI>
        // The prefix might be empty
        String tok = consumeToken();
        Map<String, URI> map = new HashMap<String, URI>(2);
        if (tok.equals("=")) {
            // Default namespace
            URI uri = parseURI();
            map.put("", uri);
        }
        else {
            String prefix = tok;
            String delim = consumeToken();
            if (!delim.equals("=")) {
                throwException("=");
            }
            URI uri = parseURI();
            map.put(prefix, uri);
        }
        return map;
    }


    public OWLImportsDeclaration parseImportsDeclaration(OWLOntology ont) throws ParserException {
        String section = consumeToken();
        if (!section.equalsIgnoreCase(IMPORTS)) {
            throwException(IMPORTS);
        }
        URI importedOntologyURI = parseURI();
        return dataFactory.getOWLImportsDeclarationAxiom(ont, importedOntologyURI);
    }


    public URI parseURI() throws ParserException {
        String quotedURI = consumeToken();
        if (!quotedURI.startsWith("<") || !quotedURI.endsWith(">")) {
            throwException("<URI>");
        }
        return URI.create(quotedURI.substring(1, quotedURI.length() - 1));
    }


    private void processDeclaredEntities() {
        addNamesToSet(buffer, CLASS, classNames);
        addNamesToSet(buffer, DATA_PROPERTY, dataPropertyNames);
        addNamesToSet(buffer, OBJECT_PROPERTY, objectPropertyNames);
        addNamesToSet(buffer, INDIVIDUAL, individualNames);
    }


    private static void addNamesToSet(String buffer, String sectionName, Set<String> names) {
        Pattern p = Pattern.compile("(" + sectionName + "\\s*)(\\S*)");
        Matcher matcher = p.matcher(buffer);
        while (matcher.find()) {
            names.add(matcher.group(2));
        }
    }

    public void parseOntology(OWLOntologyManager manager, OWLOntology ont) throws ParserException,
            OWLOntologyCreationException,
            OWLOntologyChangeException {
        Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
        URI ontologyURI = null;
        processDeclaredEntities();
        while (true) {
            String section = peekToken();
            if (ontologyURI == null && section.equals(ONTOLOGY)) {
                // Consume ontology header token
                consumeToken();
                ontologyURI = parseURI();
                setBase(ontologyURI + "#");
                // Annotations?
                while(peekToken().equals(ANNOTATIONS)) {
                    Set<OWLAnnotation> annos = parseAnnotations();
                    for(OWLAnnotation anno : annos) {
                        axioms.add(dataFactory.getOWLOntologyAnnotationAxiom(ont, anno));
                    }
                }
            }
            else if (section.equals(CLASS)) {
                axioms.addAll(parseClassFrame());
            }
            else if (section.equals(OBJECT_PROPERTY)) {
                axioms.addAll(parseObjectPropertyFrame());
            }
            else if (section.equals(DATA_PROPERTY)) {
                axioms.addAll(parseDataPropertyFrame());
            }
            else if (section.equals(INDIVIDUAL)) {
                axioms.addAll(parseIndividualFrame());
            }
            else if (section.equals(VALUE_PARTITION)) {
                axioms.addAll(parseValuePartitionFrame());
            }
            else if (section.equals(IMPORTS)) {
                OWLImportsDeclaration decl = parseImportsDeclaration(ont);
                axioms.add(decl);
                manager.makeLoadImportRequest(decl);
            }
            else if (section.equals(NAMESPACE)) {
                Map<String, URI> nsMap = parseNamespace();
                namespaceMap.putAll(nsMap);
            }
            else if (section.equals(EOF)) {
                break;
            }
            else {
                throwException(CLASS, OBJECT_PROPERTY, DATA_PROPERTY, INDIVIDUAL, IMPORTS, VALUE_PARTITION);
            }
        }

        List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>(axioms.size());
        for (OWLAxiom ax : axioms) {
            changes.add(new AddAxiom(ont, ax));
        }
        changes.add(new SetOntologyURI(ont, ontologyURI));
        manager.applyChanges(changes);
    }


    private void throwException(String... keywords) throws ParserException {
        Set<String> theKeywords = new HashSet<String>();
        theKeywords.addAll(Arrays.asList(keywords));
        theKeywords.addAll(potentialKeywords);
        potentialKeywords.clear();
        throw new ParserException(token,
                                  tokenStartPos,
                                  line,
                                  tokenStartCol,
                                  false,
                                  false,
                                  false,
                                  false,
                                  false,
                                  theKeywords);
    }


    private void throwException(boolean classNameExpected, boolean objectPropertyNameExpected,
                                boolean dataPropertyNameExpected, boolean individualNameExpected,
                                boolean datatypeNameExpected, String... keywords) throws ParserException {
        Set<String> theKeywords = new HashSet<String>();
        theKeywords.addAll(Arrays.asList(keywords));
        if (objectPropertyNameExpected) {
            theKeywords.add(INV);
        }
        theKeywords.addAll(potentialKeywords);
        potentialKeywords.clear();
        ManchesterOWLSyntaxTokenizer.Token lastToken = getLastToke();
        throw new ParserException(token,
					        		lastToken.getPos(),
					                lastToken.getRow(),
					                lastToken.getCol(),
                                  classNameExpected,
                                  objectPropertyNameExpected,
                                  dataPropertyNameExpected,
                                  individualNameExpected,
                                  datatypeNameExpected,
                                  theKeywords);
    }


    private void throwException(boolean classNameExpected, boolean objectPropertyNameExpected,
                                boolean dataPropertyNameExpected, boolean individualNameExpected) throws
            ParserException {
        Set<String> keywords = new HashSet<String>();
        if (objectPropertyNameExpected) {
            keywords.add(INV);
        }
        ManchesterOWLSyntaxTokenizer.Token lastToken = getLastToke();
        keywords.addAll(potentialKeywords);
        potentialKeywords.clear();
        throw new ParserException(token,
                                  lastToken.getPos(),
                                  lastToken.getRow(),
                                  lastToken.getCol(),
                                  classNameExpected,
                                  objectPropertyNameExpected,
                                  dataPropertyNameExpected,
                                  individualNameExpected,
                                  false,
                                  keywords);
    }


    private class DefaultEntityChecker implements OWLEntityChecker {

        private Map<String, OWLDataType> dataTypeNameMap;


        public DefaultEntityChecker() {
            dataTypeNameMap = new HashMap<String, OWLDataType>();
            for (XSDVocabulary v : XSDVocabulary.values()) {
                dataTypeNameMap.put(v.getURI().getFragment(), dataFactory.getOWLDataType(v.getURI()));
                dataTypeNameMap.put("xsd:" + v.getURI().getFragment(), dataFactory.getOWLDataType(v.getURI()));
            }
        }


        public OWLClass getOWLClass(String name) {
            if(name.equals("Thing")) {
                return dataFactory.getOWLThing();
            }
            return dataFactory.getOWLClass(getURI(name));
        }


        public OWLObjectProperty getOWLObjectProperty(String name) {
            if (objectPropertyNames.contains(name)) {
                return dataFactory.getOWLObjectProperty(getURI(name));
            }
            else {
                return null;
            }
        }


        public OWLDataProperty getOWLDataProperty(String name) {
            if (dataPropertyNames.contains(name)) {
                return dataFactory.getOWLDataProperty(getURI(name));
            }
            else {
                return null;
            }
        }


        public OWLIndividual getOWLIndividual(String name) {
            if (individualNames.contains(name)) {
                return dataFactory.getOWLIndividual(getURI(name));
            }
            else {
                return null;
            }
        }


        public OWLDataType getOWLDataType(String name) {
            return dataTypeNameMap.get(name);
        }
    }


    private Map<String, URI> nameURIMap = new HashMap<String, URI>();


    public URI getURI(String name) {
        URI uri = nameURIMap.get(name);
        if (uri != null) {
            return uri;
        }
        int colonIndex = name.indexOf(':');
        if (colonIndex != -1) {
            String prefix = name.substring(0, colonIndex);
            URI ns = namespaceMap.get(prefix);
            if (ns != null) {
                uri = URI.create(ns + name);
                nameURIMap.put(name, uri);
                return uri;
            }
        }
        uri = URI.create(base + name);
        nameURIMap.put(name, uri);
        return uri;
    }
    
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  Parsing "Inline" Axioms
    //
    
    private int tokenIndex;
    
    public OWLAxiom parseAxiom() throws ParserException {

        String token = peekToken();
        if (isClassName(token)) {
            return parseAxiomWithClassExpressionStart();
        }
        else if (isObjectPropertyName(token)) {
            return parseAxiomWithObjectPropertyStart();
        }
        else if (isDataPropertyName(token)) {
            return parseAxiomWithDataPropertyStart();
        }
        else if (isIndividualName(token)) {

        }
        else if (token.equalsIgnoreCase("inv")) {
            return parseAxiomWithObjectPropertyStart();
        }
        else if (token.equalsIgnoreCase("(")) {
            return parseAxiomWithClassExpressionStart();
        }
        else if (token.equalsIgnoreCase("{")) {
            return parseAxiomWithClassExpressionStart();
        }
        else if (token.equalsIgnoreCase(FUNCTIONAL)) {
            return parseFunctionPropertyAxiom();
        }
        else if (token.equalsIgnoreCase(INVERSE_FUNCTIONAL)) {
            return parseInverseFunctionalPropertyAxiom();
        }
        else if (token.equalsIgnoreCase(SYMMETRIC)) {
            return parseSymmetricPropertyAxiom();
        }
        else if (token.equalsIgnoreCase(TRANSITIVE)) {
            return parseTransitivePropertyAxiom();
        }
        else if (token.equalsIgnoreCase(REFLEXIVE)) {
            return parseReflexivePropertyAxiom();
        }
        else if (token.equalsIgnoreCase(IRREFLEXIVE)) {
            return parseIrreflexivePropertyAxiom();
        }
        else {
        	throw createException(true, true, true, true, false, false, "(", "{", "inv", FUNCTIONAL, INVERSE_FUNCTIONAL, SYMMETRIC, TRANSITIVE, REFLEXIVE, IRREFLEXIVE);
        }
        return null;
    }
    
    public OWLAxiom parseAxiomWithClassExpressionStart() throws ParserException {
        OWLDescription ce = parseIntersection();
        return parseClassAxiomRemainder(ce);
    }

    public OWLAxiom parseClassAxiomRemainder(OWLDescription startExpression) throws ParserException {
        String kw = consumeToken();
        System.out.println("Parse class axiom rem: " + startExpression);
        if (kw.equalsIgnoreCase(ManchesterOWLSyntax.SUBCLASS_OF.toString())) {
            OWLDescription superClass = parseDescription();
            return getDataFactory().getOWLSubClassAxiom(startExpression, superClass);
        }
        else if (kw.equalsIgnoreCase(ManchesterOWLSyntax.DISJOINT_WITH.toString())) {
        	OWLDescription disjointClass = parseDescription();
            return getDataFactory().getOWLDisjointClassesAxiom(startExpression, disjointClass);
        }
        else if (kw.equalsIgnoreCase(ManchesterOWLSyntax.EQUIVALENT_TO.toString())) {
        	OWLDescription equivClass = parseDescription();
            return getDataFactory().getOWLEquivalentClassesAxiom(startExpression, equivClass);
        }
        else if (kw.equalsIgnoreCase(AND)) {
        	OWLDescription conjunct = parseIntersection();
            Set<OWLDescription> conjuncts = conjunct.asConjunctSet();
            conjuncts.add(startExpression);
            OWLDescription ce = getDataFactory().getOWLObjectIntersectionOf(conjuncts);
            return parseClassAxiomRemainder(ce);
        }
        else if (kw.equalsIgnoreCase(OR)) {
        	OWLDescription disjunct = parseIntersection();
            Set<OWLDescription> disjuncts = disjunct.asDisjunctSet();
            disjuncts.add(startExpression);
            OWLDescription ce = getDataFactory().getOWLObjectUnionOf(disjuncts);
            return parseClassAxiomRemainder(ce);

        }
        else {
            System.out.println("Throwing exception!");
            throw createException(SUB_CLASS_OF, DISJOINT_WITH, EQUIVALENT_TO, AND, OR);
        }
    }

    public OWLAxiom parseAxiomWithObjectPropertyStart() throws ParserException {
        OWLObjectPropertyExpression prop = parseObjectPropertyExpression();
        String kw = consumeToken();
        if (kw.equalsIgnoreCase(SOME)) {
        	OWLDescription filler = parseIntersection();
            return parseClassAxiomRemainder(getDataFactory().getOWLObjectSomeRestriction(prop, filler));
        }
        else if (kw.equalsIgnoreCase(ONLY)) {
        	OWLDescription filler = parseIntersection();
            return parseClassAxiomRemainder(getDataFactory().getOWLObjectAllRestriction(prop, filler));

        }
        else if (kw.equalsIgnoreCase(MIN)) {
            int cardi = parseInteger();
            OWLDescription filler = parseIntersection();
            return parseClassAxiomRemainder(getDataFactory().getOWLObjectMinCardinalityRestriction(prop, cardi, filler));

        }
        else if (kw.equalsIgnoreCase(MAX)) {
            int cardi = parseInteger();
            OWLDescription filler = parseIntersection();
            return parseClassAxiomRemainder(getDataFactory().getOWLObjectMaxCardinalityRestriction(prop, cardi, filler));

        }
        else if (kw.equalsIgnoreCase(EXACTLY)) {
            int cardi = parseInteger();
            OWLDescription filler = parseIntersection();
            return parseClassAxiomRemainder(getDataFactory().getOWLObjectExactCardinalityRestriction(prop, cardi, filler));
        }
        else if (kw.equalsIgnoreCase(SUB_PROPERTY_OF)) {
        	OWLObjectPropertyExpression superProperty = parseObjectPropertyExpression();
            return getDataFactory().getOWLSubObjectPropertyAxiom(prop, superProperty);
        }
        else if (kw.equalsIgnoreCase(EQUIVALENT_TO)) {
        	OWLObjectPropertyExpression equivProp = parseObjectPropertyExpression();
            return getDataFactory().getOWLEquivalentObjectPropertiesAxiom(prop, equivProp);

        }
        else if (kw.equalsIgnoreCase(INVERSE_OF)) {
        	OWLObjectPropertyExpression invProp = parseObjectPropertyExpression();
            return getDataFactory().getOWLInverseObjectPropertiesAxiom(prop, invProp);

        }
        else if (kw.equalsIgnoreCase(DISJOINT_WITH)) {
        	OWLObjectPropertyExpression disjProp = parseObjectPropertyExpression();
            return getDataFactory().getOWLDisjointObjectPropertiesAxiom(prop, disjProp);
        }
        else if (kw.equalsIgnoreCase(ManchesterOWLSyntax.DOMAIN.toString())) {
        	OWLDescription domain = parseClassExpression();
            return getDataFactory().getOWLObjectPropertyDomainAxiom(prop, domain);
        }
        else if (kw.equals(RANGE)) {
        	OWLDescription range = parseClassExpression();
            return getDataFactory().getOWLObjectPropertyRangeAxiom(prop, range);
        }
        else if (kw.equalsIgnoreCase("o")) {
            String sep = kw;
            List<OWLObjectPropertyExpression> chain = new ArrayList<OWLObjectPropertyExpression>();
            chain.add(prop);
            while (sep.equals("o")) {
            	OWLObjectPropertyExpression chainProp = parseObjectPropertyExpression();
                chain.add(chainProp);
                sep = consumeToken();
            }
            if (!sep.equalsIgnoreCase(SUB_PROPERTY_OF)) {
                throwException(SUB_PROPERTY_OF);
            }
            OWLObjectPropertyExpression superProp = parseObjectPropertyExpression();
            return getDataFactory().getOWLObjectPropertyChainSubPropertyAxiom(chain, superProp);
        }
        else {
        	throw createException(SOME, ONLY, MIN, MAX, EXACTLY, SUB_PROPERTY_OF, EQUIVALENT_TO, INVERSE_OF, DISJOINT_WITH, DOMAIN, RANGE, "o");
        }
    }

    public OWLAxiom parseAxiomWithDataPropertyStart() throws ParserException {
    	OWLDataPropertyExpression prop = parseDataProperty();
        return null;
    }


    public OWLAxiom parseInverseFunctionalPropertyAxiom() throws ParserException {
        String kw = consumeToken();
        if (!kw.equalsIgnoreCase(INVERSE_FUNCTIONAL)) {
        	throw createException(INVERSE_FUNCTIONAL);
        }
        OWLObjectPropertyExpression prop = parseObjectPropertyExpression();
        return getDataFactory().getOWLInverseFunctionalObjectPropertyAxiom(prop);
    }


    public OWLAxiom parseSymmetricPropertyAxiom() throws ParserException {
        String kw = consumeToken();
        if (!kw.equalsIgnoreCase(SYMMETRIC)) {
        	throw createException(SYMMETRIC);
        }
        OWLObjectPropertyExpression prop = parseObjectPropertyExpression();
        return getDataFactory().getOWLSymmetricObjectPropertyAxiom(prop);
    }



    public OWLAxiom parseTransitivePropertyAxiom() throws ParserException {
        String kw = consumeToken();
        if (!kw.equalsIgnoreCase(TRANSITIVE)) {
        	throw createException(TRANSITIVE);
        }
        OWLObjectPropertyExpression prop = parseObjectPropertyExpression();
        return getDataFactory().getOWLTransitiveObjectPropertyAxiom(prop);
    }


    public OWLAxiom parseReflexivePropertyAxiom() throws ParserException {
        String kw = consumeToken();
        if (!kw.equalsIgnoreCase(REFLEXIVE)) {
        	throw createException(REFLEXIVE);
        }
        OWLObjectPropertyExpression prop = parseObjectPropertyExpression();
        return getDataFactory().getOWLReflexiveObjectPropertyAxiom(prop);
    }


    public OWLAxiom parseIrreflexivePropertyAxiom() throws ParserException {
        String kw = consumeToken();
        if (!kw.equalsIgnoreCase(IRREFLEXIVE)) {
        	throw createException(IRREFLEXIVE);
        }
        OWLObjectPropertyExpression prop = parseObjectPropertyExpression();
        return getDataFactory().getOWLIrreflexiveObjectPropertyAxiom(prop);
    }


    public OWLAxiom parseFunctionPropertyAxiom() throws ParserException {
        String kw = consumeToken();
        if (!kw.equalsIgnoreCase(FUNCTIONAL)) {
        	throw createException(FUNCTIONAL);
        }
        String name = peekToken();
        if (isObjectPropertyName(name)) {
            OWLObjectPropertyExpression prop = parseObjectPropertyExpression();
            return getDataFactory().getOWLFunctionalObjectPropertyAxiom(prop);
        }
        else if (isDataPropertyName(name)) {
            OWLDataProperty prop = parseDataProperty();
            return getDataFactory().getOWLFunctionalDataPropertyAxiom(prop);
        }
        else {
            consumeToken();
            throw createException(false, true, true, false);
        }
    }
    
    protected String consumeToken() {
        token = getToken().getToken();
        tokenIndex++;
        return token;
    }


    protected void consumeToken(String expected) throws ParserException {
        String tok = consumeToken();
        if (!tok.equals(expected)) {
        	throw createException(expected);
        }
    }
    
    public ManchesterOWLSyntaxTokenizer.Token getToken() {
        return tokens.get((tokenIndex < tokens.size()) ? tokenIndex : tokenIndex - 1);
    }
    
    protected ManchesterOWLSyntaxTokenizer getTokenizer(String s) {
        return new ManchesterOWLSyntaxTokenizer(s);
    }
    
    
    
    public OWLDataFactory getDataFactory() {
        return dataFactory;
    }
    
    public OWLDescription parseClassExpression() throws ParserException {
    	OWLDescription desc = parseIntersection();
        if (!consumeToken().equals(ManchesterOWLSyntaxTokenizer.EOF)) {
        	throw createException(ManchesterOWLSyntaxTokenizer.EOF);
        }
        return desc;
    }
    
    protected ParserException createException(String... keywords) throws ParserException {
        Set<String> theKeywords = new HashSet<String>();
        theKeywords.addAll(Arrays.asList(keywords));
        theKeywords.addAll(potentialKeywords);
        potentialKeywords.clear();
        ManchesterOWLSyntaxTokenizer.Token lastToken = getLastToke();
        return new ParserException(getTokenSequence(), lastToken.getPos(), lastToken.getRow(), lastToken.getCol(), false, false, false, false, false, false, theKeywords);
    }


    protected ParserException createException(boolean classNameExpected, boolean objectPropertyNameExpected, boolean dataPropertyNameExpected, boolean individualNameExpected, boolean datatypeNameExpected, boolean annotationPropertyNameExpected, String... keywords) throws ParserException {
        Set<String> theKeywords = new HashSet<String>();
        theKeywords.addAll(Arrays.asList(keywords));
        if (objectPropertyNameExpected) {
            theKeywords.add(INV);
        }
        theKeywords.addAll(potentialKeywords);
        potentialKeywords.clear();
        ManchesterOWLSyntaxTokenizer.Token lastToken = getLastToke();
        return new ParserException(getTokenSequence(), lastToken.getPos(), lastToken.getRow(), lastToken.getCol(), classNameExpected, objectPropertyNameExpected, dataPropertyNameExpected, individualNameExpected, datatypeNameExpected, annotationPropertyNameExpected, theKeywords);
    }


    protected ParserException createException(boolean classNameExpected, boolean objectPropertyNameExpected, boolean dataPropertyNameExpected, boolean individualNameExpected) throws ParserException {
        Set<String> keywords = new HashSet<String>();
        if (objectPropertyNameExpected) {
            keywords.add(INV);
        }
        keywords.addAll(potentialKeywords);
        potentialKeywords.clear();
        ManchesterOWLSyntaxTokenizer.Token lastToken = getLastToke();
        return new ParserException(getTokenSequence(), lastToken.getPos(), lastToken.getRow(), lastToken.getCol(), classNameExpected, objectPropertyNameExpected, dataPropertyNameExpected, individualNameExpected, false, false, keywords);
    }
    
    protected ManchesterOWLSyntaxTokenizer.Token getLastToke() {
        if (tokenIndex - 1 > -1) {
            return tokens.get(tokenIndex - 1);
//            return tokenIndex < tokens.size() ? tokens.get(tokenIndex) : tokens.get(tokens.size() - 1);
        }
        else {
            return tokens.get(0);
        }
    }
    
    protected List<String> getTokenSequence() {
        List<String> seq = new ArrayList<String>();
        int index = tokenIndex - 1;
        if (index < 0) {
            index = 0;
        }
        while (index < tokens.size() && seq.size() < 4 && seq.indexOf(ManchesterOWLSyntaxTokenizer.EOF) == -1) {
            seq.add(tokens.get(index).getToken());
            index++;
        }
        if (seq.size() == 0) {
            seq.add(ManchesterOWLSyntaxTokenizer.EOF);
        }
        return seq;
    }
    

}
