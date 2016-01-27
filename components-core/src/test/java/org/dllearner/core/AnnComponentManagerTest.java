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
package org.dllearner.core;

import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 * User: Chris
 * Date: 4/17/12
 * Time: 9:16 PM
 *
 * Tests for the AnnComponentManager
 */
public class AnnComponentManagerTest {


    @Test
    public void testGetComponentsOfType() {

        Collection<Class<? extends Component>> components = AnnComponentManager.getInstance().getComponentsOfType(ReasonerComponent.class);
//        System.out.println(components);
        // currently: [class org.dllearner.reasoning.OWLAPIReasoner, class org.dllearner.reasoning.FastInstanceChecker]
        Assert.assertTrue(components.size() >= 1);
    }
}
