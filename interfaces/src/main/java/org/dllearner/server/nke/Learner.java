package org.dllearner.server.nke;

import com.hp.hpl.jena.ontology.OntModel;
import org.aksw.commons.jena.Constants;
import org.dllearner.core.LearningAlgorithm;
import org.dllearner.core.owl.Description;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This object encapsulates the Learning process,
 *
 * @author Sebastian Hellmann - http://bis.informatik.uni-leipzig.de/SebastianHellmann
 *         Created: 15.06.11
 */
public class Learner {

    public LearningResult learn(Set<String> pos, Set<String> neg, OntModel model, int maxTime) throws IOException {

        LearningResult lr = new LearningResult();
        PipedOutputStream out = new PipedOutputStream();
        model.write(out, Constants.RDFXML);
        //TODO pipe this into the OWL API? Maybe there is a better way?
        PipedInputStream in = new PipedInputStream(out);

        //TODO insert algortihm here

        return lr;

    }


    protected void finalize() {
        //TODO cleanup anything there was

    }

    public class LearningResult {
        public Set<String> falsePositives = new HashSet<String>();
        public Set<String> falseNegatives = new HashSet<String>();
        //change this if necessary,
        public List<Description> best5Results = new ArrayList<Description>();

    }

}
