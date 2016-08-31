package de.uniulm.in.ki.mbrenner.fame.genetic;

import de.uniulm.in.ki.mbrenner.fame.genetic.AxiomEvaluator.Evaluator;
import de.uniulm.in.ki.mbrenner.fame.genetic.RuleGenerator.Generator;
import de.uniulm.in.ki.mbrenner.fame.genetic.Verification.Verifier;
import de.uniulm.in.ki.mbrenner.fame.simple.extractor.RBMExtractorNoDef;
import de.uniulm.in.ki.mbrenner.fame.simple.rule.RuleBuilder;
import de.uniulm.in.ki.mbrenner.fame.simple.rule.RuleSet;
import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by spellmaker on 14.06.2016.
 */
public class GeneticExtractor {
    private Set<OWLAxiom> ontology;
    private Set<OWLEntity> signature;

    private Random r;

    public static PrintStream out = System.out;

    int generation_size = 10;
    int mutation_multiplier = 10;
    int mutation_changes = 100;
    int runs = 100;

    public GeneticExtractor(OWLOntology ontology, Set<OWLEntity> signature, int runs){
        this.ontology = ontology.getAxioms();
        this.signature = signature;
        r = new Random();
        this.runs = runs;
    }

    public void setParams(int generation_size, int mutation_multiplier, int mutation_changes, int runs){
        this.generation_size = generation_size;
        this.mutation_multiplier = mutation_multiplier;
        this.mutation_changes = mutation_changes;
        this.runs = runs;
    }

    public Solution botSolution(){
        Map<OWLObject, Boolean> s = new HashMap<>();
        ontology.forEach(x -> x.getSignature().forEach(y -> s.put(y, true)));
        return new Solution(s);
    }

    public Solution topSolution(){
        Map<OWLObject, Boolean> s = new HashMap<>();
        ontology.forEach(x -> x.getSignature().forEach(y -> s.put(y, false)));
        return new Solution(s);
    }

    public Solution botTopSolution(){
        Map<OWLObject, Boolean> s = new HashMap<>();
        ontology.forEach(x -> x.getSignature().forEach(y -> s.put(y, y instanceof OWLClass)));
        return new Solution(s);
    }

    public Solution getRandom(){
        Map<OWLObject, Boolean> map = new HashMap<>();
        ontology.forEach(x -> x.getSignature().forEach(y -> map.put(y, r.nextBoolean())));
        return new Solution(map);
    }

    /*public Solution combine(Solution s1, Solution s2){
        Map<OWLObject, Boolean> res = new HashMap<>();
        boolean first = true;
        for(Map.Entry<OWLObject, Boolean> e : s1.map.entrySet()){
            if(first)   res.put(e.getKey(), e.getValue());
            else        res.put(e.getKey(), s2.map.get(e.getKey()));
            first = !first;
        }
        return new Solution(res);
    }*/

    public Solution mutate(Solution s){
        Map<OWLObject, Boolean> res = new HashMap<>(s.map);
        for(int i = 0; i < mutation_changes; i++){
            int pos = r.nextInt(s.map.size());
            Iterator<Map.Entry<OWLObject, Boolean>> iter = s.map.entrySet().iterator();
            Map.Entry<OWLObject, Boolean> c = null;
            while(pos-- >= 0){
                c = iter.next();
            }
            res.put(c.getKey(), !c.getValue());
        }
        return new Solution(res);
    }

