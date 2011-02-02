package javatools.datatypes;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import javatools.administrative.D;
/** 
This class is part of the Java Tools (see http://mpii.de/yago-naga/javatools).
It is licensed under the Creative Commons Attribution License 
(see http://creativecommons.org/licenses/by/3.0) by 
the YAGO-NAGA team (see http://mpii.de/yago-naga).
  

  
 

  This class represents a Sparse Vector, i.e. a vector that has only
  few non-zero entries. Supports special (space-)optimized routines
  for binary vectors. Supports k-means with eucledian, cosine or other distances.
 */
public class SparseVector implements Serializable, Cloneable {
  /** Contains a double value associated to this Sparse Vector */
  protected double label;
  /** Contains the values of this vector (corresponding to dim). May change. May be set to BINARY. */
  protected double[] val;
  /** dim[i] tells what dimension val[i] belongs to. Does not change. May be set to STANDARDDIM */
  protected int[] dim;
  /** Standard dimensions 0... (gets prolonged if needed)*/
  protected static int[] STANDARDDIM={0,1,2,3,4,5};
  /** Standard values for a binary vector (just 1,1,1,...; gets prolonged if needed) */
  protected static double[] BINARY=new double[]{1.0,1.0,1.0};
  /** Holds the number of non-zero values of this vector */
  protected int size;
  /** Holds the squared two_norm of this vector, computed on demand */
  protected double twonorm_sq=-1;  
  
  /** Duplicates a vector */
  public SparseVector(SparseVector v) {
    label=v.label;
    dim=v.dim;
    size=v.size;
    if(v.val==BINARY) {
      val=BINARY;
    } else {
      val=v.val.clone();      
    }
  }
  
  /** Ensures that the BINARY array has enough 1's */
  protected static void updateBINARY(int num) {
    if(BINARY.length>= num) return;
    BINARY=new double[num];
    Arrays.fill(BINARY, 1.0);
  }
  
  /** Creates a binary Sparse Vector from an (unsorted) list of dimensions and a label*/
  public SparseVector(double l, List<Integer> dimensions) {    
    label=l;    
    dim=new int[dimensions.size()];    
    size=dimensions.size();
    int i=0;
    for(int d : dimensions) dim[i++]=d;
    Arrays.sort(dim);
    updateBINARY(size);
    val=BINARY;    
  }
  
  /** Creates a binary Sparse Vector from an unsorted array of dimensions
    * and a label
    * Zero-entries and duplicates in the dimension array will be ignored.
    * Left here as a legacy from LEILA  */
  public SparseVector(double l, int[] d) {
    Arrays.sort(d);
    // Count zeroes
    int zeroes=0;
    while(d[zeroes]==0) zeroes++;
    // Count duplicates
    for(int i=zeroes;i<d.length-1;i++) {
      if(d[i]==d[i+1]) {
        d[i]=0;
        zeroes++;
      }
    }  
    dim=new int[d.length-zeroes];
    int j=0;
    for(int i=0;i<d.length;i++) if(d[i]!=0) dim[j++]=d[i];
    size=dim.length;
    updateBINARY(size);
    val=BINARY;
    label=l;
  }
  
  /** Creates a Sparse Vector from (sorted) dimensions and values */
  public SparseVector(double[] v,int[] d) {
    this(0.0,d,v);
  }
  
  /** Creates a Sparse Vector from (sorted) dimensions, (modifiable) values and a label */
  public SparseVector(double l,int[] d, double[] v) {
    val=v;
    dim=d;
    label=l;
    size=dim.length;
  }
  
  /** Creates a SparseVector from actual values*/
  public SparseVector(double label, double... values) {
    this.label=label;
    val=values;
    if(STANDARDDIM.length<values.length) {
      STANDARDDIM=new int[values.length];
      for(int i=0;i<STANDARDDIM.length;i++) {
        STANDARDDIM[i]=i;
      }      
    }
    dim=STANDARDDIM;  
    size=values.length;
  }
    
