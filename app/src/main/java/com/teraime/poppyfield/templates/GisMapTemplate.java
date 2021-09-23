package com.teraime.poppyfield.templates;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
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
import com.google.android.material.snackbar.Snackbar;
import com.teraime.poppyfield.MainActivity;
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

public class GisMapTemplate extends TemplateFragment implements OnMapReadyCallback {


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
        List<Block> layers = model.getSelectedWorkFlow().getBlocksOfType(Block.GIS_LAYER);
        List<Block> gisObjects = model.getSelectedWorkFlow().getBlocksOfType(Block.GIS_POINTS);
        Log.d("v","GETBLOCKSOFTYPE "+model.getSelectedWorkFlow().getBlocksOfType(Block.GIS_LAYER));

        for(Block l: layers) {
        //LayerDescriptor<String,> ld = new

            Log.d("v","Layer"+l.getAttrs().toString());

        }
        if (ActivityCompat.checkSelfPermission(this.getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this.getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("google","FAIL ON PERMISSIONS");
            ActivityResultLauncher<String> requestPermission = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted)
                    Log.d("v", "GRANTED");
                else
                    Log.d("v", "REFUSED");
            });
            if (ActivityCompat.shouldShowRequestPermissionRationale(this.getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Provide an additional rationale to the user if the permission was not granted
                // and the user would benefit from additional context for the use of the permission.
                // For example if the user has previously denied the permission.
                Log.d("SEC",
                        "check");
                Snackbar.make(getActivity().findViewById(R.id.drawerLayout), "To be able to show your position on the map, FieldApp needs your permission to access your fine grained location",
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.ok, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                requestPermission.launch(android.Manifest.permission.ACCESS_FINE_LOCATION);
                            }
                        })
                        .show();
            } else
                requestPermission.launch(android.Manifest.permission.ACCESS_FINE_LOCATION);
                //ActivityCompat.requestPermissions(GisMapTemplate.this.getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            //new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            Log.d("SEC",
                    "no check");
            googleMap.setMyLocationEnabled(true);
        }
        for(Block gisObject: gisObjects) {

            Log.d("v",gisObject.getBlockType());
            Log.d("v", gisObject.getAttrs().toString());

        }

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
