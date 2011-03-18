package org.dllearner.algorithm.tbsl.ltag.parser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.dllearner.algorithm.tbsl.ltag.data.LTAG_Tree_Constructor;
import org.dllearner.algorithm.tbsl.ltag.data.TreeNode;
import org.dllearner.algorithm.tbsl.ltag.reader.ParseException;
import org.dllearner.algorithm.tbsl.sem.util.Pair;

/**
 * The LTAGLexicon constructor.construct() method takes a list of filenames. For
 * each file the grammar entries are read and loaded into the LTAGLexicon
 * instance. A grammar entry has a certain format: < anchor> || < treeString> ||
 * < DUDEString>. anchor is the string that represents the anchor of the ltag
 * tree. The anchor string supports the wildcard ".+" (more information in
 * earleyParser.GrammarFilter). The treeString is parsed by the ltagParser. The
 * DudeString is saved as a string within the LTAGLexicon. However it is parsed
 * later by the DudeParser when the semantics are constructed. This is done
 * during the build of the derived trees (see earleyParser.DerivedTree).
 * 
 * @author felix
 * 
 */
public class LTAG_Lexicon_Constructor {

	public LTAGLexicon construct(List<String> fileNames) {

		LTAGLexicon G = new TAG();

		for (String fileName : fileNames) {

			addFileToGrammar(fileName, G);

		}

		return G;

	}

	public void addFileToGrammar(String fileName, LTAGLexicon g) {

		ArrayList<Pair<String, TreeNode>> trees = new ArrayList<Pair<String, TreeNode>>();
		ArrayList<List<String>> semantics = new ArrayList<List<String>>();

		LTAG_Tree_Constructor constructor = new LTAG_Tree_Constructor();

		try {

			BufferedReader in = new BufferedReader(new FileReader(fileName));

			String zeile = null;
			int lineNo = 0;

			while ((zeile = in.readLine()) != null) {

				if ((zeile = zeile.trim()).equals("") || zeile.startsWith("//")) {
					continue;
				}

				String[] items = zeile.trim().split("\\|\\|");
				int i = items.length;

				if (i != 3) {

					System.err
							.println("FormatError at '"
									+ zeile
									+ "'.\nUse <anchor> || <treeString> || <DUDEString | DUDEString | ...>");
					continue;

				}

				try {

					trees.add(new Pair<String, TreeNode>(items[0].trim(),
							constructor.construct(items[1].trim())));

					List<String> dudeStrings = new ArrayList<String>();
					for (String s : items[2].trim().split(";;")) {
						if (!s.equals("")) {
							dudeStrings.add(s.trim());
						}
					}
					
					semantics.add(dudeStrings);

				} catch (ArrayIndexOutOfBoundsException aioobe) {

					System.err.println("FormatError at Line: " + zeile
							+ "\nMissing Anchor? "
							+ "Please write: '<anchor>: <tree>'\n");

					continue;

				} catch (ParseException e) {

					System.err.println("ParseException in '"
							+ fileName.substring(fileName.lastIndexOf("/") + 1)
							+ "' at Line " + lineNo + ": '" + items[1].trim()
							+ "'.");
					continue;

				}

				lineNo++;

			}

			in.close();

		} catch (IOException e) {

			System.err.println("IOException: File '" + fileName
					+ "' not found!");
			return;

		}

		g.addTrees(trees, semantics);
		return;

	}
}
