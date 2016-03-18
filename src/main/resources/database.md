###Database setup
####1. install postgresql with postgis
```
sudo apt-get install postgresql postgis pgadmin3 postgresql-contrib
```
####2. create database in postgres with PostGIS extension
Use pgadminIII to create and access database

####3. Install osm2pgsql
```
sudo apt-get install osm2pgsql
```
####4. Download data
```
wget http://download.geofabrik.de/europe/monaco-latest.osm.pbf  -O /home/petr/temp/monaco-latest.osm.pbf
```
####3. Import data
```
osm2pgsql -s -U postgres -d monaco monaco-latest.osm.pbf
```

####Create view to expose streets
planet_osm_line may contain several lines per one street. These lines connect, but in the database they are separate. This would create problem when showing central point of a street as the street location. It would show central point of each street segment.
Therefore these lines need to be first merged. Then First segment is selected and on this segment, the central point is found and exposed as location of the street. The merge is done only for segments that touch each other and have same name.
```
CREATE OR REPLACE VIEW streets AS 
SELECT row_number() OVER () AS id, a.name, ST_Transform(ST_Line_Interpolate_Point(ST_GeometryN(ST_LineMerge(ST_Collect(a.way)),1), 0.5), 4326) AS geom 
FROM planet_osm_line AS a 
LEFT JOIN planet_osm_line AS b ON 
ST_Touches(a.way,b.way) 
    AND a.name = b.name 
GROUP BY ST_Touches(a.way,b.way), a.name
```
