package de.uniulm.in.ki.mbrenner.oremanager;

public interface OREFilter{
	boolean accept(String... s);
	default int[] positions(){
		return null;
	}
}