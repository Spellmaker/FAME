//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package de.uniulm.in.ki.mbrenner.fame.evaluation.relatedtools.HyS;

import de.tu_dresden.inf.lat.hys.CommandLineOptions;
import de.tu_dresden.inf.lat.hys.TimeStamps;
import de.tu_dresden.inf.lat.hys.graph_tools.*;
import de.tu_dresden.inf.lat.hys.owl.OWLOntologyParser;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.util.VersionInfo;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;

public class HyS {
    private static GraphReachability[] dlg;
    private static byte byte_levelOfCondensation;
    private static CommandLineOptions clo;
    private static Collection<Edge> SimpleHyperedgeDependencies = null;
    private static OWLOntologyParser ontParser = new OWLOntologyParser();
    private static AdjacencyList graph_ADG;
    private static long[] arrayLong_timeStamps = new long[10];
    private static TimeStamps timeStamp = new TimeStamps();
    private static Map<Integer, Set<OWLAxiom>> map_modules = new HashMap();
    private static File[] sigFiles = null;
    private static Map<Integer, Set<OWLEntity>> map_Signatures = new HashMap();
    private static int int_NumberOfSignatures = 0;
    private static int[] arrayInt_NumberOfSymbolsInSignature = null;

    public HyS(OWLOntology var1, ModuleType var2) {
        print_HyS_initial_message();
        if(var2 != ModuleType.BOT) {
            System.out.println("Error: only syntactic bot-locality is currently supported");
            System.exit(0);
        }

        ontParser.loadOntology(var1);
        AdjacencyList var3 = ontParser.buildAxiomDependencyGraph();
        dlg = new GraphReachability[3];
        byte_levelOfCondensation = 0;
        dlg[byte_levelOfCondensation] = ontParser.initialise_ModuleExtraction_with_Graph(var3);
    }

    public void condense(SCCAlgorithm var1) {
        if(byte_levelOfCondensation == 2) {
            System.out.println("Error: no more than 2 condensation steps possible");
            System.exit(0);
        }

        SimpleHyperedgeDependencies = null;
        if(var1 == SCCAlgorithm.TARJAN) {
            dlg[byte_levelOfCondensation].compute_condensedGraph_Tarjan();
            dlg[byte_levelOfCondensation + 1] = ontParser.initialise_ModuleExtraction_with_QuotientGraph(dlg[byte_levelOfCondensation]);
            ++byte_levelOfCondensation;
        }

        if(var1 == SCCAlgorithm.MREACHABILITY) {
            dlg[byte_levelOfCondensation].compute_condensedGraph_MReachability();
            dlg[byte_levelOfCondensation + 1] = ontParser.initialise_ModuleExtraction_with_QuotientGraph(dlg[byte_levelOfCondensation]);
            ++byte_levelOfCondensation;
        }

    }

    public Set<Node> getNodes() {
        HashSet var1 = new HashSet(dlg[byte_levelOfCondensation].int_NumberOfInputGraphNodes);
        Node[] var2 = dlg[byte_levelOfCondensation].getNodeArray();

        for(int var3 = 0; var3 < dlg[byte_levelOfCondensation].int_NumberOfInputGraphNodes; ++var3) {
            var1.add(var2[var3]);
        }

        return var1;
    }

    public Set<OWLAxiom> getAxioms(Node var1) {
        HashSet var2 = new HashSet();
        if(byte_levelOfCondensation == 0) {
            var2.add(ontParser.mapIndex_Axiom[var1.name]);
        }

        Iterator var3;
        Integer var4;
        if(byte_levelOfCondensation == 1) {
            var3 = dlg[1].map_SCCIndex_SCC.array_IntList[var1.name].iterator();

            while(var3.hasNext()) {
                var4 = (Integer)var3.next();
                var2.add(ontParser.mapIndex_Axiom[var4.intValue()]);
            }
        }

        if(byte_levelOfCondensation == 2) {
            var3 = dlg[1].map_SCCIndex_SCC.array_IntList[var1.name].iterator();

            while(var3.hasNext()) {
                var4 = (Integer)var3.next();
                Iterator var5 = dlg[0].map_SCCIndex_SCC.array_IntList[var4.intValue()].iterator();

                while(var5.hasNext()) {
                    Integer var6 = (Integer)var5.next();
                    var2.add(ontParser.mapIndex_Axiom[var6.intValue()]);
                }
            }
        }

        return var2;
    }

