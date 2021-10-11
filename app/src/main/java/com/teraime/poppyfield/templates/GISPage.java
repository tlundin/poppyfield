package com.teraime.poppyfield.templates;

import static com.teraime.poppyfield.gis.Geomatte.convert;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.data.geojson.GeoJsonFeature;
import com.google.maps.android.data.geojson.GeoJsonLayer;
import com.google.maps.android.data.geojson.GeoJsonPoint;
import com.google.maps.android.data.geojson.GeoJsonPointStyle;
import com.google.maps.android.data.geojson.GeoJsonPolygon;
import com.google.maps.android.data.geojson.GeoJsonPolygonStyle;
import com.google.maps.android.ui.IconGenerator;
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
            String obj_context = gisObject.getAttr("obj_context");
           // labelE = Expressor.preCompileExpression(label);
           // objContextE = Expressor.preCompileExpression(objectContext);
        }

        try {
            File source = Paths.get(model.getCacheFolder(), "cache", "Traktersb").toFile();
            JSONObject geoJsonData = new JSONObject(convert(source));

            GeoJsonLayer gl = new GeoJsonLayer(googleMap, geoJsonData);
            fillPolygons(gl);
            gl.addLayerToMap();
            gl.setOnFeatureClickListener((GeoJsonLayer.GeoJsonOnFeatureClickListener) feature -> {
                Log.d("flutter", "HEPP");
                Log.d("v", feature.getProperties().toString());
                //model.getPageStack().changePage(elem.get("target"));
            });

        } catch (JSONException | FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void fillPolygons(GeoJsonLayer layer) {
        // Iterate over all the features stored in the layer
        for (GeoJsonFeature feature : layer.getFeatures()) {
            // Check if the magnitude property exists
            if (feature.getGeometry() instanceof GeoJsonPolygon) {
                if (feature.hasProperty("TYPKOD")) {
                    String typkod = feature.getProperty("TYPKOD");
                    Log.d("google", "TYPkod " + typkod);
                    if (typkod.equals("HP")) {
                        GeoJsonPolygonStyle ps = new GeoJsonPolygonStyle();
                        ps.setFillColor(Color.CYAN);
                        LatLng x = ((GeoJsonPolygon) feature.getGeometry()).getCoordinates().get(0).get(0);
                        addText(x, feature.getProperty("Shape_Area"), 1, 12);
                        feature.setPolygonStyle(ps);
                    }
                } else
                    Log.d("google", "no typ");
            } else if (feature.getGeometry() instanceof GeoJsonPoint) {
                IconGenerator icg = new IconGenerator(model.getActivity());
                icg.setStyle(IconGenerator.STYLE_WHITE);
                Bitmap bmp = icg.makeIcon(feature.getProperty("TRAKT"));
                GeoJsonPointStyle gp = new GeoJsonPointStyle();
                gp.setIcon(BitmapDescriptorFactory.fromBitmap(bmp));
                //gp.setTitle("French Mc Cheeze");
                feature.setPointStyle(gp);
            }
        }
    }
    public Marker addText(final LatLng location, final String text, final int padding,
                          final int fontSize) {
        Marker marker = null;

        if (model.getActivity() == null || model.getMap() == null || location == null || text == null
                || fontSize <= 0) {
            return marker;
        }

        final TextView textView = new TextView(model.getActivity());
        textView.setText(text);
        textView.setTextSize(fontSize);

        final Paint paintText = textView.getPaint();

        final Rect boundsText = new Rect();
        paintText.getTextBounds(text, 0, textView.length(), boundsText);
        paintText.setTextAlign(Paint.Align.CENTER);

        final Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        final Bitmap bmpText = Bitmap.createBitmap(boundsText.width() + 2
                * padding, boundsText.height() + 2 * padding, conf);

        final Canvas canvasText = new Canvas(bmpText);
        paintText.setColor(Color.WHITE);

        canvasText.drawText(text, canvasText.getWidth() / 2,
                canvasText.getHeight() - padding - boundsText.bottom, paintText);

        final MarkerOptions markerOptions = new MarkerOptions()
                .position(location)
                .icon(BitmapDescriptorFactory.fromBitmap(bmpText))
                .anchor(0.5f, 1);

        marker = model.getMap().addMarker(markerOptions);

        return marker;
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
