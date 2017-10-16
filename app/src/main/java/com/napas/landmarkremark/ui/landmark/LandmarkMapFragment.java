package com.napas.landmarkremark.ui.landmark;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.napas.landmarkremark.R;
import com.napas.landmarkremark.model.Landmark;
import com.napas.landmarkremark.ui.base.BaseFragment;
import com.napas.landmarkremark.util.Constants;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;

public class LandmarkMapFragment extends BaseFragment implements GoogleApiClient.OnConnectionFailedListener {

    @BindView(R.id.progress)
    ProgressBar progress;
    @BindView(R.id.et_search)
    EditText etSearch;
    @BindView(R.id.map_view)
    MapView mapView;
    @BindView(R.id.fab_add)
    FloatingActionButton fabAdd;

    private ILandmarkListener mListener;
    private GoogleApiClient mGoogleApiClient;
    private String mUserName;
    private GoogleMap mGoogleMap;
    private Landmark mCurrentPlace;
    private List<Landmark> landmarks;

    public static LandmarkMapFragment newInstance() {
        LandmarkMapFragment fragment = new LandmarkMapFragment();
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ILandmarkListener) {
            mListener = (ILandmarkListener) context;
        } else {
            throw new ClassCastException(context.toString() + " must implement ILandmarkListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUserName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(getActivity(), this)
                .build();
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_landmark_map, container, false);
        setUnBinder(ButterKnife.bind(this, view));
        mapView.onCreate(savedInstanceState);
        if (mCurrentPlace == null) {
            getCurrentLocation();
        } else {
            initMap();
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
        mGoogleApiClient.stopAutoManage(getActivity());
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(getContext(), "Google Places API connection failed with error code:" +
                connectionResult.getErrorCode(), Toast.LENGTH_LONG).show();
    }

    @OnClick(R.id.fab_add)
    public void onBtnAddClicked() {
        if (mCurrentPlace == null) return;
        mListener.onAddClicked(mCurrentPlace);
    }

    @OnTextChanged(R.id.et_search)
    public void onSearchQueryChanged() {
        if (landmarks == null) return;

        String query = etSearch.getText().toString().trim().toLowerCase();
        for (Landmark landmark : landmarks) {
            if (landmark.getName().toLowerCase().contains(query) ||
                    landmark.getNote().toLowerCase().contains(query) ||
                    landmark.getCreatedBy().toLowerCase().contains(query)) {
                moveMap(landmark.getLatitude(), landmark.getLongitude());
                return;
            }
        }
        moveMap(mCurrentPlace.getLatitude(), mCurrentPlace.getLongitude());
    }

    private void getCurrentLocation() {
        if (mGoogleApiClient == null || ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return;

        showProgress(true);
        PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi.getCurrentPlace(mGoogleApiClient, null);
        result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
            @Override
            public void onResult(PlaceLikelihoodBuffer likelyPlaces) {
                if (likelyPlaces.getCount() > 0) {
                    Place place = likelyPlaces.get(0).getPlace();
                    LatLng latLng = place.getLatLng();
                    mCurrentPlace = new Landmark(place.getName().toString(), place.getAddress().toString(), latLng.latitude, latLng.longitude, mUserName, null);
                    initMap();
                }
                likelyPlaces.release();
                showProgress(false);
            }
        });
    }

    private void initMap() {
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                try {
                    MapsInitializer.initialize(getActivity().getApplicationContext());
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }

                mGoogleMap = mMap;
                mGoogleMap.getUiSettings().setMapToolbarEnabled(false);
                showLandmarks();

                // show current location
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                    return;
                mGoogleMap.setMyLocationEnabled(true);

                if (mCurrentPlace == null)
                    return;
                moveMap(mCurrentPlace.getLatitude(), mCurrentPlace.getLongitude());
            }
        });
    }

    private void moveMap(double latitude, double longitude) {
        if (mGoogleMap == null) return;
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15));
    }

    private void showLandmarks() {
        if (mGoogleMap == null) return;

        if (landmarks == null) {  // have not retrieved data
            getLandmarksFromDb();
        } else {    // there are existing data
            for (Landmark landmark : landmarks) {
                mGoogleMap.addMarker(createMarker(landmark));
            }
        }
    }

    private void getLandmarksFromDb() {
        // get reference to 'landmarks' node
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference(Constants.KEY_LANDMARKS);
        landmarks = new ArrayList<>();
        // listen to value changes
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mGoogleMap.clear();
                landmarks.clear();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Landmark landmark = postSnapshot.getValue(Landmark.class);
                    landmarks.add(landmark);
                    // add a marker to the map
                    mGoogleMap.addMarker(createMarker(landmark));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(), databaseError.toException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private MarkerOptions createMarker(Landmark landmark) {
        return new MarkerOptions()
                .position(new LatLng(landmark.getLatitude(), landmark.getLongitude()))
                .title(landmark.getName())
                .snippet(landmark.getCreatedBy() + " says \"" + landmark.getNote() + "\"");
    }

    private void showProgress(boolean show) {
        progress.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}
