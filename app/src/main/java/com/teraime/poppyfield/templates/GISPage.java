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

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.data.geojson.GeoJsonFeature;
import com.google.maps.android.data.geojson.GeoJsonLayer;
import com.google.maps.android.data.geojson.GeoJsonLineString;
import com.google.maps.android.data.geojson.GeoJsonLineStringStyle;
import com.google.maps.android.data.geojson.GeoJsonPoint;
import com.google.maps.android.data.geojson.GeoJsonPointStyle;
import com.google.maps.android.data.geojson.GeoJsonPolygon;
import com.google.maps.android.data.geojson.GeoJsonPolygonStyle;
import com.google.maps.android.ui.IconGenerator;
import com.teraime.poppyfield.base.Block;
import com.teraime.poppyfield.base.Expressor;
import com.teraime.poppyfield.base.Workflow;
import com.teraime.poppyfield.gis.GisObject;
import com.teraime.poppyfield.viewmodel.WorldViewModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
        List<Block> gisBlocks = workFlow.getBlocksOfType(Block.GIS_POINTS);
        Log.d("v", "GETBLOCKSOFTYPE " + workFlow.getBlocksOfType(Block.GIS_LAYER));
        Expressor expr = Expressor.gl();

        for (Block l : layers) {
            Log.d("v", "Layer" + l.getAttrs().toString());
        }
        for (Block gisBlock : gisBlocks) {
            Log.d("vagel", gisBlock.getBlockType());
            Log.d("vagel", gisBlock.getAttrs().toString());
            boolean createAllowed = gisBlock.getAttr("create_allowed").equals("true");
            if (!createAllowed) {
                Log.d("vagel","Create not allowed");
                String obj_context = gisBlock.getAttr("obj_context");
                Map<String, String> context = expr.evaluate(expr.preCompileExpression(obj_context));
                String gisType = context.get("gistyp");
                if (gisType != null) {
                    try {
                        Log.d("vagel","Gis not null "+gisType);
                        File source = Paths.get(model.getCacheFolder(), "cache", gisType).toFile();
                        JSONObject geoJsonData = new JSONObject(convert(source));
                        GeoJsonLayer gl = new GeoJsonLayer(googleMap, geoJsonData);
                        fillPolygons(gl,gisBlock,gisType);
                        gl.addLayerToMap();
                        gl.setOnFeatureClickListener((GeoJsonLayer.GeoJsonOnFeatureClickListener) feature -> {
                            Log.d("vagel", feature.getProperties().toString());
                            Map<String, String> props = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
                            for (String k : feature.getPropertyKeys())
                                props.put(k, feature.getProperty(k));
                            model.setCurrentSelectionContext(props);
                            model.getPageStack().changePage(gisBlock.getAttr("on_click"));
                        });
                    } catch (JSONException | IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void fillPolygons(GeoJsonLayer layer, Block gisblock, String gisType) {
        Map<String, String> gisBlockAttrs = gisblock.getAttrs();
        Map<String, String> props = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        int color = Color.parseColor(gisBlockAttrs.get("color"));
        // Iterate over all the features stored in the layer
        for (GeoJsonFeature feature : layer.getFeatures()) {
            props.clear();
            for (String k : feature.getPropertyKeys())
                props.put(k, feature.getProperty(k));
            model.setCurrentSelectionContext(props);
            // Check if the magnitude property exists
            if (feature.getGeometry() instanceof GeoJsonPolygon) {
                        GeoJsonPolygonStyle ps = new GeoJsonPolygonStyle();
                        ps.setFillColor(color);
                        LatLng x = ((GeoJsonPolygon) feature.getGeometry()).getCoordinates().get(0).get(0);
                        addText(x, feature.getProperty("Shape_Area"), 1, 12);
                        feature.setPolygonStyle(ps);


            } else if (feature.getGeometry() instanceof GeoJsonPoint) {

                String label = gisblock.getLabel();
                if (label!=null && label.startsWith("@")) {
                    String key = label.substring(1, label.length());
                    if (key.length()>0)
                        label = props.get(key);
                }
                IconGenerator icg = new IconGenerator(model.getActivity());
                icg.setStyle(IconGenerator.STYLE_WHITE);
                Bitmap bmp = icg.makeIcon(label);
                GeoJsonPointStyle gp = new GeoJsonPointStyle();
                gp.setIcon(BitmapDescriptorFactory.fromBitmap(bmp));
                feature.setPointStyle(gp);
            } else if (feature.getGeometry() instanceof GeoJsonLineString) {
                GeoJsonLineStringStyle gl = new GeoJsonLineStringStyle();
                gl.setColor(color);
                LatLng x = ((GeoJsonLineString) feature.getGeometry()).getCoordinates().get(0);
                addText(x, feature.getProperty("Shape_Area"), 1, 12);
                feature.setLineStringStyle(gl);
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
        source = source.split(",")[0];
        source = Expressor.gl().analyze(Expressor.gl().preCompileExpression(source));
        Log.d("v3","In reload - source: "+source);
        model.updateBoundary(source);
        drawLayers(model.getMap());
    };
}
