package com.example.josemartins.sdis_weeat.gui;


import android.Manifest;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;


import com.example.josemartins.sdis_weeat.R;
import com.example.josemartins.sdis_weeat.logic.Utils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


import org.json.JSONObject;


import java.io.IOException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import network.messaging.Message;
import network.messaging.distributor.server.ServerDistributor;

import static java.lang.System.currentTimeMillis;


public class MapFragment extends Fragment implements GoogleMap.OnMapLongClickListener, OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback, GoogleMap.OnMarkerClickListener {

    private View rootView;
    private GoogleMap myMap;
    private MapView mMapView;
    private final Map<LatLng, Marker> mapMarkers = new ConcurrentHashMap<>();
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //reconstruct markers
        try {
            Utils.client.makeRequest(Utils.serverUrl,"POST",new Message(ServerDistributor.GET_CHAT_GROUPS, new String()));
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        myMap = googleMap;
        myMap.setOnMapLongClickListener(this);

        myMap.setOnInfoWindowClickListener(marker -> {
           goToChat(marker.getPosition());

        });


        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            myMap.setMyLocationEnabled(true);
            goToLocation();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        myMap.setMyLocationEnabled(true);
                        goToLocation();
                    }
                }
            }
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.map_fragment, container, false);
        MapsInitializer.initialize(this.getActivity());
        mMapView = (MapView) rootView.findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(this);

        return rootView;
    }


    private void addMarker(LatLng latLng, String title, String snippet) {
        Marker marker = myMap.addMarker(new MarkerOptions().position(latLng).title(title).snippet(snippet));
        mapMarkers.put(latLng, marker);
    }

    @Override
    public void onMapLongClick(LatLng latLng) {

        AlertDialog.Builder createGroup = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();

        View v = inflater.inflate(R.layout.create_group, null);

        EditText group_name = (EditText) v.findViewById(R.id.group_name);
        EditText group_date = (EditText) v.findViewById(R.id.group_date);

        group_date.setInputType(InputType.TYPE_NULL);
        group_date.setOnClickListener(v1 -> openTimePicker(group_date));

        createGroup.setView(v);

        createGroup.setTitle("Marcar encontro");

        createGroup.setNegativeButton("Cancelar", (dialog, id) -> dialog.cancel());

        createGroup.setPositiveButton("Criar", (dialog, id) -> {

            if (!group_name.getText().toString().trim().equals("")) {
                String groupNameValue = group_name.getText().toString();
                String groupDateValue = group_date.getText().toString();

                addMarker(latLng, groupNameValue,groupDateValue);

                //Add chat room to the database
                try {
                    JSONObject jsonChatRoom = new JSONObject();
                    jsonChatRoom.put("lat",latLng.latitude);
                    jsonChatRoom.put("long",latLng.longitude);
                    jsonChatRoom.put("timestamp",456456465);//Todo -> Hard - Coded
                    jsonChatRoom.put("title",groupNameValue);

                    Utils.client.makeRequest(Utils.serverUrl,"POST",new Message(ServerDistributor.ADD_CHAT_GROUP, jsonChatRoom.toString()));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                AlertDialog.Builder toChat = new AlertDialog.Builder(getActivity());

                toChat.setMessage("Deseja ir para o chat?");
                toChat.setNegativeButton("Não", (dialog1, which) -> dialog1.cancel());
                toChat.setPositiveButton("Sim", (dialog1, which) -> goToChat(latLng));

                AlertDialog chatView = toChat.create();
                chatView.show();
            }
        });

        AlertDialog alertDialog = createGroup.create();
        alertDialog.show();

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.d("debug", "lat " + marker.getPosition().latitude + " long " + marker.getPosition().longitude);

        goToChat(marker.getPosition());
        return true;
    }

    public void goToChat(LatLng pos) {
        Log.d("debug", "lat " + pos.latitude + " long " + pos.longitude);

        Intent i = new Intent(getActivity(), ChatActivity.class);
        i.putExtra("lat",pos.latitude);
        i.putExtra("long",pos.longitude);
        startActivity(i);
    }

    public void openTimePicker(EditText group_date) {
        final Calendar c = Calendar.getInstance();
        int mHour = c.get(Calendar.HOUR_OF_DAY);
        int mMinute = c.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(),
                (view, hourOfDay, minute) -> {
                    if (minute < 10 && minute > 0)
                        group_date.setText(hourOfDay + ":0" + minute);
                    else
                        group_date.setText(hourOfDay + ":" + minute);
                }, mHour, mMinute, false);

        timePickerDialog.show();
    }

    public void changeMapType(int type) {
        myMap.setMapType(type);
    }

    public void goToLocation() {

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

            Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();


            LatLng currPoint = new LatLng(latitude, longitude);
            CameraPosition cameraPosition = new CameraPosition.Builder().target(currPoint).zoom(12).build();
            myMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        }
    }
}