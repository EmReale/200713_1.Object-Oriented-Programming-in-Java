package unfoldingMaps;

import java.util.ArrayList;
import java.util.List;

import de.fhpotsdam.unfolding.utils.ScreenPosition;
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.GeoJSONReader;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.marker.SimplePointMarker;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.providers.Google;
import processing.core.PGraphics;

/** Implements a visual marker for ocean earthquakes on an earthquake map
 * @author UC San Diego Intermediate Software Development MOOC team
 * @author ER
 *
 */

public class OceanQuakeMarker extends EarthquakeMarker {
	
	EarthquakeCityMap earthMap = new EarthquakeCityMap();
	
	public OceanQuakeMarker(PointFeature quake) {
		super(quake);
		isOnLand = false;
	}
	
	/* Draws the earthquake as a square
	* Draws a line between the earthquake and cities in the threat circle 
	* */
	@Override
	public void drawEarthquake(PGraphics pg, float x, float y) {
		pg.rect(x-radius, y-radius, 2*radius, 2*radius);
		//Get cities and map (static fields) from EarthquakeCityMap
		List<Marker> cityMarkers = earthMap.getCityMarker();
		UnfoldingMap map = earthMap.getMap();
		
		// Iterates through each cityMarker
		for (Marker city: cityMarkers) {
			//Get screen position of cities
			float xcity = map.getScreenPosition(city.getLocation()).x;
			float ycity = map.getScreenPosition(city.getLocation()).y;
			//Draw a line between city/ earthquake if it's within the threat zone
			if (city.getDistanceTo(getLocation()) <= threatCircle()) {
						pg.strokeWeight(1);
						pg.line(x, y, xcity, ycity);
			}
		}
	}
}
