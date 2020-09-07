package unfoldingMaps;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.GeoJSONReader;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.AbstractShapeMarker;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.MultiMarker;
import de.fhpotsdam.unfolding.providers.Google;
import de.fhpotsdam.unfolding.providers.MBTilesMapProvider;
import de.fhpotsdam.unfolding.utils.MapUtils;
import module6.EarthquakeMarker;
import parsing.ParseFeed;
import processing.core.PApplet;

/** EarthquakeCityMap
 * An application with an interactive map displaying earthquake data.
 * Author: UC San Diego Intermediate Software Development MOOC team
 * @author ER
 * Date: 2020
 * */
public class EarthquakeCityMap extends PApplet {
	
	// We will use member variables, instead of local variables, to store the data
	// that the setup and draw methods will need to access (as well as other methods)
	
	// This is to get rid of eclipse warnings
	private static final long serialVersionUID = 1L;

	// IF YOU ARE WORKING OFFILINE, change the value of this variable to true
	private static final boolean offline = true;
	
	// This is where to find the local tiles, for working without an Internet connection
	public static String mbTilesString = "blankLight-1-3.mbtiles";
	
	//feed with magnitude 2.5+ Earthquakes
	private String earthquakesURL = "https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/2.5_week.atom";
	
	// The files containing city names/info and country names/info
	private String cityFile = "city-data.json";
	private String countryFile = "countries.geo.json";
	
	// The map
	private static UnfoldingMap map;
	
	// Markers for each city
	private static List<Marker> cityMarkers;
	// Markers for each earthquake
	private List<Marker> quakeMarkers;

	// A List of country markers
	private List<Marker> countryMarkers;
	
	// Last clicked/ hovered over
	private CommonMarker lastSelected;
	private CommonMarker lastClicked;
	
	// Variable for sortAndPrit method
	private int numToPrint;
	
	
	/* PApplet method containing elements to be set up once  
	 */
	public void setup() {		
		// 1.Initialises canvas and map tiles
		size(900, 700, OPENGL);
		if (offline) {
		    map = new UnfoldingMap(this, 200, 50, 650, 600, new MBTilesMapProvider(mbTilesString));
		    earthquakesURL = "2.5_week.atom";  // The same feed, but saved August 7, 2015
		}
		else {
			map = new UnfoldingMap(this, 200, 50, 650, 600, new Google.GoogleMapProvider());
			// IF YOU WANT TO TEST WITH A LOCAL FILE, uncomment the next line
		    //earthquakesURL = "2.5_week.atom";
		}
		MapUtils.createDefaultEventDispatcher(this, map);
		
		
		// 2. Reads in earthquake data and geometric properties
	    //    load country features and markers
		List<Feature> countries = GeoJSONReader.loadData(this, countryFile);
		countryMarkers = MapUtils.createSimpleMarkers(countries);
		
		//  Reads in city data
		List<Feature> cities = GeoJSONReader.loadData(this, cityFile);
		cityMarkers = new ArrayList<Marker>();
		for(Feature city : cities) {
		  cityMarkers.add(new CityMarker(city));
		}
	    
		// Reads in earthquake RSS feed
	    List<PointFeature> earthquakes = ParseFeed.parseEarthquake(this, earthquakesURL);
	    quakeMarkers = new ArrayList<Marker>();
	    
	    for(PointFeature feature : earthquakes) {
		  //check if LandQuake
		  if(isLand(feature)) {
		    quakeMarkers.add(new LandQuakeMarker(feature));
		  }
		  // OceanQuakes
		  else {
		    quakeMarkers.add(new OceanQuakeMarker(feature));
		  }
	    }

	    // to use for debugging
	    printQuakes();
	 		
	    // 3. Adds markers to map
	    // countryMarkers are used for their geometric properties, not added to map
	    map.addMarkers(quakeMarkers);
	    map.addMarkers(cityMarkers);
	    
	    // Calls method sortAndPrint
	    numToPrint = 5;
	    sortAndPrint(numToPrint);
	    
	}
	
	/* Draw method - constantly refreshes
	 */
	public void draw() {
		background(0);
		map.draw();
		addKey();
	}
	
	/*Event handler that gets called automatically when the mouse moves */
	@Override
	public void mouseMoved() {
		// clear the last selection
		if (lastSelected != null) {
			lastSelected.setSelected(false);
			lastSelected = null;
		}
		selectMarkerIfHover(quakeMarkers);
		selectMarkerIfHover(cityMarkers);
	}
	
