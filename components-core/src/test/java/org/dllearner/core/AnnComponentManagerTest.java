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
        Assert.assertEquals(3,components.size());
    }
}
