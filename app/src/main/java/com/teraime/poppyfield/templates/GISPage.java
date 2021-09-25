package com.teraime.poppyfield.templates;

import static com.teraime.poppyfield.gis.Geomatte.convert;

import android.util.Log;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.data.geojson.GeoJsonLayer;
import com.teraime.poppyfield.base.Block;
import com.teraime.poppyfield.base.Workflow;
import com.teraime.poppyfield.viewmodel.WorldViewModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.List;

public class GISPage extends Page {


    public GISPage(WorldViewModel model, String template, Workflow wf) {
        super(model, template, wf);
    }
    @Override
    public void onCreate(Fragment f) {
        super.onCreate(f);
    }

    public void onMapReady() {
        model.getMap().setMapType(GoogleMap.MAP_TYPE_NORMAL);
        reload();

    }

    private void drawLayers(GoogleMap googleMap) {
        List<Block> layers = workFlow.getBlocksOfType(Block.GIS_LAYER);
        List<Block> gisObjects = workFlow.getBlocksOfType(Block.GIS_POINTS);
        Log.d("v", "GETBLOCKSOFTYPE " + workFlow.getBlocksOfType(Block.GIS_LAYER));

        for (Block l : layers) {
            Log.d("v", "Layer" + l.getAttrs().toString());
        }
        for (Block gisObject : gisObjects) {

            Log.d("v", gisObject.getBlockType());
            Log.d("v", gisObject.getAttrs().toString());

        }

        try {
            File source = Paths.get(model.getCacheFolder(), "cache", "Traktersb").toFile();
            JSONObject geoJsonData = new JSONObject(convert(source));

            GeoJsonLayer gl = new GeoJsonLayer(googleMap, geoJsonData);
            gl.addLayerToMap();
            gl.setOnFeatureClickListener((GeoJsonLayer.GeoJsonOnFeatureClickListener) feature -> Log.d("v", feature.getProperties().toString()));

        } catch (JSONException | FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void reload() {
        Block gis = workFlow.getBlock(Block.GIS);
        String source = gis.getAttr("source");
        Log.d("v","In reload - source: "+source);
        model.updateBoundary(source);
        drawLayers(model.getMap());
    };
}