	/*Method that is called when the mouse hovers over an element */
	private void selectMarkerIfHover(List<Marker> markers){
		
		// Ensure that no other marker is selected
		// If there is a marker under the cursor, and lastSelected is null
		if (lastSelected != null) {
			return;
		}
		//Iterate through markers
		for (Marker m: markers) {
			//Cast it to be able to set it as lastSelected (which is a CommonMarker)
			CommonMarker marker = (CommonMarker)m;
			//See if the markers is beneath mouseX and mouseY
			if(marker.isInside(map,mouseX,mouseY)) {
				// Set the lastSelected to be the marker found under the cursor
				lastSelected = marker;
				//If it is, the first one selected set to m.setSelected(true);
				marker.setSelected(true);
				return;
			}
		}
	}
	
	/* The event handler for mouse clicks
	 * It will display an earthquake and its threat circle of cities
	 * Or if a city is clicked, it will display all the earthquakes 
	 * where the city is in the threat circle
	 */
	@Override
	public void mouseClicked(){
		//If a marker has been clicked, lastSelected = null
		//unHide all if nothing's been clicked
		if (lastClicked != null) {
			unhideMarkers();
			lastClicked = null;
		}
		//If something has been clicked:
		else if (lastClicked == null) {
			quakeClicked();
			//If it wasn't an earthquake it must be a city:
			if (lastClicked == null) {
				cityClicked();
			}
		}
	}
		
	
	/* Helper method to determine if a city has been clicked
	 * and what to show if it has been
	 */
	private void cityClicked() {
		//Check that none have been clicked
		if (lastClicked != null) {
			return;
		}
		//Iterate through cityMarkers
		for (Marker cm: cityMarkers) {
			if (!cm.isHidden() && cm.isInside(map, mouseX, mouseY)) {
				lastClicked = (CommonMarker)cm;		
				//Hide cities that weren't clicked
				for (Marker mhide: cityMarkers) {
					if (mhide != lastClicked) {
						mhide.setHidden(true);
					}
				}
				//Show earthquakes which pose a threat
				for (Marker mark: quakeMarkers) {
					EarthquakeMarker quakemarker = (EarthquakeMarker)mark;
					if (quakemarker.getDistanceTo(cm.getLocation()) > quakemarker.threatCircle()) {
						quakemarker.setHidden(true);
					}
				}
				return;
			}
		}
	}
	
	/* Helper method to determine if a quake has been clicked
	 * and what to show if it has been
	 */
	private void quakeClicked(){
		//Check that nothing has already been selected
		if (lastClicked != null) {
			return;
		}
		for (Marker qm: quakeMarkers) {
			//Cast to earthquakeMarker && check if it's been clicked
			EarthquakeMarker marker = (EarthquakeMarker)qm;
			if (!marker.isHidden() && marker.isInside(map,mouseX,mouseY)) {
				lastClicked = marker;
				//Hide eqmarkers that weren't clicked:
				for (Marker mark: quakeMarkers) {
					if (mark != lastClicked) {
						mark.setHidden(true);
					}
				}
				//Hide all city markers unless within the threat zone
				for (Marker cm: cityMarkers) {
					if (cm.getDistanceTo(marker.getLocation()) > marker.threatCircle()) {
						cm.setHidden(true);
					}
				}
				return;
			}
		}
	}
	
	/* Loops through all markers and unhides them */
	private void unhideMarkers() {
		for(Marker marker : quakeMarkers) {
			marker.setHidden(false);
		}
			
		for(Marker marker : cityMarkers) {
			marker.setHidden(false);
		}
	}
	
