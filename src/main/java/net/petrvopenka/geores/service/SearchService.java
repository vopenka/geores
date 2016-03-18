package net.petrvopenka.geores.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

import net.petrvopenka.geores.entity.OsmLineEntity;
import net.petrvopenka.geores.entity.StreetEntity;
import net.petrvopenka.geores.repository.OsmLineRepository;
import net.petrvopenka.geores.repository.StreetRepository;
import net.petrvopenka.geores.utils.InvertCoordinatesFilter;

@Service
public class SearchService {
	
	@Autowired
	OsmLineRepository osmLineRepository;
	
	@Autowired
	StreetRepository streetRepository;
	
	
	public String search(String q, boolean transformToLatLong){
		
		// TODO parse search string and call different searches
		
		// Rue du Gabian
		// Impasse
		String json = searchLineHighway(q, transformToLatLong);
		return json;
	}
	
	
	public String searchStreet(String q){
		String geoJson = null;
		
		List<StreetEntity> streets = streetRepository.findByNameContaining(q);
		
		System.out.println("size of dataset: " + streets.size());
		
		SimpleFeatureTypeBuilder featureTypeBuilder = new SimpleFeatureTypeBuilder(); 
		
		// define attributes
		featureTypeBuilder.add("midpoint", Point.class); // if we want CRS in the output geoJson, we would specify it here as parameter
		featureTypeBuilder.add("name", String.class);
		featureTypeBuilder.setName("street");
		
		SimpleFeatureType featureType = featureTypeBuilder.buildFeatureType();
        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(featureType);
        
        List<SimpleFeature> featuresList = new ArrayList<SimpleFeature>();
        int i = 0;
        for(StreetEntity street : streets){
        	
        	// Attributes must by added in the same order as they were defined
        	featureBuilder.add(street.getMidpoint());
	        featureBuilder.add(street.getName());
	        SimpleFeature feature = featureBuilder.buildFeature(String.valueOf(i++));
	        featuresList.add(feature);
        }
        SimpleFeatureCollection features = new ListFeatureCollection(featureType,featuresList);
        
        // to avoid default 4 decimal places and loose precision for lat long values we define 7 decimals (approximately cm precision)
        FeatureJSON fj = new FeatureJSON(new GeometryJSON(7)); 

        try {
			geoJson = fj.toString(features);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return geoJson;
	}
	
	/**
	 * Search name of a street or road. Streets, roads, motorways, etc. are stored as lines and further defined by "highway" attribute.
	 * For any other lines, this attribute is null. Therefore we select lines that that have highways not null.
	 * @param q Search string
	 * @param transformToLatLong Source data is in 900913 projections, if needed in LatLong values, then transform
	 * @return geoJson
	 */
	public String searchLineHighway(String q, boolean transformToLatLong){
		
		List<OsmLineEntity> highways = osmLineRepository.findByHighwayNotNullAndNameContaining(q);

		String geoJson = null;
		
		try {
			
			// OSM is in Web Mercator projection (3857, unofficially 900913)
			CoordinateReferenceSystem sphericalMercator = CRS.decode("EPSG:3857");
			
			// Google maps uses Latitude Longitude (although it is not based on WGS84 elipsoid, I call this WGS84)
	        CoordinateReferenceSystem wgs84 = CRS.decode("EPSG:4326");
	        boolean lenient = false; // if different datums set to true to allow for some error
	        MathTransform transform = CRS.findMathTransform(sphericalMercator, wgs84, lenient);
			
			SimpleFeatureTypeBuilder featureTypeBuilder = new SimpleFeatureTypeBuilder(); 

			// define attributes
			featureTypeBuilder.add("geom", LineString.class); // if we want CRS in the output geoJson, we would specify it here as parameter
			featureTypeBuilder.add("name", String.class);
			featureTypeBuilder.setName("highway");
	        
	        SimpleFeatureType featureType = featureTypeBuilder.buildFeatureType();
	        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(featureType);
	        
	        
	        List<SimpleFeature> featuresList = new ArrayList<SimpleFeature>();
	        int i = 0;
	        for(OsmLineEntity highway : highways){

	        	Geometry sourceGeometry = highway.getGeom();
	        	Geometry targetGeometry = highway.getGeom();
	        	
	        	if(transformToLatLong){
	        		targetGeometry = JTS.transform(sourceGeometry, transform);
	        	}
	        	
	        	// Invert Coordinates since GeoJSON likes them that way
	        	targetGeometry.apply(new InvertCoordinatesFilter());

	        	// Attributes must by added in the same order as they were defined
	        	featureBuilder.add(targetGeometry);
		        featureBuilder.add(highway.getName());
		        SimpleFeature feature = featureBuilder.buildFeature(String.valueOf(i++));
		        featuresList.add(feature);
	        }
	        SimpleFeatureCollection features = new ListFeatureCollection(featureType,featuresList);
	        
	        // to avoid default 4 decimal places and loose precision for lat long values we define 7 decimals (approximately cm precision)
	        FeatureJSON fj = new FeatureJSON(new GeometryJSON(7)); 

	        geoJson = fj.toString(features);

        } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (NoSuchAuthorityCodeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		return geoJson;
	}

}
