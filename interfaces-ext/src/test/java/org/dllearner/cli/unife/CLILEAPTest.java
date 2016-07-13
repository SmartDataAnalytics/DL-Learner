/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dllearner.cli.unife;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Giuseppe Cota <giuseppe.cota@unife.it>
 */
public class CLILEAPTest {

    public CLILEAPTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of main method, of class CLILEAP.
     */
    //@Test
    public void testMain1() throws Exception {
        System.out.println("main");
        String[] args = {"../examples/probabilistic/family/run.conf"};
        CLILEAP.main(args);
        // TODO review the generated test code and remove the default call to fail.
        assertTrue(true);
    }

    @Test
    public void testMain2() throws Exception {
        System.out.println("main");
        String[] args = {"../examples/probabilistic/carcinogenesis/run.conf"};
        CLILEAP.main(args);
        // TODO review the generated test code and remove the default call to fail.
        assertTrue(true);
    }



}