    public static Set<OWLAxiom> getAxioms(Set<Node> var0) {
        HashSet var1 = new HashSet();
        Iterator var2;
        Node var3;
        if(byte_levelOfCondensation == 0) {
            var2 = var0.iterator();

            while(var2.hasNext()) {
                var3 = (Node)var2.next();
                var1.add(ontParser.mapIndex_Axiom[var3.name]);
            }
        }

        Iterator var4;
        Integer var5;
        if(byte_levelOfCondensation == 1) {
            var2 = var0.iterator();

            while(var2.hasNext()) {
                var3 = (Node)var2.next();
                var4 = dlg[0].map_SCCIndex_SCC.array_IntList[var3.name].iterator();

                while(var4.hasNext()) {
                    var5 = (Integer)var4.next();
                    var1.add(ontParser.mapIndex_Axiom[var5.intValue()]);
                }
            }
        }

        if(byte_levelOfCondensation == 2) {
            var2 = var0.iterator();

            while(var2.hasNext()) {
                var3 = (Node)var2.next();
                var4 = dlg[1].map_SCCIndex_SCC.array_IntList[var3.name].iterator();

                while(var4.hasNext()) {
                    var5 = (Integer)var4.next();
                    Iterator var6 = dlg[0].map_SCCIndex_SCC.array_IntList[var5.intValue()].iterator();

                    while(var6.hasNext()) {
                        Integer var7 = (Integer)var6.next();
                        var1.add(ontParser.mapIndex_Axiom[var7.intValue()]);
                    }
                }
            }
        }

        return var1;
    }

    public Set<Node> getConnectedComponent(Set<OWLEntity> var1) {
        ontParser.computeModule(dlg[byte_levelOfCondensation], var1);
        return dlg[byte_levelOfCondensation].getConnectedComponent();
    }

    private static void getAxiomsOfConnectedComponent(Set<OWLAxiom> var0) {
        var0.clear();
        int var1;
        if(byte_levelOfCondensation == 0) {
            for(var1 = 0; var1 < dlg[0].int_NumberOfInputGraphNodes; ++var1) {
                if(dlg[0].byteArray_Nodes[var1] == 1) {
                    var0.add(ontParser.mapIndex_Axiom[var1]);
                }
            }
        }

        Iterator var2;
        Integer var3;
        if(byte_levelOfCondensation == 1) {
            for(var1 = 0; var1 < dlg[1].int_NumberOfInputGraphNodes; ++var1) {
                if(dlg[1].byteArray_Nodes[var1] == 1) {
                    var2 = dlg[0].map_SCCIndex_SCC.array_IntList[var1].iterator();

                    while(var2.hasNext()) {
                        var3 = (Integer)var2.next();
                        var0.add(ontParser.mapIndex_Axiom[var3.intValue()]);
                    }
                }
            }
        }

        if(byte_levelOfCondensation == 2) {
            for(var1 = 0; var1 < dlg[2].int_NumberOfInputGraphNodes; ++var1) {
                if(dlg[2].byteArray_Nodes[var1] == 1) {
                    var2 = dlg[1].map_SCCIndex_SCC.array_IntList[var1].iterator();

                    while(var2.hasNext()) {
                        var3 = (Integer)var2.next();
                        Iterator var4 = dlg[0].map_SCCIndex_SCC.array_IntList[var3.intValue()].iterator();

                        while(var4.hasNext()) {
                            Integer var5 = (Integer)var4.next();
                            var0.add(ontParser.mapIndex_Axiom[var5.intValue()]);
                        }
                    }
                }
            }
        }

    }

