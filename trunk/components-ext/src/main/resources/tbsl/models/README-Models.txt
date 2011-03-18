Stanford POS Tagger, v. 2.0 - 23 Dec 2009.
Copyright (c) 2002-2009 The Board of Trustees of
The Leland Stanford Junior University. All Rights Reserved.

This document contains (some) information about the models included in
this release and that may be downloaded for the POS tagger website at
http://nlp.stanford.edu/software/tagger.shtml .  If you have downloaded
the full tagger, all of the models mentioned in this document are in the
downloaded package in the same directory as this readme.  Otherwise,
included in the download are two 
English taggers, and the other taggers may be downloaded from the
website.  All taggers are accompanied by the props files used to create
them; please examine these files for more detailed information about the
creation of the taggers.

For English, the bidirectional taggers are slightly more accurate, but
tag much more slowly; choose the appropriate tagger based on your
speed/performance needs.

English taggers
---------------------------
bidirectional-distsim-wsj-0-18.tagger
Trained on WSJ sections 0-18 using a bidirectional architecture and
including word shape and distributional similarity features.
Penn Treebank tagset.
Performance:
97.28% correct on WSJ 19-21
(90.46% correct on unknown words)

left3words-wsj-0-18.tagger
Trained on WSJ sections 0-18 using the left3words architecture and
includes word shape features.  Penn tagset.
Performance:
96.97% correct on WSJ 19-21
(88.85% correct on unknown words)

left3words-distsim-wsj-0-18.tagger
Trained on WSJ sections 0-18 using the left3words architecture and
includes word shape and distributional similarity features. Penn tagset.
Performance:
97.01% correct on WSJ 19-21
(89.81% correct on unknown words)


Chinese tagger
---------------------------
chinese.tagger
Trained on a combination of Chinese Treebank texts from Chinese and Hong
Kong sources. 
LDC Chinese Treebank POS tag set.
Performance:
94.13% on a combination of Chinese and Hong Kong texts
(78.92% on unknown words)

Arabic tagger
---------------------------
arabic.tagger
Trained on the train part of the ATB p1-3 split done for the 2005 JHU
Summer Workshop (Diab split), using (augmented) Bies tags.
(Augmented) Bies mapping of Penn Arabic Treebank tags
Performance:
96.42% on dev portion according to Diab split
(80.45% on unknown words)


German tagger
---------------------------
Trained on the first 80% of the Negra corpus, which uses the STTS tagset.
The Stuttgart-Tübingen Tagset (STTS) is a set of 54 tags for annotating
German text corpora with part-of-speech labels, which was jointly
developed by the Institut für maschinelle Sprachverarbeitung of the
University of Stuttgart and the Seminar für Sprachwissenschaft of the
University of Tübingen. See: 
http://www.ims.uni-stuttgart.de/projekte/CQPDemos/Bundestag/help-tagset.html
Performance:
96.91% on the first half of the remaining 20% of the Negra corpus (dev set)
(90.41% on unknown words)
