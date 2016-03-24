-- First create views for temporary storage of data

CREATE MATERIALIZED VIEW streets_view AS
SELECT row_number() OVER () AS id, foo.name AS name, ST_Transform(ST_ClosestPoint(geom, ST_Centroid(foo.geom)),4326) AS geom
FROM (SELECT a.name, ST_LineMerge(ST_Collect(a.way)) AS geom, ST_Touches(a.way,b.way) AS touch
	FROM planet_osm_line AS a LEFT JOIN planet_osm_line AS b ON ST_Touches(a.way,b.way) AND a.name = b.name 
	WHERE ST_Touches(a.way,b.way)
	GROUP BY ST_Touches(a.way,b.way), a.name
	UNION
	SELECT a.name, a.way AS geom, ST_Touches(a.way,b.way) AS touch
	FROM planet_osm_line AS a LEFT JOIN planet_osm_line AS b ON ST_Touches(a.way,b.way) AND a.name = b.name 
	WHERE ST_Touches(a.way,b.way) IS NOT true) AS foo;

CREATE MATERIALIZED VIEW countries AS 
	SELECT row_number() OVER () AS id, name, ST_Transform(way, 4326) AS geom
	FROM planet_osm_polygon
	WHERE admin_level = '4' AND name IS NOT NULL;

CREATE MATERIALIZED VIEW counties AS 
	SELECT row_number() OVER () AS id, name, ST_Transform(way, 4326) AS geom
	FROM planet_osm_polygon
	WHERE admin_level = '6' AND name IS NOT NULL;

CREATE MATERIALIZED VIEW ceremonials AS 
	SELECT row_number() OVER () AS id, name, ST_Transform(way, 4326) AS geom
	FROM planet_osm_polygon
	WHERE boundary = 'ceremonial' AND name IS NOT NULL;

CREATE MATERIALIZED VIEW cities AS 
	SELECT row_number() OVER () AS id, name, ST_Transform(way, 4326) AS geom
	FROM planet_osm_polygon
	WHERE admin_level = '8' AND name IS NOT NULL;

CREATE MATERIALIZED VIEW towns AS 
	SELECT row_number() OVER () AS id, name, ST_Transform(way, 4326) AS geom
	FROM planet_osm_point
	WHERE place = 'city' OR place = 'town' OR place = 'village' AND name IS NOT NULL;

-- Create first version of 'streets' table
-- use first ceremonials as counties.

SELECT s.id AS id, s.name AS street, ctr.name AS country, cer.name AS county, pc.postcode AS postcode, s.geom AS geom 
	INTO streets
	FROM streets_view AS s 
	LEFT JOIN countries AS ctr ON ST_Contains(ctr.geom, s.geom) 
	LEFT JOIN ceremonials AS cer ON ST_Contains(cer.geom, s.geom)
	LEFT JOIN postcodes_4326 AS pc ON ST_Contains(pc.geom, s.geom)
	WHERE s.name IS NOT NULL;

ALTER TABLE streets ADD PRIMARY KEY (id);

-- Overide ceremonials by real counties. Ceremonials stay only in case there is no county in place

UPDATE streets AS s
	SET county=ct.name
	FROM counties AS ct
	WHERE ST_Contains(ct.geom, s.geom);

-- Add cities

ALTER TABLE streets ADD COLUMN city text;

-- Spatial join closests city,town,village to the street

UPDATE streets AS s
	SET city=jtown.name
	FROM (SELECT DISTINCT ON(s.id) s.id AS id, t.name AS name
		FROM streets AS s, towns AS t
		WHERE ST_DWithin(s.geom, t.geom, 50000)
		ORDER BY s.id, ST_Distance(s.geom, t.geom)) jtown
		WHERE s.id = jtown.id;
		


	