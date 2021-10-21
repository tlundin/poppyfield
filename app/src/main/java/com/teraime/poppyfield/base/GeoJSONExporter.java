package com.teraime.poppyfield.base;

import android.app.Activity;
import android.content.Context;
import android.util.JsonWriter;
import android.util.Log;

import com.teraime.poppyfield.gis.GisConstants;
import com.teraime.poppyfield.loader.Configurations.VariablesConfiguration;
import com.teraime.poppyfield.viewmodel.WorldViewModel;
import org.json.JSONObject;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class GeoJSONExporter  {

    private JsonWriter writer;
	private final List<String> coordLess = new ArrayList<>();
	private final Map<String,String> rutMap = new HashMap<>();
	private final Map<String,String> authorMap = new HashMap<>();
	private int varC=0,exx=0;
	private final WorldViewModel mWorld;

	GeoJSONExporter(WorldViewModel model) {
		mWorld = model;
	}

	public JSONObject writeVariables(DBHelper.DBColumnPicker cp) {
		Logger o = Logger.gl();
        StringWriter sw = new StringWriter();
		writer = new JsonWriter(sw);

		try {
			if (cp!=null && cp.moveToFirst()) {
				writer.setIndent("  ");
				//Begin main obj
				writer.beginObject();
				Log.d("nils","Writing header");
				write("name","Export");
				write("type","FeatureCollection");
				writer.name("crs");
				writer.beginObject();
				write("type","name");
				writer.name("properties");
				writer.beginObject();
				write("name","EPSG:3006");
				writer.endObject();
				//end header
				writer.endObject();
				writer.name("features");
				writer.beginArray();

				Map<String,String> currentHash=null;

				//gisobjects: A map between UID and variable key-value pairs.
				HashMap<String, Map<String, Map<String, String>>> gisObjects=null;


				Map<String, String> gisObjM;
				do {
					String uuid=null,spy=null;
					currentHash = cp.getKeyColumnValues();
					if (currentHash==null) {
						
						o.e("Missing keyHash!");
						Log.e("vortex","Missing keyHash!");
						continue;
					}
					uuid = currentHash.get("uid");
					rutMap.put(uuid,currentHash.get(NamedVariables.AreaTerm));

					//Log.d("botox","CURRENT_HASH: "+currentHash);
					if (uuid==null) {
						Log.e("vortex","missing uid!!!");
						Log.e("vortex","keyhash: "+currentHash);
					}
					else {
						if (gisObjects==null)
							gisObjects = new HashMap<String,Map<String,Map<String,String>>>();
						//Find maps per spy.
						Map<String, Map<String, String>> gisObjH = gisObjects.get(uuid);
						if (gisObjH==null) {
							//No spys at all? Create new sub.
							gisObjH = new HashMap<String, Map<String, String>>();
							gisObjects.put(uuid,gisObjH);
						}

						//Hack for multiple SPY1 variables.
						Map<String,String> row;
						if (cp.getVariable()!=null) {
							String name = cp.getVariable().name;
							//Try to find in variable config.
							row = mWorld.getVariableExtraFields(name);
							if (row!=null) {
								name =  row.get(VariablesConfiguration.Col_Variable_Name);
							}
							authorMap.put(uuid,cp.getVariable().creator);
						} else {
							o.e("Variable was null!");
						}
					}
				} while (cp.next());
				Log.d("GEOJSONEXP","now inserting into json.");
				//For each fixedGid (uid)...
				if (gisObjects!=null) {
					final int sz = gisObjects.keySet().size();
					int curr = 0;
					for (final String keyUID:gisObjects.keySet()) {
						//Log.d("vortex", "Spy sets under " + keyUID);
						final int cf = curr++;
						Map<String, Map<String, String>> gisObjH = gisObjects.get(keyUID);
						//First do the default.
						gisObjM = gisObjH.get(null);
						if (gisObjM==null) {
							Log.e("vortex", "NULL gisobjM.Keys: "+gisObjH.keySet());
							gisObjM = gisObjH.get(gisObjH.keySet().iterator().next());
							//String coor = gisObjM.remove(GisConstants.GPS_Coord_Var_Name);
							//Log.d("vortex","COOR is "+coor);

							continue;
						}
						String coordinates = getCoordinates(keyUID, gisObjM.remove(GisConstants.GPS_Coord_Var_Name));

						String[] polygons = null;
						if (coordinates == null) {
							o.e("Missing coordinates for "+keyUID);
						}
						polygons = coordinates.split("\\|");
						String geoType= gisObjM.get(GisConstants.Geo_Type);
						Log.d("gorgon","geotype: "+geoType);
							//Try to figure out geotype from number of coordinates
							if (geoType==null) {
								if (polygons.length == 1) {
									String[] cs = polygons[0].split(",");
									if (cs != null) {
										if (cs.length == 2)
											geoType = "Point";
										else if (cs.length > 2)
											geoType = "Polygon";
									}
								} else if (polygons.length >= 2) {
									geoType = "Polygon";

								}
							}
						if (geoType==null){
							Log.d("brex","WRONG: "+polygons[0]);
							
							o.e("Failed to assign geotype for " + keyUID+" with polygons length = "+polygons.length+" and coordinates "+coordinates);
							continue;
						}
						boolean isPoly= "Polygon".equalsIgnoreCase(geoType);
						boolean isLineString = "Linestring".equalsIgnoreCase(geoType);
						if (isLineString)
							geoType="LineString";
						if (isPoly) {
							String firstPoly = polygons[0];
							String[] coordio = firstPoly.split(",");
							if (coordio.length<6){
								exx++;
								Log.e("bengo","polygon has "+coordio.length+" count:"+exx);
								continue;
							}
						}
						//Beg of line.
						writer.beginObject();
						write("type", "Feature");
						writer.name("geometry");
						writer.beginObject();
						write("type", geoType);
						writer.name("coordinates");
						Log.d("brex","RUTA: "+rutMap.get(keyUID));
						Log.d("brex","GID: "+keyUID);

						if (isPoly||isLineString)
							writer.beginArray();

						for (String polygon : polygons) {

							String[] coords = polygon.split(",");
							if (isPoly)
								writer.beginArray();
							Log.d("vortex", "is poly? "+isPoly);
							Log.d("vortex", "geotype is  "+geoType);
							Log.d("vortex", "Length is "+coords.length);
							try {
								for (int i = 0; i < coords.length; i += 2) {
									Log.d("vortex", "coord [" + i + "] :" + coords[i]+" [" + (i+1) + "] :" + coords[i+1]);
									writer.beginArray();
									printCoord(writer, coords[i]);
									printCoord(writer, coords[i + 1]);
									writer.endArray();
								}
								//Close poly if not done.
								if (isPoly && !lastCoordEqualToFirst(coords)) {
									Log.d("vortex","Closing poly..");
									writer.beginArray();
									printCoord(writer, coords[0]);
									printCoord(writer, coords[1]);
									writer.endArray();
								}



							} catch (IllegalStateException e) {
								Log.e("brex","Illegalstate!!");
								Log.e("brex","Full Poly is "+polygon);
							}
							if (isPoly)
								writer.endArray();
						}
						if (isPoly||isLineString)
							writer.endArray();
						//End geometry.
						writer.endObject();
						writer.name("properties");
						writer.beginObject();
						//Add the UUID
						write(GisConstants.FixedGid, keyUID);
						String ruta = rutMap.get(keyUID);
						String author = authorMap.get(keyUID);
						if (ruta!=null)
							write(NamedVariables.AreaTerm.toUpperCase(),ruta);
						write("author", author);
						//write("timestamp",cp.getVariable().timeStamp);
						//write("author",cp.getKeyColumnValues().get("author"));
						for (String mKey : gisObjM.keySet()) {
							write(mKey, gisObjM.get(mKey));
							//Log.d("volde", "var, value: " + mKey + "," + gisObjM.get(mKey));
						}
						//Check if there are other spy than default.
						if (gisObjH != null && !gisObjH.isEmpty()) {
							writer.name("sub");
							writer.beginArray();
							for (String key : gisObjH.keySet()) {
								if (key == null) {
									//Log.d("volde", "skipping null key");
									continue;
								}
								//Log.d("volde", "found some extra under " + key);
								gisObjM = gisObjH.get(key);
								if (gisObjM != null) {
									writer.beginObject();
									write("SPY", key);
									for (String mKey : gisObjM.keySet()) {
										write(mKey, gisObjM.get(mKey));
										//Log.d("volde", "var, value: " + mKey + "," + gisObjM.get(mKey));
									}
									writer.endObject();
								}

							}
							writer.endArray();
						}
						writer.endObject();

						//eol
						writer.endObject();

					}

				} else {
					
					o.e("GisObjects was null!");
					return null;
				}
				//End of array.
				writer.endArray();
				//End of all.
				writer.endObject();
				return new JSONObject(sw.toString());
			}else
				Log.e("vortex","EMPTY!!!");
		} catch (Exception e) {

			e.printStackTrace();

			cp.close();
		} finally {
			cp.close();
		}

		return null;	}

	private boolean lastCoordEqualToFirst(String[] coords) {
		return (coords==null || coords.length==0 || (coords[0].equals(coords.length-2) &&
				coords[1].equals(coords.length-1))) ;


	}

	private String getCoordinates(String uid, String thisYear) {
		//If there is a coord for this year, return it.
		if (thisYear!=null)
			return thisYear;
		//otherwise, search historical for uid.
		return
				null;//GlobalState.getInstance().getDb().findVarFromUID(uid,GisConstants.GPS_Coord_Var_Name);
	}

	private void printCoord(JsonWriter writer, String coord) {
		try {
			if (coord == null || "null".equalsIgnoreCase(coord)) {
				Log.e("vortex", "coordinate was null in db. ");

				writer.nullValue();

			} else {
				try {
					writer.value(Float.parseFloat(coord));
				} catch (NumberFormatException e) {
					writer.nullValue();
				}
            }
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getType() {
		return "json";
	}

	private void write(String name,String value) throws IOException {
		String val = (value==null||value.length()==0)?"NULL":value;
		writer.name(name).value(val);
	}



}
