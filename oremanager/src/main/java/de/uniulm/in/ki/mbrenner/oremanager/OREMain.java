package de.uniulm.in.ki.mbrenner.oremanager;

import de.uniulm.in.ki.mbrenner.oremanager.cmd.CommandSwitch;
import de.uniulm.in.ki.mbrenner.oremanager.cmd.FilterCommand;
import de.uniulm.in.ki.mbrenner.oremanager.cmd.ProfileCommand;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class OREMain {
	private static CommandSwitch[] switchArray = {new FilterCommand(), new ProfileCommand()};
	private static Map<String, CommandSwitch> switches;

	public static void main(String[] args) throws Exception{
		//args[0] = ore path
		//remainder: configuration
		/*
		-p specifies profile. currently: all, el, dl, ql, pure_dl
		-f specifies a filter. currently: size, nofilter
		 */
		if(args[0].startsWith("-")){
			System.out.println("error: expected ore directory as first argument");
			System.exit(0);
		}


		OREManager ore = new OREManager();
		Path oreDir = Paths.get(args[0]);
		initCommands(oreDir, ore);
		int i = 1;
		while(i < args.length){
			i = switches.get(args[i]).process(args, i + 1);
		}

		List<OREFilter> filters = new LinkedList<>();
		for(CommandSwitch c : switchArray){
			filters.addAll(c.getFilters());
		}

		String out = "";

		for(File f : ore.filterOntologies(filters.toArray(new OREFilter[0]))){
			out += f + " ";
		}

		if(out.length() > 0) {
			out = out.substring(0, out.length() - 1);
		}
		System.out.print(out);
	}

	private static void initCommands(Path oreDir, OREManager ore){
		switches = new HashMap<>();
		for(CommandSwitch c : switchArray) {
			c.init(oreDir, ore);
			switches.put(c.getSwitch(), c);
		}
	}
}
