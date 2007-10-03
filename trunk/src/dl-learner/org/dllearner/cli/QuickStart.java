/**
 * Copyright (C) 2007, Jens Lehmann
 *
 * This file is part of DL-Learner.
 * 
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.dllearner.cli;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

/**
 * A tool to quickly start a learning example. It detects all conf files in 
 * the examples directory and offers the user to start one of them. 
 * 
 * @author Sebastian Hellmann
 * @author Jens Lehmann
 */
public class QuickStart {

	static HashMap<String, ArrayList<String>> hm = null;
	static String pm = ".";// pathmodifier

	public static void main(String[] args) {

		String lastused = readit();
		String tab = "	";
		int the_Number = 0;
		ArrayList<String> FinalSelection = new ArrayList<String>();
		FinalSelection.add("na");

		hm = new HashMap<String, ArrayList<String>>();
		String path = pm + File.separator + "examples";
		File f = new File(path);
		getAllConfs(f, path);

		// System.out.println(hm.size());
		Iterator<String> i = hm.keySet().iterator();
		Object[] sort = new Object[hm.size()];
		int count = 0;
		while (i.hasNext())
			sort[count++] = i.next();
		Arrays.sort(sort);
		Object s;
		String s1 = "";
		// String tmp="";
		for (int aa = 0; aa < sort.length; aa++)
		// while (i.hasNext())
		{
			s = sort[aa];
			s1 = (String) s;
			if (s1.startsWith(pm + "\\examples\\") || s1.startsWith(pm + "/examples/"))
				System.out.println(s1.substring(10).toUpperCase());
			else
				System.out.println(s);

			ArrayList<String> al = hm.get(s);
			String[] files = new String[al.size()];
			for (int j = 0; j < al.size(); j++) {
				files[j] = al.get(j);
			}
			Arrays.sort(files);
			for (int j = 0; j < files.length; j++) {
				the_Number++;
				FinalSelection.add(the_Number, s + files[j] + ".conf");// tmp=the_Number+":"+tab+files[j];
				System.out.println("  " + the_Number + ":" + tab + files[j] + "");

			}
			// System.out.println(FinalSelection.get(1));

		}// end while
		System.out.println("Last Used: " + lastused + "\n"
				+ "->press enter to use it again, else choose number:");
		boolean number = false;
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			int target = 0;
			String Selected = "";
			while (true) {
				String cmd = br.readLine();
				try {
					if (cmd.length() == 0) {
						number = false;
						break;
					}
					target = Integer.parseInt(cmd);
					number = true;
					break;
				} catch (Exception e) {
					System.out.println("Not a number");
				}
				;
			}// end while
			if (number) {
				try {
					Selected = FinalSelection.get(target);
				} catch (Exception e) {
					System.out.println("number does not exist");
				}
				;
				writeit(Selected);
				System.out.println(Selected);
			} else if (!number) {
				Selected = lastused;
			}

			System.out.println("ToDo: start commandline interface with selected conf file");
			// DLLearner.main(new String[] { Selected });

		} catch (Exception e) {
			e.printStackTrace();
		}// System.out.println(s+" : "+hm.get(s).get(0));

		// System.out.println(f.isDirectory()+f.getAbsolutePath());
	}

	public static void getAllConfs(File f, String path) {
		path = path + File.separator;
		// System.out.println(path);
		String[] act = f.list();
		for (int i = 0; i < act.length; i++) {
			// System.out.println(act[i]);

			if (new File(path + act[i]).isDirectory()) {

				getAllConfs(new File(path + act[i]), path + act[i]);
				// al.add(new File(act[i]));
			} else if (act[i].endsWith(".conf")) {
				if (hm.get(path) == null) {
					hm.put(path, new ArrayList<String>());
				}
				hm.get(path).add(act[i].substring(0, act[i].length() - 5));
				// System.out.println(act[i].substring(0,act[i].length()-5));
				// System.out.println(hm.get(path).size());
				// hm.put(new
				// File(act[i]).getAbsolutePath(),act[i].substring(0,act[i].length()-4));
			}
		}// end for

	}

	static void writeit(String lastused) {
		try {
			FileWriter fw = new FileWriter(".lastUsedExample");
			fw.write(lastused);
			fw.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static String readit() {
		String lu = "";
		try {
			RandomAccessFile raf = new RandomAccessFile(".lastUsedExample", "r");
			String line = "";
			while ((line = raf.readLine()) != null) {
				lu = line;
			}
		} catch (Exception e) {
			writeit("na");
		}
		return lu;
	}

	static String readCP() {
		String lu = "";
		try {
			RandomAccessFile raf = new RandomAccessFile("classpath.start", "r");
			String line = "";
			while ((line = raf.readLine()) != null) {
				lu += line;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lu;
	}

}
