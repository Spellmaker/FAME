package de.uniulm.in.ki.mbrenner.fame.debug;

import de.uniulm.in.ki.mbrenner.fame.util.OntologiePaths;
import de.uniulm.in.ki.mbrenner.fame.simple.rule.RuleBuilder;
import de.uniulm.in.ki.mbrenner.fame.simple.rule.Rule;
import de.uniulm.in.ki.mbrenner.fame.simple.rule.RuleSet;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.io.File;
import java.util.*;

/**
 * Created by spellmaker on 21.04.2016.
 */
public class GraphBuilder {
    static Map<Integer, Node> nodes;
    static Map<Node, Set<Node>> successors;
    static int index;
    static List<Set<Node>> components;
    static Stack<Node> s;

    static void addEdge(Node n1, Node n2){
        Set<Node> l = successors.get(n1);
        if(l == null){
            l = new HashSet<>();
            successors.put(n1, l);
        }
        l.add(n2);
    }

    public static void doMagic() throws OWLOntologyCreationException {
        File f = new File(OntologiePaths.galen);
        OWLOntologyManager m = OWLManager.createOWLOntologyManager();
        OWLOntology o = m.loadOntologyFromOntologyDocument(f);
        System.out.println("loaded");
        RuleSet rs = new RuleBuilder().buildRules(o);
        System.out.println("rules build");

        nodes = new HashMap<>();
        successors = new HashMap<>();
        index = 0;
        components = new LinkedList<>();
        s = new Stack<>();

        for(Rule r : rs){
            Set<Integer> s = new HashSet<>();
            s.add(r.getHeadOrAxiom());
            if(r.getHead() == null){
                s.addAll(rs.getAxiomSignature(r.getAxiom()));
            }
            for(Integer head : s) {
                for (Integer body : r) {
                    addEdge(getNode(body), getNode(head));
                }
            }
        }

        System.out.println(nodes.size());

        //tarjan
        for(Node n : nodes.values()){
            if(n.index < 0){
                strongconnect(n);
            }
        }
        System.out.println("components: " + components.size());
    }

    static void strongconnect(Node v){
        v.index = index;
        v.lowlink = index;
        index++;
        s.push(v);
        v.onStack = true;
        Set<Node> succ = successors.get(v);
        if(succ != null) {
            for (Node w : succ) {
                if(w.index < 0){
                    strongconnect(w);
                    v.lowlink = Math.min(v.lowlink, w.lowlink);
                }
                else if(w.onStack){
                    v.lowlink = Math.min(v.lowlink, w.index);
                }
            }
        }

        if(v.lowlink == v.index){
            Set<Node> c = new HashSet<>();
            components.add(c);
            Node w = null;
            do {
                w = s.pop();
                w.onStack = false;
                c.add(w);
            }while(w != v);
        }
    }

    static Node getNode(Integer i){
        Node n = nodes.get(i);
        if(n == null){
            n = new Node(i);
            nodes.put(i, n);
        }
        return n;
    }
}

class Node{
    Integer value;
    int lowlink = -1;
    int index = -1;
    boolean onStack = false;

    public Node(int i ){
        this.value = i;
    }
}

class Edge{
    Node n1;
    Node n2;

    public Edge(Node n1, Node n2){
        this.n1 = n1;
        this.n2 = n2;
    }
}