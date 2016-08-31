package de.uniulm.in.ki.mbrenner.fame.definitions;

import de.uniulm.in.ki.mbrenner.fame.definitions.evaluator.DefinitionEvaluator;
import de.uniulm.in.ki.mbrenner.fame.simple.extractor.RBMExtractorNoDef;
import de.uniulm.in.ki.mbrenner.fame.simple.rule.RuleBuilder;
import org.semanticweb.owlapi.model.*;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Class which uses the DefinitionEvaluator to determine if a set of axioms really is a correct module
 *
 * Created by spellmaker on 25.05.2016.
 */
public class TestCorrectness {
    /**
     * Tests if the provided module is correct
     * @param ontology The ontology from which the module has been extracted
     * @param module The module
     * @param definitions The definitions used to make the remaining axioms definition local
     * @param signature The signature for which the module is supposed to be definition local
     * @return A set of axioms which are not definition local but are also not part of the module
     */
    public static @Nonnull
    Collection<OWLAxiom> isDefinitionLocalModule(@Nonnull OWLOntology ontology, @Nonnull Set<OWLAxiom> module,
                                                 @Nonnull Map<OWLObject, OWLObject> definitions, @Nonnull Set<OWLEntity> signature){
        Set<OWLEntity> extSignature = module.stream().map(OWLAxiom::getSignature).flatMap(Collection::stream).collect(Collectors.toSet());
        extSignature.addAll(signature);

        DefinitionEvaluator eval = new DefinitionEvaluator();
        return ontology.getAxioms().stream().
                filter(x -> x instanceof OWLLogicalAxiom).                      //ignore non logical axioms
                filter(x -> !module.contains(x)).                               //ignore axioms included in the module
                filter(x -> !eval.isDefinitionLocal(x, extSignature, definitions)).//ignore axioms which are definition local
                collect(Collectors.toList());
    }

    public static @Nonnull Collection<OWLAxiom> isProperSubset(@Nonnull OWLOntology ontology, @Nonnull Set<OWLAxiom> module,
                                                               @Nonnull Set<OWLEntity> signature){
        Set<OWLAxiom> botModule = new RBMExtractorNoDef().extractModule(new RuleBuilder().buildRules(ontology), signature).
                stream().filter(x -> x instanceof OWLLogicalAxiom).collect(Collectors.toSet());

        return module.stream().filter(x -> x instanceof OWLLogicalAxiom).filter(x -> !botModule.contains(x)).collect(Collectors.toSet());
    }
}
