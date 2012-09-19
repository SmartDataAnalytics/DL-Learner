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
import java.util.List;
import java.util.Map;

/**
 * A tool to quickly start a learning example. It detects all conf files in the
 * examples directory and offers the user to start one of them.
 * 
 * @author Sebastian Hellmann
 * @author Jens Lehmann
 */
public class QuickStart {
 
//	static HashMap<String, ArrayList<String>> hm = null;
	static String pm = ".";// pathmodifier
	static List<String> conffiles = new ArrayList<String>();

	public static void main(String[] args) {
		
		
		
		String lastused = readit();
		String tab = "	";
		int the_Number = 0;
		ArrayList<String> finalSelection = new ArrayList<String>();
		finalSelection.add("na");

		HashMap<String, ArrayList<String>> hm = new HashMap<String, ArrayList<String>>();
		String path = pm + File.separator + "examples";
		File f = new File(path);
		getAllConfs(f, path, hm);

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
				finalSelection.add(the_Number, s + files[j] + ".conf");// tmp=the_Number+":"+tab+files[j];
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
			String selected = "";
			boolean query=false;
			while (true) {
				String cmd = br.readLine();
				if(cmd.equalsIgnoreCase("q")|| cmd.equalsIgnoreCase("query"))  {
					query = (query)?false:true ;
					System.out.println("Query mode switched. Now: "+query);
					continue;
				}else if(cmd.equalsIgnoreCase("exit") || cmd.equalsIgnoreCase("quit")) {
					System.out.println("Bye...");
					System.exit(0);
				}
				try {
					if (cmd.length() == 0) {
						number = false;
						break;
					}
					target = Integer.parseInt(cmd);
					number = true;
					break;
				} catch (Exception e) {
					
					for(String one:conffiles){
					
						if(one.contains(cmd)){
							System.out.println("Did you mean "+one+" ? (Press enter to confirm,\n" +
									"any key+enter for another try)");
							cmd = br.readLine();
							if(cmd.length()==0){
								writeit(one);
								if(!query) {

									CLI.main(new String[] { one });
								}else {
									CLI.main(new String[] {"-q",one});
								}
								return;
							}else {break;}
						}
					}
					
					System.out.println("Not a number");
					continue;
				}
				
			}// end while
			if (number) {
				try {
					selected = finalSelection.get(target);
				} catch (Exception e) {
					System.out.println("number does not exist");
				}
				;
				writeit(selected);
				System.out.println(selected);
			} else if (!number) {
				selected = lastused;
			}

			// DLLearner.main(new String[] { Selected });
			if(!query) {
				CLI.main(new String[] { selected });
			}else {
				CLI.main(new String[] {"-q",selected});
			}

		} catch (Exception e) {
			e.printStackTrace();
		}// System.out.println(s+" : "+hm.get(s).get(0));

		// System.out.println(f.isDirectory()+f.getAbsolutePath());
	}

	public static void getAllConfs(File f, String path, Map<String, ArrayList<String>> confs) {
		path = path + File.separator;
		// System.out.println(path);
		String[] act = f.list();System.out.println(f);
		for (int i = 0; i < act.length; i++) {
			// System.out.println(act[i]);

			if (new File(path + act[i]).isDirectory()) {

				getAllConfs(new File(path + act[i]), path + act[i], confs);
				// al.add(new File(act[i]));
			} else if (act[i].endsWith(".conf")) {
				if (confs.get(path) == null) {
					confs.put(path, new ArrayList<String>());
				}
				confs.get(path).add(act[i].substring(0, act[i].length() - 5));
				conffiles.add(path+act[i]);
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