  /** Creates a Sparse Vector from a SVM-light-like input string of the form
      label dim:val ... # comments */
  public SparseVector(String s) {
    s+=' '; // Simplify parsing
    size=0;    
    // Count non-zero entries, check whether the vector is binary
    boolean isBinary=true;
    for(int i=0;i<s.length() && s.charAt(i)!='#';i++) {
        if(s.charAt(i)==':') {
          double v=Double.parseDouble(s.substring(i+1,s.indexOf(' ',i)));
          if(v!=0.0) size++;
          if(v!=0.0 && v!=1.0) isBinary=false;
        }        
    }    
    label=Double.parseDouble(s.substring(0,s.indexOf(' ')));
    dim=new int[size];
    if(isBinary) {
      updateBINARY(size);
      val=BINARY; 
    }
    else {
      val=new double[size];
    }    
    int stringIndex=s.indexOf(' ');
    for(int n=0;n<size;) {    
      while(s.charAt(stringIndex)==' ') stringIndex++;
      int j=s.indexOf(':',stringIndex);
      dim[n]=Integer.parseInt(s.substring(stringIndex,j));
      stringIndex=s.indexOf(' ',stringIndex);
      double v=Double.parseDouble(s.substring(j+1,stringIndex));
      if(!isBinary) val[n]=v;
      if(v!=0.0) n++;
    }
  }

  /** Kicks out 0-entries (without affecting the vector in a mathematical sense) */
  public SparseVector compress() {
    if(val==BINARY) return(this);
    // Count non-zero-entries, check for binary
    int numNonZeros=0;
    boolean isBinary=true;
    for(int i=0;i<val.length;i++) {
       if(val[i]!=0.0) numNonZeros++;
       if(val[i]!=0.0 && val[i]!=1.0) isBinary=false;
    }   
    if(isBinary) {
      updateBINARY(numNonZeros);
      val=BINARY;
    }
    if(numNonZeros==size) return(this);    
    // Duplicate the vector
    double[] oldval=val;
    int[] olddim=dim;
    val=new double[numNonZeros];
    if(!isBinary) dim=new int[numNonZeros];    
    int index=0;
    for(int i=0;i<oldval.length;i++) {
       if(oldval[i]!=0.0) {
         if(!isBinary) val[index]=oldval[i];
         dim[index]=olddim[i];         
         index++;
       }    
    }   
    return(this);
  }
  
  /** Returns a String representation in an SVM-light-like format
    *    label dim:val ... */
  public String toString() {
    StringBuilder r=new StringBuilder();
    r.append(label);
    for(int i=0;i<size;i++) {
         r.append(' ').append(dim[i]).append(':').append(val[i]);
    }
    return(r.toString());
  }

  /** Returns the scalar product (dot product) of this vector with another one.
    * This code is optimized by speed, not by size */
  public double sprod(SparseVector v) {
    int i=-1;
    int j=-1;     
    double r=0.0;
    while(true) {
      if(++i>=this.size()) return(r);
      if(++j>=   v.size()) return(r);
      while(this.dim[i]<v.dim[j])
         if(++i>=this.size()) return(r);
      while(this.dim[i]>v.dim[j])
         if(++j>=v.size()) return(r);
      r+=this.val[i]*v.val[j];
    }
  }

  /** Tells whether this vector is binary */
  public boolean isBinary() {
    return(val==BINARY);
  }
  
  /** Returns the number of non-zero elements.*/
  public int size() {    
    return(size);
  }
  
  /** Returns this vector's squared two-norm */
  public double squaredl2norm() {
    if(twonorm_sq==-1) twonorm_sq=this.sprod(this);
    return(twonorm_sq);
  }

  /** Returns this vector's two-norm */
  public double l2norm() {
    return(Math.sqrt(squaredl2norm()));
  }
  
  /** Returns the cosine of this vector with another vector */
  public double cosine(SparseVector v) {
    return(sprod(v)/this.l2norm()/v.l2norm());
  }
  
  /** Retrieves the i-th value */
  public double get(int i) {
    int index=index(i);
    if(index==-1) return(0);    
    return(val[index]);
  }
  
  /** Visualizes two-dimensional vectors */
  public static String visualize(SparseVector... vectors) {
    return(visualize(Arrays.asList(vectors)));
  }
  
  /** Visualizes two-dimensional vectors */
  public static String visualize(List<SparseVector> vectors1, List<SparseVector> vectors2) {
    List<SparseVector> l=new ArrayList<SparseVector>();
    l.addAll(vectors1);
    l.addAll(vectors2);
    return(visualize(l));
  }
  
  /** Visualizes two-dimensional vectors */
  public static String visualize(SparseVector[] vectors1, SparseVector[] vectors2) {
    List<SparseVector> l=new ArrayList<SparseVector>();
    l.addAll(Arrays.asList(vectors1));
    l.addAll(Arrays.asList(vectors2));
    return(visualize(l));
  }
  
