package de.uniulm.in.ki.mbrenner.oremanager.filters;

import de.uniulm.in.ki.mbrenner.oremanager.OREFilter;

public class ORENoFilter implements OREFilter {

	@Override
	public boolean accept(String... s) {
		return true;
	}

	@Override
	public int[] positions(){
		int[] res = new int[1];
		res[0] = 0;
		return res;
	}
}
