package org.dllearner.kb.repository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import com.google.common.base.Charsets;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.dllearner.kb.repository.lov.LOVRepository;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

/**
 * @author Lorenz Buehmann
 */
public class RepositoryDownloader {



    public static void main(String[] args) throws Exception{

        OntologyRepository repository = new LOVRepository();
        repository.initialize();

        // create Options object
        OptionParser parser = new OptionParser();
        OptionSpec<File> baseDir =
                parser.accepts( "basedir" )
                        .withRequiredArg().ofType( File.class )
                        .defaultsTo(new File(System.getProperty("java.io.tmpdir") + File.separator + repository.getName() + File.separator));
        OptionSpec<Void> downloadOption =
                parser.accepts( "download" );
        OptionSpec<Void> parseOption =
                parser.accepts( "parse" );

        OptionSet options = parser.parse(args);

        File dir = options.valueOf(baseDir);
        dir.mkdirs();

        File downloadDir = new File(dir, "download");
        File downloadSuccessfulDir = new File(downloadDir, "successful");
        File downloadFailedDir = new File(downloadDir, "failed");
        downloadSuccessfulDir.mkdirs();
        downloadFailedDir.mkdirs();
        File parsedDir = new File(dir, "parsed");
        File parsedSuccessfulDir = new File(parsedDir, "successful");
        File parsedFailedDir = new File(parsedDir, "failed");
        parsedSuccessfulDir.mkdirs();
        parsedFailedDir.mkdirs();

        Collection<OntologyRepositoryEntry> entries = repository.getEntries();
        System.out.println("Repository size: " + entries.size());

        boolean downloadEnabled = options.has(downloadOption);
        boolean parseEnabled = options.has(parseOption);

        final Map<String, String> map = Collections.synchronizedMap(new TreeMap<>());

        System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "4");

        System.out.println("download dir is " + downloadDir);

        entries.parallelStream().forEach(entry -> {
            try {

                File f = null;
                long sizeInMb = 101;
                if(!new File(downloadSuccessfulDir, entry.getOntologyShortName() + ".rdf").exists()) {

                    System.out.println("Loading " + entry.getOntologyShortName() + " from " + entry.getPhysicalURI());

                    try(InputStream is = repository.getInputStream(entry)) {
                        f = new File(downloadSuccessfulDir, entry.getOntologyShortName() + ".rdf");

                        IOUtils.copy(is, new FileOutputStream(f));

                        sizeInMb = f.length() / (1024 * 1024);

                        System.out.println(entry.getOntologyShortName() + ": " + FileUtils.byteCountToDisplaySize(f.length()));
                        map.put(entry.getOntologyShortName(), FileUtils.byteCountToDisplaySize(f.length()));
                    } catch (Exception e) {
                        com.google.common.io.Files.asCharSink(new File(downloadFailedDir, entry.getOntologyShortName() + ".txt"),
                                Charsets.UTF_8).write(ExceptionUtils.getMessage(e));
                        return;
                    }
                }

                if(f == null) {
                    System.out.println("Loading " + entry.getOntologyShortName() + " from disk");

                    f = new File(downloadSuccessfulDir, entry.getOntologyShortName() + ".rdf");

                    System.out.println(entry.getOntologyShortName() + ": " + FileUtils.byteCountToDisplaySize(f.length()));

                    sizeInMb = f.length() / (1024 * 1024);
                }

                if(f.exists() && parseEnabled && sizeInMb < 100) {
                    try {
                        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
                        man.addMissingImportListener(e -> {
                            System.out.println("Missing import: " + e.getImportedOntologyURI());
                        });
                        OWLOntologyLoaderConfiguration conf = new OWLOntologyLoaderConfiguration();
                        conf.setMissingImportHandlingStrategy(MissingImportHandlingStrategy.SILENT);
                        conf.addIgnoredImport(IRI.create("http://www.co-ode.org/ontologies/lists/2008/09/11/list.owl"));
                        man.setOntologyLoaderConfiguration(conf);
                        OWLOntology ont = man.loadOntologyFromOntologyDocument(f);
                        System.out.println("#Axioms: " + ont.getLogicalAxiomCount());

                        com.google.common.io.Files.asCharSink(
                                new File(parsedSuccessfulDir, entry.getOntologyShortName() + ".txt"),
                                Charsets.UTF_8).write(
                                ont.getLogicalAxiomCount() + "\t" +
                                        ont.getClassesInSignature().size() + "\t" +
                                        ont.getObjectPropertiesInSignature().size() + "\t" +
                                        ont.getDataPropertiesInSignature().size() + "\t" +
                                        ont.getIndividualsInSignature().size()
                        );

                        map.replace(entry.getOntologyShortName(), map.get(entry.getOntologyShortName()) + "||#Axioms: " + ont.getLogicalAxiomCount());
                        man.removeOntology(ont);
                    } catch (Exception e1) {
                        System.err.println("Failed to parse " + entry.getOntologyShortName());
                        map.replace(entry.getOntologyShortName(), map.get(entry.getOntologyShortName()) + "||Parse Error");
                        com.google.common.io.Files.asCharSink(
                                new File(parsedFailedDir, entry.getOntologyShortName() + ".txt"),
                                Charsets.UTF_8).write(ExceptionUtils.getMessage(e1));
                    }
                }
            } catch (Exception e) {
                System.err.println("Failed to load "  + entry.getOntologyShortName() + ". Reason: " + e.getMessage());
//				e.printStackTrace();
                map.put(entry.getOntologyShortName(), "Load error");
            }
        });

        map.forEach((k, v) -> System.out.println(k + " -> " + v));
    }
}