    public Set<Node> getTailNodes(Node var1) {
        if(SimpleHyperedgeDependencies == null) {
            SimpleHyperedgeDependencies = dlg[byte_levelOfCondensation].getSimpleHyperedgeDependencies();
        }

        HashSet var2 = new HashSet();
        Iterator var3 = SimpleHyperedgeDependencies.iterator();

        while(var3.hasNext()) {
            Edge var4 = (Edge)var3.next();
            if(var4.to == var1) {
                var2.add(var4.from);
            }
        }

        return var2;
    }

    public Set<Node> getHeadNodes(Node var1) {
        if(SimpleHyperedgeDependencies == null) {
            SimpleHyperedgeDependencies = dlg[byte_levelOfCondensation].getSimpleHyperedgeDependencies();
        }

        HashSet var2 = new HashSet();
        Iterator var3 = SimpleHyperedgeDependencies.iterator();

        while(var3.hasNext()) {
            Edge var4 = (Edge)var3.next();
            if(var4.from == var1) {
                var2.add(var4.to);
            }
        }

        return var2;
    }

    private static Set<OWLEntity> load_signature_from_file(Set<OWLEntity> var0, File var1) throws IOException, FileNotFoundException {
        BufferedReader var2 = new BufferedReader(new FileReader(var1));
        HashSet var3 = new HashSet();

        for(String var4 = var2.readLine(); var4 != null; var4 = var2.readLine()) {
            Iterator var5 = var0.iterator();

            while(var5.hasNext()) {
                OWLEntity var6 = (OWLEntity)var5.next();
                if(var6.toStringID().equals(var4)) {
                    var3.add(var6);
                    break;
                }
            }
        }

        return var3;
    }

    private static File[] getFilesFromDirectory(Path var0, String var1) {
        File[] var2 = var0.toFile().listFiles();
        int var3 = 0;

        for(int var5 = 0; var5 < var2.length; ++var5) {
            if(var2[var5].isFile()) {
                String[] var4 = var2[var5].getName().split("\\.");
                if(var4.length != 0 && var4[var4.length - 1].equals(var1)) {
                    var2[var3++] = var2[var5];
                }
            }
        }

        File[] var7 = new File[var3];

        for(int var6 = 0; var6 < var3; ++var6) {
            var7[var6] = var2[var6];
        }

        return var7;
    }

    private static void load_signature_from_directory_fast(Path var0) throws IOException {
        sigFiles = getFilesFromDirectory(var0, "sig");
        map_Signatures = new HashMap(sigFiles.length);
        int_NumberOfSignatures = 0;
        arrayInt_NumberOfSymbolsInSignature = new int[sigFiles.length];
        HashMap var1 = new HashMap();
        Iterator var2 = ontParser.setOWLEntity_WorkingSignature.iterator();

        while(var2.hasNext()) {
            OWLEntity var3 = (OWLEntity)var2.next();
            var1.put(var3.toStringID(), ontParser.mapSignature_Index.get(var3));
        }

        File[] var10 = sigFiles;
        int var11 = var10.length;

        for(int var4 = 0; var4 < var11; ++var4) {
            File var5 = var10[var4];
            HashSet var6 = new HashSet();
            BufferedReader var7 = new BufferedReader(new FileReader(var5));

            for(String var8 = var7.readLine(); var8 != null; var8 = var7.readLine()) {
                if(var1.containsKey(var8)) {
                    Integer var9 = (Integer)var1.get(var8);
                    var6.add(ontParser.mapIndex_Signature[var9.intValue()]);
                }
            }

            map_Signatures.put(Integer.valueOf(int_NumberOfSignatures), var6);
            arrayInt_NumberOfSymbolsInSignature[int_NumberOfSignatures] = var6.size();
            ++int_NumberOfSignatures;
        }

    }

