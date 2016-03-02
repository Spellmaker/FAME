package de.uniulm.in.ki.mbrenner.oremanager;

public class ORESizeFilter implements OREFilter{
	private int pos;
	private int min;
	private int max;
	
	public ORESizeFilter(int pos, int min, int max){
		this.pos = pos;
		this.min = min;
		this.max = max;
	}

	@Override
	public boolean accept(String... s) {
		return Integer.parseInt(s[pos]) > min && Integer.parseInt(s[pos]) < max;
	}
}
