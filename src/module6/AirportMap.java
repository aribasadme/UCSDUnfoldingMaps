package module6;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.utils.MapUtils;
import de.fhpotsdam.unfolding.utils.ScreenPosition;
import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.GeoJSONReader;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.data.ShapeFeature;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.SimpleLinesMarker;
import de.fhpotsdam.unfolding.marker.SimplePointMarker;
import de.fhpotsdam.unfolding.providers.AbstractMapProvider;
import de.fhpotsdam.unfolding.providers.AcetateProvider;
import de.fhpotsdam.unfolding.providers.Google;
import de.fhpotsdam.unfolding.providers.MBTilesMapProvider;
import de.fhpotsdam.unfolding.providers.Microsoft;
import de.fhpotsdam.unfolding.providers.OpenStreetMap;

import parsing.ParseFeed;
import processing.core.PApplet;
import processing.core.PShape;

/**
 * An applet that shows airports (and routes) on a world map.
 * 
 * @author Adam Setters and the UC San Diego Intermediate Software Development
 *         MOOC team
 *
 */
public class AirportMap extends PApplet {

	// You can ignore this. It's to get rid of eclipse warnings
	private static final long serialVersionUID = 1L;

	/**
	 * This is where to find the local tiles, for working without an Internet
	 * connection
	 */
	public static String mbTilesString = "blankLight-1-3.mbtiles";

	// The files containing city names and info and country names and info
	private String airportsSource = "airports.dat";
	private String airportsVisitedSource = "airports-visited.dat";
	private String countryFile = "countries.geo.json";

	// Big map showing a detailed area
	UnfoldingMap map;
	// Small map showing the overview, i.e. the world
	UnfoldingMap mapOverviewStatic;
	// Interactive finder box atop the overview map.
	ViewportRect viewportRect;

	// Map providers
	AbstractMapProvider provider1;
	AbstractMapProvider provider2;
	AbstractMapProvider provider3;
	AbstractMapProvider provider4;

	// Markers for each airport
	private List<AirportMarker> airportList;
	private List<AirportMarker> airportsVisitedList;
	// Markers for each route
	private List<Marker> routeList;

	// A list of country markers
	private List<Marker> countryMarkers;

	// Control mouse interaction variables
	private CommonMarker lastSelected;
	private CommonMarker lastClicked;
	
	private PShape plane;