    private static void computeModulesForAllLoadedSignatures() {
        for(int var0 = 0; var0 < int_NumberOfSignatures; ++var0) {
            ontParser.computeModule(dlg[byte_levelOfCondensation], (Set)map_Signatures.get(Integer.valueOf(var0)));
            getAxiomsOfConnectedComponent((Set)map_modules.get(Integer.valueOf(var0)));
        }
    }

    private static void print_stats(String var0, GraphReachability var1) {
    }

    private static void computeModule(int var0, CommandLineOptions var1) throws OWLException, IOException {
        load_signature_from_directory_fast(var1.getPathToDirectory());

        int var2;
        for(var2 = 0; var2 < int_NumberOfSignatures; ++var2) {
            map_modules.put(Integer.valueOf(var2), new HashSet(ontParser.int_NumberOfAxiomIndices));
        }

        TimeStamps var10000 = timeStamp;
        TimeStamps.take("load signatures");
        ++var0;
        computeModulesForAllLoadedSignatures();
        var10000 = timeStamp;
        TimeStamps.take("compute modules");
        ++var0;
        if(CommandLineOptions.arrayByte_switches[7] == 0) {

            for(var2 = 0; var2 < int_NumberOfSignatures; ++var2) {
                ontParser.store_setOWLAxioms_as_ontology((Set)map_modules.get(Integer.valueOf(var2)), Paths.get(sigFiles[var2].getPath() + ".owl", new String[0]));
            }
        }

        var10000 = timeStamp;
        TimeStamps.take("store modules");
    }

    private static void executionPath_Module() throws OWLException, IOException {
        TimeStamps var10000 = timeStamp;
        TimeStamps.take("");
        ontParser.loadOntology(clo.getPathToOntology());
        CommandLineOptions var1 = clo;
        if(CommandLineOptions.arrayByte_switches[8] == 1) {
            ontParser.print_info_on_ontology();
        }

        var10000 = timeStamp;
        TimeStamps.take("load ontology");
        AdjacencyList var0 = ontParser.buildAxiomDependencyGraph();
        byte_levelOfCondensation = 0;
        dlg[byte_levelOfCondensation] = ontParser.initialise_ModuleExtraction_with_Graph(var0);
        var1 = clo;
        if(CommandLineOptions.arrayByte_switches[8] == 1) {
            dlg[byte_levelOfCondensation].print_info();
        }

        var10000 = timeStamp;
        TimeStamps.take("compute hypergraph (ADH)");
        var1 = clo;
        if(CommandLineOptions.arrayByte_switches[2] == 1) {
            computeModule(2, clo);
        }

        var1 = clo;
        if(CommandLineOptions.arrayByte_switches[3] == 1) {
            dlg[byte_levelOfCondensation].compute_condensedGraph_Tarjan();
            dlg[byte_levelOfCondensation + 1] = ontParser.initialise_ModuleExtraction_with_QuotientGraph(dlg[byte_levelOfCondensation]);
            ++byte_levelOfCondensation;
            var1 = clo;
            if(CommandLineOptions.arrayByte_switches[8] == 1) {
                dlg[byte_levelOfCondensation].print_info();
            }

            var10000 = timeStamp;
            computeModule(3, clo);
        }

        var1 = clo;
        if(CommandLineOptions.arrayByte_switches[4] == 1) {
            dlg[byte_levelOfCondensation].compute_condensedGraph_Tarjan();
            dlg[byte_levelOfCondensation + 1] = ontParser.initialise_ModuleExtraction_with_QuotientGraph(dlg[byte_levelOfCondensation]);
            ++byte_levelOfCondensation;
            var1 = clo;
            if(CommandLineOptions.arrayByte_switches[8] == 1) {
                dlg[byte_levelOfCondensation].print_info();
            }

            var10000 = timeStamp;
            TimeStamps.take("compute partially condensed hypergraph (pcADH)");
            dlg[byte_levelOfCondensation].compute_condensedGraph_MReachability();
            dlg[byte_levelOfCondensation + 1] = ontParser.initialise_ModuleExtraction_with_QuotientGraph(dlg[byte_levelOfCondensation]);
            ++byte_levelOfCondensation;
            var1 = clo;
            if(CommandLineOptions.arrayByte_switches[8] == 1) {
                dlg[byte_levelOfCondensation].print_info();
            }

            var10000 = timeStamp;
            TimeStamps.take("compute condensed hypergraph (cADH)");
            computeModule(4, clo);
        }

        var10000 = timeStamp;
        TimeStamps.printStats();
    }

