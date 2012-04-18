package org.dllearner.core;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;

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
        Assert.assertEquals(3,components.size());
    }
}
