package net.petrvopenka.geores.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import com.vividsolutions.jts.geom.Point;

import lombok.Data;

@Data
@Entity
@Table(name="streets")
public class StreetEntity {
	
	
	@Id
	private long id;
	
	@Type(type="org.hibernate.spatial.GeometryType")
	@Column(name = "geom")
	private Point midpoint;
	
	private String name;

}
