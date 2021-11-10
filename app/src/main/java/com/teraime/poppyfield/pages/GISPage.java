package com.teraime.poppyfield.pages;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
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
import com.teraime.poppyfield.base.Context;
import com.teraime.poppyfield.base.Expressor;
import com.teraime.poppyfield.base.Tools;
import com.teraime.poppyfield.base.Workflow;
import com.teraime.poppyfield.gis.Geomatte;
import com.teraime.poppyfield.gis.PhotoMeta;
import com.teraime.poppyfield.templates.TemplateFragment;
import com.teraime.poppyfield.viewmodel.WorldViewModel;

import org.json.JSONObject;

import java.util.List;
import java.util.Map;

public class GISPage extends Page {

    BitmapDescriptor mImgOverlay;
    LatLngBounds mBoundaries;
    GroundOverlay mGroundOverlay;


    public GISPage(WorldViewModel model, String template, Workflow wf, String name) {
        super(model, template, wf,name);
    }
    @Override
    public void onCreate(TemplateFragment f) {
        super.onCreate(f);
    }

    public void onMapReady() {
        model.getMap().setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        reload();
    }

    private void spawnLayers() {
        //List<Block> layers = workFlow.getBlocksOfType(Block.GIS_LAYER);
        List<Block> gisBlocks = workFlow.getBlocksOfType(Block.GIS_POINTS);
        for (Block gisBlock : gisBlocks) {
            model.generateLayer(gisBlock,mWorkFlowContext);
        }
    }

    public void drawLayer(Block gisBlock, JSONObject geoJsonData) {
        Log.d("v", "GETBLOCKSOFTYPE " + workFlow.getBlocksOfType(Block.GIS_LAYER));
        GoogleMap googleMap = model.getMap();
        GeoJsonLayer gl = new GeoJsonLayer(googleMap, geoJsonData);
        fillPolygons(gl,gisBlock);
        gl.addLayerToMap();
        Log.d("drawLayer","Adding layer "+gisBlock.getAttr("label"));
        gl.setOnFeatureClickListener((GeoJsonLayer.GeoJsonOnFeatureClickListener) feature -> {

            Log.d("GISPage","got click on feature");
            //feature contains all keys under properties.
            //required for lookup.
            String featureCols = feature.getProperty("COLUMNS");
            String featureVars = feature.getProperty("VARIABLES");
            Log.d("drawLayer", feature.getProperties().toString());
            //create a new content for this object and request a page change.
            model.getPageStack().changePage(gisBlock.getAttr("on_click"),new Context(null,Tools.jsonObjectToMap(featureVars),Tools.jsonObjectToMap(featureCols)));
        });

    }


    private void fillPolygons(GeoJsonLayer layer, Block gisBlock) {
        Map<String, String> gisBlockAttrs = gisBlock.getAttrs();
        int color = Color.parseColor(gisBlockAttrs.get("color"));
        // Iterate over all the features stored in the layer
        for (GeoJsonFeature feature : layer.getFeatures()) {
            Map<String, String> featureVars = Tools.jsonObjectToMap(feature.getProperty("VARIABLES"));
            Map<String, String> featureCols = Tools.jsonObjectToMap(feature.getProperty("COLUMNS"));
            assert featureVars!=null;
            assert featureCols!=null;
            if (feature.getGeometry() instanceof GeoJsonPolygon) {
                GeoJsonPolygonStyle ps = new GeoJsonPolygonStyle();
                ps.setFillColor(color);
                LatLng x = ((GeoJsonPolygon) feature.getGeometry()).getCoordinates().get(0).get(0);
                addText(x, featureVars.get("shape_area"), 1, 12);
                feature.setPolygonStyle(ps);
            } else if (feature.getGeometry() instanceof GeoJsonPoint) {
                //Log.d("vagel","PROPS: "+props.toString());
                String label = Expressor.analyze(gisBlock.getLabelExpr(),new Context(null,featureVars,featureCols));
                if (label!=null && label.startsWith("@")) {
                    String key = label.substring(1);
                    if (key.length()>0)
                        label = featureCols.get(key);
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
                addText(x, feature.getProperty("shape_area"), 1, 12);
                feature.setLineStringStyle(gl);
            }
        }
    }
    public Marker addText(final LatLng location, final String text, final int padding,
                          final int fontSize) {
        Marker marker = null;

        if (model.getActivity() == null || model.getMap() == null || location == null || text == null
                || fontSize <= 0) {
            return null;
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
        model.setLoadState("LOADING");
        if (mBoundaries == null)
            determineBoundary();
        else
            model.setBoundaryFromCoordinates(mBoundaries);
        spawnLayers();
    };




    private void determineBoundary() {
        Block gis = workFlow.getBlock(Block.GIS);
        String N =    gis.getAttr("N");
        String E =     gis.getAttr("E");
        String S = gis.getAttr("S");
        String W =  gis.getAttr("W");
        PhotoMeta p = new PhotoMeta(N,E, S,W );
        if (p.isValid()) {
            Log.d("GISPage", p.toString());
            LatLng NE = Geomatte.convertToLatLong(p.E, p.N);
            LatLng SW = Geomatte.convertToLatLong(p.W, p.S);
            LatLngBounds latLngBounds = new LatLngBounds(SW, NE);
            model.setBoundaryFromCoordinates(latLngBounds);
        } else {
            String source = gis.getAttr("source");
            if (source != null) {
                source = source.split(",")[0];
                source = Expressor.analyze(Expressor.preCompileExpression(source), mWorkFlowContext);
                Log.d("v3", "In reload - source: " + source);
                model.setBoundaryFromImage(source);
            }
        }
    }

    public BitmapDescriptor getImgOverlay() {
        if (mImgOverlay == null)
            mImgOverlay = model.consumeImgOverlay();
        return mImgOverlay;
    }

    public void setBoundary(LatLngBounds latLngBounds) {
        mBoundaries = latLngBounds;
    }

    public void setGroundOverlay(GroundOverlay groundOverlay) {
        Log.d("CLEAR","setting Groundoverlay for "+mName);
        mGroundOverlay = groundOverlay;
    }

    public void clear() {
        Log.d("CLEAR","CALLING CLEAR ON PREV GISPAGE");
        model.getMap().clear();
        Log.d("CLEAR","Checking Groundoverlay for "+mName);
        if (mGroundOverlay != null) {
            Log.d("CLEAR","Removing ground overlay");
            mGroundOverlay.remove();
        }
    }


}
