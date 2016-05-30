package de.uniulm.in.ki.mbrenner.fame.evaluation;

import java.io.File;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by spellmaker on 11.04.2016.
 */
public class MergeResults implements EvaluationCase{
    @Override
    public String getParameter() {
        return "merge";
    }

    @Override
    public String getHelpLine() {
        return null;
    }

    @Override
    public void evaluate(List<File> ontologies, List<String> options) throws Exception {
        String normpath = options.get(0);
        String hyspath = options.get(1);
        String targetfile = options.get(2);

        List<String> content = new LinkedList<>();
        EvaluationMain.out.println("Collecting files");
        try(DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(normpath))){
            for(Path p : directoryStream){
                if(!Files.isDirectory(p)) {
                    EvaluationMain.out.println("combining " + p + " and " + Paths.get(hyspath).resolve(p.getFileName()));
                    String s = Files.readAllLines(p).get(0);
                    String t = Files.readAllLines(Paths.get(hyspath).resolve(p.getFileName())).get(0);
                    content.add(s + ";" + t);
                }
            }
            Files.write(Paths.get(targetfile), content);
        }
    }
}
