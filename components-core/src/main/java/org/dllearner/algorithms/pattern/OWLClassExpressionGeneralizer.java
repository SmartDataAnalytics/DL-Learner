package org.dllearner.algorithms.pattern;

import org.dllearner.core.owl.OWLObjectIntersectionOfImplExt;
import org.dllearner.core.owl.OWLObjectUnionOfImplExt;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.semanticweb.owlapi.util.OWLClassExpressionVisitorExAdapter;
import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

/**
 * @author Lorenz Buehmann
 */
public class OWLClassExpressionGeneralizer extends OWLClassExpressionVisitorExAdapter<Stream<OWLClassExpression>> {

    private AtomicInteger cnt = new AtomicInteger(1);

    private OWLDataFactory df;
    private String NS = "http://dl-learner.org/pattern/";
    private PrefixManager pm = new DefaultPrefixManager();

    private final OWLClass CE = new OWLClassImpl(IRI.create(NS + "CE"));
    private final OWLClass INTERSECTION = new OWLClassImpl(IRI.create(NS + "INTERSECTION"));
    private final OWLClass UNION = new OWLClassImpl(IRI.create(NS + "UNION"));

    public OWLClassExpressionGeneralizer(OWLDataFactory df) {
        super(Stream.empty());
        this.df = df;

        pm.setDefaultPrefix(NS);
    }

    @Nonnull
    @Override
    protected Stream<OWLClassExpression> doDefault(@Nonnull OWLClassExpression ce) {
        return Stream.of(ce);
    }

    public OWLClassExpressionGeneralizer() {
        this(new OWLDataFactoryImpl());
    }

    Function<OWLEntity, OWLEntity> classRenamingFn = k -> k;

    public Set<OWLClassExpression> generalize(OWLClassExpression ce) {
        cnt.set(1);

        Stream<OWLClassExpression> genCEs = ce.accept(this);

//        OWLClassExpressionRenamer renamer = new OWLClassExpressionRenamer(df, new HashMap<>());
//        renamer.setClassRenamingFn(classRenamingFn);

        return genCEs.map(genCE -> {
            OWLClassExpressionRenamer renamer = new OWLClassExpressionRenamer(df, new HashMap<>());
            renamer.setClassRenamingFn(classRenamingFn);
            renamer.reset();
            return renamer.rename(genCE);
        }).collect(Collectors.toCollection(TreeSet::new));
    }

    @Override
    public Stream<OWLClassExpression> visit(OWLClass ce) {
        return Stream.of(newCE());
    }

    @Override
    public Stream<OWLClassExpression> visit(OWLObjectSomeValuesFrom ce) {
        Stream<OWLClassExpression> res = ce.getFiller().accept(this)
                .map(f -> df.getOWLObjectSomeValuesFrom(ce.getProperty(), f));
//        res.add(newCE());
        return res;
    }

    @Override
    public Stream<OWLClassExpression> visit(OWLDataSomeValuesFrom ce) {
        return Stream.of(ce);
    }

    @Override
    public Stream<OWLClassExpression> visit(OWLObjectAllValuesFrom ce) {
        Stream<OWLClassExpression> res = ce.getFiller().accept(this)
                .map(f -> df.getOWLObjectAllValuesFrom(ce.getProperty(), f));
//        res.add(newCE());
        return res;
    }

    @Override
    public Stream<OWLClassExpression> visit(OWLDataAllValuesFrom ce) {
        return Stream.of(ce);
    }

    @Override
    public Stream<OWLClassExpression> visit(OWLObjectMinCardinality ce) {
        return (ce.getCardinality() == 1)
                ? df.getOWLObjectSomeValuesFrom(ce.getProperty(), ce.getFiller()).accept(this)
                : ce.getFiller().accept(this)
                    .map(f -> df.getOWLObjectMinCardinality(ce.getCardinality(), ce.getProperty(), f));
    }

    @Override
    public Stream<OWLClassExpression> visit(OWLObjectMaxCardinality ce) {
        return ce.getFiller().accept(this)
                .map(f -> df.getOWLObjectMaxCardinality(ce.getCardinality(), ce.getProperty(), f));
    }

    @Override
    public Stream<OWLClassExpression> visit(OWLObjectExactCardinality ce) {
        return ce.getFiller().accept(this)
                .map(f -> df.getOWLObjectExactCardinality(ce.getCardinality(), ce.getProperty(), f));
    }

    @Override
    public Stream<OWLClassExpression> visit(OWLDataMinCardinality ce) {
        return Stream.of(ce);
    }

    @Override
    public Stream<OWLClassExpression> visit(OWLDataMaxCardinality ce) {
        return Stream.of(ce);
    }

    @Override
    public Stream<OWLClassExpression> visit(OWLDataExactCardinality ce) {
        return Stream.of(ce);
    }

