package org.dllearner.kb.repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class OntologyRepositoryManager {
	private static OntologyRepositoryManager instance;

    private List<OntologyRepository> repositories;

    private OntologyRepositoryManager() {
        repositories = new ArrayList<>();
    }

    public static synchronized OntologyRepositoryManager getManager() {
        if(instance == null) {
            instance = new OntologyRepositoryManager();
        }
        return instance;
    }


    public void addRepository(OntologyRepository repository) {
        repositories.add(repository);
    }

    public Collection<OntologyRepository> getOntologyRepositories() {
        return Collections.unmodifiableList(repositories);
    }
}
