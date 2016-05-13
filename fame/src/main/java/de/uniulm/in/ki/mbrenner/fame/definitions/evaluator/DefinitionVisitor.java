package de.uniulm.in.ki.mbrenner.fame.definitions.evaluator;

/**
 * Created by spellmaker on 27.04.2016.
 */
public abstract class DefinitionVisitor {
    protected DefinitionEvaluator parent;

    public DefinitionVisitor(DefinitionEvaluator parent){
        this.parent = parent;
    }
}
