package com.teraime.poppyfield.templates;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.material.snackbar.Snackbar;
import com.google.maps.android.data.geojson.GeoJsonLayer;
import com.teraime.poppyfield.R;
import com.teraime.poppyfield.base.Block;
import com.teraime.poppyfield.base.Tools;
import com.teraime.poppyfield.gis.Geomatte;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.List;

public class GisMapTemplate extends TemplateFragment implements OnMapReadyCallback {


    private MapView mMap;
    private String mSource,mName;
    private GoogleMap gMap;
    private ActivityResultLauncher<String> requestPermission;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @SuppressLint("MissingPermission")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d("v","in oncreate for gismap");
        requestPermission = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                if (model.getMap()!=null)
                    model.getMap().setMyLocationEnabled(true);
                Log.d("v", "GRANTED");
            }
            else
                Log.d("v", "REFUSED");
        });
        View v = super.onCreateView(inflater,container,savedInstanceState,R.layout.template_gis_map);
        mMap = v.findViewById(R.id.myMap);
        mMap.onCreate(savedInstanceState);
        mMap.getMapAsync(this);

        return v;
    }

    @Override
    public void onResume() {
        mMap.onResume();
        super.onResume();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        model.setMap(googleMap);
        Block gis = model.getSelectedWorkFlow().getBlock(Block.GIS);
        mSource = gis.getAttr("source");
        LiveData<LatLngBounds> camBoundsL = model.getMapBoundary(this);
        Observer<? super LatLngBounds> boundsObserver = new Observer<LatLngBounds>() {
            @Override
            public void onChanged(LatLngBounds latLngBounds) {
                googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds,0));
                camBoundsL.removeObservers(GisMapTemplate.this.getViewLifecycleOwner());
                continueAfterFocus(googleMap);
            }
        };
        camBoundsL.observe(this,boundsObserver);

        if (ActivityCompat.checkSelfPermission(this.getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this.getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("google","FAIL ON PERMISSIONS");

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
            //googleMap.setMyLocationEnabled(true);
        }


    }

private void continueAfterFocus(GoogleMap googleMap) {
    List<Block> layers = model.getSelectedWorkFlow().getBlocksOfType(Block.GIS_LAYER);
    List<Block> gisObjects = model.getSelectedWorkFlow().getBlocksOfType(Block.GIS_POINTS);
    Log.d("v","GETBLOCKSOFTYPE "+model.getSelectedWorkFlow().getBlocksOfType(Block.GIS_LAYER));

    for (Block g: gisObjects) {
        Log.d("v","Layer"+g.getAttrs().toString());
    }
    for(Block gisObject: gisObjects) {

        Log.d("v",gisObject.getBlockType());
        Log.d("v", gisObject.getAttrs().toString());

    }
    String s = null;

    try {
        JSONObject geoJsonData = new JSONObject(convert("Traktersb"));

        GeoJsonLayer gl = new GeoJsonLayer(googleMap,geoJsonData);
        gl.addLayerToMap();

    } catch (JSONException | FileNotFoundException e) {
        e.printStackTrace();
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

    private String convert(String type) throws FileNotFoundException {
        StringBuilder result = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(Paths.get(this.getActivity().getFilesDir().getPath(), "cache",type).toFile())));
        boolean geoLines = false;
        String xE = null, Ny = null;
        try {
        String line;
        while ((line = reader.readLine()) != null) {
            Log.d("vroom",line);
            if (line.contains("properties"))
                geoLines = false;
            if (geoLines) {
                if (isCoord(line)) {
                    if (xE == null) {
                        if (line.trim().equals("0.0"))
                            continue;
                        xE = line;
                    } else if (Ny == null) {
                        Ny = line;
                        double n, e;
                        n = Double.parseDouble(Ny.trim().replace(",", ""));
                        e = Double.parseDouble(xE.trim().replace(",", ""));
                        xE = null;
                        Ny = null;
                        LatLng ll = Geomatte.convertToLatLong(e, n);
                        String lng = "           " + ll.longitude + ",";
                        String lat = "           " + ll.latitude;
                        result.append(lng);
                        result.append(lat);
                    }
                } else
                    result.append(line);

            } else {
                if (line.contains("coordinates"))
                    geoLines = true;
                result.append(line);
            }
        }
        reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result.toString();
    }
    private boolean isCoord(String line) {
        String p = line.trim();
        if (p.length()==0)
            return false;
        boolean isLine = Character.isDigit(p.toCharArray()[0]);
        //Log.d("google",line+" is a coord: "+isLine);
        return Character.isDigit(p.toCharArray()[0]);
    }








    public String getMetaSource() {
        return mSource;
    }

    @Override
    public String getName() {
        return this.getTag();
    }

    @Override
    public void onDestroy() {
        mMap.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onStop() {
        mMap.onStop();
        super.onStop();
    }

    @Override
    public void onStart() {
        mMap.onStart();
        super.onStart();
    }

    @Override
    public void onPause() {
        mMap.onPause();
        super.onPause();
    }

}
