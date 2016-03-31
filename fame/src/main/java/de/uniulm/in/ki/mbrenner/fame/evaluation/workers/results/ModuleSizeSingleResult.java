package de.uniulm.in.ki.mbrenner.fame.evaluation.workers.results;

/**
 * Created by spellmaker on 23.03.2016.
 */
public class ModuleSizeSingleResult {
    public boolean doDef;

    public int size;
    public int logical_size;

    public ModuleSizeSingleResult(boolean doDef, int size, int logical_size) {
        this.doDef = doDef;
        this.size = size;
        this.logical_size = logical_size;
    }
}