	public void setup() {
		// setting up PAppler
		size(900, 650, OPENGL);

		provider1 = new AcetateProvider.All();
		provider2 = new Google.GoogleMapProvider();
		provider3 = new Microsoft.AerialProvider();
		provider4 = new OpenStreetMap.OpenStreetMapProvider();

		// setting up map and default events
		// Detail map with default mouse and keyboard interactions
		map = new UnfoldingMap(this, 10, 10, 600, 600);
		map.zoomAndPanTo(4, new Location(50.26f, 12.1f));;
		map.setZoomRange(3, 10);
		
		// Static overview map
		mapOverviewStatic = new UnfoldingMap(this, 620, 10, 185, 185);

		MapUtils.createDefaultEventDispatcher(this, map);

		viewportRect = new ViewportRect();
		
		// get features from airport data
		List<PointFeature> airportFeatures = ParseFeed.parseAirports(this, airportsSource);
		
		List<PointFeature> airportsVisitedFeatures = ParseFeed.parseAirports(this, airportsVisitedSource);

		// list for markers, hashmap for quicker access when matching with
		// routes
		airportList = new ArrayList<AirportMarker>();
		HashMap<Integer, Location> airportsHash = new HashMap<Integer, Location>();
		airportsVisitedList = new ArrayList<AirportMarker>();
		HashMap<Integer, Location> airportsVisitedHash = new HashMap<Integer, Location>();
		
		// create markers from features
		for (PointFeature af : airportFeatures) {
			if (af.getProperty("code") != null) {
				AirportMarker m = new AirportMarker(af);
				m.setRadius(5);
				m.setId(af.getId());
				airportList.add(m);

				// put airport in hashmap with OpenFlights unique id for key
				airportsHash.put(Integer.parseInt(af.getId()), af.getLocation());

			}
		}
	
		// create image markers from airports visited
		plane = loadShape("airplane.svg");
		plane.scale(0.03f);
		
		for (PointFeature avf : airportsVisitedFeatures) {
			if (avf.getProperty("code") != null) {
				AirportMarker pm = new PlaneMarker(avf, loadShape("airplane.svg"));
				pm.setId(avf.getId());
				airportsVisitedList.add(pm);
				
				// put airport in hashmap with OpenFlights unique id for key
				airportsVisitedHash.put(Integer.parseInt(avf.getId()), avf.getLocation());

			}
		}
		
		sortAndRemoveAirports();

		// parse route data
		List<ShapeFeature> routes = ParseFeed.parseRoutes(this, "routes.dat");
		routeList = new ArrayList<Marker>();
		for (ShapeFeature route : routes) {
			// get source and destination airportIds
			int source = Integer.parseInt((String) route.getProperty("source"));
			int dest = Integer.parseInt((String) route.getProperty("destination"));
			
			// get locations for airports on route
			if (airportsVisitedHash.containsKey(source) && airportsVisitedHash.containsKey(dest)) {
				route.addLocation(airportsVisitedHash.get(source));
				route.addLocation(airportsVisitedHash.get(dest));
			}

			SimpleLinesMarker sl = new SimpleLinesMarker(route.getLocations(), route.getProperties());
			sl.setColor(color(0,255,0));
			sl.setHidden(true);
			// System.out.println(sl.getProperties());
			// UNCOMMENT IF YOU WANT TO SEE ALL ROUTES
			routeList.add(sl);
		}
		
		
		// Load country polygons and adds them as markers
		List<Feature> countries = GeoJSONReader.loadData(this, countryFile);
		countryMarkers = MapUtils.createSimpleMarkers(countries);

		// UNCOMMENT IF YOU WANT TO SEE ALL ROUTES
		// map.addMarkers(countryMarkers);
		map.addMarkers(routeList);
		map.addMarkers(new ArrayList<Marker>(airportList));
		map.addMarkers(new ArrayList<Marker>(airportsVisitedList));
		
	}

	private void sortAndRemoveAirports() {
		int itemIndex;
		Collections.sort(airportList);
		Collections.sort(airportsVisitedList);
		for (AirportMarker airportVisited : airportsVisitedList){
			if (airportList.contains(airportVisited)){
				itemIndex = airportList.indexOf(airportVisited);
				airportList.set(itemIndex, airportVisited);
			}
		}		
	}

	public void draw() {
		background(0);
		
		map.draw();
		mapOverviewStatic.draw();
		
		addKey();
		
		// Viewport is updated by the actual area of the detail map
		ScreenPosition tl = mapOverviewStatic.getScreenPosition(map.getTopLeftBorder());
		ScreenPosition br = mapOverviewStatic.getScreenPosition(map.getBottomRightBorder());
		viewportRect.setDimension(tl, br);
		viewportRect.draw();
	}


	float oldX;
	float oldY;

	public void panViewportOnDetailMap() {
		float x = viewportRect.x + viewportRect.w / 2;
		float y = viewportRect.y + viewportRect.h / 2;
		Location newLocation = mapOverviewStatic.mapDisplay.getLocation(x, y);
		map.panTo(newLocation);
	}

	public void mousePressed() {
		if (viewportRect.isOver(mouseX, mouseY)) {
			viewportRect.dragged = true;
			oldX = mouseX - viewportRect.x;
			oldY = mouseY - viewportRect.y;
		}
	}

	public void mouseReleased() {
		viewportRect.dragged = false;
	}

	public void mouseDragged() {
		if (viewportRect.dragged) {
			viewportRect.x = mouseX - oldX;
			viewportRect.y = mouseY - oldY;

			panViewportOnDetailMap();
		}
	}
	
	public void mouseClicked() {
		if (lastClicked != null) {
			unhideVisited();
			hideRoutes();
			lastClicked = null;
		}
		else if (lastClicked == null) 
		{
			checkAirportsForClick();
		}
	}
	
