package de.uniulm.in.ki.mbrenner.oremanager;

import java.io.File;
import java.nio.file.Paths;

public class OREMain {
	
	public static void main(String[] args) throws Exception{
		//args[0] = ore path
		OREManager ore = new OREManager();
		ore.load(Paths.get(args[0]), "el/consistency", "el/classification", "el/instantiation");
		String out = "";
		for(File f : ore.filterOntologies(new ORESizeFilter(0, 100, 102), OREPositions.LOGICAL_AXIOM_COUNT)){
			out += f + " ";
		}
		out = out.substring(0, out.length() - 1);
		
		System.out.print(out);
	}
}
