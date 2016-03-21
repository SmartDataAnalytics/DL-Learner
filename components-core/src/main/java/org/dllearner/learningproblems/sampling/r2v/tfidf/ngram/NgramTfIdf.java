package org.dllearner.learningproblems.sampling.r2v.tfidf.ngram;

import org.dllearner.learningproblems.sampling.r2v.tfidf.TfIdf;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import org.apache.commons.cli.*;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Utility to calculate tf-idf for text n-grams
 */
public class NgramTfIdf {
    /**
     * Tokenize a set of documents and extract n-gram terms
     *
     * @param tokenizer document tokenizer
     * @param ns        n-gram orders
     * @param documents set of documents from which to extract terms
     * @return iterator over document terms, where each document's terms is an iterator over strings
     */
    public static Iterable<Collection<String>> ngramDocumentTerms(Tokenizer tokenizer, List<Integer> ns,
                                                                  Iterable<String> documents) {
        // Tokenize the documents.
        List<List<String>> tokenizedDocuments = new ArrayList<>();
        for (String document : documents) {
            List<String> tokens = tokenizer.tokenize(document);
            tokenizedDocuments.add(tokens);
        }
        // Extract N-grams as the terms in our model.
        List<Collection<String>> documentTerms = new ArrayList<>();
        for (List<String> tokenizedDocument : tokenizedDocuments) {
            Collection<String> terms = new ArrayList<>();
            for (int n : ns) {
                for (List<String> ngram : ngrams(n, tokenizedDocument)) {
                    String term = StringUtils.join(ngram, " ");
                    terms.add(term);
                }
            }
            documentTerms.add(terms);
        }
        return documentTerms;
    }

    /**
     * Tokenize a set of documents as alphanumeric words and extract n-gram terms
     *
     * @param ns        n-gram orders
     * @param documents set of documents from which to extract terms
     * @return iterator over document terms, where each document's terms is an iterator over strings
     */
    public static Iterable<Collection<String>> ngramDocumentTerms(List<Integer> ns, Iterable<String> documents) {
        return ngramDocumentTerms(new TrigramRegexTokenizer(), ns, documents);
    }

    private static List<List<String>> ngrams(int n, List<String> tokens) {
        List<List<String>> ngrams = new ArrayList<>();
        for (int i = 0; i < tokens.size() - n + 1; i++) {
            ngrams.add(tokens.subList(i, i + n));
        }
        return ngrams;
    }

    public static String termStatistics(Map<String, Double> stats) {
        // Print terms in decreasing numerical order
        List<Map.Entry<String, Double>> es = new ArrayList<>(stats.entrySet());
        Collections.sort(es, new Comparator<Map.Entry<String, Double>>() {
            @Override
            public int compare(Map.Entry<String, Double> a, Map.Entry<String, Double> b) {
                return b.getValue().compareTo(a.getValue());
            }
        });
        List<String> fields = new ArrayList<>();
        for (Map.Entry<String, Double> e : es) {
            fields.add(String.format("%s = %6f", e.getKey(), e.getValue()));
        }
        return StringUtils.join(fields, "\n");
    }

    /**
     * Sample command line application. Run with --help for more information.
     *
     * @param args command line arguments
     * @throws IOException when unable to open corpus file
     */
    public static void main(String[] args) throws IOException {
        CommandLineParser parser = new GnuParser();
        Options options = new Options();
        options.addOption("ngram", true, "n-gram order");
        options.addOption("norm", true, "normalization: none, cosine");
        options.addOption("smooth", false, "smooth idf counts");
        options.addOption("noaddone", false, "do not add 1 to idf");
        options.addOption("help", false, "help");

        String filename = null;
        List<Integer> ns = Lists.newArrayList(1);
        TfIdf.Normalization normalization = TfIdf.Normalization.NONE;
        boolean smooth = false;
        boolean noAddOne = false;
        try {
            String s;

            HelpFormatter formatter = new HelpFormatter();
            CommandLine cmd = parser.parse(options, args);
            if (cmd.hasOption("help")) {
                formatter.printHelp("NgramTfIdf FILE", options, true);
                System.exit(0);
            }
            s = cmd.getOptionValue("ngram");
            if (null != s) {
                for (String i : StringUtils.split(s, ",")) {
                    ns.add(Integer.valueOf(i));
                }
            }
            if (cmd.hasOption("norm")) {
                s = cmd.getOptionValue("norm");
                switch (s) {
                    case "none":
                        normalization = TfIdf.Normalization.NONE;
                        break;
                    case "cosine":
                        normalization = TfIdf.Normalization.COSINE;
                        break;
                    default:
                        formatter.printHelp("NgramTfIdf FILE", "", options, "Invalid normalization " + s, true);
                        System.exit(-1);
                }
            }
            smooth = cmd.hasOption("smooth");
            noAddOne = cmd.hasOption("noaddone");
            String[] positional = cmd.getArgs();
            if (positional.length == 0) {
                formatter.printHelp("NgramTfIdf FILE", "", options, "Specify a documents file", true);
                System.exit(-1);
            }
            filename = positional[0];
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        }

        File file = new File(filename);
        List<String> text = Files.readLines(file, Charsets.UTF_8);
        Iterable<Collection<String>> documents = ngramDocumentTerms(ns, text);

        Iterable<Map<String, Double>> tfs = TfIdf.tfs(documents);
        Map<String, Double> idf = TfIdf.idfFromTfs(tfs, smooth, !noAddOne);
        System.out.println("IDF\n" + termStatistics(idf));
        System.out.println("TF-IDF");
        for (Map<String, Double> tf : tfs) {
            Map<String, Double> tfIdf = TfIdf.tfIdf(tf, idf, normalization);
            System.out.println(termStatistics(tfIdf));
        }
    }
}
