package de.uniulm.in.ki.mbrenner.fame.debug;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by spellmaker on 18.04.2016.
 */
public class WaitStream extends PrintStream {
    private PrintStream out;
    private Map<String, String> replacement;

    public WaitStream(PrintStream out) {
        super(out);
        this.out = out;
        replacement = new HashMap<>();
    }

    public void addReplacement(String source, String dest){
        replacement.put(source, dest);
    }

    @Override
    public void println(String s){
        try {
            for(Map.Entry<String, String> entry : replacement.entrySet()){
                s = s.replace(entry.getKey(), entry.getValue());
            }
            out.print(s);
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
