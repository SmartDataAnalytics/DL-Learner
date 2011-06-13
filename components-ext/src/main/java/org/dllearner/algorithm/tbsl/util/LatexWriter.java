package org.dllearner.algorithm.tbsl.util;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Scanner;

public class LatexWriter {
	
	private static String NL = System.getProperty("line.separator");
	private static final String PRAEAMBEL_FILE = "tbsl/evaluation/praeambel.tex";
	private StringBuilder sb;
	
	public LatexWriter() {
		sb = new StringBuilder();
		
		loadPraeambel();
	}
	
	private void loadPraeambel(){
		try {
			Scanner scanner = new Scanner(new FileInputStream(this.getClass().getClassLoader().getResource(PRAEAMBEL_FILE).getPath()));
			try {
			  while (scanner.hasNextLine()){
			    sb.append(scanner.nextLine() + NL);
			  }
			}
			finally{
			  scanner.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void makeTitle(){
		sb.append("\\maketitle\n");
	}
	
	public void buildTableOfContents(){
		sb.append("\\tableofcontents\n");
		sb.append("\\newpage\n");
	}
	
	public void beginDocument(){
		sb.append("\\begin{document}\n");
		makeTitle();
		buildTableOfContents();
	}
	
	public void endDocument(){
		sb.append("\\end{document}");
	}
	
	public void beginSection(String title){
		sb.append("\\section{").append(title).append("}\n");
	}
	
	public void beginSubsection(String title){
		sb.append("\\subsection*{").append(title).append("}\n");
		sb.append("\\addcontentsline{toc}{subsection}{").append(title).append("}\n");
	}
	
	public void beginSubSubsection(String title){
		sb.append("\\subsubsection*{").append(title).append("}\n");
		sb.append("\\addcontentsline{toc}{subsubsection}{").append(title).append("}\n");
	}
	
	public void beginEnumeration(){
		sb.append("\\begin{enumerate}\n");
	}
	
	public void endEnumeration(){
		sb.append("\\end{enumerate}\n");
	}
	
	public void beginEnumerationItem(){
		sb.append("\\item{\n");
	}
	
	public void endEnumerationItem(){
		sb.append("}\n");
	}
	
	public void addListing(String listing){
		sb.append("\\begin{lstlisting}[language=SPARQL, basicstyle=\\scriptsize, showstringspaces=false]\n");
		sb.append(listing).append("\n");
		sb.append("\\end{lstlisting}\n");
	}
	
	public void addText(String text){
		sb.append(text).append("\n");
	}
	
	public void write(String file){
		try {
			Writer output = new BufferedWriter(new FileWriter(file));
			    try {
			      output.write( sb.toString() );
			    }
			    finally {
			      output.close();
			    }
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
