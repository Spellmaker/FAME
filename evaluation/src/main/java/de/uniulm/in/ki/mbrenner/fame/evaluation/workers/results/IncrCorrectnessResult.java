package de.uniulm.in.ki.mbrenner.fame.evaluation.workers.results;

import de.uniulm.in.ki.mbrenner.fame.util.ModuleDiff;

/**
 * Created by spellmaker on 24.03.2016.
 */
public class IncrCorrectnessResult {
    public ModuleDiff diff;
    public int iteration;
    public int incrCases;

    public IncrCorrectnessResult(ModuleDiff diff, int iteration, int incrCases){
        this.diff = diff;
        this.iteration = iteration;
        this.incrCases = incrCases;
    }

    public String toString(){
        return "Diff: " + diff + " Iterations: " + iteration + " IncrCases: " + incrCases;
    }
}
