package de.uniulm.in.ki.mbrenner.oremanager;

public class ORENoFilter implements OREFilter{

	@Override
	public boolean accept(String... s) {
		return true;
	}

}
