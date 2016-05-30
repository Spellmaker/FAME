package de.uniulm.in.ki.mbrenner.fame.evaluation.framework;

import de.uniulm.in.ki.mbrenner.fame.evaluation.EvaluationCase;
import de.uniulm.in.ki.mbrenner.fame.evaluation.EvaluationMain;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by spellmaker on 25.05.2016.
 */
public class SingleLevelEvaluationCase<T> implements EvaluationCase {
    private SingleLevelWorkerFactory<T> workerFactory;
    private String parameter;
    private String help;

    public SingleLevelEvaluationCase(SingleLevelWorkerFactory<T> workerFactory, String parameter, String help){
        this.workerFactory = workerFactory;
        this.parameter = parameter;
        this.help = help;
    }

    @Override
    public String getParameter() {
        return parameter;
    }

    @Override
    public String getHelpLine() {
        return help;
    }

    public void evaluate(List<File> ontologies, List<String> options) throws Exception{
        Path outDir = null;
        if(!options.isEmpty()) Paths.get(options.get(0));
        List<String> subOptions = options.size() > 1 ? options.subList(1, options.size()) : Collections.emptyList();

        EvaluationMain.out.println(workerFactory.getGreeting());
        ExecutorService pool = Executors.newFixedThreadPool(5);

        List<Future<T>> futures = new LinkedList<>();
        Map<Future<T>, File> fileMap = new HashMap<>();
        for(int i = 0; i < ontologies.size(); i++){
            File cFile = ontologies.get(i);
            try {
                Future<T> f = pool.submit(workerFactory.getWorker(cFile, subOptions));
                futures.add(f);
                fileMap.put(f, cFile);
            }
            catch(Exception e){
                EvaluationMain.out.println("Could not initialize task for file " + cFile + ": " + e);
            }
        }

        while(!futures.isEmpty()){
            for(int i = 0; i < futures.size(); i++){
                Future<T> cFuture = futures.get(i);
                if(cFuture.isDone()){
                    futures.remove(i--);
                    try {
                        T res = cFuture.get();
                        String csv = workerFactory.newResult(res, ontologies.size() - futures.size(), ontologies.size());
                        if(outDir != null) Files.write(outDir.resolve(fileMap.get(cFuture).getName()), Collections.singleton(csv));
                    }
                    catch(Exception e){
                        EvaluationMain.out.println("Task for file " + fileMap.get(cFuture) + " had errors: " + e);
                    }
                }
            }
        }
        if(!pool.isShutdown()) pool.shutdown();

        workerFactory.printFinalResult();
    }
}
