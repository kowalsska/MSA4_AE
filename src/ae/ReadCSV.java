package ae;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class ReadCSV {
	
	  public Map<String, Map<String, Integer>> coocurrenceAll = new HashMap<>();

	  public static void main(String[] args) {
	
		ReadCSV obj = new ReadCSV();
		obj.getCoocurrence();
	
	  }
	
	  public void getCoocurrence() {
	
		String csvFile1 = "photos.csv";
		BufferedReader br1 = null;
		String line = "";
		String cvsSplitBy = ",";
		List<Map<String, String>> allPhotos = new ArrayList<>();
		Set<String> tagsSet = new HashSet<>();
		Map<String, String> tagNames = this.getTagNames();
		
		System.out.println(tagNames);
		System.out.println(tagNames.size());
	
		try {
			br1 = new BufferedReader(new FileReader(csvFile1));
			while ((line = br1.readLine()) != null) {
				
				Map<String, String> singlePhoto = new HashMap<>();
				
				String[] photo = line.split(cvsSplitBy);
				
				if(tagNames.containsKey(photo[0])){
					photo[0] = tagNames.get(photo[0]);
				}
				if(tagNames.containsKey(photo[1])){
					photo[1] = tagNames.get(photo[1]);
				}
				if(tagNames.containsKey(photo[2])){
					photo[2] = tagNames.get(photo[2]);
				}
	
				singlePhoto.put("tag1", photo[0]);
				singlePhoto.put("tag2", photo[1]);
				singlePhoto.put("tag3", photo[2]);
				
				//if(!singlePhoto.containsValue("0")) {
					allPhotos.add(singlePhoto);
					tagsSet.add(photo[0]);
					tagsSet.add(photo[1]);
					tagsSet.add(photo[2]);
				//}
				
			}
			
			System.out.println(tagsSet);
			System.out.println(tagsSet.size());
			
			//Create map of all tags with empty maps as values
			for (String tag : tagsSet) {
				Map<String, Integer> newMap = new HashMap<>();
				coocurrenceAll.put(tag, newMap);
			}
			
			//System.out.println(coocurrenceAll.size());
			
			
			
			for(Entry<String, Map<String, Integer>> tagMap : coocurrenceAll.entrySet()) {
				
				for(Map<String, String> photo : allPhotos) {
	
					if(photo.containsValue(tagMap.getKey())) {
						String cooc1 = photo.get("tag1");
						String cooc2 = photo.get("tag2");
						String cooc3 = photo.get("tag3");
						

						
						Map<String, Integer> tagsMap = tagMap.getValue();
						
						if(!cooc1.equals(tagMap.getKey())) {
							if(tagsMap.containsKey(cooc1)) {
								int value = tagsMap.get(cooc1);
								int newValue = value + 1;
								tagsMap.put(cooc1, newValue);
							} else {
								tagsMap.put(cooc1, 1);
							}
						}
						
						if(!cooc2.equals(tagMap.getKey())) {
							if(tagsMap.containsKey(cooc2)) {
								int value = tagsMap.get(cooc2);
								int newValue = value + 1;
								tagsMap.put(cooc2, newValue);
							} else {
								tagsMap.put(cooc2, 1);
							}
						}
						
						if(!cooc3.equals(tagMap.getKey())) {
							if(tagsMap.containsKey(cooc3)) {
								int value = tagsMap.get(cooc3);
								int newValue = value + 1;
								tagsMap.put(cooc3, newValue);
							} else {
								tagsMap.put(cooc3, 1);
							}
						}
					}
				}
				
			}
			
			PrintWriter writer = new PrintWriter("coocurrence.csv", "UTF-8");
			
			for(Map.Entry<String, Map<String, Integer>> tagMap : coocurrenceAll.entrySet()) {
				
				StringBuilder aLine = new StringBuilder();
				Map<String, Integer> entries = tagMap.getValue();
				
				aLine.append(tagMap.getKey());
				
				for(Entry<String, Integer> entry : entries.entrySet()){
					//aLine.append(", " + entry.getKey() + "=" + entry.getValue() );
					aLine.append(", " + entry.getKey());
				}
				
				writer.println(aLine.toString());
				
			}
			
			writer.close();
	
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br1 != null) {
				try {
					br1.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	
	  }
	  
	  public Map<String, String> getTagNames() {
		
		String csvFile1 = "tags.csv";
		BufferedReader br1 = null;
		String csvFile2 = "photos_tags.csv";
		BufferedReader br2 = null;
		String line = "";
		String cvsSplitBy = ",";
		Map<String, String> tags = new HashMap<>();
	
		try {
			br2 = new BufferedReader(new FileReader(csvFile2));
			while ((line = br2.readLine()) != null) {
				
				String[] tag = line.split(cvsSplitBy);
	
				tags.put(tag[0], tag[1]);
			}
			
//			br1 = new BufferedReader(new FileReader(csvFile1));
//			while ((line = br1.readLine()) != null) {
//				
//				String[] tag = line.split(cvsSplitBy);
//	
//				tags.put(tag[1], tag[0]);
//			}
	
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br2 != null) {
				try {
					br2.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return tags;
	  }

}