package de.uniulm.in.ki.mbrenner.oremanager.cmd;

import de.uniulm.in.ki.mbrenner.oremanager.OREFilter;
import de.uniulm.in.ki.mbrenner.oremanager.OREManager;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by spellmaker on 14.03.2016.
 */
public abstract class CommandSwitch {
    private List<OREFilter> filters = new LinkedList<>();
    protected OREManager ore;
    protected Path oreDir;
    private String cmdSwitch;

    public CommandSwitch(String cmdSwitch){
        this.cmdSwitch = cmdSwitch;
    }

    public void init(Path oreDir, OREManager ore){
        this.oreDir = oreDir;
        this.ore = ore;
    }

    public List<OREFilter> getFilters(){
        return filters;
    }

    protected void addFilter(OREFilter filter){
        this.filters.add(filter);
    }

    public String getSwitch(){
        return cmdSwitch;
    }

    public abstract int process(String[] cmd, int position) throws Exception;

}
