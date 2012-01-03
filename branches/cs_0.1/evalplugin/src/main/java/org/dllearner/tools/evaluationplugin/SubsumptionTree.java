package org.dllearner.tools.evaluationplugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.owl.Description;
import org.dllearner.learningproblems.EvaluatedDescriptionPosNeg;
import org.dllearner.utilities.owl.OWLAPIDescriptionConvertVisitor;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

public class SubsumptionTree {
	private static final Logger logger = Logger.getLogger(SubsumptionTree.class);
	public static boolean debug = false;

	public class Node implements Comparable<Node> {

//		public Node parent = null;
		public double accuracy;
		public boolean root = false;

		// by length?
		public SortedSet<EvaluatedDescription> equivalents = new TreeSet<EvaluatedDescription>(
				new Comparator<EvaluatedDescription>() {
					@Override
					public int compare(EvaluatedDescription o1, EvaluatedDescription o2) {
						int ret = o2.getDescriptionLength() - o1.getDescriptionLength();
						return (ret == 0) ? -1 : 0;
					}
				});

		// by accuracy
		public SortedSet<Node> subClasses = new TreeSet<Node>();
		
		public Node(EvaluatedDescription ed, boolean root) {
			this.root = root;
			if (this.root) {
				accuracy = 0.0d;
			}else{
				equivalents.add(ed);
				accuracy = ed.getAccuracy();
			}
		}

		public Node(EvaluatedDescription ed) {
			this(ed,false);
		}

		// happens only if n is sure to be a subclass
		public void insert(Node n) {
			logger.warn("******************");
			if (subClasses.isEmpty()) {
				logger.warn("Adding " + n.getEvalDesc() + "\n\t as subclass of " + this.getEvalDesc());
				subClasses.add(n);
			} else {
				SortedSet<Node> subClassesTmp = new TreeSet<Node>(subClasses);
				for (Node sub : subClassesTmp) {
					logger.warn("Testing relation between: " + n.getEvalDesc() + "\n\t and "
							+ sub.getEvalDesc());
					
					OWLClassExpression desc1 = OWLAPIDescriptionConvertVisitor.getOWLClassExpression(sub.getDesc());
					OWLClassExpression desc2 = OWLAPIDescriptionConvertVisitor.getOWLClassExpression(n.getDesc());
					boolean passOn = false;
					boolean superClass = false;
					passOn = rc.isEntailed(factory.getOWLSubClassOfAxiom(desc2, desc1));
					superClass = rc.isEntailed(factory.getOWLSubClassOfAxiom(desc1, desc2));

					// EquivalentClass of subclass
					if (passOn && superClass) {
						logger.warn("Adding " + n.getEvalDesc() + "\n\t as EQUIVALENTclass of "
								+ sub.getEvalDesc());
//						n.parent = sub.parent;
						sub.equivalents.add(n.getEvalDesc());
						// superclass of subclass
					} else if (superClass) {
						logger.warn("Adding " + n.getEvalDesc() + "\n\t as SUPERclass of "
								+ sub.getEvalDesc());
//						n.parent = this;
//						sub.parent = n;
						subClasses.remove(sub);
						subClasses.add(n);
						n.insert(sub);
						// passOn to next Class
					} else if (passOn) {
						logger
								.warn("Passing " + n.getEvalDesc() + "\n\t as SUBclass to "
										+ sub.getEvalDesc());
						sub.insert(n);
						// add to own subclasses
					} else {
						logger
								.warn("Adding " + n.getEvalDesc() + "\n\t as SUBclass of "
										+ this.getEvalDesc());
//						n.parent = this;
						subClasses.add(n);
					}
				}
			}
		}


		public EvaluatedDescription getEvalDesc() {
			return (equivalents.isEmpty()) ? null : equivalents.first();
		}

		public Description getDesc() {
			return (equivalents.isEmpty()) ? null : equivalents.first().getDescription();
		}

		@Override
		public String toString() {
			return "subs/equivs: "+subClasses.size()+"|"+equivalents.size()+"  \n"+getEvalDesc().toString()+"\n"+subClasses;
		}
		
		public String _toString(String tab) {
			StringBuffer ret = new StringBuffer();
			ret.append((root) ? "Thing\n" : tab + getEvalDesc() + "\n");
			tab += "     ";
			for (Node sub : subClasses) {
				ret.append(sub._toString(tab));
			}
			return ret.toString();
		}
		
		public SortedSet<Node> getSubClasses(){
			return subClasses;
		}
		
		public SortedSet<EvaluatedDescription> getEquivalentClasses(){
			return equivalents;
		}

		@Override
		public int compareTo(Node node) {
			if (this.equals(node)) {
				return 0;
			}

			int ret = (int) Math.round(accuracy - node.accuracy);
			if (ret == 0) {
				ret = node.getDesc().getLength() - getDesc().getLength();
			}
			if (ret == 0) {
				ret = -1;
			}
			return ret;
		}

		public boolean equals(Node node) {
			return this == node;
		}

	}

	private Node root;

	private final OWLReasoner rc;
	private OWLDataFactory factory;

	public SubsumptionTree(OWLReasoner rc) {
		logger.trace("Output for DescriptionSubsumptionTree deactivated (in class)");
		logger.setLevel((debug) ? Level.WARN : Level.OFF);
		this.rc = rc;
		factory = OWLManager.createOWLOntologyManager().getOWLDataFactory();
		this.root = new Node(null,true);
//		this.root.parent = null;
	}

	public static void main(String[] args) {

	}

	public void insert(Collection<EvaluatedDescription> evaluatedDescriptions) {
		for (EvaluatedDescription evaluatedDescription : evaluatedDescriptions) {
			logger.warn("Next to insert: " + evaluatedDescription.toString());
			Node n = new Node(evaluatedDescription);
			this.root.insert(n);
		}
	}

	public void insertEdPosNeg(Collection<EvaluatedDescriptionPosNeg> evaluatedDescriptions, int limit,
			double accuracyThreshold) {
		
		List<EvaluatedDescription> newSet = new ArrayList<EvaluatedDescription>();
		int i = 0;
		for (EvaluatedDescriptionPosNeg evaluatedDescription : evaluatedDescriptions) {
			if (i >= evaluatedDescriptions.size() || newSet.size() >= limit) {
				break;
			}
			if (evaluatedDescription.getAccuracy() > accuracyThreshold) {
				newSet.add((EvaluatedDescription) evaluatedDescription);
				logger.warn(evaluatedDescription);
			}
			i++;
		}

		for (EvaluatedDescription evaluatedDescription : newSet) {
			logger.warn("Next to insert: " + evaluatedDescription.toString());
			Node n = new Node(evaluatedDescription);
			this.root.insert(n);
		}
		logger.warn("Finished Inserting");

	}

	@Override
	public String toString() {
//		for (Node n : root.subClasses) {
//			System.out.println(n);
//		}
//		System.out.println(root.subClasses);
		return root._toString("");
	}
	
	public Node getRoot(){
		return root;
	}

}
