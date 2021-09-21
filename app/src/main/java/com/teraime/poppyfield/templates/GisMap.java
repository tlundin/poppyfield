package com.teraime.poppyfield.templates;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.internal.Preconditions;
import com.google.android.libraries.maps.CameraUpdateFactory;
import com.google.android.libraries.maps.GoogleMap;
import com.google.android.libraries.maps.MapView;

import com.google.android.libraries.maps.OnMapReadyCallback;
import com.google.android.libraries.maps.model.LatLng;
import com.google.android.libraries.maps.model.LatLngBounds;
import com.teraime.poppyfield.R;
import com.teraime.poppyfield.base.Block;
import com.teraime.poppyfield.base.Constants;
import com.teraime.poppyfield.gis.Geomatte;
import com.teraime.poppyfield.gis.PhotoMeta;
import com.teraime.poppyfield.loader.Loader;
import com.teraime.poppyfield.loader.LoaderCb;
import com.teraime.poppyfield.loader.WebLoader;
import com.teraime.poppyfield.loader.parsers.JGWParser;

import java.text.ParseException;
import java.util.List;

public class GisMap extends TemplateFragment implements OnMapReadyCallback {


    private MapView mMap;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        super.onCreateView(inflater,container,savedInstanceState,R.layout.template_gis_map);
        mMap = (MapView) mView.findViewById(R.id.myMap);
        mMap.onCreate(savedInstanceState);
        mMap.getMapAsync(this);
        return mView;
    }

    @Override
    public void onMapReady(com.google.android.libraries.maps.GoogleMap googleMap) {
        for (Block b:model.getSelectedWorkFlow().getBlocks()) {
            Log.d("v", b.getBlockType() + " attr");
            Log.d("v", b.getAttrs().toString());
        }
        Block gis = model.getSelectedWorkFlow().getBlock(Block.GIS);
        String pic = gis.getAttr("source");

        WebLoader.getMapMetaData(new LoaderCb() {
            @Override
            public void loaded(List<String> file) {
                for (String r:file) {
                    Log.d("GIS",r);
                }
                PhotoMeta p=null;
                try {
                    p = JGWParser.parse(file,919,993);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                LatLng NE = Geomatte.convertToLatLong(p.E,p.N);
                LatLng SW = Geomatte.convertToLatLong(p.W,p.S);
                LatLngBounds latLngBounds = new LatLngBounds(SW,NE);

                googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds,0));
                //googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
            }
        }, model.getApp(), pic);

    }

    private int getGoogleMapType(String mapType) {
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
