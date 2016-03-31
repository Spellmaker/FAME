package de.uniulm.in.ki.mbrenner.oremanager.filters;

import de.uniulm.in.ki.mbrenner.oremanager.OREFilter;
import de.uniulm.in.ki.mbrenner.oremanager.OREPositions;

public class ORESizeFilter implements OREFilter {
	private int pos;
	private int min;
	private int max;

	public ORESizeFilter(int min, int max){
		this.pos = 0;
		this.min = min;
		this.max = max;
	}

	public ORESizeFilter(int pos, int min, int max){
		this.pos = pos;
		this.min = min;
		this.max = max;
	}

	@Override
	public boolean accept(String... s) {
		return Integer.parseInt(s[pos]) > min && Integer.parseInt(s[pos]) < max;
	}

	@Override
	public int[] positions(){
		int[] res = new int[1];
		res[0] = OREPositions.LOGICAL_AXIOM_COUNT;
		return res;
	}
}
