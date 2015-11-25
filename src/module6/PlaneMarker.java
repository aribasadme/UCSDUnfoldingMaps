package module6;

import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PShape;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.AbstractMarker;

/**
 * This marker displays an image at its location.
 */
public class PlaneMarker extends AirportMarker {

	PShape img;
	protected float size = 5;
	protected int space = 6;

	public PlaneMarker(PointFeature feature, PShape img) {
		super(feature);
		img.scale(0.03f);
		this.img = img;
	}


	@Override
	public void drawAirplane(PGraphics pg, float x, float y) {
		pg.pushStyle();
		pg.shapeMode(PConstants.CENTER);
		// The image is drawn in object coordinates, i.e. the marker's origin (0,0) is at its geo-location.
		pg.shape(img, x, y);
		pg.popStyle();
		
	}

}
