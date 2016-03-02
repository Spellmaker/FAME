package de.uniulm.in.ki.mbrenner.oremanager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class ORELoader {
	public static List<File> getEL_ORE(String basepath){
		Path list = Paths.get("orefiles.txt");
		if(Files.exists(list)){	
			System.out.println("[INFO] Reading test files from 'orefiles.txt'");
			BufferedReader br = null;
			try{
				br = new BufferedReader(new FileReader(list.toFile()));
				List<File> result = new LinkedList<>();
				String s = br.readLine();
				while(s != null){
					result.add(new File(s));
					s = br.readLine();
				}
				br.close();
				return result;
			}
			catch(Exception e){
				if(br != null)
					try {
						br.close();
					} catch (IOException e1) {
						//this should never happen
						e1.printStackTrace();
					}
			}
		}
		System.out.println("[INFO] Could not access 'orefiles.txt', rederiving file list");
		Path ore_folder = Paths.get(basepath);
		Path el = ore_folder.resolve("el");
		File el_class = el.resolve("classification").resolve("fileorder.txt").toFile();
		File el_cons = el.resolve("consistency").resolve("fileorder.txt").toFile();
		File el_instantiation = el.resolve("instantiation").resolve("fileorder.txt").toFile();
				
		Set<File> files = new HashSet<>();
		try{
			System.out.println("[INFO] resolving classification files");
			getFiles(ore_folder.resolve("files"), el_class, files);
		}
		catch(IOException e){
			System.out.println("[WARN] Could not gather files from classification");
		}
		try{
			System.out.println("[INFO] resolving consistency files");
			getFiles(ore_folder.resolve("files"), el_cons, files);
		}
		catch(IOException e){
			System.out.println("[WARN] Could not gather files from consistency");
		}
		try{
			System.out.println("[INFO] resolving instantiation files");
			getFiles(ore_folder.resolve("files"), el_instantiation, files);
		}
		catch(IOException e){
			System.out.println("[WARN] Could not gather files from instantiation");
		}
		List<File> result = new LinkedList<>();
		result.addAll(files);
		
		//store list
		BufferedWriter bw = null;
		try{
			bw = new BufferedWriter(new FileWriter(list.toFile()));
			for(File f : result){
				bw.write(f.toString() + "\n");
			}
			System.out.println("[INFO] Stored file list in 'orefiles.txt'");
		}
		catch(IOException e){
			System.out.println("[WARN] Could not store file list in 'orefiles.txt'");
		}
		finally{
			if(bw != null)
				try {
					bw.close();
				} catch (IOException e) {
					//this should never happen
					e.printStackTrace();
				}
		}
		return result;
	}
	
	public static void getFiles(Path filePath, File fileorder, Set<File> files) throws FileNotFoundException, IOException{
		BufferedReader br = new BufferedReader(new FileReader(fileorder));
		String line = br.readLine();
		while(line != null){
			if(!Files.exists(filePath.resolve(line))){
				System.out.println("[WARN] File not found: '" + line + "'");
				continue;
			}
			files.add(filePath.resolve(line).toFile());
			line = br.readLine();
		}
		br.close();
	}
}
