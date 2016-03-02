package de.uniulm.in.ki.mbrenner.oremanager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Manages access to the ORE test ontologies.
 * @author spellmaker
 *
 */
public class OREManager {
	private Map<String, Integer> mapLabelToCol;
	private Map<String, Integer> mapFileToRow;
	
	private String[] captions;
	private List<String[]> data;
	private Path fileDir;
	
	/**
	 * Default constructor
	 * Initializes internal data structures
	 */
	public OREManager(){
		mapLabelToCol = new HashMap<>();
		mapFileToRow = new HashMap<>();
		data = new LinkedList<>();
	}
	
	private int[] convertToId(String[] labels){
		int[] positions = new int[labels.length];
		for(int i = 0; i < labels.length; i++) positions[i] = mapLabelToCol.get(labels[i]);
		return positions;
	}
	
	/**
	 * Copies ontologies matching the filter to a new location and creates a file containing the appropriate metadata
	 * @param target The target directory to which the files should be copied
	 * @param filter A filter which decides which elements should be copied
	 * @param positions The indices of the data in the metadata table which the filter needs to be supplied with
	 * @throws IOException If the files could not be copied
	 */
	public void copyOntologies(Path target, OREFilter filter, int ...positions) throws IOException{
		StringBuilder res = new StringBuilder(captions[0]);
		for(int i = 1; i < captions.length; i++){
			res.append(",").append(captions[i]);
		}
		res.append("\n");
		List<File> files = filterOntologies(filter, positions);
		for(File f : files){
			Files.copy(f.toPath(), target.resolve(f.getName()));
			String[] mdata = getMetadata(f.getName());
			res.append(mdata[0]);
			for(int i = 1; i < mdata.length; i++){
				res.append(",").append(mdata[i]);
			}
			res.append("\n");
		}
		BufferedWriter bw = new BufferedWriter(new FileWriter(target.resolve("metadata.csv").toFile()));
		bw.write(res.toString());
		bw.close();
	}
	
	/**
	 * Copies ontologies matching the filter to a new location
	 * @param target The target directory to which the files should be copied
	 * @param filter A filter which decides which elements should be copied
	 * @param labels The labels of the data in the metadata table which the filter needs to be supplied with
	 * @throws IOException If the files could not be copied
	 */
	public void copyOntologies(Path target, OREFilter filter, String ...labels) throws IOException{
		copyOntologies(target, filter, convertToId(labels));
	}
	
	/**
	 * Filters the ore ontologies and returns ontologies satisfying a filter
	 * @param filter The filter deciding which ontologies should be included
	 * @param positions The indices of the data in the metadata table that the filter needs 
	 * @return A list of ontologies satisfying the filter
	 */
	public List<File> filterOntologies(OREFilter filter, int...positions){
		List<File> result = new LinkedList<>();
		for(String[] ont : data){
			String[] input = new String[positions.length];
			for(int i = 0; i < input.length; i++){
				input[i] = ont[positions[i]];
			}
			
			if(filter.accept(input)){
				result.add(fileDir.resolve(ont[mapLabelToCol.get("filename")]).toFile());
			}
		}
		return result;
	}
	
	/**
	 * Filters the ore ontologies and returns ontologies satisfying a filter
	 * @param filter The filter deciding which ontologies should be included
	 * @param labels The labels of the data in the metadata table that the filter needs 
	 * @return A list of ontologies satisfying the filter
	 */
	public List<File> filterOntologies(OREFilter filter, String ...labels){
		return filterOntologies(filter, convertToId(labels));
	}
	
	/**
	 * Converts a column caption to an index
	 * @param s The column label
	 * @return The index of the column
	 */
	public int getCaptionIndex(String s){
		return mapLabelToCol.get(s);
	}
	
	/**
	 * Provides access to the column captions
	 * @return An array containing the column captions
	 */
	public String[] getCaptions(){
		return captions;
	}
	
	public String[] getMetadata(String filename){
		return getMetadata(mapFileToRow.get(filename));
	}
	
	public String[] getMetadata(int file){
		return data.get(file);
	}
	
	public String getMetadata(String filename, String label){
		return getMetadata(mapFileToRow.get(filename), mapLabelToCol.get(label));
	}
	
	public String getMetadata(String filename, int pos){
		return data.get(mapFileToRow.get(filename))[pos];
	}
	
	public String getMetadata(int file, int pos){
		return data.get(file)[pos];
	}
	
	private void loadSingle(File fileorder) throws IOException {
		boolean createCaptions = captions == null;
		BufferedReader br = new BufferedReader(new FileReader(fileorder));
		String s = br.readLine();
		if(createCaptions){
			captions = tokenizeLine(s, true);
		}
		else{
			String[] tmp = tokenizeLine(s, false);
			if(tmp.length != captions.length){
				if(br != null) br.close();
				throw new IOException("ore metadata tables do not match");
			}
			for(int i = 0; i < tmp.length; i++){
				if(!tmp[i].equals(captions[i])){
					if(br != null) br.close();
					throw new IOException("ore metadata tables do not match ('" + tmp[i] + "' vs '" + captions[i] + "')");
				}
			}
		}
		s = br.readLine();
		int filename = mapLabelToCol.get("filename");
		while(s != null){
			String[] tmp = tokenizeLine(s, false);
			if(!mapFileToRow.containsKey(tmp[filename])){
				data.add(tmp);
				mapFileToRow.put(tmp[filename], data.size() - 1);
			}
			s = br.readLine();
		}
		br.close();
	}
	
	/**
	 * Initializes the manager by loading the ore ontologies (currently only el)
	 * @param ore_directory The root ore directory
	 * @param subdirs Which subdirectories should be loaded. Current options are: {el, ql, dl, dl_pure}\{classification, instantiation, consistency}
	 * @throws IOException If the ontologies could not be loaded
	 */
	public void load(Path ore_directory, String ...subdirs) throws IOException{
		fileDir = ore_directory.resolve("files");
		for(String s : subdirs){
			loadSingle(ore_directory.resolve(s).resolve("metadata.csv").toFile());
		}
		
		Set<String> names = new HashSet<>();
		for(int i = 0; i < data.size(); i++){
			names.add(data.get(i)[mapLabelToCol.get("filename")]);
		}
	}
	
	/**
	 * Loads all files in a specified directory
	 * @param directory The directory
	 * @return A list of files in the directory
	 * @throws IOException If an error occured
	 */
	public List<File> loadDirectory(Path directory) throws IOException{
		List<File> result = new LinkedList<>();
		Files.newDirectoryStream(directory).forEach(x -> result.add(x.toFile()));
		return result;
	}
	
	private String[] tokenizeLine(String line, boolean addToMap){
		String[] result = line.split(",");
		for(int pos = 0; pos < result.length; pos++){
			if(addToMap){
				mapLabelToCol.put(result[pos], pos);
			}
		}
		
		
		//StringTokenizer st = new StringTokenizer(line, ",");
		//String[] result = new String[st.countTokens()];
		/*for(int pos = 0; pos < result.length; pos++){
			result[pos] = st.nextToken();
			if(pos == watchpos){
				System.out.println(result[pos]);
			}
			if(addToMap){
				if(result[pos].equals("abox_size")){
					System.out.println("index is " + pos);
					watchpos = pos;
				}
				mapping.put(result[pos], pos);
			}
		}*/
		return result;
	}
}