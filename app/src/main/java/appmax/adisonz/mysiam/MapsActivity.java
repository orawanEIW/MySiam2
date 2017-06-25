package appmax.adisonz.mysiam;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

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

    }   //onMapReady

    private void myCreateMaker(String strName, LatLng latLng, int intImage) {       //ชี้ตำแหน่ง สร้างรูป marker
        mMap.addMarker(new MarkerOptions().position(latLng).title(strName)
        .icon(BitmapDescriptorFactory.fromResource(intImage)));
    }

        //Main Class
}
