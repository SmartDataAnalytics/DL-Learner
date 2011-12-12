package org.dllearner.utilities;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;

/**
 * 
 * @author Uthaya
 *
 */
// n + v only
//David Hope, 2008, University Of Sussex

public class ICFinder
{
	private String[]					editor		=	null;
	private String					icfilename	=	"";
	private BufferedReader		in				=	null;
	private String					line			=	"";
// look up
	private Hashtable<String, Double>	lookup	=	null; // quick look up for synset counts (we require Double as Resnik counts are doubles)
// counts for nouns and verbs
	private	double					nouns_sum								=	0.0;
	private	double					verbs_sum								=	0.0;
	private	double					nounsandverbs_sum					=	0.0; // ** the ??? normaliser ??? ** for the 'getProbability' method
// <ROOTS> for nouns and verbs
	private	double					nounroot_sum	=	0.0;
	private	double					verbroot_sum	=	0.0;
	private	ArrayList<String>	nounroots			=	null;
	private	ArrayList<String>	verbroots			=	null;

	public ICFinder(String icfilename)
	{
		System.out.println("... calculating IC <roots> ...");
		System.out.println("... ICFinder");

// your IC file
		this.icfilename = icfilename;
// quick look up table
		lookup	=	new Hashtable<String, Double>();
// get some useful 'constants'
		nounroots = new ArrayList<String>();
		verbroots = new ArrayList<String>();
		Vector<Double> constants = setup();
		nouns_sum				=	constants.get(0);
		verbs_sum				=	constants.get(1);
		nounsandverbs_sum	=	( nouns_sum + verbs_sum );
		nounroot_sum			=	constants.get(2);
		verbroot_sum			=	constants.get(3);
	}

	public double getRootSum(String pos)
	{
		if(pos.equalsIgnoreCase("v"))
			return (verbroot_sum);
		return (nounroot_sum);
	}


// 'getFrequency': get the count for the {synset} from the IC file
	private double getFrequency(String synset, String pos)
	{
		if(lookup.containsKey(synset + pos))
			return ( lookup.get(synset + pos) );
    	return ( 0.0 );
	}

// 'getProbability': get the probability of the {synset}
	private double getProbability(String synset, String pos)
	{
		double 	freq			=	getFrequency(synset, pos);
		if(freq == 0.0)
			return ( 0.0 );

		double	probability	=	0.0;

		if(pos.equalsIgnoreCase("n"))
				probability = ( freq /  nounroot_sum );	// Ted Pedersen et al. use the sum of the noun<root> counts *not* the sum of the noun counts

		if(pos.equalsIgnoreCase("v"))
				probability = ( freq / verbroot_sum );		// Ted Pedersen et al. use the sum of the verb<root> counts *not* the sum of the verb counts

		return ( probability );
	}


// does all / any type of synset i.e. standard synset | <lcs> synset
// !!! we are using the notion of a 'fake'<root> as per the Perl implementation !!!
// !!! there is no option to turn the 'fake'<root> off in this implementation - it all gets a bit silly (hard to justify) if we do this !!!
	public double getIC(String synset, String pos)
	{
		double ic = 0.0;
// Case 1. There is *no* <lcs> ...............................................................................................................................................
//  If the 'synset' is empty (null Object or an empty String), - this implies that no <lcs>|synset was found for a (pair of synsets) and thus,
//  they must join at an 'imaginary' <root> point in the WordNet space (tree). We call this the'fake'<root>.
//  Further, *if* we are assuming a 'fake' root' (which we do; we default to it as per the Perl implementation), - this implies
//  that it subsumes all other <roots>. This being the case, the 'fake'<root> must then have an Information Content(ic) value of 0
//  as it provides us with zero information
		if(synset == null || synset.length() == 0)
		{
			return ( ic );
		}
// .......................................................................................................................................................................................
// Case 2. There is an <lcs> but it has a frequency of zero and thus it has a probability of zero and thus is just not valid as input
// to the Information Content equation ( we will get 'Infinity') - so, we simply return 0
		double p			 	=	getProbability(synset, pos);
		if(p == 0.0)
		{
			return ( ic );
		}
		else
		{
			ic = -Math.log(p);
		}
// .......................................................................................................................................................................................
// Case 3. There is an <lcs>, -- it may be a <root> or it may be a boring old synset but - it does have a frequency, thus it does have
//  a probability and thus we may calculate the Information Content for this synset. If the synset is a <root> and there is only 1 such
// <root> for the POS, then, effectively the Information Contente will be zero, otherwise we should get a value that is greater than zero
    	return ( ic );
	}

// utility: get counts for {synsets} | just nouns | just verbs | noun'fake'<root> | verb'fake'<root>
// these are used to calculate probabilities of {synsets} and to 'back-off' to a <root> value if no LCS exists for 2 words
	private Vector<Double> setup()
	{
		String	unit	=	"";
		double	uc		=	0.0;
		double	nc		=	0.0;
		double	vc		=	0.0;
		double	nrc	=	0.0;
		double	vrc	=	0.0;
		Vector<Double>	counts = new Vector<Double>();
    	try
    	{
        	in = new BufferedReader(new FileReader(icfilename));
        	while ((line = in.readLine()) != null)
        	{
				editor = line.split("\\s"); // IC files are space delimited
				for(int i = 0; i < editor.length; i++)
				{
					unit	=	editor[i];
// nouns
					if(unit.endsWith("n"))
					{
						lookup.put(editor[0], Double.parseDouble(editor[1]));
						uc =		Double.parseDouble(editor[1]); // get the value: the 'count' for the {synset}
						nc	+=	uc;// add to noun total
						if(editor.length == 3) // if ROOT
						{
							nrc += uc;// add to noun<root> total
							// store noun <root>
							nounroots.add(editor[0].substring(0,editor[0].length()-1));
						}
					}else if(unit.endsWith("v")) // verbs
					{
						lookup.put(editor[0], Double.parseDouble(editor[1]));
						uc =		Double.parseDouble(editor[1]); // get the value: the 'count' for the {synset}
						vc	+=	uc; // add to verb total
						if(editor.length == 3) // if ROOT
						{
							vrc += uc; // add to verb<root> total
							// store verb<root>
							verbroots.add(editor[0].substring(0,editor[0].length()-1));
						}
					}/*else{
						System.err.println("Adj? "+ unit);
					}*/
				}
        	}
        in.close();
    	}
    	catch (IOException e){e.printStackTrace();}
    	counts.add(nc); counts.add(vc); counts.add(nrc);	counts.add(vrc);
    	return ( counts );
	}

	public ArrayList<String>	getNounRoots()
	{
		return ( nounroots );
	}
	public ArrayList<String>	getVerbRoots()
	{
		return ( verbroots );
	}
}
