package unfoldingMaps;

import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.geo.Location;
import processing.core.PConstants;
import processing.core.PGraphics;


/** Implements a visual marker for cities on an earthquake map
 *  Author: UC San Diego Intermediate Software Development MOOC team
 *  @author ER
 */

public class CityMarker extends CommonMarker {
	// The size of the triangle marker
	public static int TRI_SIZE = 5;  
	
	/* Constructor overrides the super class's location */
	public CityMarker(Location location) {
		super(location);
	}
	
	/* Constructor overrides the super class's location */
	public CityMarker(Feature city) {
		super(((PointFeature)city).getLocation(), city.getProperties());
	}
	
	
	/* Draws a marker for each city */
	public void drawMarker(PGraphics pg, float x, float y) {
		// Save previous drawing style
		// Used as this code was not written in the PApplet draw() method
		pg.pushStyle();
		
		// Draw a triangle
		pg.fill(150, 30, 30);
		pg.triangle(x, y-TRI_SIZE, x-TRI_SIZE, y+TRI_SIZE, x+TRI_SIZE, y+TRI_SIZE);
		
		// Restore previous drawing style
		pg.popStyle();
	}
	
	
	/*Shows the title of a city if selected */
	public void showTitle(PGraphics pg, float x, float y){
		//Retrieve and save information to use
		String name = getCity() + " " + getCountry() + " ";
		String pop = "Pop: " + getPopulation() + " Million";
		
		pg.pushStyle();
		
		pg.fill(255, 255, 255);
		pg.textSize(12);
		pg.rectMode(PConstants.CORNER);
		pg.rect(x, y-TRI_SIZE-39, Math.max(pg.textWidth(name), pg.textWidth(pop)) + 6, 39);
		pg.fill(0, 0, 0);
		pg.textAlign(PConstants.LEFT, PConstants.TOP);
		pg.text(name, x+3, y-TRI_SIZE-33);
		pg.text(pop, x+3, y - TRI_SIZE -18);
		
		pg.popStyle();
	}
	
	/* Returns the city name */
	private String getCity(){
		return getStringProperty("name");
	}
	
	/* Returns the country name */
	private String getCountry(){
		return getStringProperty("country");
	}
	
	/* Returns the city's population */
	private float getPopulation(){
		return Float.parseFloat(getStringProperty("population"));
	}
}
