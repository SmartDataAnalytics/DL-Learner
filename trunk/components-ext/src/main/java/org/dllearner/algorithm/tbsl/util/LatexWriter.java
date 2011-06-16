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
	private StringBuilder summary;
	
	private int cnt = 1;
	
	public LatexWriter() {
		sb = new StringBuilder();
		summary = new StringBuilder();
		
		beginSummaryTable();
	}
	
	private String loadPraeambel(){
		StringBuilder praeamble = new StringBuilder();
		try {
			Scanner scanner = new Scanner(new FileInputStream(this.getClass().getClassLoader().getResource(PRAEAMBEL_FILE).getPath()));
			try {
			  while (scanner.hasNextLine()){
				  praeamble.append(scanner.nextLine() + NL);
			  }
			}
			finally{
			  scanner.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return praeamble.toString();
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
		sb.append("\\section{").append(title).append("}").append("\\label{" + cnt++ + "}\n");
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
	
	public void beginSummaryTable(){
		summary.append("\\small\n");
		summary.append("\\begin{tabular}{| c | p{10cm} | c | c | c |}\\hline\n");
		summary.append("id & question & P & R & \\\\\\hline\\hline\n");
	}
	
	public void endSummaryTable(){
		
		summary.append("\\end{tabular}\n");
	}
	
	public void addSummaryTableEntry(int id, String question, double precision, double recall, String errorCode){
		String precisionStr = "";
		String recallStr = "";
		if(precision != -1 && recall != -1){
			precisionStr = Double.toString(precision);
			recallStr = Double.toString(recall);
		}
		summary.append("\\ref{" + id + "}").append(" & ").append(question).
		append(" & ").append(precisionStr).append(" & ").append(recallStr).append(" & ").append(errorCode).append("\\\\\\hline\n");
		
	}
	
	public void write(String file){
		endSummaryTable();
		StringBuilder latex = new StringBuilder();
		latex.append(loadPraeambel());
		latex.append("\\begin{document}");
		latex.append("\\maketitle\n");
		latex.append("\\newpage\n");
		latex.append(summary.toString());
		latex.append("\\newpage\n");
		latex.append("\\tableofcontents\n");
		latex.append("\\newpage\n");
		latex.append(sb.toString());
		latex.append("\\end{document}");
		
		try {
			Writer output = new BufferedWriter(new FileWriter(file));
			    try {
			      output.write( latex.toString() );
			    }
			    finally {
			      output.close();
			    }
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
