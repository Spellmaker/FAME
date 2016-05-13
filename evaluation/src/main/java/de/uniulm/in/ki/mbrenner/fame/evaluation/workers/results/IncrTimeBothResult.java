package de.uniulm.in.ki.mbrenner.fame.evaluation.workers.results;

/**
 * Created by spellmaker on 24.03.2016.
 */
public class IncrTimeBothResult {
    public long incremental;
    public long normal;
    public long half;

    public IncrTimeBothResult(IncrTimeResult incremental, IncrTimeResult normal, IncrTimeResult half){
        this.incremental = incremental.time;
        this.normal = normal.time;
        this.half = half.time;
        if(this.incremental < 0 || this.normal < 0 || this.half < 0){
            this.incremental = -1;
            this.normal = -1;
            this.half = -1;
        }
    }
}
