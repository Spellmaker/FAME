package de.uniulm.in.ki.mbrenner.fame.evaluation;

import de.uniulm.in.ki.mbrenner.fame.evaluation.workers.PreparationWorker;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by spellmaker on 11.03.2016.
 */
public class ModulePreparation implements EvaluationCase{
    @Override
    public void evaluate(List<File> ontologies, List<String> options) throws Exception {
        EvaluationMain.out.println("pre-calculating modules for " + ontologies.size() + " ontologies");
        Path outDir = Paths.get(options.get(0));
        ExecutorService genPool = Executors.newFixedThreadPool(2);
        ExecutorService subPool = Executors.newFixedThreadPool(3);

        int cnt = -1;
        if(options.size() >= 2){
            cnt = Integer.parseInt(options.get(1));
        }


        List<PreparationWorker> workers = new ArrayList<>(ontologies.size());

        for(File f : ontologies){
            PreparationWorker w = new PreparationWorker(subPool, f, outDir, cnt);
            workers.add(w);
            genPool.submit(w);
        }

        int size = workers.size();
        while(!workers.isEmpty()){
            for(int i = 0; i < workers.size(); i++){
                if(workers.get(i).isDone()){
                    EvaluationMain.out.println("finished ontology " + (1 + size - workers.size() + "/" + size));
                    workers.remove(i);
                    i--;
                }
            }
        }

        genPool.shutdown();
        subPool.shutdown();
        EvaluationMain.out.println("thread pool terminated");
    }
}
