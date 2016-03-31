package de.uniulm.in.ki.mbrenner.fame.evaluation.workers.results;

/**
 * Created by spellmaker on 24.03.2016.
 */
public class IncrTimeBothResult {
    public long incremental;
    public long normal;

    public IncrTimeBothResult(IncrTimeResult incremental, IncrTimeResult normal){
        this.incremental = incremental.time;
        this.normal = normal.time;
        if(this.incremental < 0 || this.normal < 0){
            this.incremental = -1;
            this.normal = -1;
        }
    }
}
