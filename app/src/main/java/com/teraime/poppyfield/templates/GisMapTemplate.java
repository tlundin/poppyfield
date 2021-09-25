package com.teraime.poppyfield.templates;

import static com.teraime.poppyfield.gis.Geomatte.convert;

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
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.material.snackbar.Snackbar;
import com.google.maps.android.data.geojson.GeoJsonLayer;
import com.teraime.poppyfield.R;
import com.teraime.poppyfield.base.Block;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.List;

public class GisMapTemplate extends TemplateFragment implements OnMapReadyCallback {

    private GISPage mPage;
    private MapView mMap;
    private ActivityResultLauncher<String> requestPermission;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @SuppressLint("MissingPermission")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d("GIS","Oncreate for Map");
        View v = super.onCreateView(inflater,container,savedInstanceState,R.layout.template_gis_map);
        mPage = (GISPage)model.getPageStack().getInfocusPage();
        assert v!=null;
        requestPermission = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> { if (isGranted) { if (model.getMap()!=null) model.getMap().setMyLocationEnabled(true);}});

        LiveData<LatLngBounds> camBoundsL = model.getMapBoundary();
        Observer<? super LatLngBounds> boundsObserver = (Observer<LatLngBounds>) latLngBounds -> {
            model.getMap().moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 0));

        };
        camBoundsL.observe(getViewLifecycleOwner(),boundsObserver);

        mMap = v.findViewById(R.id.myMap);
        mMap.onCreate(savedInstanceState);
        mMap.getMapAsync(this);


        mPage.onCreate(this);
        return v;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        model.setMap(googleMap);
        mPage.onMapReady();
        showUserIfAllowed(googleMap);
    }







    private void showUserIfAllowed(GoogleMap googleMap) {

        if (this.getActivity()!=null) {
            if (ActivityCompat.checkSelfPermission(this.getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this.getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this.getActivity(),
                        Manifest.permission.ACCESS_FINE_LOCATION)) {
                    // Provide an additional rationale to the user if the permission was not granted
                    // and the user would benefit from additional context for the use of the permission.
                    // For example if the user has previously denied the permission.
                    Snackbar.make(getActivity().findViewById(R.id.drawerLayout), "To be able to show your position on the map, FieldApp needs your permission to access your fine grained location",
                            Snackbar.LENGTH_INDEFINITE)
                            .setAction(R.string.ok, view -> requestPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION))
                            .show();
                } else
                    requestPermission.launch(android.Manifest.permission.ACCESS_FINE_LOCATION);
                //ActivityCompat.requestPermissions(GisMapTemplate.this.getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                //new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                googleMap.setMyLocationEnabled(true);
            }
        }

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
    public void onResume() {
        mMap.onResume();
        super.onResume();
    }

    @Override
    public void onPause() {
        mMap.onPause();
        super.onPause();
    }


}
