package org.dllearner.scripts;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.dllearner.utilities.MapUtils;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxOWLObjectRendererImpl;

public class OWLAxiomsHTMLWriter {

	public static void main(String[] args) throws Exception{
		if(args.length != 2){
			System.out.println("Usage: OWLAxiomsHTMLWriter <ontology> <targetFile>");
		}
		ToStringRenderer.getInstance().setRenderer(new ManchesterOWLSyntaxOWLObjectRendererImpl());
		
		String ontologyURL = args[0];
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		OWLDataFactory dataFactory = man.getOWLDataFactory();
		OWLOntology ontology = man.loadOntologyFromOntologyDocument(new File(ontologyURL));
		OWLAnnotationProperty anProp = dataFactory.getOWLAnnotationProperty(IRI.create("http://www.dl-learner.org/ontologies/enrichment.owl#confidence"));
		
		StringBuilder sb = new StringBuilder();
		DecimalFormat dfPercent = new DecimalFormat("0.00%");
		sb.append("<html>\n");
		sb.append("<table border=\"3\">\n");
		sb.append("<thead><tr><th>Source Class</th><th>Equivalent Class Expression</th><th>Accuracy</th></tr></thead>\n");
		sb.append("<tbody>\n");
		
		SortedMap<OWLClass, Map<OWLClassExpression, Double>> map = new TreeMap<OWLClass, Map<OWLClassExpression,Double>>();
		for (OWLEquivalentClassesAxiom axiom : ontology.getAxioms(AxiomType.EQUIVALENT_CLASSES)) {
			List<OWLClassExpression> classExpressionsAsList = axiom.getClassExpressionsAsList();
			OWLClass left = classExpressionsAsList.get(0).asOWLClass();
			if(!left.toStringID().startsWith("http://dbpedia.org/ontology/"))continue;//skip not DBpedia
			OWLClassExpression right = classExpressionsAsList.get(1);
			OWLLiteral lit = (OWLLiteral) axiom.getAnnotations(anProp).iterator().next().getValue();
			double accuracy = lit.parseDouble();
			Map<OWLClassExpression, Double> equivalentClasses = map.get(left);
			if(equivalentClasses == null){
				equivalentClasses = new HashMap<OWLClassExpression, Double>();
				map.put(left, equivalentClasses);
			}
			equivalentClasses.put(right, accuracy);
		}
		
		for (Entry<OWLClass, Map<OWLClassExpression, Double>> entry : map.entrySet()) {
			OWLClass cls = entry.getKey();
			Map<OWLClassExpression, Double> equivalentClasses = entry.getValue();
			List<Entry<OWLClassExpression, Double>> sorted = MapUtils.sortByValues(equivalentClasses);
			sb.append("<tr><th rowspan=\"" + (sorted.size()+1) + "\">" + cls.toString() + "</th>\n");
			for (Entry<OWLClassExpression, Double> expr : sorted) {
				OWLClassExpression classExpression = expr.getKey();
				Double value = expr.getValue();
				sb.append("<tr>"); 
		     	sb.append("<td>" + classExpression.toString() + "</td>");
		     	sb.append("<td>" + dfPercent.format(value.doubleValue()) + "</td>");
		     	sb.append("</tr>\n");
			}

		}
		
		sb.append("</tbody>\n");
		sb.append("</table>\n");
		sb.append("</html>\n");
		
		FileOutputStream fos = new FileOutputStream(new File(args[1]));
		fos.write(sb.toString().getBytes());
		fos.close();
	}

}
