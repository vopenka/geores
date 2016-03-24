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

####Run sql scripts
Couple of tables and views need to be created.
run sql scripts:
[streets](streets.sql)
