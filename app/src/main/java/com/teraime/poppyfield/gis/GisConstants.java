package com.teraime.poppyfield.gis;

import com.google.android.gms.maps.GoogleMap;

public class GisConstants {
	public final static String POINT = "Point";
	public static final String MULTI_POINT = "MultiPoint";
	public static final String LINE_STRING = "LineString";
	public static final String POLYGON = "Polygon";
	public static final String FixedGid = "FIXEDGID";
	public static final String GlobalGid= "GlobalID";
	public static final String SKYDDSVART = "skyddsvärt";
	public static final String TYPE_COLUMN = "gistyp";
	public static final String Geo_Type = "geotype";
	//public static final String Location = "gpscoord";
	public static final String SWEREF = "Sweref";
	public static final String LATLONG= "latlong";
	public static final String RutaID = "trakt";
	public static final String DefaultTag = "Def";
	public static final String GPS_Coord_Var_Name = "gpscoord";
	public static final String MULTI_POLYGON = "Multipolygon";
	public static final String ObjectID ="OBJECTID";

	public static int getGoogleMapType(String mapType) {
		switch (mapType) {
			case "normal":
				return GoogleMap.MAP_TYPE_NORMAL;
			case "satellite":
				return GoogleMap.MAP_TYPE_SATELLITE;
			case "terrain":
				return GoogleMap.MAP_TYPE_TERRAIN;
			case "hybrid":
				return GoogleMap.MAP_TYPE_HYBRID;
			case "none":
				return GoogleMap.MAP_TYPE_NONE;
			default:
				return GoogleMap.MAP_TYPE_NORMAL;

		}
	}

}
