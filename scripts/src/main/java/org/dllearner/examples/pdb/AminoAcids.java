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
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return resprint;
	}
	
	public static HashMap<Resource, String> getAminoAcidArffAttributeMap(){
		HashMap<Resource, String> resdata = new HashMap<Resource, String>(30); 
		resdata.put(ALA, new String("2,0,0.5,?,?"));
		resdata.put(CYS, new String("1,0,1,?,0"));
		resdata.put(ASP, new String("0,-1,1,?,-1"));
		resdata.put(GLU, new String("0,-1,2,?,-1"));
		resdata.put(PHE, new String("2,0,2,1,?"));
		resdata.put(GLY, new String("2,0,0.5,?,?"));
		resdata.put(HIS, new String("1,1,2,1,1"));
		resdata.put(ILE, new String("2,0,2,0,?"));
		resdata.put(LYS, new String("1,1,2,?,1"));
		resdata.put(LEU, new String("2,0,2,0,?"));
		resdata.put(MET, new String("2,0,2,?,?"));
		resdata.put(ASN, new String("0,0,1,?,0"));
		resdata.put(PRO, new String("?,0,1,?,?"));
		resdata.put(GLN, new String("0,0,2,?,0"));
		resdata.put(ARG, new String("0,1,2,?,1"));
		resdata.put(SER, new String("0,0,0.5,?,0"));
		resdata.put(THR, new String("1,0,1,?,0,"));
		resdata.put(VAL, new String("2,0,1,0,?"));
		resdata.put(TRP, new String("1,0,2,1,1"));
		resdata.put(TYR, new String("1,0,2,1,0"));
		resdata.put(SEL, new String("?,?,?,?,?"));
		
		return resdata;
	}

	
}