    private static void print_memory_usage() {
        long var0 = 1048576L;
        Runtime var2 = Runtime.getRuntime();
        long var3 = var2.totalMemory() - var2.freeMemory();
        System.out.println("\n(.) memory stats: " + var3 / var0 + " MByte used");
    }

    private static void executionPath_AD() throws OWLException, IOException {
        TimeStamps var10000 = timeStamp;
        TimeStamps.take("");
        ontParser.loadOntology(clo.getPathToOntology());
        CommandLineOptions var1 = clo;
        if(CommandLineOptions.arrayByte_switches[8] == 1) {
            ontParser.print_info_on_ontology();
        }

        var10000 = timeStamp;
        TimeStamps.take("load ontology");
        AdjacencyList var0 = ontParser.buildAxiomDependencyGraph();
        byte_levelOfCondensation = 0;
        dlg[byte_levelOfCondensation] = ontParser.initialise_ModuleExtraction_with_Graph(var0);
        var1 = clo;
        if(CommandLineOptions.arrayByte_switches[8] == 1) {
            dlg[byte_levelOfCondensation].print_info();
        }

        var10000 = timeStamp;
        TimeStamps.take("compute hypergraph (ADH)");
        dlg[byte_levelOfCondensation].compute_condensedGraph_Tarjan();
        dlg[byte_levelOfCondensation + 1] = ontParser.initialise_ModuleExtraction_with_QuotientGraph(dlg[byte_levelOfCondensation]);
        ++byte_levelOfCondensation;
        var1 = clo;
        if(CommandLineOptions.arrayByte_switches[8] == 1) {
            dlg[byte_levelOfCondensation].print_info();
        }

        var10000 = timeStamp;
        TimeStamps.take("compute partially condensed hypergraph (pcADH)");
        dlg[byte_levelOfCondensation].compute_condensedGraph_MReachability();
        dlg[byte_levelOfCondensation + 1] = ontParser.initialise_ModuleExtraction_with_QuotientGraph(dlg[byte_levelOfCondensation]);
        ++byte_levelOfCondensation;
        var1 = clo;
        if(CommandLineOptions.arrayByte_switches[8] == 1) {
            dlg[byte_levelOfCondensation].print_info();
        }

        var10000 = timeStamp;
        TimeStamps.take("compute condensed hypergraph (cADH)");
        OWLOntologyParser var2 = ontParser;
        GraphReachability var10001 = dlg[byte_levelOfCondensation];
        Path var10002 = clo.getPathToOntology();
        CommandLineOptions var10003 = clo;
        var2.storeAtomsAndDependencies(var10001, var10002, CommandLineOptions.arrayByte_switches[7]);
        var10000 = timeStamp;
        TimeStamps.take("store atoms & dependencies");
        var10000 = timeStamp;
        TimeStamps.printStats();
    }

    private static void print_HyS_initial_message() {
    }

    public static void main(String[] var0) throws OWLException, IOException {
        print_HyS_initial_message();
        clo = new CommandLineOptions();
        CommandLineOptions var10000 = clo;
        CommandLineOptions.parseCommandLine(var0);
        dlg = new GraphReachability[3];
        var10000 = clo;
        if(CommandLineOptions.arrayByte_switches[0] == 1) {
            executionPath_AD();
            System.exit(0);
        }

        var10000 = clo;
        if(CommandLineOptions.arrayByte_switches[1] == 1) {
            executionPath_Module();
            System.exit(0);
        }

    }
}
