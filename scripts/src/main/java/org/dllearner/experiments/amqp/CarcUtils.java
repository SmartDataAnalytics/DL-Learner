package org.dllearner.experiments.amqp;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLIndividual;

import uk.ac.manchester.cs.owl.owlapi.OWLNamedIndividualImpl;

import com.google.common.collect.Sets;

public class CarcUtils {
	static Set<OWLIndividual> buildPos() {
		HashSet<OWLIndividual> pos = Sets.newHashSet(
				(OWLIndividual) new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d1")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d10")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d101")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d102")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d103")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d106")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d107")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d108")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d11")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d12")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d13")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d134")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d135")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d136")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d138")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d140")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d141")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d144")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d145")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d146")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d147")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d15")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d17")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d19")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d192")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d193")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d195")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d196")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d197")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d198")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d199")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d2")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d20")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d200")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d201")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d202")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d203")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d204")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d205")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d21")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d22")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d226")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d227")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d228")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d229")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d231")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d232")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d234")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d236")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d239")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d23_2")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d242")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d245")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d247")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d249"))
		);

		return pos;
	}

	static Set<OWLIndividual> buildNeg() {
		HashSet<OWLIndividual> neg = Sets.newHashSet(
				(OWLIndividual) new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d110")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d111")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d114")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d116")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d117")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d119")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d121")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d123")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d124")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d125")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d127")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d128")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d130")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d133")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d150")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d151")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d154")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d155")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d156")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d159")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d160")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d161")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d162")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d163")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d164")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d165")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d166")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d169")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d170")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d171")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d172")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d173")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d174")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d178")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d179")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d180")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d181")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d183")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d184")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d185")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d186")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d188")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d190")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d194")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d207")),
				new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/carcinogenesis#d208_1"))
		);

		return neg;
	}
}