	private void hideRoutes() {
		for (Marker marker : routeList) {
			marker.setHidden(true);
		}
	}

	private void checkAirportsForClick() {
		if (lastClicked != null) return;
		
		for (Marker m : airportsVisitedList) {
			AirportMarker marker = (AirportMarker) m;
			if (!marker.isHidden() && marker.isInside(map, mouseX, mouseY)) {
				lastClicked = marker;
				marker.setSelected(true);
				// Show routes from that airport
				for (Marker route : routeList) {
					if (route.getProperty("destination").equals(lastClicked.getId()) || route.getProperty("source").equals(lastClicked.getId())) {
						route.setHidden(false);
					}
					
				}
				return;
			}
		}
	}
	
	public void mouseMoved() {
		// clear the last selection and unhide
		if (lastSelected != null) {
			lastSelected.setSelected(false);
			lastSelected = null;
			unhideAirports();

		}
		selectMarkerIfHover(airportList);
		// loop();
	}

	// If there is a marker selected
	private void selectMarkerIfHover(List<AirportMarker> markers) {
		// Abort if there's already a marker selected
		if (lastSelected != null) {
			return;
		}

		for (Marker m : markers) {
			CommonMarker marker = (CommonMarker) m;
			if (marker.isInside(map, mouseX, mouseY)) {
				lastSelected = marker;
				marker.setSelected(true);
				// Hide all the other airports except those visited
				for (Marker mhide : airportList) {
					if (mhide != lastSelected && !airportsVisitedList.contains(mhide)) {
						mhide.setHidden(true);
					}
				}
				return;
			}
		}
	}

	private void unhideAirports() {
		for (Marker marker : airportList) {
			marker.setHidden(false);
		}
	}	
	private void unhideVisited() {
		for (Marker marker : airportsVisitedList) {
			marker.setHidden(false);
		}
	}
		
	private void addKey() {
		fill(205, 210, 212);
		noStroke();

		int xbase = 620;
		int ybase = 205;

		rect(xbase, ybase, 185, 250);

		fill(0);
		textAlign(LEFT, CENTER);
		textSize(14);
		text("International Airports", xbase + 10, ybase + 25);

		fill(0, 0, 0);
		textAlign(LEFT, CENTER);
		textSize(12);
		text("Airports", xbase + 25, ybase + 48);
		text("Visited Airports", xbase + 25, ybase + 70);

		fill(0, 0, 0);
		ellipse(xbase + 15, ybase + 50, 10, 10);
		line(xbase + 15, ybase + 50, xbase + 30, ybase + 80);
		shapeMode(CENTER);
		shape(plane, xbase + 15, ybase + 72);
		
		fill(0, 0, 0);
		line(xbase + 15, ybase + 50, xbase + 30, ybase + 80);
	}

	public void keyPressed() {
		if (key == '1') {
			map.mapDisplay.setProvider(provider1);
			mapOverviewStatic.mapDisplay.setProvider(provider1);
		} else if (key == '2') {
			map.mapDisplay.setProvider(provider2);
			mapOverviewStatic.mapDisplay.setProvider(provider2);
		} else if (key == '3') {
			map.mapDisplay.setProvider(provider3);
			mapOverviewStatic.mapDisplay.setProvider(provider3);
		} else if (key == '4') {
			map.mapDisplay.setProvider(provider4);
			mapOverviewStatic.mapDisplay.setProvider(provider4);
		}
	}

	class ViewportRect {
		float x;
		float y;
		float w;
		float h;
		boolean dragged = false;

		public boolean isOver(float checkX, float checkY) {
			return checkX > x && checkY > y && checkX < x + w && checkY < y + h;
		}

		public void setDimension(ScreenPosition tl, ScreenPosition br) {
			this.x = tl.x;
			this.y = tl.y;
			this.w = br.x - tl.x;
			this.h = br.y - tl.y;
		}

		public void draw() {
			noFill();
			stroke(251, 114, 0, 240);
			rect(x, y, w, h);
		}

	}

}
