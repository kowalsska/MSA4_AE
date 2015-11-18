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
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	// TASK 1
	//
	// Creating co-occurrence matrix of tags from on photos_tags.csv file
	public Map<String, Map<String, Double>> getCoocurrence() {

		String csvFile1 = "photos_tags.csv";
		BufferedReader br1 = null;
		String line = "";
		String cvsSplitBy = ",";
		Set<String> tagsSet = new HashSet<>();

		Map<String, List<String>> photoMap = new HashMap<>();

		//Reading photo_tags.csv and creating a map of photos where values are lists of co-existing tags
		try {
			br1 = new BufferedReader(new FileReader(csvFile1));
			while ((line = br1.readLine()) != null) {

				String[] photo = line.split(cvsSplitBy);
				tagsSet.add(photo[1]); //A set of all tags
				//photo[0] - photo ID
				//photo[1] - tag name
				if(photoMap.containsKey(photo[0])) { //If map already contains entries for this photo
					List<String> coocList = photoMap.get(photo[0]); //Extract co-occurrence list of the photo
					coocList.add(photo[1]); //Add new tag
					photoMap.put(photo[0], coocList); //Update the list
				} else {
					List<String> newList = new ArrayList<>(); //Create a new list
					newList.add(photo[1]); //Add the tag
					photoMap.put(photo[0], newList); //Put the list with in the map for the current photo
				}
			}
		} catch (FileNotFoundException e) { //Handling exceptions
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
		for(String tag : tagsSet) { //Iterate over every tag in the set that was created above
			for(Entry<String, List<String>> photo : photoMap.entrySet()){ //For every photo
				List<String> tagArray = photo.getValue(); //Extract the list of co-existing tags in the photo
				Map<String, Double> tagMap = new HashMap<>(); //Create a map to store tag names and values
				for(String sTag : tagArray) { //For every tag in the list of co-existing tags
					if(tagArray.contains(tag)) { //If the current tag we're iterating over (see top of this algorithm) is in the list
						if(!tagsCoocurrence.containsKey(tag)) { // And the main Map doesn't contain this tag yet
							tagMap.put(sTag, 1.0); //Add the tag with initial value 1.0 to the inner map
							tagsCoocurrence.put(tag, tagMap); //Add inner map to the main map using the current tag we're iterating over now as the key
						} else { //If the main Map contains this tag already
							Map<String, Double> exMap = tagsCoocurrence.get(tag); //Extract the existing map from the key
							if(exMap.containsKey(sTag)) { //If the existing map contains the current tag
								double value = exMap.get(sTag) + 1; //It means another co-occurrence occurred. Add 1 to the current co-occurrence value
								exMap.put(sTag, value); //Update the map at this tag
							} else {
								exMap.put(sTag, 1.0); // If the existing map does not contain the current tag, put 1.0 as initial value
							}
						}
					} else { //If the current tag we're iterating over (see top of this algorithm) is not in the list
						if(!tagsCoocurrence.containsKey(tag)) { //And if the main Map doesn't contain the current tag
							tagMap.put(sTag, 0.0); //Add the tag with initial value 0.0 to the inner map
							tagsCoocurrence.put(tag, tagMap); //Add inner map to the main map using the current tag we're iterating over now as the key
						} else { //The main Map contains the current tag
							Map<String, Double> exMap = tagsCoocurrence.get(tag); //Extract the inner map of the current tag
							if(exMap.containsKey(sTag)) { // If extracted map contains the tag from the list of co-existing tags we're iterating over
								continue; // Nothing happens
							} else { // If extracted map does not have the tag from the list of co-existing tags
								exMap.put(sTag, 0.0); //Initialize its value to 0.0
							}
						}
					}
				} 
			}
		}

		//Replacing value of co-ocurrence of a tag within itself with -1 to indicate that this value is not relevant
		for(Entry<String, Map<String, Double>> tagMap : tagsCoocurrence.entrySet()) {
			String key = tagMap.getKey();
			tagMap.getValue().put(key, -1.0);
		}
		
		return tagsCoocurrence;
	}
	
	//Separate output file creating method for the co-occurrence matrix
	public void coocurenceToFile(Map<String, Map<String, Double>> tagsCoocurrence) {
		PrintWriter writer = null;
		try {
			writer = new PrintWriter("coocurrencePhotoTags.csv", "UTF-8");
			StringBuilder firstRow = new StringBuilder();
			//The tagsCoocurrence Map is hashed and returns 'shadow' tag as first entry.
			//I'm doing the same to create the .csv file with co-occurrence matrix
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
					aLine.append(", " + entry.getValue() ); //Adding comma separated values for every entry in the Map
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
	//
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	// TASK 2
	//
	//Method for returning five tags with highest co-occurrence values from the Map
	public List<List<Object>> findFiveHighestValues(Map<String, Double> singleMap) {
		List<Double> allItems = new ArrayList<>();
		for(Entry<String, Double> entry : singleMap.entrySet()) { //Get an ArrayList all values of co-existing tags for specified tag
			allItems.add(entry.getValue());
		}
		allItems.sort(null); //Sort the values
		List<Double> topFiveValues = allItems.subList(allItems.size()-5, allItems.size()); //Get top 5 values
		List<List<Object>> topTags = new ArrayList<>();
		for(Entry<String, Double> entry : singleMap.entrySet()) { //Iterate over all tags again
			if(topFiveValues.contains(entry.getValue())) { //Find tags of the top 5 values
				List<Object> miniArray = new ArrayList<>(); //Create ArrayList for key and value pairs
				miniArray.add(entry.getKey()); //Name of the tag
				miniArray.add(entry.getValue()); //Value of the tag
				topTags.add(miniArray); //Add it to the top 5 list
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
	
	// Method for getting top 5 recommendations without IDF
	public void topFive(Map<String, Map<String, Double>> tagsCoocurrence) {
		//Looking for top 5 tags which co-occur with the following tags: water, people, london
		List<List<Object>> waterTop = findFiveHighestValues(tagsCoocurrence.get("water"));
		List<List<Object>> peopleTop = findFiveHighestValues(tagsCoocurrence.get("people"));
		List<List<Object>> londonTop = findFiveHighestValues(tagsCoocurrence.get("london"));
		
		//Printing out the results
		System.out.println("Top 5 for 'water': " + waterTop);
		System.out.println("Top 5 for 'people': " + peopleTop);
		System.out.println("Top 5 for 'london': " + londonTop);
	}
	//
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	// TASK 3
	//
	//Getting tag suggestions with popularity factor (IDF)
	public final double COLLECTION_SIZE = 10000; //Number of entries in photos.csv. I just used value 10000 as it was given in the exercise sheet

	//Creating a map of tags and their popularity values (the number of images tagged with tag X)
	//These values are taken from tags.csv file
	public Map<String, Double> tagsPopularityMap() {
		String csvFile = "tags.csv";
		BufferedReader br1 = null;
		String line = "";
		String cvsSplitBy = ",";
		Set<String> tagsSet = new HashSet<>();

		Map<String, Double> tagsPopularityMap = new HashMap<>();

		//Reading the file and
		//creating the map of popularity values. Names of tags are keys, popularity values are values
		try {
			br1 = new BufferedReader(new FileReader(csvFile));
			while ((line = br1.readLine()) != null) {
				String[] tag = line.split(cvsSplitBy);
				String key = tag[0];
				double value = Integer.parseInt(tag[1]);
				tagsPopularityMap.put(key, value);
				} 
			}catch (FileNotFoundException e) { //Exceptions
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
	
	//Method to round numbers to 3 decimals places (as shown in the example on Moodle)
	public static double roundNumber(double value, int places) {
	    long factor = (long) Math.pow(10, places);
	    value = value * factor;
	    long tmp = Math.round(value);
	    return (double) tmp / factor;
	}

	//Getting IDF value of a number
	public double getIDF(double x) {
		double value = COLLECTION_SIZE / x; // x - number of images with given tag - value taken from tags.csv
		double logValue = Math.log(value);
		return logValue;
	}
	
	//One method doing 2 things
	// 1. For specified tag, change values of co-occurrence by multiplying them by the IDF number
	// 2. Return top 5 co-existing tags for the specified tags, using findFiveHighestValues(Map m) method (see above)
	public List<List<Object>> calculateTopFiveWithIDF(Map<String, Map<String, Double>> map, Map<String, Double> tagsPopularityMap, String key) {
		Map<String, Double> singleMap = map.get(key); //Extract the map of the specified tag
		for(Entry<String, Double> entry : singleMap.entrySet()) { //For every co-existing tag 
			Double x = tagsPopularityMap.get(entry.getKey()); //Get the number of images with given tag from tagsPopularityMap
			Double oldValue = entry.getValue(); //Get old co-occurrence value
			Double valueWithIDF = getIDF(x) * oldValue; //Multiply it by IDF value of x
			entry.setValue(roundNumber(valueWithIDF, 3)); //Update the value in the map and round up to 3 decimal numbers
		}
		List<List<Object>> singleMapTop = findFiveHighestValues(singleMap); //Running the method to find top 5 tags of this new updated with IDF value map
		return singleMapTop;
	}
	
	//Method for getting top 5 recommendations with IDF
	public void topFiveWithIDF(Map<String, Map<String, Double>> map, Map<String, Double> tagsPopularityMap) {
		//Looking for top 5 tags which co-occur with the following tags: water, people, london
		List<List<Object>> waterTop = calculateTopFiveWithIDF(map, tagsPopularityMap, "water");
		List<List<Object>> peopleTop = calculateTopFiveWithIDF(map, tagsPopularityMap, "people");
		List<List<Object>> londonTop = calculateTopFiveWithIDF(map, tagsPopularityMap, "london");
		
		//Printing out the results
		System.out.println("Top 5 with IDF for 'water': " + waterTop);
		System.out.println("Top 5 with IDF for 'people': " + peopleTop);
		System.out.println("Top 5 with IDF for 'london': " + londonTop);
	}
}
