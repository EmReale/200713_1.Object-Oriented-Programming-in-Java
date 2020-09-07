package unfoldingMaps;

import de.fhpotsdam.unfolding.data.PointFeature;
import processing.core.PGraphics;

/** Implements a visual marker for land earthquakes on an earthquake map
 * @author UC San Diego Intermediate Software Development MOOC team
 * @author ER
 */
public class LandQuakeMarker extends EarthquakeMarker {
	
	/* Calls and overrides EarthquakeMarker constructor */ 
	public LandQuakeMarker(PointFeature quake) {
		super(quake);
		// Sets field in EarthquakeMarker
		isOnLand = true;
	}

	
	/* Draws the shape at the location of the Earthquake
	 * Fill colour is set in EarthquakeMarker */
	@Override
	public void drawEarthquake(PGraphics pg, float x, float y) {
		// The radius refers to its severity
		pg.ellipse(x, y, 2*radius, 2*radius);
	}
	

	/* Returns the country that the earthquake was in */
	public String getCountry() {
		return (String) getProperty("country");
	}	
}