    private Solution[] step(Solution[] old){
        Set<OWLAxiom> minimal = ontology;
        Solution minimalSolution = null;
        Solution[] expanded = new Solution[generation_size * mutation_multiplier];
        int pos = 0;
        for(Solution s : old){
            for(int i = 0; i < mutation_multiplier; i++) {
                expanded[pos++] = mutate(s);
            }
        }
        for(Solution s : expanded){
            Generator gen = new Generator();
            gen.buildRules(ontology, s.map);
            Set<OWLAxiom> mod = gen.getResult(signature);
            s.fitness = mod.size();
            //out.println("size: " + mod.size());

            if(mod.size() < ontology.size()){
                minimal = mod;
                minimalSolution = s;
            }
        }

        Arrays.sort(expanded);
        expanded = Arrays.copyOf(expanded, generation_size);

        if(minimal.size() < ontology.size()){
            out.println("Found at least one module which is smaller than before, performing correctness check");
            boolean check = Verifier.verifyModule(ontology, minimal, minimalSolution.map).isEmpty();
            out.println("Correctness check says: " + check);
            if(!check){
                out.println("Storing state for debugging purposes...");
                List<String> lines = new LinkedList<>();
                minimal.forEach(x -> lines.add(x.toString()));
                List<String> lines2 = new LinkedList<>();
                ontology.forEach(x -> lines2.add(x.toString()));
                List<String> lines3 = new LinkedList<>();
                minimalSolution.map.entrySet().forEach(x -> {lines3.add(x.getKey().toString()); lines3.add(x.getValue().toString());});
                try {
                    Files.write(Paths.get("wrong_module"), lines);
                    Files.write(Paths.get("parent_module"), lines2);
                    Files.write(Paths.get("interpretation"), lines3);
                }
                catch(IOException e){
                    out.println("not the best thing to happen - IO failed");
                }

                out.println("Crashing the program on purpose for termination");
                return null;
            }
            ontology = minimal;
            Set<OWLEntity> relevant = ontology.stream().map(OWLAxiom::getSignature).flatMap(Collection::stream).collect(Collectors.toSet());
            for(Solution s : expanded){
                Map<OWLObject, Boolean> tmp = new HashMap<>(s.map);
                s.map.entrySet().stream().filter(relevant::contains).forEach(x -> tmp.put(x.getKey(), x.getValue()));
                s.map = tmp;
            }
        }

        return expanded;
    }

    private Set<OWLAxiom> star(){
        Set<OWLAxiom> current = ontology;
        Set<OWLAxiom> next = ontology;
        do{
            current = next;
            Solution bot = botSolution();
            Generator gen = new Generator();
            gen.buildRules(current, bot.map);
            next = gen.getResult(signature);
            Solution top = topSolution();
            gen = new Generator();
            gen.buildRules(next, top.map);
            next = gen.getResult(signature);
        }while(!current.equals(next));
        return current;
    }


    public Set<OWLAxiom> extract(){
        int best = ontology.size();
        int generation = 0;

        Solution[] current = new Solution[generation_size];
        current[0] = botSolution();
        current[1] = topSolution();
        current[2] = botTopSolution();

        RuleSet rs = new RuleSet();
        new RuleBuilder().buildRules(ontology, ontology.stream().map(OWLAxiom::getSignature).flatMap(Collection::stream).collect(Collectors.toSet()), true, rs, rs);
        Set<OWLAxiom> botMod = new RBMExtractorNoDef().extractModule(rs, signature);
        Set<OWLAxiom> starMod = star();
        ontology = starMod; //start of as small as possible
        out.println("each solution contains " + current[0].map.size() + " mappings");

        for(int i = 0; i < generation_size; i++) current[i] = getRandom();
        int cnt = 0;
        while(cnt++ < runs){
            current = step(current);
            best = current[0].fitness;
            out.println("End of generation " + generation++ + ": Best result is " + best + " bot: " + botMod.size() + " star: " + starMod.size());
        }

        Generator gen = new Generator();
        gen.buildRules(ontology, current[0].map);
        return gen.getResult(signature);
    }
}

class Solution implements Comparable<Solution>{
    Map<OWLObject, Boolean> map;
    int fitness;

    public Solution(Map<OWLObject, Boolean> map){
        this.map = map;
        this.fitness = -1;
    }

    @Override
    public int compareTo(Solution o) {
        return fitness - o.fitness;
    }
}