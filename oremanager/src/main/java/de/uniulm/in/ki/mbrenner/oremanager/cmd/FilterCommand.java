package de.uniulm.in.ki.mbrenner.oremanager.cmd;

import de.uniulm.in.ki.mbrenner.oremanager.filters.ORECountFilter;
import de.uniulm.in.ki.mbrenner.oremanager.filters.ORENoFilter;
import de.uniulm.in.ki.mbrenner.oremanager.filters.ORESizeFilter;

/**
 * Created by spellmaker on 14.03.2016.
 */
public class FilterCommand extends CommandSwitch {
    public FilterCommand(){
        super("-f");
    }

    @Override
    public int process(String[] cmd, int position) throws Exception {
        String f = cmd[position];
        if(f.equals("size")){
            this.addFilter(new ORESizeFilter(Integer.parseInt(cmd[position + 1]), Integer.parseInt(cmd[position + 2])));
            return position + 3;
        }
        else if(f.equals("nofilter")){
            this.addFilter(new ORENoFilter());
            return position + 1;
        }
        else if(f.equals("count")){
            this.addFilter(new ORECountFilter(Integer.parseInt(cmd[position + 1])));
            return position + 2;
        }
        else{
            throw new Exception("unexpected filter command: " + f);
        }
    }
}
