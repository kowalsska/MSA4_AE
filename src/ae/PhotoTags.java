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
import java.util.Set;
import java.util.Map.Entry;

public class PhotoTags {

	public static void main(String[] args) {

		PhotoTags obj = new PhotoTags();
		Map<String, Map<String, Integer>> tagsCoocurrence = obj.getCoocurrence();
		obj.coocurenceToFile(tagsCoocurrence);
		obj.topFive(tagsCoocurrence);

	}
	
	public List<String> findFiveHighestValues(Map<String, Integer> singleMap) {
		List<Integer> allItems = new ArrayList<>();
		for(Entry<String, Integer> entry : singleMap.entrySet()) {
			allItems.add(entry.getValue());
		}
		allItems.sort(null);
		List<Integer> topFiveValues = allItems.subList(allItems.size()-5, allItems.size());
		List<String> topTags = new ArrayList<>();
		for(Entry<String, Integer> entry : singleMap.entrySet()) {
			if(topFiveValues.contains(entry.getValue()) && !topTags.contains(entry.getKey())) {
				topTags.add(entry.getKey());
			}
		}
		return topTags;
	}
	
	public void topFive(Map<String, Map<String, Integer>> map) {
		//Looking for top 5 tags which co-occur with the following tags: water, people, london
		List<String> waterTop = findFiveHighestValues(map.get("water"));
		List<String> peopleTop = findFiveHighestValues(map.get("people"));
		List<String> londonTop = findFiveHighestValues(map.get("london"));
		
		System.out.println("Top 5 for 'water': " + waterTop);
		System.out.println("Top 5 for 'people': " + peopleTop);
		System.out.println("Top 5 for 'london': " + londonTop);

	}

	public Map<String, Map<String, Integer>> getCoocurrence() {

		String csvFile1 = "photos_tags.csv";
		BufferedReader br1 = null;
		String line = "";
		String cvsSplitBy = ",";
		Set<String> tagsSet = new HashSet<>();

		Map<String, List<String>> tagsMap = new HashMap<>();

		//Reading photo_tags.csv file and creating a set of all tags
		try {
			br1 = new BufferedReader(new FileReader(csvFile1));
			while ((line = br1.readLine()) != null) {

				String[] tag = line.split(cvsSplitBy);
				tagsSet.add(tag[1]);

				if(tagsMap.containsKey(tag[0])) {
					List<String> coocList = tagsMap.get(tag[0]);
					coocList.add(tag[1]);
					tagsMap.put(tag[0], coocList);
				} else {
					List<String> newList = new ArrayList<>();
					newList.add(tag[1]);
					tagsMap.put(tag[0], newList);
				}
			}
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

		Map<String, Map<String, Integer>> tagsCoocurrence = new HashMap<>();

		//Getting coocurrence of all tags in the set
		for(String tag : tagsSet) {
			for(Entry<String, List<String>> photo : tagsMap.entrySet()){
				List<String> tagArray = photo.getValue();
				Map<String, Integer> tagMap = new HashMap<>();
				for(String sTag : tagArray) {
					if(tagArray.contains(tag)) {
						if(!tagsCoocurrence.containsKey(tag)) {
							tagMap.put(sTag, 1);
							tagsCoocurrence.put(tag, tagMap);
						} else {
							Map<String, Integer> exMap = tagsCoocurrence.get(tag);
							if(exMap.containsKey(sTag)) {
								int value = exMap.get(sTag) + 1;
								exMap.put(sTag, value);
							} else {
								exMap.put(sTag, 1);
							}
						}
					} else {
						if(!tagsCoocurrence.containsKey(tag)) {
							tagMap.put(sTag, 0);
							tagsCoocurrence.put(tag, tagMap);
						} else {
							Map<String, Integer> exMap = tagsCoocurrence.get(tag);
							if(exMap.containsKey(sTag)) {
								continue;
							} else {
								exMap.put(sTag, 0);
							}
						}
					}
				} 
			}
		}

		//Replacing value of coocurrence of a tag within itself with -1
		for(Map.Entry<String, Map<String, Integer>> tagMap : tagsCoocurrence.entrySet()) {
			String key = tagMap.getKey();
			tagMap.getValue().put(key, -1);
		}
		
		return tagsCoocurrence;
	}
	
	//Separate printing method
	public void coocurenceToFile(Map<String, Map<String, Integer>> map) {
		PrintWriter writer = null;
		try {
			writer = new PrintWriter("coocurrencePhotoTags.csv", "UTF-8");
			
			StringBuilder firstRow = new StringBuilder();
			Map<String, Integer> exampleEntry = map.get("shadow");
			for(Entry<String, Integer> tag : exampleEntry.entrySet()) {
				firstRow.append(" ," + tag.getKey());
			}
			
			writer.println(firstRow);

			for(Map.Entry<String, Map<String, Integer>> tagMap : map.entrySet()) {

				StringBuilder aLine = new StringBuilder();
				Map<String, Integer> entries = tagMap.getValue();

				aLine.append(tagMap.getKey());

				for(Entry<String, Integer> entry : entries.entrySet()){
					aLine.append(", " + entry.getValue() );
				}
				writer.println(aLine.toString());
			}
		}catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}

}
