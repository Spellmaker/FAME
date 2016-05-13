package de.uniulm.in.ki.mbrenner.fame.util;

/**
 * Created by spellmaker on 15.03.2016.
 */
public class OutputFormatter {
    public static String formatCSV(Object...strings){
        String out = "";
        for(Object s : strings){
            out += s.toString() + ";";
        }
        if(strings.length > 0){
            out = out.substring(0, out.length() - 1);
        }
        return out;
    }
}