  /** Visualizes two-dimensional vectors */
  public static String visualize(List<SparseVector> vectors) {
    double xmax=0;
    double xmin=0;
    double ymax=0;
    double ymin=0;
    for(SparseVector v : vectors) {
      if(v.get(0)>xmax) xmax=v.get(0);
      if(v.get(0)<xmin) xmin=v.get(0);
      if(v.get(1)>ymax) ymax=v.get(1);
      if(v.get(1)<ymin) ymin=v.get(1);      
    }    
    if(xmax==xmin) xmax=xmin+10;
    if(ymax==ymin) ymax=ymin+10;
    final int screenx=80;
    final int screeny=24;
    StringBuilder result=new StringBuilder(screenx*screeny+10);
    for(int i=0;i<screenx-1;i++) result.append('-');
    result.append('\n');
    for(int lines=0;lines<screeny-2;lines++) {
      for(int col=0;col<screenx-2;col++) result.append(' ');
      result.append("|\n");
    }
    for(int i=0;i<screenx-1;i++) result.append('-');    
    
    result.setCharAt((int) ((0-xmin)/(xmax-xmin)*(screenx-2)+screenx+
        screenx*(0-ymin)/(ymax-ymin)*(screeny-2)),'X');
    
    for(SparseVector v : vectors) {
      result.setCharAt((int) ((v.get(0)-xmin)/(xmax-xmin)*(screenx-2)+screenx+
                        screenx*((int)((v.get(1)-ymin)/(ymax-ymin)*(screeny-2)))), v.charLabel());
    }    
    return(result.toString());
  }
  
  /** Returns a char label for the vector */
  public char charLabel() {
    if(label>=1 && label<10) return((char)(label+'0'));
    if(label>0) return('+');
    if(label<0) return('-');
    return('0');
  }
  
  /** Computes a distance of two vectors */
  public interface Distance {
    public double distance(SparseVector v1, SparseVector v2);
  }
  
  /** Computes the eucledian distance */
  public static final Distance eucledianDistance= new Distance() {
    public double distance(SparseVector v1, SparseVector v2) {
      return(v1.eucledianDistance(v2));
    }
  };

  /** Computes the cosine distance */
  public static final Distance cosineDistance= new Distance() {
    public double distance(SparseVector v1, SparseVector v2) {
      return(1-v1.cosine(v2));
    }
  };

  /** Does a simple K-means */
  public static void kMeans(SparseVector[] dots, SparseVector[] centers) {
    kMeans(dots, centers,eucledianDistance,0.1,10);
  }
  
  /** Does a simple K-means until the number of iterations is exceeded*/
  public static void kMeans(SparseVector[] dots, SparseVector[] centers, Distance distanceFunction, double epsilon, int iterations) {    
    while(iterations-->0) {
      //D.p("Iteration", iterations);
      int[] dot2center=new int[dots.length];      
      // Find for every dot its closest center
      for(int dot=0;dot<dots.length;dot++) {
        double bestDist=Double.MAX_VALUE;
        for(int center=0;center<centers.length;center++) {
          double dist=distanceFunction.distance(dots[dot],centers[center]);          
          if(dist<bestDist) {
            bestDist=dist;
            dot2center[dot]=center;
          }
        }
      }   
      // Backup and erase the centers
      SparseVector[] oldCenters=new SparseVector[centers.length];
      for(int center=0;center<centers.length;center++) {
        oldCenters[center]=centers[center];
        centers[center]=new SparseVector(centers[center].label,0); 
      }
      // Recompute the centers
      int[] numCenterMembers=new int[centers.length];
      for(int dot=0;dot<dots.length;dot++) {
        centers[dot2center[dot]].add(dots[dot]);
        numCenterMembers[dot2center[dot]]++;
      } 
      double maxEpsilon=0.0;
      for(int center=0;center<centers.length;center++) {
        if(numCenterMembers[center]!=0) { 
          centers[center].multiply(1.0/numCenterMembers[center]);
          double d=oldCenters[center].eucledianDistance(centers[center]);
          if(d>maxEpsilon) maxEpsilon=d;
        }
      }
      if(maxEpsilon<epsilon) break;
    }
  }
  
  /** Returns the distance to another vector */
  public double eucledianDistance(SparseVector v) {
    int myIndex=0;
    int vIndex=0;
    double result=0;
    while(vIndex<v.size || myIndex<size) {      
      if(vIndex>=v.size || myIndex<size && dim[myIndex]<v.dim[vIndex]) {
        result+=val[myIndex]*val[myIndex];
        myIndex++;
        continue;
      }
      if(myIndex>=size || vIndex<v.size && dim[myIndex]>v.dim[vIndex]) {
        result+=v.val[vIndex]*v.val[vIndex];
        vIndex++;
        continue;
      }      
      double d=v.val[vIndex]-this.val[myIndex];
      result+=d*d;
      myIndex++;
      vIndex++;
    }
    return(Math.sqrt(result));
  }
  
