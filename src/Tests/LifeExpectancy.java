package Tests;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.providers.Google;
import de.fhpotsdam.unfolding.utils.MapUtils;
import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.marker.Marker;

import processing.core.PApplet;

public class LifeExpectancy extends PApplet
{
	UnfoldingMap map;
	HashMap<String,Float> lifeExpByCountry;
	List<Marker> countryMarkers;
	
	public static void main(String args[]) {
		PApplet.main(new String[] { LifeExpectancy.class.getName() });
	}
	
	public void setup()
	{
		size(800, 600, OPENGL);
		map = new UnfoldingMap(this, 50, 50, 700, 500, new Google.GoogleMapProvider());
		map.zoomToLevel(2);
		MapUtils.createDefaultEventDispatcher(this, map);
		
		lifeExpByCountry = loadLifeExpectancyFromCSV("LifeExpectancyWorldBank.csv");
		println("Loaded " + lifeExpByCountry.size() + " data entries");
		
		shadeCountries();
	}
	
	public void draw()
	{
		map.draw();
	}
	
	private void shadeCountries(){
		for (Marker marker : countryMarkers) {
			// Find data for country of the current marker
			String countryId = marker.getId();
			Float dataEntry = lifeExpByCountry.get(countryId);

			if (dataEntry != null) {
				// Encode value as brightness (values range: 0-1000)
				float transparency = map(dataEntry, 0, 700, 10, 255);
				marker.setColor(color(255, 0, 0, transparency));
			} else {
				// No value available
				marker.setColor(color(100, 120));
			}
		}
	}
	
	private HashMap<String, Float> loadLifeExpectancyFromCSV(String fileName)
	{
		HashMap<String, Float> lifeExpMap = new HashMap<String, Float>();
		List<Feature> countries;
		
		String[] rows = loadStrings(fileName);
		
		for (String row : rows){
			// Reads country name and average life expectancy
			String[] columns = row.split(",");
			if (columns.length >= 3) {
				float value = Float.parseFloat(columns[5]);
				lifeExpMap.put(columns[4], value);
			}
		}
		return lifeExpMap;
	}

}
