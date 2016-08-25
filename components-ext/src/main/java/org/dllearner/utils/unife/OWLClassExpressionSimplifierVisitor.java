/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dllearner.utils.unife;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitor;

/**
 *
 * @author Giuseppe Cota <giuseppe.cota@unife.it>
 */
public interface OWLClassExpressionSimplifierVisitor extends OWLClassExpressionVisitor {

    public OWLClassExpression getOWLClassExpression();
    
}
