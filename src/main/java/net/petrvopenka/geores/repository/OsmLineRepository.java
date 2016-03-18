package net.petrvopenka.geores.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import net.petrvopenka.geores.entity.OsmLineEntity;

@RepositoryRestResource
public interface OsmLineRepository extends PagingAndSortingRepository<OsmLineEntity, Long>{

	public List<OsmLineEntity> findByHighwayAndName(String highway, String name);
	public List<OsmLineEntity> findByHighwayNotNullAndNameContaining(String name);
	
	@Query(value = "SELECT ST_AsEWKT(ST_Line_Interpolate_Point(geometry, 0.5)) FROM planet_osm_line WHERE name=?1;", nativeQuery = true)
	public Collection findMidPointsOfHighwayByNameLike(String name);
	
}
