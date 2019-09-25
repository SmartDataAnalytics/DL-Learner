/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
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
package org.dllearner.algorithms.qtl.experiments;

import com.google.common.collect.ComparisonChain;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.xsd.XSDDateTime;
import org.apache.jena.datatypes.xsd.impl.XSDAbstractDateTimeType;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.expr.*;
import org.apache.jena.sparql.syntax.*;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class VirtuosoUtils {
	
	/**
	 * Returns a rewritten query which tried to workaround issues with xsd:date
	 * literals and applies matching on the string value instead, i.e. a triple pattern
	 * <code>?s :p "2002-09-24"^xsd:string</code> will be converted to 
	 * <code>?s :p ?date1 . FILTER(STR(?date1) = "2002-09-24")</code>.
	 * @param query the query to rewrite
	 * @return the rewritten query
	 */
	public static Query rewriteForVirtuosoDateLiteralBug(Query query){
		final Query copy = QueryFactory.create(query);
		final Element queryPattern = copy.getQueryPattern();
		final List<ElementFilter> filters = new ArrayList<>();
		ElementWalker.walk(queryPattern, new ElementVisitorBase() {
			
			int cnt = 0;
			
			@Override
			public void visit(ElementTriplesBlock el) {
				Set<Triple> newTriplePatterns = new TreeSet<>((o1, o2) -> ComparisonChain.start()
                        .compare(o1.getSubject().toString(), o2.getSubject().toString())
                        .compare(o1.getPredicate().toString(), o2.getPredicate().toString())
                        .compare(o1.getObject().toString(), o2.getObject().toString()).result());

				Iterator<Triple> iterator = el.patternElts();
				while (iterator.hasNext()) {
					Triple tp = iterator.next();

					if (tp.getObject().isLiteral()) {
						RDFDatatype dt = tp.getObject().getLiteralDatatype();
						if (dt != null && dt instanceof XSDAbstractDateTimeType) {
							iterator.remove();
							// new triple pattern <s p ?var> 
							Node objectVar = NodeFactory.createVariable("date" + cnt++);
							newTriplePatterns.add(Triple.create(tp.getSubject(), tp.getPredicate(), objectVar));

							String lit = tp.getObject().getLiteralLexicalForm();
							Object literalValue = tp.getObject().getLiteralValue();
							Expr filterExpr = new E_Equals(new E_Str(new ExprVar(objectVar)), NodeValue.makeString(lit));
							if (literalValue instanceof XSDDateTime) {
								Calendar calendar = ((XSDDateTime) literalValue).asCalendar();
								Date date = new Date(calendar.getTimeInMillis() + TimeUnit.HOURS.toMillis(2));
								SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
								String inActiveDate = format1.format(date);
								filterExpr = new E_LogicalOr(filterExpr, new E_Equals(
										new E_Str(new ExprVar(objectVar)), NodeValue.makeString(inActiveDate)));
							}
							ElementFilter filter = new ElementFilter(filterExpr);
							filters.add(filter);
						}
					}
				}
				
				for (Triple tp : newTriplePatterns) {
					el.addTriple(tp);
				}
				
				for (ElementFilter filter : filters) {
					((ElementGroup)queryPattern).addElementFilter(filter);
				}
			}
			
			@Override
			public void visit(ElementPathBlock el) {
				Set<Triple> newTriplePatterns = new TreeSet<>((o1, o2) -> ComparisonChain.start()
                        .compare(o1.getSubject().toString(), o2.getSubject().toString())
                        .compare(o1.getPredicate().toString(), o2.getPredicate().toString())
                        .compare(o1.getObject().toString(), o2.getObject().toString()).result());

				Iterator<TriplePath> iterator = el.patternElts();
				while (iterator.hasNext()) {
					Triple tp = iterator.next().asTriple();

					if (tp.getObject().isLiteral()) {
						RDFDatatype dt = tp.getObject().getLiteralDatatype();
						if (dt != null && dt instanceof XSDAbstractDateTimeType) {
							iterator.remove();
							// new triple pattern <s p ?var> 
							Node objectVar = NodeFactory.createVariable("date" + cnt++);
							newTriplePatterns.add(Triple.create(tp.getSubject(), tp.getPredicate(), objectVar));

							String lit = tp.getObject().getLiteralLexicalForm();
							Object literalValue = tp.getObject().getLiteralValue();
							Expr filterExpr = new E_Equals(new E_Str(new ExprVar(objectVar)), NodeValue.makeString(lit));
							if (literalValue instanceof XSDDateTime) {
								Calendar calendar = ((XSDDateTime) literalValue).asCalendar();
								Date date = new Date(calendar.getTimeInMillis() + TimeUnit.HOURS.toMillis(2));
								SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
								String inActiveDate = format1.format(date);
								filterExpr = new E_LogicalOr(filterExpr, new E_Equals(
										new E_Str(new ExprVar(objectVar)), NodeValue.makeString(inActiveDate)));
							}
							ElementFilter filter = new ElementFilter(filterExpr);
							filters.add(filter);
						}

					}
				}
				
				for (Triple tp : newTriplePatterns) {
					el.addTriple(tp);
				}
				
			}
		});
		for (ElementFilter filter : filters) {
			((ElementGroup)queryPattern).addElementFilter(filter);
		}
		return copy;
	}

}
