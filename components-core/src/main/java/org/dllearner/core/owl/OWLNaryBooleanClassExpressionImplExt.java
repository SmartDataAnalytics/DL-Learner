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
package org.dllearner.core.owl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.CollectionFactory;

import uk.ac.manchester.cs.owl.owlapi.OWLAnonymousClassExpressionImpl;

/**
 * @author Matthew Horridge, The University Of Manchester, Bio-Health Informatics
 *         Group, Date: 26-Oct-2006
 *         
 * modified by
 * @author Lorenz Buehmann
 * @since Jan 24, 2015
 * 
 * Modification: use a list instead of a set of operands to allow for 
 * (A and A) as well as (A or A)
 */
public abstract class OWLNaryBooleanClassExpressionImplExt extends
        OWLAnonymousClassExpressionImpl implements
        OWLNaryBooleanClassExpression {

    private static final long serialVersionUID = 30406L;
    private final List<OWLClassExpression> operands;

    /**
     * @param operands
     *        operands
     */
    public OWLNaryBooleanClassExpressionImplExt(
            Set<? extends OWLClassExpression> operands) {
        super();
        this.operands = new ArrayList<>(new TreeSet<>(operands));
    }
    
    /**
     * @param operands
     *        operands
     */
    public OWLNaryBooleanClassExpressionImplExt(
            List<? extends OWLClassExpression> operands) {
        super();
        this.operands = new ArrayList<>(operands);
    }

    @Override
    public void addSignatureEntitiesToSet(Set<OWLEntity> entities) {
        for (OWLClassExpression operand : operands) {
            addSignatureEntitiesToSetForValue(entities, operand);
        }
    }

    @Override
    public void addAnonymousIndividualsToSet(Set<OWLAnonymousIndividual> anons) {
        for (OWLClassExpression operand : operands) {
            addAnonymousIndividualsToSetForValue(anons, operand);
        }
    }

    @Override
    public List<OWLClassExpression> getOperandsAsList() {
        return new ArrayList<>(operands);
    }

    @Override
    public Set<OWLClassExpression> getOperands() {
        return CollectionFactory
                .getCopyOnRequestSetFromImmutableCollection(operands);
    }

    @Override
    public boolean isClassExpressionLiteral() {
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            if (!(obj instanceof OWLNaryBooleanClassExpression)) {
                return false;
            }
            return ((OWLNaryBooleanClassExpression) obj).getOperandsAsList().equals(
                    operands);
        }
        return false;
    }

    @Override
    protected int compareObjectOfSameType(OWLObject object) {
        return compareLists(operands,
                ((OWLNaryBooleanClassExpression) object).getOperandsAsList());
    }
}
