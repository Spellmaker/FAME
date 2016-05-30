package de.uniulm.in.ki.mbrenner.fame.evaluation.framework;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by spellmaker on 25.05.2016.
 */
public interface SingleLevelWorkerFactory<T> {
    Callable<T> getWorker(File file, List<String> options);
    String getGreeting();
    String newResult(T result, int finishedTasks, int maxTasks);
    void printFinalResult();
}
