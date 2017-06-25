package appmax.adisonz.mysiam;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.akexorcist.googledirection.DirectionCallback;
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
    private LocationManager locationManager;    //open service
    private Criteria criteria;                  //กำหนดรายละเอียดของการค้นหา
    private double latADouble = 13.718467, lngADouble = 100.452816;     //หน้าละติจูด หลังลองติจูด
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

    }   //Main Method

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

    private void CheckAndEditLocation() {       //เช็ค user ว่ามีในฐานข้อมูลไหม

        MyConstant myConstant = new MyConstant();
        String tag = "SiamV3";
        boolean b = true;
        String urlPHP = null;

        try {

            //Check
            GetAllData getAllData = new GetAllData(MapsActivity.this);
            getAllData.execute(myConstant.getUrlGetAllLocation());      //ดึงค่าตัวแปรจากอีกคลาสมาใช้
            String strJSON = getAllData.get();
            Log.d(tag, "JSON ==>" + strJSON);

            JSONArray jsonArray = new JSONArray(strJSON);       //สร้างแบบ JSON กึง JSON มาอ่าน
            for (int i=0; i<jsonArray.length(); i+=1) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (userStrings[1].equals(jsonObject.getString("Name"))) {     //ชื่อที่ล็อดอินเข้ามาตรงกับในฐานข้อมูล
                    b = false;
                }else  {
                        myCreateMaker(jsonObject.getString("Name"),
                                new LatLng(Double.parseDouble(jsonObject.getString("Lat")),
                                        Double.parseDouble(jsonObject.getString("Lng"))), mkInts[1]);

                }


            }       //For
            if (b) {
                // NO Name
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

            Log.d(tag, "Result ==>" + addandEditLocation.get());



        } catch (Exception e) {
            Log.d(tag, "e check ==>" + e.toString());
        }

    }


    @Override
    protected void onStop() {           //ดึง method ปิด service
        super.onStop();
        locationManager.removeUpdates(locationListener);    //หยุดการทำงาน
    }

    public Location myFindLocation(String strProvider) {        //ทำงานเสร็จจะโยน location ละ,ลองออกไป
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
            locationManager.requestLocationUpdates(strProvider, 1000, 10, locationListener);      //ต้องขยับเกิน10เมตรพิกัดถึงจะเปลี่ยน
        }
        return location;
    }


    public LocationListener locationListener = new LocationListener() {       //สิ่งที่ทำงานอัตโนมัติ ค้นหาตำแหน่งอัตโมัติทุกการเคลื่อนไหว
        @Override
        public void onLocationChanged(Location location) {      //ค้นหาตำแหน่งอัตโมัติทเมื่อขยับตำแหน่ง
            latADouble = location.getLatitude();
            latADouble = location.getLongitude();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {   //ค้นหาตำแหน่งอัตโมัติทเมื่อ network หายหรือเปลี่ยน

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    private void setUp() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);     //ขออนุญาติการใช้สิทธิ์เปิดเผยตำแหน่งของเครื่อง
        criteria = new Criteria();                      //กำหนดว่าต้องการอะไรบ้าง
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(false);            //สนใจแค่ละ,ลอง ไม่สนใจระดับน้ำทะเล x
        criteria.setBearingRequired(false);             //สนใจแค่ละ,ลอง ไม่สนใจระดับน้ำทะเล z
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

        myCreateMaker(userStrings[1], userLatLng, mkInts[0]);
        CheckAndEditLocation();

        //Click Marker
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Log.d("SiamV4", "Marker Lat ==>" + marker.getPosition().latitude);
                Log.d("SiamV4", "Marker Lng ==>" + marker.getPosition().longitude);
                return true;

            }
        });

    }   //onMapReady

    private void myCreateMaker(String strName, LatLng latLng, int intImage) {       //ชี้ตำแหน่ง สร้างรูป marker
        mMap.addMarker(new MarkerOptions().position(latLng).title(strName)
        .icon(BitmapDescriptorFactory.fromResource(intImage)));
    }

    @Override
    public void onDirectionSuccess(Direction direction, String rawBody) {
        ArrayList<LatLng> arrayList = direction.getRouteList()
                .get(0)
                .getLegList()
                .get(0)
                .getDirectionPoint();
        mMap.addPolyline(DirectionConverter
                .createPolyline(MapsActivity.this, arrayList, 5, Color.RED));
    }

    @Override
    public void onDirectionFailure(Throwable t) {

    }

    //Main Class
}