    @Override
    public Stream<OWLClassExpression> visit(OWLObjectIntersectionOf ce) {
        return Stream.concat(processNaryBooleanClassExpression(ce), Stream.of(newAnd()));
    }

    private OWLClass newAnd() {
        return df.getOWLClass("AND", pm);
    }

    @Override
    public Stream<OWLClassExpression> visit(OWLObjectUnionOf ce) {
        return Stream.concat(processNaryBooleanClassExpression(ce), Stream.of(UNION));
    }

    private Stream<OWLClassExpression> processNaryBooleanClassExpression(OWLNaryBooleanClassExpression ce) {
        try {
            List<OWLClassExpression> operands = ce.getOperandsAsList();

            // split by atomic class and complex CE
            Map<Boolean, List<OWLClassExpression>> operandsSplit = operands.stream().collect(Collectors.partitioningBy(OWLClassExpression::isAnonymous));

            List<OWLClassExpression> operandsAtomic = operandsSplit.get(false);
            List<OWLClassExpression> operandsComplex = operandsSplit.get(true);

            // compute the generalizations for the complex CEs
            Map<OWLClassExpression, List<OWLClassExpression>> operandToGeneralizations = operandsComplex.stream().collect(
                    Collectors.toMap(
                            Function.identity(),
                            op -> op.accept(OWLClassExpressionGeneralizer.this).collect(Collectors.toList())));

            if(!operandsAtomic.isEmpty()) {
                operandsComplex.add(operandsAtomic.get(0));
                operandToGeneralizations.put(operandsAtomic.get(0), Collections.singletonList(CE));
            }

            Stream<List<OWLClassExpression>> combinations = getCombinationsStream(operandsComplex);

            final Constructor<? extends OWLNaryBooleanClassExpression> constructor = ce.getClass().getConstructor(Set.class);

            return combinations.flatMap(c -> {
                List<List<OWLClassExpression>> opsGen = c.stream().map(op -> operandToGeneralizations.get(op)).collect(Collectors.toList());

                Set<List<OWLClassExpression>> operandCombinations = getCombinations(opsGen);

                return operandCombinations.stream().map(opC -> {
                    // add placeholder CE for missing operands
                    if (opC.size() < operands.size()) {
                        opC.add(newCE());
                    }

                    Set<OWLClassExpression> newOperands = new HashSet<>(opC);

                    try {
                        return (newOperands.size() == 1) ? newOperands.iterator().next() :
                                ((ce.getClassExpressionType() == ClassExpressionType.OBJECT_INTERSECTION_OF)
                                        ? new OWLObjectIntersectionOfImplExt(opC)
                                        : new OWLObjectUnionOfImplExt(opC));
//                                (OWLClassExpression)constructor.newInstance(newOperands);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                });

            });
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("Failed to process boolean CE");
    }

    private OWLClassExpression newCE() {
//        return df.getOWLClass("CE" + cnt.getAndIncrement(), pm);
        return CE;
    }

    public static <T> Stream<List<T>> getCombinationsStream(List<T> list) {
        // there are 2 ^ list.size() possible combinations
        // stream through them and map the number of the combination to the combination
        return LongStream.range(1 , 1 << list.size())
                .mapToObj(l -> bitMapToList(l, list));
    }

    public static <T> List<T> bitMapToList(long bitmap, List<T> list) {
        // use the number of the combination (bitmap) as a bitmap to filter the input list
        return IntStream.range(0, list.size())
                .filter(i -> 0 != ((1 << i) & bitmap))
                .mapToObj(list::get)
                .collect(Collectors.toList());
    }

    public static <T> Set<List<T>> getCombinations(List<List<T>> lists) {
        Set<List<T>> combinations = new HashSet<List<T>>();
        Set<List<T>> newCombinations;

        int index = 0;

        // extract each of the integers in the first list
        // and add each to ints as a new list
        for(T i: lists.get(0)) {
            List<T> newList = new ArrayList<T>();
            newList.add(i);
            combinations.add(newList);
        }
        index++;
        while(index < lists.size()) {
            List<T> nextList = lists.get(index);
            newCombinations = new HashSet<List<T>>();
            for(List<T> first: combinations) {
                for(T second: nextList) {
                    List<T> newList = new ArrayList<T>();
                    newList.addAll(first);
                    newList.add(second);
                    newCombinations.add(newList);
                }
            }
            combinations = newCombinations;

            index++;
        }

        return combinations;
    }

    private static <T> Stream<T> cartesian(BinaryOperator<T> aggregator,
                                           Supplier<Stream<T>>... streams) {
        return Arrays.stream(streams)
                .reduce((s1, s2) ->
                        () -> s1.get().flatMap(t1 -> s2.get().map(t2 -> aggregator.apply(t1, t2))))
                .orElse(Stream::empty).get();
    }
}
