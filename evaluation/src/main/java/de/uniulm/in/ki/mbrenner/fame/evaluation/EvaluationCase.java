package de.uniulm.in.ki.mbrenner.fame.evaluation;

import java.io.File;
import java.util.List;

public interface EvaluationCase {
	public void evaluate(List<File> ontologies, List<String> options) throws Exception;
}
