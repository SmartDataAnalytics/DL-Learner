package org.dllearner.autosparql.server.search;

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Writer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 * Convert the prolog file wn_s.pl from the <a href="http://www.cogsci.princeton.edu/2.0/WNprolog-2.0.tar.gz">WordNet prolog download</a>
 * into a Lucene index suitable for looking up synonyms and performing query expansion ({@link SynExpand#expand SynExpand.expand(...)}).
 *
 * This has been tested with WordNet 2.0.
 *
 * The index has fields named "word" ({@link #F_WORD})
 * and "syn" ({@link #F_SYN}).
 * <p>
 * The source word (such as 'big') can be looked up in the
 * "word" field, and if present there will be fields named "syn"
 * for every synonym. What's tricky here is that there could be <b>multiple</b>
 * fields with the same name, in the general case for words that have multiple synonyms.
 * That's not a problem with Lucene, you just use {@link org.apache.lucene.document.Document#getValues}
 * </p>
 * <p>
 * While the WordNet file distinguishes groups of synonyms with
 * related meanings we don't do that here.
 * </p>
 *
 * This can take 4 minutes to execute and build an index on a "fast" system and the index takes up almost 3 MB.
 *
 * @see <a href="http://www.cogsci.princeton.edu/~wn/">WordNet home page</a>
 * @see <a href="http://www.cogsci.princeton.edu/~wn/man/prologdb.5WN.html">prologdb man page</a>
 * @see <a href="http://www.hostmon.com/rfc/advanced.jsp">sample site that uses it</a>
 */
public class Syns2Solr
{
	/**
	 *
	 */
	private static final PrintStream o = System.out;

	/**
	 *
	 */
	private static final PrintStream err = System.err;
	
	/**
	 *
	 */
	public static final String F_SYN = "syn";

	/**
	 *
	 */
	public static final String F_WORD = "word";
	
	private static  Writer writer;

	/**
	 *
	 */
//    private static final Analyzer ana = new StandardAnalyzer(Version.LUCENE_CURRENT);

    /**
     * Takes arg of prolog file name and index directory.
     */
    public static void main(String[] args)
        throws Throwable
    {
        // get command line arguments
        String prologFilename = null; // name of file "wn_s.pl"
        String targetFile = null;
        if (args.length == 2)
        {
            prologFilename = args[0];
            targetFile = args[1];
        }
        else
        {
            usage();
            System.exit(1);
        }

        // ensure that the prolog file is readable
        if (! (new File(prologFilename)).canRead())
        {
            err.println("Error: cannot read Prolog file: " + prologFilename);
            System.exit(1);
        }
        // exit if the target index directory already exists
        if ((new File(targetFile)).exists())
        {
            err.println("Error: synonyms file already exists: " + targetFile);
            err.println("Please specify a name of a non-existent file");
            System.exit(1);
        }

        o.println("Opening Prolog file " + prologFilename);
        final FileInputStream fis = new FileInputStream(prologFilename);
        final BufferedReader br = new BufferedReader(new InputStreamReader(fis));
        writer = new BufferedWriter(new FileWriter(new File(targetFile)));
        String line;

        // maps a word to all the "groups" it's in
        final Map<String,List<String>> word2Nums = new TreeMap<String,List<String>>();
        // maps a group to all the words in it
        final Map<String,List<String>> num2Words = new TreeMap<String,List<String>>();
        // number of rejected words
        int ndecent = 0;

        // status output
        int mod = 1;
        int row = 1;
        // parse prolog file
		o.println( "[1/2] Parsing " + prologFilename);
        while ((line = br.readLine()) != null)
        {
            // occasional progress
            if ((++row) % mod == 0) // periodically print out line we read in
            {
                mod *= 2;
                o.println("\t" + row + " " + line + " " + word2Nums.size()
                    + " " + num2Words.size() + " ndecent=" + ndecent);
            }

            // syntax check
            if (! line.startsWith("s("))
            {
                err.println("OUCH: " + line);
                System.exit(1);
            }

            // parse line
            line = line.substring(2);
            int comma = line.indexOf(',');
            String num = line.substring(0, comma);
            int q1 = line.indexOf('\'');
            line = line.substring(q1 + 1);
            int q2 = line.lastIndexOf('\'');
            String word = line.substring(0, q2).toLowerCase().replace("''", "'");

            // make sure is a normal word
            if (! isDecent(word))
            {
                ndecent++;
                continue; // don't store words w/ spaces
            }

            // 1/2: word2Nums map
            // append to entry or add new one
            List<String> lis = word2Nums.get(word);
            if (lis == null)
            {
                lis = new LinkedList<String>();
                lis.add(num);
                word2Nums.put(word, lis);
            }
            else
                lis.add(num);

            // 2/2: num2Words map
            lis = num2Words.get(num);
            if (lis == null)
            {
                lis = new LinkedList<String>();
                lis.add(word);
                num2Words.put(num, lis);
            }
            else
                lis.add(word);
        }

        // close the streams
        fis.close();
        br.close();

        // create the index
		o.println( "[2/2] Building index to store synonyms, " +
				   " map sizes are " + word2Nums.size() + " and " + num2Words.size());
        index(targetFile, word2Nums, num2Words);
    }

    /**
     * Checks to see if a word contains only alphabetic characters by
     * checking it one character at a time.
     *
     * @param s string to check
     * @return <code>true</code> if the string is decent
     */
    private static boolean isDecent(String s)
    {
        int len = s.length();
        for (int i = 0; i < len; i++)
        {
            if (!Character.isLetter(s.charAt(i)))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Forms a Lucene index based on the 2 maps.
     *
     * @param indexDir the directory where the index should be created
     * @param word2Nums
     * @param num2Words
     */
    private static void index(String indexDir, Map<String,List<String>> word2Nums, Map<String,List<String>> num2Words)
        throws Throwable
    {
        int row = 0;
        int mod = 1;
        try {

          // override the specific index if it already exists
          Iterator<String> i1 = word2Nums.keySet().iterator();
          while (i1.hasNext()) // for each word
          {
              String g = i1.next();

              int n = index(word2Nums, num2Words, g);
              if (n > 0)
              {
//		o.print(g);
		writer.write(g);
                  if ((++row % mod) == 0)
                  {
                      mod *= 2;
                  }
                  writer.write("\n");
//		  o.println();
              } // else degenerate
          }
        } finally {
        }
    }

    /**
     * Given the 2 maps fills a document for 1 word.
     */
    private static int index(Map<String,List<String>> word2Nums, Map<String,List<String>> num2Words, String g)
        throws Throwable
    {
        List<String> keys = word2Nums.get(g); // get list of key#'s
        Iterator<String> i2 = keys.iterator();

        Set<String> already = new TreeSet<String>(); // keep them sorted

        // pass 1: fill up 'already' with all words
        while (i2.hasNext()) // for each key#
        {
            already.addAll(num2Words.get(i2.next())); // get list of words
        }
        int num = 0;
        already.remove(g); // of course a word is it's own syn
        Iterator<String> it = already.iterator();
        while (it.hasNext())
        {
            String cur = it.next();
            // don't store things like 'pit bull' -> 'american pit bull'
            if (!isDecent(cur))
            {
                continue;
            }
            num++;
//	    o.print(cur + ", ");
	    writer.write(cur + ", ");
        }
        return num;
    }

	/**
	 *
	 */
    private static void usage()
    {
        o.println("\n\n" +
            "java org.dllearner.autosparql.server.search.Syns2Solr <prolog file> <target file>\n\n");
    }

}
