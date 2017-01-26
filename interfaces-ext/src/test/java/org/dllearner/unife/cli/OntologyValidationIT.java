/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dllearner.unife.cli;

import java.io.IOException;
import org.dllearner.cli.CLI;
import org.dllearner.confparser.ParseException;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.ReasoningMethodUnsupportedException;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Giuseppe Cota <giuseppe.cota@unife.it>
 */
public class OntologyValidationIT {
    
    public OntologyValidationIT() {
    }

   @Test
    public void testRunCLI() throws ComponentInitException, ParseException, IOException, 
            ReasoningMethodUnsupportedException {
        System.out.println("run cli");
        String[] args = {"../examples/probabilistic/pyrimidine/run.conf"};
        CLI.main(args);
    }
    
}
