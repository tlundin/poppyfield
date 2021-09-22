package com.teraime.poppyfield.templates;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

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
    private String mSource,mName;
    private GoogleMap gMap;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d("v","in oncreate for gismap");
        View v = super.onCreateView(inflater,container,savedInstanceState,R.layout.template_gis_map);
        if (model.getMap() == null) {
            mMap = v.findViewById(R.id.myMap);
            mMap.onCreate(savedInstanceState);
            mMap.getMapAsync(this);
        } else {

        }

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onMapReady(com.google.android.libraries.maps.GoogleMap googleMap) {
        model.setMap(googleMap);
        Block gis = model.getSelectedWorkFlow().getBlock(Block.GIS);
        mSource = gis.getAttr("source");
        //googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        LiveData<LatLngBounds> camBoundsL = model.getMapBoundary(this);
        Observer<? super LatLngBounds> boundsObserver = new Observer<LatLngBounds>() {
            @Override
            public void onChanged(LatLngBounds latLngBounds) {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds,0));
            }
        };
        camBoundsL.observe(this,boundsObserver);
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

    public String getMetaSource() {
        return mSource;
    }

    @Override
    public String getName() {
        return this.getTag();
    }
}
