-- Transform to wgs84

SELECT gid,postcode,ST_Transform(geom,4326) AS geom
	INTO postcodes_4326
	FROM postcodes_27700;

ALTER TABLE postcodes_4326 ADD PRIMARY KEY (gid);
