package de.uniulm.in.ki.mbrenner.fame.evaluation.workers.results;

/**
 * Created by spellmaker on 23.03.2016.
 */
public class ModuleSizeResult {
    public double size_ndef_max;
    public double size_ndef_avg;
    public double size_def_max;
    public double size_def_avg;
    public double size_ndef_max_logical;
    public double size_ndef_avg_logical;
    public double size_def_max_logical;
    public double size_def_avg_logical;
    public boolean hasEq;

    public ModuleSizeResult(){
        this.hasEq = false;
    }

    public ModuleSizeResult(double size_ndef_max, double size_ndef_avg, double size_def_max, double size_def_avg, double size_ndef_max_logical, double size_ndef_avg_logical, double size_def_max_logical, double size_def_avg_logical) {
        this.size_ndef_max = size_ndef_max;
        this.size_ndef_avg = size_ndef_avg;
        this.size_def_max = size_def_max;
        this.size_def_avg = size_def_avg;
        this.size_ndef_max_logical = size_ndef_max_logical;
        this.size_ndef_avg_logical = size_ndef_avg_logical;
        this.size_def_max_logical = size_def_max_logical;
        this.size_def_avg_logical = size_def_avg_logical;
        this.hasEq = true;
    }

    public double getMaxPercent(){
        return getPercent(size_ndef_max, size_def_max);
    }

    public double getAvgPercent(){
        return getPercent(size_ndef_avg, size_def_avg);
    }

    public double getMaxPercentLogical(){
        return getPercent(size_ndef_max_logical, size_def_max_logical);
    }

    public double getAvgPercentLogical(){
        return getPercent(size_ndef_avg_logical, size_def_avg_logical);
    }

    private static double getPercent(double full, double partial){
        if(full == 0) return 0;
        return 100 - (100 * partial) / full;
    }
}