  /** Returns the index for a dimension (or -1)*/
  public int index(int dimension) {
    int pos=Arrays.binarySearch(dim, dimension);
    if(pos<0 || size<=pos) return(-1);
    else return(pos);
  }
  
  /** Adds a vector to this one */
  public SparseVector add(SparseVector v) {
    int addedDimensions=0;
    for(int i=0;i<v.size;i++) {
      if(index(v.dim[i])==-1) addedDimensions++;
    }   
    int[] newDim=addedDimensions==0?dim:new int[size()+addedDimensions];
    double[] newVal=addedDimensions==0&&!isBinary()?val:new double[size()+addedDimensions];
    int myIndex=0;
    int vIndex=0;       
    for(int i=0;i<size+addedDimensions;i++) {
      // Take my value
      if(vIndex>=v.size || myIndex<size && dim[myIndex]<v.dim[vIndex]) {
        newDim[i]=dim[myIndex];
        newVal[i]=val[myIndex];
        myIndex++;           
        continue;
      }
      // Take their value
      if(myIndex>=size || vIndex<v.size && dim[myIndex]>v.dim[vIndex]) {
        newDim[i]=v.dim[vIndex];
        newVal[i]=v.val[vIndex];
        vIndex++;           
        continue;        
      }
      newDim[i]=dim[myIndex];
      newVal[i]=val[myIndex]+v.val[vIndex];
      myIndex++;
      vIndex++;
    }
    val=newVal;
    dim=newDim;
    size+=addedDimensions;
    return(this);
  }
  
  /** Multiplies this vector by a scalar*/
  public SparseVector multiply(double r) {
    if(isBinary()) {
      val=new double[size()];
      for(int i=0;i<size;i++) val[i]=r;      
      return(this);
    }
    for(int i=0;i<val.length;i++) {
      val[i]=val[i]*r;
    }
    return(this);
  }

  /** Gives an iterator over the non-zero indices */
  public Iterator<Integer> nonZeroIndices() {    
    return new Iterator<Integer>() {
      int currentPos=0;
      public boolean hasNext() {
        return(currentPos<size());
      }

      public Integer next() {
        if(!hasNext()) throw new NoSuchElementException("Index "+currentPos);        
        return dim[currentPos++];
      }

      public void remove() {
        throw new UnsupportedOperationException();        
      }
      
    };
  }

  /** Clones a vector */
  public SparseVector clone() {
    return(new SparseVector(this));    
  }
  
  /** Test method */
  public static void main(String[] argv) throws Exception {
    /*FileLines l=new FileLines("c:\\fabian\\temp\\georgiana\\allCLUTO-TREC6_NOISEKM701-all-docs-terms-normalized.dat","Loading file");
    String firstLine=l.next();
    int num=Integer.parseInt(firstLine.substring(0,firstLine.indexOf(' ')));
    SparseVector[] vectors=new SparseVector[num];
    for(int i=0;i<num;i++) {
      String line=l.next();
      line="9 "+(line.replaceAll(" (\\d)\\.",":$1."));
      vectors[i]=new SparseVector(line);      
    }
    l.close();
    SparseVector[] centers=new SparseVector[37];
    for(int i=0;i<centers.length;i++) centers[i]=vectors[i];
    kMeans(vectors,centers,cosineDistance,0.1,Integer.MAX_VALUE);
    Announce.progressStart("Writing results",centers.length);
    Writer out=new BufferedWriter(new FileWriter("c:\\fabian\\temp\\georgiana\\centers2.txt"));    
    for(int i=0;i<centers.length;i++) {      
      D.writeln(out, centers[i].toString());
      D.writeln(out,"");
      Announce.progressAt(i);
    }
    out.close();
    Announce.progressDone();*/
    
    SparseVector v1a=new SparseVector(2,10,14);
    SparseVector v1b=new SparseVector(3,10,18);    
    SparseVector v2a=new SparseVector(4,20,30);
    SparseVector v2b=new SparseVector(5,25,32);
    SparseVector v3a=new SparseVector(6,20,10);
    SparseVector v3b=new SparseVector(7,23,12);    
    SparseVector c1=new SparseVector(8,15,13);
    SparseVector c2=new SparseVector(9,15,14);
    SparseVector[] centers=new SparseVector[]{c1,c2};
    SparseVector[] dots=new SparseVector[]{v1a,v1b,v2a,v2b,v3a,v3b};
    D.p(visualize(dots,centers));
    D.p("Press a key to move the centroids (the 8 and 9) according to k-means.");
    D.r();    
    kMeans(dots, centers, eucledianDistance, 0.1, 10);
    D.p(visualize(dots,centers));
  }

}
