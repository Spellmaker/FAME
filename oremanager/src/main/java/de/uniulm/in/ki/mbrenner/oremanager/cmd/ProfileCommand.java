package de.uniulm.in.ki.mbrenner.oremanager.cmd;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by spellmaker on 14.03.2016.
 */
public class ProfileCommand extends CommandSwitch {
    public ProfileCommand(){
        super("-p");
    }

    @Override
    public int process(String[] cmd, int position) throws Exception{
        List<String> profiles = new LinkedList<>();
        int i;
        for(i = position; i < cmd.length && !cmd[i].startsWith("-"); i++){
            if(cmd[i].equals("el")){
                profiles.add("el/consistency");
                profiles.add("el/classification");
                profiles.add("el/instantiation");
            }
            else if(cmd[i].equals("dl")){
                profiles.add("dl/consistency");
                profiles.add("dl/classification");
                profiles.add("dl/instantiation");
            }
            else if(cmd[i].equals("ql")){
                profiles.add("ql/consistency");
                profiles.add("ql/classification");
                profiles.add("ql/instantiation");
            }
            else if(cmd[i].equals("pure_dl")){
                profiles.add("pure_dl/consistency");
                profiles.add("pure_dl/classification");
                profiles.add("pure_dl/instantiation");
            }
            else if(cmd[i].equals("all")){
                profiles.add("el/consistency");
                profiles.add("el/classification");
                profiles.add("el/instantiation");
                profiles.add("dl/consistency");
                profiles.add("dl/classification");
                profiles.add("dl/instantiation");
                profiles.add("ql/consistency");
                profiles.add("ql/classification");
                profiles.add("ql/instantiation");
                profiles.add("pure_dl/consistency");
                profiles.add("pure_dl/classification");
                profiles.add("pure_dl/instantiation");
            }
            else{
                throw new Exception("unexpected profile: " + cmd[i]);
            }
        }
        ore.load(oreDir, profiles.toArray(new String[0]));

        return i;
    }
}
