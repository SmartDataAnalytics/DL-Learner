package javatools.datatypes;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Serializable;

import javatools.administrative.D;
/** 
This class is part of the Java Tools (see http://mpii.de/yago-naga/javatools).
It is licensed under the Creative Commons Attribution License 
(see http://creativecommons.org/licenses/by/3.0) by 
the YAGO-NAGA team (see http://mpii.de/yago-naga).
  

  
 

This class implements an SVM-light-Model. This code is the Java-translation of
Thorsten Joachim's SVM-light classifier (with permission by the author), see
<A HREF=http://svmlight.joachims.org/ target=_blank>http://svmlight.joachims.org/</A>
*/
public class SVMModel implements Serializable {
  /** Defines the enum of the Kernel-type (LINEAR,POLY,RBF,SIGMOID)*/
  public enum KernelType {LINEAR,POLY,RBF,SIGMOID};
  // Kernel parameters
  private KernelType kernel_type;
  private int poly_degree;
  private double rbf_gamma;
  private double coef_lin;
  private double coef_const;
  // Model parameters
  private int totwords;
  private int totdoc;
  private double b;
  private SparseVector[] sv;

  /** Reads the first double value from a line */
  private double readDouble(BufferedReader in) throws Exception {
    String s=in.readLine();
    s=s.substring(0,s.indexOf(' '));
    return(Double.parseDouble(s));
  }

  /** Reads the first int value from a line */
  private int readInt(BufferedReader in) throws Exception {
    String s=in.readLine();
    s=s.substring(0,s.indexOf(' '));
    return(Integer.parseInt(s));
  }

  /** Reads the SVMModel from an SVM-light model file  */
  public SVMModel(String f) throws Exception {
    this(new File(f));
  }

  /** Reads the SVMModel from a SVM-light model file  */
  public SVMModel(File f) throws Exception {
    BufferedReader in=new BufferedReader(new FileReader(f));
    if(!in.readLine().equals("SVM-light Version V6.01")) ;//throw new Exception("Unknown SVM-light version");
    switch(readInt(in)) {
      case 0: kernel_type=KernelType.LINEAR; break;
      case 1: kernel_type=KernelType.POLY; break;
      case 2: kernel_type=KernelType.RBF; break;
      case 3: kernel_type=KernelType.SIGMOID; break;
      default: throw new Exception("Unsupported kernel type");
    };
    poly_degree=readInt(in);
    rbf_gamma=readDouble(in);
    coef_lin=readDouble(in);
    coef_const=readDouble(in);
    in.readLine(); // custom
    totwords=readInt(in);
    totdoc=readInt(in);
    int sv_num=readInt(in)-1;
    b=readDouble(in);
    sv=new SparseVector[sv_num];
    for(int i=0;i<sv.length;i++) sv[i]=new SparseVector(in.readLine());
    in.close();
  }

  /** Classifies a SparseVector  */
  public double classify(SparseVector v) {
    double dist=0.0;
    for(int i=0;i<sv.length;i++) {
      dist+=kernel(sv[i],v)*sv[i].label;
    }
    return(dist - b);
  }

  /** Computes a kernel */
  public  double kernel(SparseVector a,SparseVector b) {
    switch(kernel_type) {
      case LINEAR: /* linear */
              return(a.sprod(b));
      case POLY: /* polynomial */
              return(Math.pow(coef_lin*a.sprod(b)+coef_const,poly_degree));
      case RBF: /* radial basis function */
              return(Math.exp(-rbf_gamma*(a.squaredl2norm()-2*a.sprod(b)+b.squaredl2norm())));
      case SIGMOID: /* sigmoid neural net */
              return(Math.tanh(coef_lin*a.sprod(b)+coef_const));
    }
    return(0.0); // This cannot happen, but Java wants it
  }

  /** Returns this model as a descriptive string */
  public String toString() {
    StringBuilder s=new StringBuilder(
      "Kernel: "+kernel_type+"\n"+
      "Poly-Degree: "+poly_degree+"\n"+
      "RBF-Gamma: "+rbf_gamma+"\n"+
      "Linear coefficient: "+coef_lin+"\n"+
      "Constant coefficient: "+coef_const+"\n"+
      "Number of features: "+totwords+"\n"+
      "Number of feature vectors: "+totdoc+"\n"+
      "Threshold of the hyperplane: "+b+"\n"+
      "Support Vectors:\n");
    for(SparseVector v : sv) s.append(v).append('\n');
    return(s.toString());
  }

  /** Test routine */
  public static void main(String[] argv) throws Exception {
    D.p("Enter the name of an SVM-light model file");
    SVMModel m=new SVMModel(D.r());
    D.p("Enter a vector in SVM-light notation and hit ENTER. Press CTRL+C to abort");
    D.p("SVM-light notation:  <label> <dimension>:<value> ... [# <comment>]");
    while(true) {
      D.p(m.classify(new SparseVector(D.r())));
    }
  }
}
