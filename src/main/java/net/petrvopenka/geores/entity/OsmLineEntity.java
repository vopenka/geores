package net.petrvopenka.geores.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Type;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

import lombok.Data;

@Data
@Entity
@Table(name="planet_osm_line")
public class OsmLineEntity {
	
	@Id
	private long osm_id;
	
	@Type(type="org.hibernate.spatial.GeometryType")
	@Column(name = "way")
    private LineString geom;
	
	@Transient
	private Point midPoint;
	
	private String highway;
	
	private String name;


}
