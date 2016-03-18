package net.petrvopenka.geores.utils;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateFilter;

public class InvertCoordinatesFilter implements CoordinateFilter {

	public void filter(Coordinate coord) {
        double oldX = coord.x;
        coord.x = coord.y;
        coord.y = oldX;
    }

	
}
