package org.dllearner.utils.spatial;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.dllearner.algorithms.spatial.SpatialLearningAlgorithm;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.kb.OWLFile;
import org.dllearner.learningproblems.PosNegLPStandard;
import org.dllearner.reasoning.ClosedWorldReasoner;
import org.dllearner.reasoning.spatial.SpatialReasoner;
import org.dllearner.reasoning.spatial.SpatialReasonerPostGIS;
import org.dllearner.refinementoperators.spatial.SpatialRhoDRDown;
import org.dllearner.utilities.MapUtils;
import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.owlapi.OWLDataPropertyImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLNamedIndividualImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectPropertyImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

public class ExperimentsCarFriendlyHotel {
    private static List<OWLObjectProperty> propertyPathToGeom =
            Lists.newArrayList(
                    new OWLObjectPropertyImpl(
                            IRI.create("http://www.opengis.net/ont/geosparql#hasGeometry")));
    private static OWLDataProperty wktLiteralDTypeProperty =
            new OWLDataPropertyImpl(IRI.create("http://www.opengis.net/ont/geosparql#asWKT"));

    public static void main(String[] args) throws ComponentInitException {
        String ksFilePath = args[0];

        KnowledgeSource ks = new OWLFile(ksFilePath);
        ks.init();

        ClosedWorldReasoner baseReasoner = new ClosedWorldReasoner(ks);
        baseReasoner.init();

        SpatialReasonerPostGIS reasoner = new SpatialReasonerPostGIS();
        reasoner.setDBUser("dllearner");
        reasoner.setDBName("dllearner");
        reasoner.setDBUserPW(args[1]);
        reasoner.setNearRadiusInMeters(10);
        reasoner.setReasoner(baseReasoner);

        List<OWLProperty> geometryPropertyPath = new ArrayList<>();
        geometryPropertyPath.addAll(propertyPathToGeom);
        geometryPropertyPath.add(wktLiteralDTypeProperty);
        reasoner.addGeometryPropertyPath(geometryPropertyPath);

        reasoner.init();

        SpatialRhoDRDown refinementOperator = new SpatialRhoDRDown();
        refinementOperator.setReasoner((SpatialReasoner) reasoner);
        refinementOperator.init();

        PosNegLPStandard lp = new PosNegLPStandard();
        Set<OWLIndividual> posExamples = Sets.newHashSet(
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel-6440836726638665545")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel3934954197688329242")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel1718281690305955521")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel357475226688654422")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel-7454719027322185827")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel-6041424227102816387")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel9123696420902748760")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel6525034968136650056")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel3667843772905725028")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel2790328930149134998")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel-5993132210459476838")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel4191706663978600436")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel-1874686094548561578")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel971424768524113271")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel6941251182657411780")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel7013518996727996550")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel-5824544601679338660")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel-1487844843483915595")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel-6003517687151258100")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel-3335742447263447324")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel333280982591679221")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel-8328739161030963559")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel-5678835376156935195")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel-6022089302650128911")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel6193671120191481305")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel-9189888807996626313")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel1827093404646897857")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel-3788225700401619825")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel-7400709952202273246")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel-1545596451851383980")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel-607060508446650977")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel5165721166174332873")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel-659540773218394385")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel-7085498415730327052")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel-3796057026684013477")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel4202220868455136011")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel343940880155641500")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel-4704330005705705798")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel-7004845346947068590")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel7779030511847414746")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel-7158558857848672440")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel-3325727528124814349")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel-88469904203863610")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel-7689971396081568983")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel1703641542433227098")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel-221556040915131721")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel2112100869249051253")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel919706980250551932")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel-2953276102583853114")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel5949370394063553934")));
        lp.setPositiveExamples(posExamples);

        Set<OWLIndividual> negExamples = Sets.newHashSet(
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel8525814226893293099")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel6195237671070210805")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel3348484644781776582")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel-5803583751099493629")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel-775444247044908276")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel-2255848902299147185")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel123586441958350687")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel-4112838819671364483")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel-4379188785599556074")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel-1057554283687568710")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel-8542280361076229689")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel1733394713479734034")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel7370759911936281366")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel-10933970469251427")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel1826529656728899074")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel7258164971764430480")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel-2795779090957840195")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel-420328536939638807")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel7806725488744114704")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel7460488433315408914")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel-2890534778398215002")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel4116570185202790253")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel7492235277633662506")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel5982053985467193334")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel-8784751568342467264")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel-9053432935417973518")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel5168297779356554203")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel-2843307601350962536")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel-7040345048132393194")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel1590798809455187501")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel6791205627305676857")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel883388622212197156")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel-1139161400711442153")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel-2789449466649427998")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel1360954105927423002")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel-8500917594131406459")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel7030208854469740798")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel5670191169077870749")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel5224088894394140232")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel-81654734681040613")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel-3347585212876581028")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel7165017497398260733")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel-2381980959868666780")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel5613100074846655754")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel1498443320652107010")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel5192219179923490067")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel3373891115856026484")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel5145191841617119163")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel-2424383403857285608")),
            new OWLNamedIndividualImpl(IRI.create("http://dl-learner.org/spatial#feature_hotel7463885564619498730")));
        lp.setNegativeExamples(negExamples);
        lp.setReasoner(reasoner);
        lp.init();

        SpatialLearningAlgorithm alg = new SpatialLearningAlgorithm();
        alg.setLearningProblem(lp);
        alg.setOperator(refinementOperator);
        alg.setMaxExecutionTimeInSeconds(300);
//        alg.setNoisePercentage(5);
        alg.setReasoner(reasoner);
        alg.setKeepTrackOfBestScore(true);
//        alg.setStartClass(new OWLClassImpl(IRI.create("http://dl-learner.org/spatial#SpatialFeature")));
        alg.init();
        alg.start();

        SortedMap<Long, Double> map = alg.getRuntimeVsBestScore(1, TimeUnit.SECONDS);
        System.out.println(MapUtils.asTSV(map, "runtime", "best_score"));

        alg.getCurrentlyBestEvaluatedDescriptions(20).stream().forEach(System.out::println);
    }
}
