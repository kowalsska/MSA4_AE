package ae;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class PhotoTags {

	public static void main(String[] args) {

		PhotoTags obj = new PhotoTags();
		Map<String, Map<String, Double>> tagsCoocurrence = obj.getCoocurrence();
		Map<String, Map<String, Double>> coocurrenceForIDF = obj.getCoocurrence();
		obj.coocurenceToFile(tagsCoocurrence);
		obj.topFive(tagsCoocurrence);
		Map<String, Double> tagsPopularityMap = obj.tagsPopularityMap();;
		obj.topFiveWithIDF(coocurrenceForIDF, tagsPopularityMap);
		

	}
	
	public List<List<Object>> findFiveHighestValues(Map<String, Double> singleMap) {
		List<Double> allItems = new ArrayList<>();
		for(Entry<String, Double> entry : singleMap.entrySet()) {
			allItems.add(entry.getValue());
		}
		allItems.sort(null);
		List<Double> topFiveValues = allItems.subList(allItems.size()-5, allItems.size());
		List<List<Object>> topTags = new ArrayList<>();
		for(Entry<String, Double> entry : singleMap.entrySet()) {
			if(topFiveValues.contains(entry.getValue()) && !topTags.contains(entry.getKey())) {
				List<Object> miniArray = new ArrayList<>();
				miniArray.add(entry.getKey());
				miniArray.add(entry.getValue());
				topTags.add(miniArray);
			}
		}
		
		//Sorting in descending order
		Collections.sort(topTags, new Comparator<List<Object>> () {
		    @Override
		    public int compare(List<Object> a, List<Object> b) {
		        return ((Double) b.get(1)).compareTo((Double) a.get(1));
		    }
		});

		return topTags;
	}
	
	public void topFive(Map<String, Map<String, Double>> tagsCoocurrence) {
		//Looking for top 5 tags which co-occur with the following tags: water, people, london
		List<List<Object>> waterTop = findFiveHighestValues(tagsCoocurrence.get("water"));
		List<List<Object>> peopleTop = findFiveHighestValues(tagsCoocurrence.get("people"));
		List<List<Object>> londonTop = findFiveHighestValues(tagsCoocurrence.get("london"));
		List<List<Object>> skyTop = findFiveHighestValues(tagsCoocurrence.get("sky"));
		
		System.out.println("Top 5 for 'water': " + waterTop);
		System.out.println("Top 5 for 'people': " + peopleTop);
		System.out.println("Top 5 for 'london': " + londonTop);
		System.out.println("Top 5 for 'sky': " + skyTop);

	}

	public Map<String, Map<String, Double>> getCoocurrence() {

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

		Map<String, Map<String, Double>> tagsCoocurrence = new HashMap<>();

		//Getting coocurrence of all tags in the set
		for(String tag : tagsSet) {
			for(Entry<String, List<String>> photo : tagsMap.entrySet()){
				List<String> tagArray = photo.getValue();
				Map<String, Double> tagMap = new HashMap<>();
				for(String sTag : tagArray) {
					if(tagArray.contains(tag)) {
						if(!tagsCoocurrence.containsKey(tag)) {
							tagMap.put(sTag, 1.0);
							tagsCoocurrence.put(tag, tagMap);
						} else {
							Map<String, Double> exMap = tagsCoocurrence.get(tag);
							if(exMap.containsKey(sTag)) {
								double value = exMap.get(sTag) + 1;
								exMap.put(sTag, value);
							} else {
								exMap.put(sTag, 1.0);
							}
						}
					} else {
						if(!tagsCoocurrence.containsKey(tag)) {
							tagMap.put(sTag, 0.0);
							tagsCoocurrence.put(tag, tagMap);
						} else {
							Map<String, Double> exMap = tagsCoocurrence.get(tag);
							if(exMap.containsKey(sTag)) {
								continue;
							} else {
								exMap.put(sTag, 0.0);
							}
						}
					}
				} 
			}
		}

		//Replacing value of coocurrence of a tag within itself with -1
		for(Entry<String, Map<String, Double>> tagMap : tagsCoocurrence.entrySet()) {
			String key = tagMap.getKey();
			tagMap.getValue().put(key, -1.0);
		}
		
		return tagsCoocurrence;
	}
	
	//Separate printing method for the matrix
	public void coocurenceToFile(Map<String, Map<String, Double>> tagsCoocurrence) {
		PrintWriter writer = null;
		try {
			writer = new PrintWriter("coocurrencePhotoTags.csv", "UTF-8");
			StringBuilder firstRow = new StringBuilder();
			Map<String, Double> exampleEntry = tagsCoocurrence.get("shadow");
			for(Entry<String, Double> tag : exampleEntry.entrySet()) {
				firstRow.append(" ," + tag.getKey());
			}
			
			writer.println(firstRow);

			for(Entry<String, Map<String, Double>> tagMap : tagsCoocurrence.entrySet()) {

				StringBuilder aLine = new StringBuilder();
				Map<String, Double> entries = tagMap.getValue();

				aLine.append(tagMap.getKey());

				for(Entry<String, Double> entry : entries.entrySet()){
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
	
	public final double COLLECTION_SIZE = 10000; //Number of entries in photos.csv

	public Map<String, Double> tagsPopularityMap() {
		String csvFile = "tags.csv";
		BufferedReader br1 = null;
		String line = "";
		String cvsSplitBy = ",";
		Set<String> tagsSet = new HashSet<>();

		Map<String, Double> tagsPopularityMap = new HashMap<>();

		//Reading photo_tags.csv file and creating a set of all tags
		try {
			br1 = new BufferedReader(new FileReader(csvFile));
			while ((line = br1.readLine()) != null) {
				String[] tag = line.split(cvsSplitBy);
				String key = tag[0];
				double value = Integer.parseInt(tag[1]);
				tagsPopularityMap.put(key, value);
				} 
			}catch (FileNotFoundException e) {
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
		return tagsPopularityMap;
	}
	
	public static double roundNumber(double value, int places) {
	    long factor = (long) Math.pow(10, places);
	    value = value * factor;
	    long tmp = Math.round(value);
	    return (double) tmp / factor;
	}

	public double getIDF(double x) {
		// x - number of images with given tag - value taken from tags.csv
		double value = (double)COLLECTION_SIZE / (double)x;
		double logValue = Math.log(value);
		return logValue;
	}
	
	public List<List<Object>> calculateTopFiveWithIDF(Map<String, Map<String, Double>> map, Map<String, Double> tagsPopularityMap, String key) {
		Map<String, Double> singleMap = map.get(key);
		
		for(Entry<String, Double> entry : singleMap.entrySet()) {
			Double x = tagsPopularityMap.get(entry.getKey());
			Double oldValue = entry.getValue();
			Double valueWithIDF = getIDF(x) * oldValue;
			entry.setValue(roundNumber(valueWithIDF, 3));
		}
		List<List<Object>> singleMapTop = findFiveHighestValues(singleMap);
		return singleMapTop;
	}
	
	public void topFiveWithIDF(Map<String, Map<String, Double>> map, Map<String, Double> tagsPopularityMap) {
		List<List<Object>> waterTop = calculateTopFiveWithIDF(map, tagsPopularityMap, "water");
		List<List<Object>> peopleTop = calculateTopFiveWithIDF(map, tagsPopularityMap, "people");
		List<List<Object>> londonTop = calculateTopFiveWithIDF(map, tagsPopularityMap, "london");
		List<List<Object>> skyTop = calculateTopFiveWithIDF(map, tagsPopularityMap, "sky");
		
		System.out.println("Top 5 with IDF for 'water': " + waterTop);
		System.out.println("Top 5 with IDF for 'people': " + peopleTop);
		System.out.println("Top 5 with IDF for 'london': " + londonTop);
		System.out.println("Top 5 with IDF for 'sky': " + skyTop);
	}
}
