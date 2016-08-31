package de.uniulm.in.ki.mbrenner.fame.evaluation;

import de.uniulm.in.ki.mbrenner.fame.definitions.rulebased.DRBExtractor;
import de.uniulm.in.ki.mbrenner.fame.definitions.rulebased.rule.DRBRuleSet;
import de.uniulm.in.ki.mbrenner.fame.definitions.rulebased.rulebuilder.DRBRuleBuilder;
import de.uniulm.in.ki.mbrenner.fame.evaluation.framework.OntologyWorker;
import de.uniulm.in.ki.mbrenner.fame.evaluation.framework.PrintField;
import de.uniulm.in.ki.mbrenner.fame.evaluation.framework.SingleLevelWorkerFactory;
import de.uniulm.in.ki.mbrenner.fame.evaluation.framework.WorkerResult;
import de.uniulm.in.ki.mbrenner.fame.incremental.IncrementalExtractor;
import de.uniulm.in.ki.mbrenner.fame.incremental.IncrementalModule;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * Created by spellmaker on 25.05.2016.
 */

public class DefinitionSizeComparison implements SingleLevelWorkerFactory<DefinitionSizeResult>{
    private List<String> lines;

    public DefinitionSizeComparison(){
        lines = new LinkedList<>();
    }


    @Override
    public Callable<DefinitionSizeResult> getWorker(File file, List<String> options) {
        return new DefinitionSizeComparisonWorker(file);
    }

    @Override
    public String getGreeting() {
        return "Starting module size comparison for bottom locality and definition locality";
    }

    @Override
    public void newResult(DefinitionSizeResult result){
        List<String> lines = new LinkedList<>();
        Iterator<Map.Entry<Long, Long>> iter1 = result.countsBot.entrySet().iterator();
        Iterator<Map.Entry<Long, Long>> iter2 = result.countsDef.entrySet().iterator();

        while(iter1.hasNext() || iter2.hasNext()){
            String s = "";
            if(iter1.hasNext()){
                Map.Entry<Long, Long> e = iter1.next();
                s += e.getKey() + ";" + e.getValue() + ";";
            }
            else{
                s += ";;";
            }

            if(iter2.hasNext()){
                Map.Entry<Long, Long> e = iter2.next();
                s += e.getKey() + ";" + e.getValue() + ";";
            }
            else{
                s += ";;";
            }
            lines.add(s);
        }
        try {
            Files.write(Paths.get("sizes_histogram.csv"), lines);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class DefinitionSizeResult extends WorkerResult{
    @PrintField
    public long botLocSize;
    @PrintField
    public long defLocSize;

    public Map<Long, Long> countsBot;
    public Map<Long, Long> countsDef;

    @PrintField
    public long botAvgPerEntity(){
        if(entities == 0) return 0;
        return botLocSize / entities;
    }

    @PrintField
    public long defAvgPerEntity(){
        if(entities == 0) return 0;
        return defLocSize / entities;
    }

    public DefinitionSizeResult(OntologyWorker<?> worker) {
        super(worker);
    }
}


class DefinitionSizeComparisonWorker extends OntologyWorker<DefinitionSizeResult>{
    public DefinitionSizeComparisonWorker(File f){
        super(f);
    }

    private Map<Long, Long> countsBot;
    private Map<Long, Long> countsDef;

    @Override
    public DefinitionSizeResult process(OWLOntology o) throws Exception {
        IncrementalExtractor iExtractor = new IncrementalExtractor(o);
        DRBRuleSet drb = new DRBRuleBuilder().buildRules(o);

        DefinitionSizeResult result = new DefinitionSizeResult(this);


        countsBot = new HashMap<>();
        countsDef = new HashMap<>();

        int i = 0;
        for(OWLEntity e : o.getSignature()){
            IncrementalModule module = iExtractor.extractModuleStatic(Collections.singleton(e));
            long val = module.getOWLModule().stream().filter(x -> x instanceof OWLLogicalAxiom).count();
            result.botLocSize += val;
            add(countsBot, val);
            //if(i++ > 2000) break;
        }

        EvaluationMain.out.println("completed bot-locality");

        i = 0;
        for(OWLEntity e : o.getSignature()){
            Set<OWLAxiom> module = new DRBExtractor().extractModule(drb, Collections.singleton(e));
            long val = module.stream().filter(x -> x instanceof OWLLogicalAxiom).count();
            result.defLocSize += val;
            //if(i++ > 2000) break;
            add(countsDef, val);
        }
        EvaluationMain.out.println("completed def-locality");
        result.countsBot = countsBot;
        result.countsDef = countsDef;
        return result;
    }

    private void add(Map<Long, Long> map, long modsize){
        Long c = map.get(modsize);
        if(c == null){
            c = 0L;
        }
        map.put(modsize, c + 1);
    }
}