	/* Helper method to draw GUI key */
	private void addKey() {	
		
		fill(255, 250, 240);
		
		int xbase = 25;
		int ybase = 50;
		
		rect(xbase, ybase, 150, 250);
		
		fill(0);
		textAlign(LEFT, CENTER);
		textSize(12);
		text("Earthquake Key", xbase+25, ybase+25);
		
		fill(150, 30, 30);
		int tri_xbase = xbase + 35;
		int tri_ybase = ybase + 50;
		triangle(tri_xbase, tri_ybase-CityMarker.TRI_SIZE, tri_xbase-CityMarker.TRI_SIZE, 
				tri_ybase+CityMarker.TRI_SIZE, tri_xbase+CityMarker.TRI_SIZE, 
				tri_ybase+CityMarker.TRI_SIZE);

		fill(0, 0, 0);
		textAlign(LEFT, CENTER);
		text("City Marker", tri_xbase + 15, tri_ybase);
		
		text("Land Quake", xbase+50, ybase+70);
		text("Ocean Quake", xbase+50, ybase+90);
		text("Size ~ Magnitude", xbase+25, ybase+110);
		
		fill(255, 255, 255);
		ellipse(xbase+35, 
				ybase+70, 
				10, 
				10);
		rect(xbase+35-5, ybase+90-5, 10, 10);
		
		fill(color(255, 255, 0));
		ellipse(xbase+35, ybase+140, 12, 12);
		fill(color(0, 0, 255));
		ellipse(xbase+35, ybase+160, 12, 12);
		fill(color(255, 0, 0));
		ellipse(xbase+35, ybase+180, 12, 12);
		
		textAlign(LEFT, CENTER);
		fill(0, 0, 0);
		text("Shallow", xbase+50, ybase+140);
		text("Intermediate", xbase+50, ybase+160);
		text("Deep", xbase+50, ybase+180);

		text("Past hour", xbase+50, ybase+200);
		
		fill(255, 255, 255);
		int centerx = xbase+35;
		int centery = ybase+200;
		ellipse(centerx, centery, 12, 12);

		strokeWeight(2);
		line(centerx-8, centery-8, centerx+8, centery+8);
		line(centerx-8, centery+8, centerx+8, centery-8);
			
	}

	
	
	/* Checks whether this quake occurred on land.  If it did, it sets the 
	 * "country" property of its PointFeature to the country where it occurred
	 * and returns true.  Notice that the helper method isInCountry will
	 * set this "country" property already.  Otherwise it returns false.	
	 */
	 private boolean isLand(PointFeature earthquake) {
		
		// Loops over all countries to check if location is in any of them
		// If it is, add 1 to the entry in countryQuakes corresponding to this country.
		for (Marker country : countryMarkers) {
			if (isInCountry(earthquake, country)) {
				return true;
			}
		}
		// not inside any country
		return false;
	}
	
	/* Returns true if quake is within a given country */
	private boolean isInCountry(PointFeature earthquake, Marker country) {
		// get location of feature
		Location checkLoc = earthquake.getLocation();

		// some countries represented as MultiMarker
		// looping over SimplePolygonMarkers which make them up to use isInsideByLoc
		if(country.getClass() == MultiMarker.class) {
				
			// looping over markers making up MultiMarker
			for(Marker marker : ((MultiMarker)country).getMarkers()) {
					
				// checking if inside
				if(((AbstractShapeMarker)marker).isInsideByLocation(checkLoc)) {
					earthquake.addProperty("country", country.getProperty("name"));
						
					// return if is inside one
					return true;
				}
			}
		}
			
		// check if inside country represented by SimplePolygonMarker
		else if(((AbstractShapeMarker)country).isInsideByLocation(checkLoc)) {
			earthquake.addProperty("country", country.getProperty("name"));
			return true;
		}
		return false;
	}
	
	
	/* Prints countries and no. of earthquakes */
	private void printQuakes() {
		int totalWaterQuakes = quakeMarkers.size();
		for (Marker country : countryMarkers) {
			String countryName = country.getStringProperty("name");
			int numQuakes = 0;
			for (Marker marker : quakeMarkers)
			{
				EarthquakeMarker eqMarker = (EarthquakeMarker)marker;
				if (eqMarker.isOnLand()) {
					if (countryName.equals(eqMarker.getStringProperty("country"))) {
						numQuakes++;
					}
				}
			}
			if (numQuakes > 0) {
				totalWaterQuakes -= numQuakes;
				System.out.println(countryName + ": " + numQuakes);
			}
		}
		System.out.println("OCEAN QUAKES: " + totalWaterQuakes);
	}
	
	
	/* Sorts earthquakes by decreasing magnitude and prints numToPrint 
	 * no. of quakes
	 */
	public void sortAndPrint(int numToPrint) {
		// Creates a new array of earthquakes
		List<EarthquakeMarker> quakes = new ArrayList<EarthquakeMarker>();
		for (Marker m: quakeMarkers) {
			quakes.add((EarthquakeMarker)m);
		}
		// Sorts them and prints out numToPrint no. of quakes
		Collections.sort(quakes);
		if (numToPrint>quakes.size()) {
			System.out.println(quakes);
		}
		else {
			for (int i = 0; i<numToPrint; i++) {
				System.out.println(quakes.get(i));
			}
		}
	}
		
	
	/* For the OceanMarker class to draw lines to cities within
	 * the threat circle */
	public List<Marker> getCityMarker() {
		return cityMarkers;
	}
	
	/* For the OceanMarker class to draw lines to cities within
	 * the threat circle */
	public UnfoldingMap getMap() {
		return map;
	}
}
