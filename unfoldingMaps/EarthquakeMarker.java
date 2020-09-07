package unfoldingMaps;

import de.fhpotsdam.unfolding.data.PointFeature;
import processing.core.PConstants;
import processing.core.PGraphics;
import java.util.*;

/** Implements a visual marker for earthquakes on an earthquake map
 * 
 * @author UC San Diego Intermediate Software Development MOOC team & ER
 *
 */
/* Implements the comparable interface */
public abstract class EarthquakeMarker extends CommonMarker implements Comparable<EarthquakeMarker>{
	
	// Returns whether or not the earthquake occurred on land;
	// Set by the subclasses.
	protected boolean isOnLand;

	// The radius of the Earthquake marker
	// Set in the constructor using the thresholds below
	protected float radius;
	
	// constants for distance
	protected static final float kmPerMile = 1.6f;
	
	// Greater than or equal to this threshold is a moderate earthquake
	public static final float THRESHOLD_MODERATE = 5;
	// Greater than or equal to this threshold is a light earthquake
	public static final float THRESHOLD_LIGHT = 4;

	// Greater than or equal to this threshold is an intermediate depth
	public static final float THRESHOLD_INTERMEDIATE = 70;
	// Greater than or equal to this threshold is a deep depth
	public static final float THRESHOLD_DEEP = 300;

	
	// abstract method to be implemented in subclasses
	public abstract void drawEarthquake(PGraphics pg, float x, float y);
		
	
	/* Constructor */
	public EarthquakeMarker (PointFeature feature) {
		super(feature.getLocation());
		// Add a radius property and then set the properties
		java.util.HashMap<String, Object> properties = feature.getProperties();
		float magnitude = Float.parseFloat(properties.get("magnitude").toString());
		properties.put("radius", 2*magnitude );
		setProperties(properties);
		this.radius = 1.75f*getMagnitude();
	}
	

	/* Calls abstract method drawEarthquake and then checks when earthquake occurred
	 * and draws X if recent
	 */
	@Override
	public void drawMarker(PGraphics pg, float x, float y) {
		// save previous styling
		pg.pushStyle();
			
		// determine color of marker from depth
		colorDetermine(pg);
		
		// call abstract method implemented in child class to draw marker shape
		drawEarthquake(pg, x, y);
		
		// Adds X over marker if within past day		
		String age = getStringProperty("age");
		if ("Past Hour".equals(age) || "Past Day".equals(age)) {
			
			pg.strokeWeight(2);
			int buffer = 2;
			pg.line(x-(radius+buffer), 
					y-(radius+buffer), 
					x+radius+buffer, 
					y+radius+buffer);
			pg.line(x-(radius+buffer), 
					y+(radius+buffer), 
					x+radius+buffer, 
					y-(radius+buffer));
		}
		
		// reset to previous styling
		pg.popStyle();
		
	}

	/* Show the title of the earthquake if this marker is selected */
	public void showTitle(PGraphics pg, float x, float y){
		String title = getTitle();
		pg.pushStyle();
		
		pg.rectMode(PConstants.CORNER);
		
		pg.stroke(110);
		pg.fill(255,255,255);
		pg.rect(x, y + 15, pg.textWidth(title) +6, 18, 5);
		
		pg.textAlign(PConstants.LEFT, PConstants.TOP);
		pg.fill(0);
		pg.text(title, x + 3 , y +18);
		
		
		pg.popStyle();
	}

	
	/* Return the "threat circle" radius, i.e distance up to 
	 * which this earthquake can affect things */
	public double threatCircle() {	
		double miles = 20.0f * Math.pow(1.8, 2*getMagnitude()-5);
		double km = (miles * kmPerMile);
		return km;
	}
	
	/* Determines the colour of the marker from depth
	 * Deep = red, intermediate = blue, shallow = yellow
	 */
	private void colorDetermine(PGraphics pg) {
		float depth = getDepth();
		
		if (depth < THRESHOLD_INTERMEDIATE) {
			pg.fill(255, 255, 0);
		}
		else if (depth < THRESHOLD_DEEP) {
			pg.fill(0, 0, 255);
		}
		else {
			pg.fill(255, 0, 0);
		}
	}
	
	/* Implements the comparable interface */
	public int compareTo(EarthquakeMarker other) {
		//to return them from high to low put OTHER FIRST
		//to return from low to high, put THIS FIRST
		return Float.compare(other.getMagnitude(),this.getMagnitude());
	}
	
	/* Returns the earthquake's string representation */
	public String toString() {
		return getTitle();
	}

	// GETTERS FOR EARTHQUAKE PROPERTIES
	
	/* Returns the earthquake's magnitude */
	public float getMagnitude() {
		return Float.parseFloat(getProperty("magnitude").toString());
	}
	
	/* Returns the earthquake's depth */
	public float getDepth() {
		return Float.parseFloat(getProperty("depth").toString());	
	}
	
	/* Returns the earthquake's title */
	public String getTitle() {
		return (String) getProperty("title");	
	}
	
	/* Returns the radius */
	public float getRadius() {
		return Float.parseFloat(getProperty("radius").toString());
	}
	
	/* Returns whether or not the earthquake is on land */
	public boolean isOnLand() {
		return isOnLand;
	}
}
