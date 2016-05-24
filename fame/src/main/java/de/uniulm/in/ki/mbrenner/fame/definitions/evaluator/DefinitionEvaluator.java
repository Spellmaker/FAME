package de.uniulm.in.ki.mbrenner.fame.definitions.evaluator;

import de.uniulm.in.ki.mbrenner.fame.definitions.CombinedObjectProperty;
import de.uniulm.in.ki.mbrenner.fame.definitions.IndicatorClass;
import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by spellmaker on 27.04.2016.
 */
public class DefinitionEvaluator{
    Map<OWLObject, OWLObject> definitions;
    Set<OWLObject> definingSymbols;
    Set<OWLEntity> signature;
    OWLDataFactory data;

    DefinitionAxiomVisitor axiomVisitor;
    DefinitionObjectPropertyVisitor propertyVisitor;
    DefinitionClassVisitor classVisitor;
    //OWLClass unknownClass;
    //OWLObjectProperty unknownProperty;

    @Deprecated
    Set<OWLObject> usedDefinitions;

    public DefinitionEvaluator(){
        this.axiomVisitor = new DefinitionAxiomVisitor(this);
        this.propertyVisitor = new DefinitionObjectPropertyVisitor(this);
        this.classVisitor = new DefinitionClassVisitor(this);
        data = new OWLDataFactoryImpl();

        //unknownClass = data.getOWLClass(IRI.create("?UNKNOWNCLASS?"));
        //unknownProperty = data.getOWLObjectProperty(IRI.create("?UNKNOWNPROPERTY?"));
    }

    private void init(Set<OWLEntity> signature, Map<OWLObject, OWLObject> definitions){
        this.signature = signature;
        this.definitions = definitions;
        this.usedDefinitions = new HashSet<>();
        this.definingSymbols = new HashSet<>(definitions.values());
    }

    public OWLClassExpression getDefined(OWLClassExpression c, Set<OWLEntity> signature, Map<OWLObject, OWLObject> definitions){
        init(signature, definitions);
        c.accept(classVisitor);
        return classVisitor.currentClass;
    }

    public OWLPropertyExpression getDefined(OWLPropertyExpression c, Set<OWLEntity> signature, Map<OWLObject, OWLObject> definitions){
        init(signature, definitions);
        c.accept(propertyVisitor);
        return propertyVisitor.currentProperty;
    }

    public boolean isDefinitionLocal(OWLAxiom axiom, Set<OWLEntity> signature, Map<OWLObject, OWLObject> definitions) {
        init(signature, definitions);
        axiom.accept(axiomVisitor);

        return axiomVisitor.locality;
    }

    @Deprecated
    public Set<OWLObject> getUsedDefinitions(){
        return usedDefinitions;
    }

    public boolean isFinalSymbol(OWLObject o){
        return signature.contains(o) || o instanceof IndicatorClass || o instanceof CombinedObjectProperty;
    }
}
