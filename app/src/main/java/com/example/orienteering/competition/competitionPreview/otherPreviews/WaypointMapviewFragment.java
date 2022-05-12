package com.example.orienteering.competition.competitionPreview.otherPreviews;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.orienteering.R;
import com.example.orienteering.competition.pickCompetition.PickCompetitionViewmodel;
import com.example.orienteering.databinding.FragmentWaypointMapviewBinding;
import com.example.orienteering.nfts.nftToCompetition.CustomWaypointDesc;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


public class WaypointMapviewFragment extends Fragment implements OnMapReadyCallback, View.OnClickListener {

    public static final String SERIALIZED_WAYPOINTS = "serializedWaypoints";
    private GoogleMap mapInstance;
    private SupportMapFragment mapFragment;
    private List<CustomWaypointDesc> waypoints;

    private FragmentWaypointMapviewBinding wayMapBinding;

    public WaypointMapviewFragment() {
        // Required empty public constructor
    }


    public static WaypointMapviewFragment newInstance(String param1, String param2) {
        WaypointMapviewFragment fragment = new WaypointMapviewFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        wayMapBinding = DataBindingUtil.inflate(getLayoutInflater(),
                R.layout.fragment_waypoint_mapview, container, false);

        wayMapBinding.wayPreviewBackBtn.setOnClickListener(this);

        if ((getArguments() != null)
                    && (getArguments().containsKey(SERIALIZED_WAYPOINTS))){
            waypoints = deserializeWaypoints(getArguments().getString(SERIALIZED_WAYPOINTS));
        }

        initMap();

        return wayMapBinding.getRoot();
    }


    public void initMap() {

        if (mapFragment == null) {

            mapFragment = SupportMapFragment.newInstance();

            try {
                mapFragment.getMapAsync(this);
            } catch (NullPointerException ex) {

                Toast.makeText(getContext(), getString(R.string.gps_map_loading_err), Toast.LENGTH_SHORT).show();
                Log.e("getMap async", getString(R.string.gps_map_loading_err));
            }
        }
        getChildFragmentManager().beginTransaction().replace(R.id.way_preview_google_map, mapFragment).commit();
    }


    private List<CustomWaypointDesc> deserializeWaypoints(String serializedWaypoints){

        Type myType = new TypeToken<ArrayList<CustomWaypointDesc>>(){}.getType();

        return new Gson().fromJson(serializedWaypoints,myType);
    }


    private void showWaypoints(List<CustomWaypointDesc> waypoints){

            if (getContext()==null) return;

            int color = 0;
            // show markers on map
            for (CustomWaypointDesc waypoint : waypoints){

                if (waypoint.getSeqNumber() == waypoints.size()){

                    color = getContext().getColor(R.color.picked_green);
                }
                else if(waypoint.getSeqNumber() == 1){

                    color = getContext().getColor(R.color.black);
                }
                else {
                    color = getContext().getColor(R.color.dark_purple);
                }

                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(new LatLng(waypoint.getLat(), waypoint.getLng()));
                markerOptions.title(waypoint.getThoroughfare())
                        .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(color,String.valueOf(waypoint.getSeqNumber()))));

                mapInstance.addMarker(markerOptions);
            }
    }

    //tvorba custom markera
    // kod ispirovany podla zdroja: https://stackoverflow.com/a/35800880
    private Bitmap getMarkerBitmapFromView(int color, String number) {      //@DrawableRes int resId

        View customMarkerView = getLayoutInflater().inflate(R.layout.custom_marker_view, null);

        TextView customText = customMarkerView.findViewById(R.id.marker_number);

        customText.getBackground().setTint(color);
        customText.setText(number);
        //((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE))

        //  ImageView markerImageView = (ImageView) customMarkerView.findViewById(R.id.profile_image);
        //  markerImageView.setImageResource(resId);
        customMarkerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        customMarkerView.layout(0, 0, customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight());
        customMarkerView.buildDrawingCache();
        Bitmap returnedBitmap = Bitmap.createBitmap(customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);
        Drawable drawable = customMarkerView.getBackground();
        if (drawable != null)
            drawable.draw(canvas);
        customMarkerView.draw(canvas);
        return returnedBitmap;
    }



    private void centerMap(CustomWaypointDesc middle_Waypoint){

            //vhodne centrovanie mapy vzhladom na trat
        mapInstance.animateCamera(CameraUpdateFactory
                .newLatLngZoom(new LatLng(middle_Waypoint.getLat(), middle_Waypoint.getLng()), 14f));
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        mapInstance = googleMap;

        if ((waypoints!=null) && (!waypoints.isEmpty())){

            int midIndex = waypoints.size()/2;
            centerMap(waypoints.get(midIndex));
            showWaypoints(waypoints);
        }
    }

    @Override
    public void onClick(View v) {

        if (v.equals(wayMapBinding.wayPreviewBackBtn)){

            if (getActivity() != null){
                getActivity().onBackPressed();
            }
        }
    }
}