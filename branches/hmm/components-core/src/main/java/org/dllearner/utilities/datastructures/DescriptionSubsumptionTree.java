/**
 * Copyright (C) 2007-2011, Jens Lehmann
 *
 * This file is part of DL-Learner.
 *
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.dllearner.utilities.datastructures;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.owl.Description;
import org.dllearner.learningproblems.EvaluatedDescriptionPosNeg;

/**
 * This class takes Descritptions and a reasoner and orders the 
 * descriptions by subsumption into a tree, represented by the internal class "Node"
 * @author Sebastian Hellmann <hellmann@informatik.uni-leipzig.de>
 *
 */
public class DescriptionSubsumptionTree {
	private static final Logger logger = Logger.getLogger(DescriptionSubsumptionTree.class);
	/**
	 * turns on logging
	 */
	public static boolean debug = false;
	
	/**
	 * Datastructure for the tree
	 * @author Sebastian Hellmann <hellmann@informatik.uni-leipzig.de>
	 *
	 */
	public class Node implements Comparable<Node> {

		public double accuracy;
		public boolean root = false;

		// by length?
		/**
		 * holds descriptions of nodes with equivalent classes
		 * should be ordered by length
		 */
		public SortedSet<EvaluatedDescription> equivalents = new TreeSet<EvaluatedDescription>(
				new Comparator<EvaluatedDescription>() {
					@Override
					public int compare(EvaluatedDescription o1, EvaluatedDescription o2) {
						int ret = o2.getDescriptionLength() - o1.getDescriptionLength();
						return (ret == 0) ? -1 : 0;
					}
				});

		// by accuracy
		/**
		 * holds the nodes that are subclasses of this node.
		 * ordered by accuracy
		 */
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

		// 
		/**
		 * insert a node into the tree
		 * only used if node is sure to be a subClass of this node
		 * @param node
		 */
		public void insert(Node node) {
			logger.warn("******************");
			if (subClasses.isEmpty()) {
				logger.warn("Adding " + node.getEvalDesc() + "\n\t as subclass of " + this.getEvalDesc());
				subClasses.add(node);
			} else {
				SortedSet<Node> subClassesTmp = new TreeSet<Node>(subClasses);
				for (Node sub : subClassesTmp) {
					logger.warn("Testing relation between: " + node.getEvalDesc() + "\n\t and "
							+ sub.getEvalDesc());

					boolean passOn = rc.isSuperClassOf(/* super */sub.getDesc(),/* sub */node.getDesc());
					boolean superClass = rc.isSuperClassOf(/* super */node.getDesc(),/* sub */sub.getDesc());

					// EquivalentClass of subclass
					if (passOn && superClass) {
						logger.warn("Adding " + node.getEvalDesc() + "\n\t as EQUIVALENTclass of "
								+ sub.getEvalDesc());
//						n.parent = sub.parent;
						sub.equivalents.add(node.getEvalDesc());
						// superclass of subclass
					} else if (superClass) {
						logger.warn("Adding " + node.getEvalDesc() + "\n\t as SUPERclass of "
								+ sub.getEvalDesc());
//						n.parent = this;
//						sub.parent = n;
						subClasses.remove(sub);
						subClasses.add(node);
						node.insert(sub);
						// passOn to next Class
					} else if (passOn) {
						logger
								.warn("Passing " + node.getEvalDesc() + "\n\t as SUBclass to "
										+ sub.getEvalDesc());
						sub.insert(node);
						// add to own subclasses
					} else {
						logger
								.warn("Adding " + node.getEvalDesc() + "\n\t as SUBclass of "
										+ this.getEvalDesc());
//						n.parent = this;
						subClasses.add(node);
					}
				}
			}
		}


		/**
		 * @return the first, i.e. the shortest class description of this node
		 */
		public EvaluatedDescription getEvalDesc() {
			return (equivalents.isEmpty()) ? null : equivalents.first();
		}

		/**
		 * @return the first, i.e. the shortest class description of this node
		 */
		public Description getDesc() {
			return (equivalents.isEmpty()) ? null : equivalents.first().getDescription();
		}

		@Override
		public String toString() {
			return "subs/equivs: "+subClasses.size()+"|"+equivalents.size()+"  \n"+getEvalDesc().toString()+"\n"+subClasses;
		}

		/**
		 * a simple recursive implementation of a tree to string conversion
		 * @param tab
		 * @return
		 */
		public String _toString(String tab) {
			StringBuffer ret = new StringBuffer();
			ret.append((root) ? "Thing\n" : tab + getEvalDesc() + "\n");
			tab += "  ";
			for (Node sub : subClasses) {
				ret.append(sub._toString(tab));
			}
			return ret.toString();
		}
		
		public List<EvaluatedDescription> getOrderedBySubsumptionAndAccuracy(boolean distinct){
			List<EvaluatedDescription> l = new ArrayList<EvaluatedDescription>();
			for(Node subs:subClasses){
				l.add(subs.getEvalDesc());
			}
			
			for(Node subs:subClasses){
				if(distinct){
					for(EvaluatedDescription subsubs : subs.getOrderedBySubsumptionAndAccuracy(distinct)){
						if(!l.contains(subsubs)){
							l.add(subsubs);
						}
					}
				}else{
					l.addAll(subs.getOrderedBySubsumptionAndAccuracy(distinct));
				}
				
			}
			return l;
			
		}
		
		public double getAccuracy() {
			return accuracy;
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

		/**
		 * == is used, important, when removing nodes from subClasses SortedSet
		 * @param node
		 * @return
		 */
		public boolean equals(Node node) {
			return this == node;
		}

	}

	/*
	 * MAIN CLASS FOLLOWING BELOW
	 * 
	 * */
	
	private Node rootNode;
	private final AbstractReasonerComponent rc;

	/**
	 * 
	 * @param rc An initialized reasoner component
	 */
	public DescriptionSubsumptionTree(AbstractReasonerComponent rc) {
		logger.trace("Output for DescriptionSubsumptionTree deactivated (in class)");
		logger.setLevel((debug) ? Level.WARN : Level.OFF);
		this.rc = rc;
		this.rootNode = new Node(null,true);
	}
	
	public Node getRootNode(){
		return rootNode;
	}
	
	public List<EvaluatedDescription> getMostGeneralDescriptions(boolean distinct){
		return rootNode.getOrderedBySubsumptionAndAccuracy(distinct);
		
	}

	public void insert(Collection<? extends EvaluatedDescription> evaluatedDescriptions) {
		for (EvaluatedDescription evaluatedDescription : evaluatedDescriptions) {
			logger.warn("Next to insert: " + evaluatedDescription.toString());
			Node n = new Node(evaluatedDescription);
			this.rootNode.insert(n);
		}
	}

	/**
	 * Not very well implemented, feel free to write your own
	 * @param evaluatedDescriptions
	 * @param limit
	 * @param accuracyThreshold
	 */
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
			this.rootNode.insert(n);
		}
		logger.warn("Finished Inserting");

	}

	@Override
	public String toString() {
		return rootNode._toString("");
	}

//	public void insert(List<? extends EvaluatedDescription> currentlyBestEvaluatedDescriptions) {
//		insert(currentlyBestEvaluatedDescriptions);
//		
//	}

}
