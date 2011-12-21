package org.dllearner.examples.pdb;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.HashMap;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public final class AminoAcids {
	public static final Resource ALA = ResourceFactory.createResource("http://bio2rdf.org/pdb:Alanine");
	public static final Resource CYS = ResourceFactory.createResource("http://bio2rdf.org/pdb:Cysteine");
	public static final Resource ASP = ResourceFactory.createResource("http://bio2rdf.org/pdb:AsparticAcid");
	public static final Resource GLU = ResourceFactory.createResource("http://bio2rdf.org/pdb:GlutamicAcid");
	public static final Resource PHE = ResourceFactory.createResource("http://bio2rdf.org/pdb:Phenylalanine");
	public static final Resource GLY = ResourceFactory.createResource("http://bio2rdf.org/pdb:Glycine");
	public static final Resource HIS = ResourceFactory.createResource("http://bio2rdf.org/pdb:Histidine");
	public static final Resource ILE = ResourceFactory.createResource("http://bio2rdf.org/pdb:Isoleucine");
	public static final Resource LYS = ResourceFactory.createResource("http://bio2rdf.org/pdb:Lysine");
	public static final Resource LEU = ResourceFactory.createResource("http://bio2rdf.org/pdb:Leucine");
	public static final Resource MET = ResourceFactory.createResource("http://bio2rdf.org/pdb:Methionine");
	public static final Resource ASN = ResourceFactory.createResource("http://bio2rdf.org/pdb:Asparagine");
	public static final Resource PRO = ResourceFactory.createResource("http://bio2rdf.org/pdb:Proline");
	public static final Resource GLN = ResourceFactory.createResource("http://bio2rdf.org/pdb:Glutamine");
	public static final Resource ARG = ResourceFactory.createResource("http://bio2rdf.org/pdb:Arginine");
	public static final Resource SER = ResourceFactory.createResource("http://bio2rdf.org/pdb:Serine");
	public static final Resource THR = ResourceFactory.createResource("http://bio2rdf.org/pdb:Threonine");
	public static final Resource VAL = ResourceFactory.createResource("http://bio2rdf.org/pdb:Valine");
	public static final Resource TRP = ResourceFactory.createResource("http://bio2rdf.org/pdb:Tryptophan");
	public static final Resource TYR = ResourceFactory.createResource("http://bio2rdf.org/pdb:Tyrosine");
	public static final Resource SEL = ResourceFactory.createResource("http://bio2rdf.org/pdb:Selenomethionine");
	public static final Resource HYT = ResourceFactory.createResource("http://bio2rdf.org/pdb:2-hydroxy-tryptophan");
	public static final Resource SOC = ResourceFactory.createResource("http://bio2rdf.org/pdb:S-oxyCysteine");
	
	public static HashMap<Resource, File> getAllConfFiles (String dir, String confFileName){
		HashMap<Resource, File> aminoAcidsConfFiles = new HashMap<Resource, File>(30);
		aminoAcidsConfFiles.put(ALA, new File(dir + confFileName.replace(".conf", "." + ALA.getLocalName()) + ".conf"));
		aminoAcidsConfFiles.put(CYS, new File(dir + confFileName.replace(".conf", "." + CYS.getLocalName() + ".conf")));
		aminoAcidsConfFiles.put(ASP, new File(dir + confFileName.replace(".conf", "." + ASP.getLocalName() + ".conf")));
		aminoAcidsConfFiles.put(GLU, new File(dir + confFileName.replace(".conf", "." + GLU.getLocalName() + ".conf")));
		aminoAcidsConfFiles.put(PHE, new File(dir + confFileName.replace(".conf", "." + PHE.getLocalName() + ".conf")));
		aminoAcidsConfFiles.put(GLY, new File(dir + confFileName.replace(".conf", "." + GLY.getLocalName() + ".conf")));
		aminoAcidsConfFiles.put(HIS, new File(dir + confFileName.replace(".conf", "." + HIS.getLocalName() + ".conf")));
		aminoAcidsConfFiles.put(ILE, new File(dir + confFileName.replace(".conf", "." + ILE.getLocalName() + ".conf")));
		aminoAcidsConfFiles.put(LYS, new File(dir + confFileName.replace(".conf", "." + LYS.getLocalName() + ".conf")));
		aminoAcidsConfFiles.put(LEU, new File(dir + confFileName.replace(".conf", "." + LEU.getLocalName() + ".conf")));
		aminoAcidsConfFiles.put(MET, new File(dir + confFileName.replace(".conf", "." + MET.getLocalName() + ".conf")));
		aminoAcidsConfFiles.put(ASN, new File(dir + confFileName.replace(".conf", "." + ASN.getLocalName() + ".conf")));
		aminoAcidsConfFiles.put(PRO, new File(dir + confFileName.replace(".conf", "." + PRO.getLocalName() + ".conf")));
		aminoAcidsConfFiles.put(GLN, new File(dir + confFileName.replace(".conf", "." + GLN.getLocalName() + ".conf")));
		aminoAcidsConfFiles.put(ARG, new File(dir + confFileName.replace(".conf", "." + ARG.getLocalName() + ".conf")));
		aminoAcidsConfFiles.put(SER, new File(dir + confFileName.replace(".conf", "." + SER.getLocalName() + ".conf")));
		aminoAcidsConfFiles.put(THR, new File(dir + confFileName.replace(".conf", "." + THR.getLocalName() + ".conf")));
		aminoAcidsConfFiles.put(VAL, new File(dir + confFileName.replace(".conf", "." + VAL.getLocalName() + ".conf")));
		aminoAcidsConfFiles.put(TRP, new File(dir + confFileName.replace(".conf", "." + TRP.getLocalName() + ".conf")));
		aminoAcidsConfFiles.put(TYR, new File(dir + confFileName.replace(".conf", "." + TYR.getLocalName() + ".conf")));
		aminoAcidsConfFiles.put(SEL, new File(dir + confFileName.replace(".conf", "." + SEL.getLocalName() + ".conf")));
		aminoAcidsConfFiles.put(HYT, new File(dir + confFileName.replace(".conf", "." + HYT.getLocalName() + ".conf")));
		aminoAcidsConfFiles.put(SOC, new File(dir + confFileName.replace(".conf", "." + SOC.getLocalName() + ".conf")));

		return aminoAcidsConfFiles;
	}
	
	public static HashMap<Resource, PrintStream> getAminoAcidPrintStreamMap(HashMap<Resource, File> allConfFiles){
		// put all amino acid resources and the their conf-files together
		HashMap<Resource, PrintStream> resprint = new HashMap<Resource, PrintStream>(30);
		try{
			resprint.put(ALA, new PrintStream(allConfFiles.get(ALA)));
			resprint.put(CYS, new PrintStream(allConfFiles.get(CYS)));
			resprint.put(ASP, new PrintStream(allConfFiles.get(ASP)));
			resprint.put(GLU, new PrintStream(allConfFiles.get(GLU)));
			resprint.put(PHE, new PrintStream(allConfFiles.get(PHE)));
			resprint.put(GLY, new PrintStream(allConfFiles.get(GLY)));
			resprint.put(HIS, new PrintStream(allConfFiles.get(HIS)));
			resprint.put(ILE, new PrintStream(allConfFiles.get(ILE)));
			resprint.put(LYS, new PrintStream(allConfFiles.get(LYS)));
			resprint.put(LEU, new PrintStream(allConfFiles.get(LEU)));
			resprint.put(MET, new PrintStream(allConfFiles.get(MET)));
			resprint.put(ASN, new PrintStream(allConfFiles.get(ASN)));
			resprint.put(PRO, new PrintStream(allConfFiles.get(PRO)));
			resprint.put(GLN, new PrintStream(allConfFiles.get(GLN)));
			resprint.put(ARG, new PrintStream(allConfFiles.get(ARG)));
			resprint.put(SER, new PrintStream(allConfFiles.get(SER)));
			resprint.put(THR, new PrintStream(allConfFiles.get(THR)));
			resprint.put(VAL, new PrintStream(allConfFiles.get(VAL)));
			resprint.put(TRP, new PrintStream(allConfFiles.get(TRP)));
			resprint.put(TYR, new PrintStream(allConfFiles.get(TYR)));
			resprint.put(SEL, new PrintStream(allConfFiles.get(SEL)));
			resprint.put(HYT, new PrintStream(allConfFiles.get(HYT)));
			resprint.put(SOC, new PrintStream(allConfFiles.get(SOC)));

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return resprint;
	}
	
	public static HashMap<Resource, StringBuffer> getAminoAcidStringBufferMap(String init){
		// put all amino acid resources and the their conf-files together
		HashMap<Resource, StringBuffer> resourceString = new HashMap<Resource, StringBuffer>(30);
		resourceString.put(ALA, new StringBuffer(init));
		resourceString.put(CYS, new StringBuffer(init));
		resourceString.put(ASP, new StringBuffer(init));
		resourceString.put(GLU, new StringBuffer(init));
		resourceString.put(PHE, new StringBuffer(init));
		resourceString.put(GLY, new StringBuffer(init));
		resourceString.put(HIS, new StringBuffer(init));
		resourceString.put(ILE, new StringBuffer(init));
		resourceString.put(LYS, new StringBuffer(init));
		resourceString.put(LEU, new StringBuffer(init));
		resourceString.put(MET, new StringBuffer(init));
		resourceString.put(ASN, new StringBuffer(init));
		resourceString.put(PRO, new StringBuffer(init));
		resourceString.put(GLN, new StringBuffer(init));
		resourceString.put(ARG, new StringBuffer(init));
		resourceString.put(SER, new StringBuffer(init));
		resourceString.put(THR, new StringBuffer(init));
		resourceString.put(VAL, new StringBuffer(init));
		resourceString.put(TRP, new StringBuffer(init));
		resourceString.put(TYR, new StringBuffer(init));
		resourceString.put(SEL, new StringBuffer(init));
		resourceString.put(HYT, new StringBuffer(init));
		resourceString.put(SOC, new StringBuffer(init));
		return resourceString;
	}
	
/*
	++++ Amino acid names and numbers ++++
Every line starts with the one-letter-code, followed by their numeric representation for .arff files, 
followed by their three-letter-code and finally their name. 
  A = 1		Ala	Alanin
  C = 3		Cys	Cystein
  D = 4		Asp	Aspartat
  E = 5		Glu	Glutamat
  F = 6		Phe	Phenylalanin
  G = 7		Gly	Glycin
  H = 8		His	Histidin
  I = 9		Ile	Isoleucin
  K = 11	Lys	Lysin
  L = 12	Leu	Leucin
  M = 13	Met	Methionin
  N = 14	Asn	Asparagin
  O = 15	Pyl	Pyrrolysin
  P = 16	Pro	Prolin
  Q = 17	Gln	Glutamin
  R = 18	Arg	Arginin
  S = 19	Ser	Serin
  T = 20	Thr	Threonin
  U = 21	Sec	Selenocystein
  V = 22	Val	Valin
  W = 23	Trp	Tryptophan
  Y = 25	Tyr	Tyrosin
*/	
	public static HashMap<String, String> getAminoAcidNumber(){
		HashMap<String,String> resnum = new HashMap<String, String>(30);
		resnum.put(new String("A"), new String("1"));
		resnum.put(new String("C"), new String("3"));
		resnum.put(new String("D"), new String("4"));
		resnum.put(new String("E"), new String("5"));
		resnum.put(new String("F"), new String("6"));
		resnum.put(new String("G"), new String("7"));
		resnum.put(new String("H"), new String("8"));
		resnum.put(new String("I"), new String("9"));
		resnum.put(new String("K"), new String("11"));
		resnum.put(new String("L"), new String("12"));
		resnum.put(new String("M"), new String("13"));
		resnum.put(new String("N"), new String("14"));
		resnum.put(new String("O"), new String("15"));
		resnum.put(new String("P"), new String("16"));
		resnum.put(new String("Q"), new String("17"));
		resnum.put(new String("R"), new String("18"));
		resnum.put(new String("S"), new String("19"));
		resnum.put(new String("T"), new String("20"));
		resnum.put(new String("U"), new String("21"));
		resnum.put(new String("V"), new String("22"));
		resnum.put(new String("W"), new String("23"));
		resnum.put(new String("Y"), new String("25"));
		
		return resnum;
	}
	
	/*
	 * +++ Amino acid properties +++
	 * 
	 * the following amino acid properties were gathered from
	 * http://www.russelllab.org/aas/
	 * 
	 */
	
	public static HashMap<String, String> getAminoAcidNumericArffAttributeMap(){
		// Hydrophobicity   hydrophilic = 0; Hydrophobic = 1; aromatic = 2; aliphatic = 3
		// Polarity unpolar = 0; polar = 1; positive = 2; negative = 3; 
		// Size Tiny = 0; Small = 1; Large = 2;
		HashMap<String, String> resdata = new HashMap<String, String>(30); 
		resdata.put(new String("A"), new String("1,0,0"));
		resdata.put(new String("C"), new String("1,1,0"));
		resdata.put(new String("D"), new String("0,3,1"));
		resdata.put(new String("E"), new String("0,3,2"));
		resdata.put(new String("F"), new String("2,0,2"));
		resdata.put(new String("G"), new String("1,0,0"));
		resdata.put(new String("H"), new String("2,2,2"));
		resdata.put(new String("I"), new String("3,0,2"));
		resdata.put(new String("K"), new String("1,2,2"));
		resdata.put(new String("L"), new String("3,0,2"));
		resdata.put(new String("M"), new String("1,0,2"));
		resdata.put(new String("N"), new String("0,1,1"));
		resdata.put(new String("O"), new String("?,?,?"));
		resdata.put(new String("P"), new String("0,0,1"));
		resdata.put(new String("Q"), new String("0,1,2"));
		resdata.put(new String("R"), new String("0,2,2"));
		resdata.put(new String("S"), new String("0,1,0"));
		resdata.put(new String("T"), new String("1,1,1"));
		resdata.put(new String("V"), new String("3,0,1"));
		resdata.put(new String("W"), new String("2,1,2"));
		resdata.put(new String("X"), new String("?,?,?")); // unknown residue (e.g. modified amino acids)
		resdata.put(new String("Y"), new String("2,1,2"));
		resdata.put(new String("U"), new String("?,?,?"));	
		return resdata;
	}
	
	public static HashMap<String, String> getAminoAcidNominalArffAttributeMap(){
		// Hydrophobicity   hydrophilic = 0; Hydrophobic = 1; aromatic = 2; aliphatic = 3
		// Polarity unpolar = 0 polar = 1; positive = 2; negative = 3; 
		// Size Tiny = 0; Small = 1; Large = 2;
		HashMap<String, String> resdata = new HashMap<String, String>(30);

		resdata.put(new String("A"), new String("Hydrophobic,Unpolar,Tiny"));
		resdata.put(new String("C"), new String("Hydrophobic,Polar,Tiny"));
		resdata.put(new String("D"), new String("Hydrophilic,Negative,Small"));
		resdata.put(new String("E"), new String("Hydrophilic,Negative,Large"));
		resdata.put(new String("F"), new String("Aromatic,Unpolar,Large"));
		resdata.put(new String("G"), new String("Hydrophobic,Unpolar,Tiny"));
		resdata.put(new String("H"), new String("Aromatic,Positive,Large"));
		resdata.put(new String("I"), new String("Aliphatic,Unpolar,Large"));
		resdata.put(new String("K"), new String("Hydrophobic,Positive,Large"));
		resdata.put(new String("L"), new String("Aliphatic,Unpolar,Large"));
		resdata.put(new String("M"), new String("Hydrophobic,Unpolar,Large"));
		resdata.put(new String("N"), new String("Hydrophilic,Polar,Small"));
		resdata.put(new String("O"), new String("?,?,?"));
		resdata.put(new String("P"), new String("Hydrophilic,Unpolar,Small"));
		resdata.put(new String("Q"), new String("Hydrophilic,Polar,Large"));
		resdata.put(new String("R"), new String("Hydrophilic,Positive,Large"));
		resdata.put(new String("S"), new String("Hydrophilic,Polar,Tiny"));
		resdata.put(new String("T"), new String("Hydrophobic,Polar,Small"));
		resdata.put(new String("V"), new String("Aliphatic,Unpolar,Small"));
		resdata.put(new String("W"), new String("Aromatic,Polar,Large"));
		resdata.put(new String("X"), new String("?,?,?")); // unknown residue (e.g. modified amino acids)
		resdata.put(new String("Y"), new String("Aromatic,Polar,Large"));
		resdata.put(new String("U"), new String("?,?,?"));
		return resdata;
	}
}
