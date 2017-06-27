package appmax.adisonz.mysiam;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, DirectionCallback {

    private GoogleMap mMap;
    private LocationManager locationManager;
    private Criteria criteria;
    private double latADouble = 13.718072, lngADouble = 100.453234;
    private LatLng userLatLng;
    private int[] mkInts = new int[]{R.mipmap.mk_user, R.mipmap.mk_friend};
    private String[] userStrings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //SetUp
        setUp();




        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        createFragment();

    }   // Main Method

    @Override
    protected void onResume() {
        super.onResume();

        locationManager.removeUpdates(locationListener);

        //For Network
        Location networkLocation = myFindLocation(LocationManager.NETWORK_PROVIDER);
        if (networkLocation != null) {
            latADouble = networkLocation.getLatitude();
            lngADouble = networkLocation.getLongitude();
        }

        //For GPS
        Location gpsLocation = myFindLocation(LocationManager.GPS_PROVIDER);
        if (gpsLocation != null) {
            latADouble = gpsLocation.getLatitude();
            lngADouble = gpsLocation.getLongitude();
        }

        Log.d("SiamV2", "Lat ==> " + latADouble);
        Log.d("SiamV2", "Lng ==> " + lngADouble);




    }

    private void CheckAndEditLocation() {

        MyConstant myConstant = new MyConstant();
        String tag = "SiamV3";
        boolean b = true;
        String urlPHP = null;

        try {

            //Check
            GetAllData getAllData = new GetAllData(MapsActivity.this);
            getAllData.execute(myConstant.getUrlGetAllLocation());
            String strJSON = getAllData.get();
            Log.d(tag, "JSON ==> " + strJSON);

            JSONArray jsonArray = new JSONArray(strJSON);
            for (int i=0; i<jsonArray.length();i+=1) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (userStrings[1].equals(jsonObject.getString("Name"))) {
                    b = false;
                } else {
                    myCreateMarker(jsonObject.getString("Name"),
                            new LatLng(Double.parseDouble(jsonObject.getString("Lat")),
                                    Double.parseDouble(jsonObject.getString("Lng"))),
                            mkInts[1]);
                }


            }   // for

            if (b) {
                //No Name
                Log.d(tag, "No Name");
                urlPHP = myConstant.getUrlAddLocation();

            } else {
                //Have Name
                Log.d(tag, "Have Name");
                urlPHP = myConstant.getUrlEditLocation();
            }

            AddandEditLocation addandEditLocation = new AddandEditLocation(MapsActivity.this);
            addandEditLocation.execute(userStrings[1],
                    Double.toString(latADouble),
                    Double.toString(lngADouble),
                    urlPHP);

            Log.d(tag, "Result ==> " + addandEditLocation.get());



        } catch (Exception e) {
            Log.d(tag, "e check ==> " + e.toString());
        }

    }



    @Override
    protected void onStop() {
        super.onStop();
        locationManager.removeUpdates(locationListener);
    }

    public Location myFindLocation(String strProvider) {

        Location location = null;

        if (locationManager.isProviderEnabled(strProvider)) {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return null;
            }
            locationManager.requestLocationUpdates(strProvider,
                    1000, 10, locationListener);

        }

        return location;
    }

    public LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            latADouble = location.getLatitude();
            lngADouble = location.getLongitude();
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

    private void setUp() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        userStrings = getIntent().getStringArrayExtra("Login");

    }

    private void createFragment() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //Setup Center Map
        userLatLng = new LatLng(latADouble, lngADouble);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 16));

        myCreateMarker(userStrings[1], userLatLng, mkInts[0]);

        CheckAndEditLocation();

        //Click Marker
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                Log.d("SiamV4", "Marker Lat ==> " + marker.getPosition().latitude);
                Log.d("SiamV4", "Marker Lng ==> " + marker.getPosition().longitude);

                GoogleDirection.withServerKey("AIzaSyAloVYlvZeXa7A86bqofs_0ytQ4Pz-CBaQ")
                        .from(new LatLng(13.718072, 100.453234))
                        .to(marker.getPosition())
                        .transportMode(TransportMode.DRIVING)
                        .execute(MapsActivity.this);


                return true;

            }
        });

    }   // onMapReady

    private void myCreateMarker(String strName, LatLng latLng, int intImage) {
        mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title(strName)
                .icon(BitmapDescriptorFactory.fromResource(intImage)));
    }

    @Override
    public void onDirectionSuccess(Direction direction, String rawBody) {

        if (direction.isOK()) {
            ArrayList<LatLng> arrayList = direction.getRouteList()
                    .get(0)
                    .getLegList()
                    .get(0)
                    .getDirectionPoint();
            mMap.addPolyline(DirectionConverter
                    .createPolyline(MapsActivity.this, arrayList, 5, Color.RED));
        }

    }

    @Override
    public void onDirectionFailure(Throwable t) {

    }
}   // Main Class