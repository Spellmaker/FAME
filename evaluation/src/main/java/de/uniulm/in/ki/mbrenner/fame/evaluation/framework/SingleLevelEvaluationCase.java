package de.uniulm.in.ki.mbrenner.fame.evaluation.framework;

import de.uniulm.in.ki.mbrenner.fame.evaluation.EvaluationCase;
import de.uniulm.in.ki.mbrenner.fame.evaluation.EvaluationMain;
import de.uniulm.in.ki.mbrenner.fame.util.Misc;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by spellmaker on 25.05.2016.
 */
public class SingleLevelEvaluationCase<T extends WorkerResult> implements EvaluationCase {
    private SingleLevelWorkerFactory<T> workerFactory;
    private String parameter;
    private boolean writeComplete;

    public SingleLevelEvaluationCase(SingleLevelWorkerFactory<T> workerFactory, String parameter, boolean writeComplete){
        this.workerFactory = workerFactory;
        this.parameter = parameter;
        this.writeComplete = writeComplete;
    }

    @Override
    public String getParameter() {
        return parameter;
    }

    @Override
    public String getHelpLine() {
        return workerFactory.getHelp();
    }

    public void evaluate(List<File> ontologies, List<String> options) throws Exception{
        //locate out path
        Path outDir = EvaluationMain.outPath.resolve(this.getParameter());
        Files.createDirectories(outDir);
        for(File f : outDir.toFile().listFiles()){
            Misc.deleteFolder(f);
        }
        EvaluationMain.out.println(workerFactory.getGreeting());
        ExecutorService pool = Executors.newFixedThreadPool(5);

        List<Future<T>> futures = new LinkedList<>();
        Map<Future<T>, File> fileMap = new HashMap<>();
        for(int i = 0; i < ontologies.size(); i++){
            File cFile = ontologies.get(i);
            try {
                Future<T> f = pool.submit(workerFactory.getWorker(cFile, options));
                futures.add(f);
                fileMap.put(f, cFile);
            }
            catch(Exception e){
                EvaluationMain.out.println("Could not initialize task for file " + cFile + ": " + e);
            }
        }

        List<String> exceptions = new LinkedList<>();
        List<String> lines = new LinkedList<>();

        while(!futures.isEmpty()){
            for(int i = 0; i < futures.size(); i++){
                Future<T> cFuture = futures.get(i);
                if(cFuture.isDone()){
                    futures.remove(i--);
                    try {
                        T res = cFuture.get();
                        workerFactory.newResult(res);
                        String csv = res.toString();
                        EvaluationMain.out.println("Finished Task " + (ontologies.size() - futures.size()) + " of " + ontologies.size());
                        String h = res.getHeader();
                        if(h != null) EvaluationMain.out.println(h);
                        EvaluationMain.out.println(csv);
                        if(lines.isEmpty()) lines.add(res.getHeader());
                        lines.add(csv);
                        if(outDir != null) Files.write(outDir.resolve(fileMap.get(cFuture).getName()), Collections.singleton(csv));
                    }
                    catch(ExecutionException e) {
                        if(e.getCause() instanceof OntologySizeException){
                            EvaluationMain.out.println(e.getCause().getMessage());
                        }
                        else{
                            EvaluationMain.out.println("Task for file " + fileMap.get(cFuture) + " had errors: " + e);
                            e.printStackTrace();
                            exceptions.add(fileMap.get(cFuture) + ": " + e);
                        }
                    }
                    catch(Exception e){
                        EvaluationMain.out.println("Task for file " + fileMap.get(cFuture) + " failed unexpectedly: " + e);
                        e.printStackTrace();
                        exceptions.add(fileMap.get(cFuture) + ": " + e);
                    }
                }
            }
        }
        if(!pool.isShutdown()) pool.shutdown();


        lines.forEach(EvaluationMain.out::println);
        EvaluationMain.out.println("General Exceptions:");
        exceptions.forEach(EvaluationMain.out::println);
        if(exceptions.isEmpty()) EvaluationMain.out.println("None");
        workerFactory.finish();

        if(writeComplete && outDir != null){
            Path p = outDir.resolve(getParameter() + "-completed.csv");
            Files.write(p, lines);
        }
    }
}
