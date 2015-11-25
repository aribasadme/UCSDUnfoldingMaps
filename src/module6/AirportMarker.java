package module6;

import java.util.List;

import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.marker.SimpleLinesMarker;
import processing.core.PConstants;
import processing.core.PFont;
import processing.core.PGraphics;

/**
 * A class to represent AirportMarkers on a world map.
 * 
 * @author Adam Setters and the UC San Diego Intermediate Software Development
 *         MOOC team
 *
 */
public class AirportMarker extends CommonMarker implements Comparable<AirportMarker>{

	protected String name;
	protected int quote;
	protected float size = 5;
	protected int space = 6;

	private PFont font;
	private float fontSize = 12;

	public static List<SimpleLinesMarker> routes;

	public AirportMarker(Feature city) {
		super(((PointFeature) city).getLocation(), city.getProperties());

	}

	@Override
	public void drawMarker(PGraphics pg, float x, float y) {
		// save previous styling
		pg.pushStyle();
		
		pg.fill(color);
		pg.ellipse(x, y, size, size);

		// call method implemented in child class to draw marker shape
		drawAirplane(pg, x, y);
		
		// reset to previous styling
		pg.popStyle();
	}

	@Override
	public void showTitle(PGraphics pg, float x, float y) {
		name = getCity() + " (" + getCode() + ")";

		pg.pushStyle();
		pg.pushMatrix();
		// show rectangle with title
		if (selected) {
			pg.translate(0, 0, 1);
		}
		pg.strokeWeight(strokeWeight);
		if (selected) {
			pg.fill(highlightColor);
			pg.stroke(highlightStrokeColor);
		} else {
			pg.fill(color);
			pg.stroke(strokeColor);
		}

		// label
		if (selected && name != null) {
			if (font != null) {
				pg.textFont(font);
			}
			pg.fill(highlightColor);
			pg.stroke(highlightStrokeColor);
			pg.rect(x + strokeWeight / 2, y - fontSize + strokeWeight / 2 - space, pg.textWidth(name) + space * 1.5f,
					fontSize + space);
			pg.fill(255, 255, 255);
			pg.text(name, Math.round(x + space * 0.75f + strokeWeight / 2),
					Math.round(y + strokeWeight / 2 - space * 0.75f));
		}
		// show routes

		pg.popMatrix();
		pg.popStyle();

	}

	/** toString
	 * Returns an earthquake marker's string representation
	 * @return the string representation of an earthquake marker.
	 */
	public String toString()
	{
		return getCode();
	}
	/*
	 * getters for earthquake properties
	 */
	public String getCountry() {
		return (String) getProperty("country");
	}

	public String getCity() {
		return (String) getProperty("city");
	}

	public int getAltitude() {
		return Integer.parseInt(getProperty("altitude").toString());
	}

	public String getCode() {
		return (String) getProperty("code");
	}

	public void drawAirplane(PGraphics pg, float x, float y) {
	}

	@Override
	public int compareTo(AirportMarker m) {
		return (this.getCode()).compareTo(m.getCode());
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof AirportMarker))
            return false;
	    AirportMarker m = (AirportMarker) o;
	    return m.getCode().equals(this.getCode());
	}

	@Override
	public int hashCode() {
	    return 31*this.getCode().hashCode();
	}

}
