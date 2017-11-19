package com.example.tongyu.googleapi;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private final String LOCAL_FILE_NAME = "myfilename1";
    private List<Marker> markers = new ArrayList<>();
    private static final String LOGTAG = "MapsActivity";
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private String mCurrentPhotoPath;
    static final int REQUEST_TAKE_PHOTO = 1;
    private static final String CAMERA_FP_AUTHORITY = "com.example.tongyu.googleapi.fileprovider";
    LocationManager lm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // check permission
        checkAndRequestPermissions();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);


        findViewById(R.id.btn_takepicture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        // enable the location center button
        enablelocation();
        initializeComponents();

        // update the map by reading the files
        updateMap();

    }

    public void initializeComponents() {
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        // get location
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        double longitude = location.getLongitude();
        double latitude = location.getLatitude();

        LatLng myLocation = new LatLng(latitude, longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(13.0f));
    }

    public void dispatchTakePictureIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (intent.resolveActivity(getPackageManager()) != null) {

            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.e("a", ex.getMessage());
            }

            if (photoFile != null) {
                //Use the FileProvider defined in the Manifest as the authority for sharing across the Intent
                //Provides a content:// URI instead of a file:// URI which throws an error post API 24
                Uri photoURI = FileProvider.getUriForFile(this, CAMERA_FP_AUTHORITY, photoFile);
                //Put the content:// URI as the output location for the photo
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                //Start the Camera Application for a result
                startActivityForResult(intent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    public File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        //Use ExternalStoragePublicDirectory so that it is accessible for the MediaScanner
        //Associate the directory with your application by adding an additional subdirectory
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyCamera");
        if (!storageDir.exists()) {
            storageDir.mkdir();
        }
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        Log.d("", "Storage Directory: " + storageDir.getAbsolutePath());
        return image;
    }

    public void enablelocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    1);
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
            return;
        }
        mMap.setMyLocationEnabled(true);
    }

    public void galleryAddPic() {
        File f = new File(mCurrentPhotoPath);
        Intent myMediaIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        myMediaIntent.setData(Uri.fromFile(f));
        getApplicationContext().sendBroadcast(myMediaIntent);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        double longitude = location.getLongitude();
        double latitude = location.getLatitude();

        // rounding the lat and long to 3 decimal place
        longitude = Math.round(longitude * 1000);
        longitude = longitude/1000;
        latitude = Math.round(latitude * 1000);
        latitude = latitude/1000;

        saveToFile(latitude,longitude,mCurrentPhotoPath);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //If the Request is to take a photo and it returned OK
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            //Add the photo to the gallery
            galleryAddPic();
            finish();
            startActivity(getIntent());
        }

    }

    private final int MY_PERMISSIONS_REQUEST = 100;
    private boolean mFineLocationPermissionGranted = false;
    private boolean mCoarseLocationPermissionGranted = false;
    private boolean mStoragePermissionGranted = false;
    private final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 0;
    private final int PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
    private final int PERMISSIONS_REQUEST_ACCESS_STORAGE = 2;

    // grant all three permissions
    private boolean checkAndRequestPermissions() {
        int permissionLocationFine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int permissionLocationCoarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        int storagePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

        List<String> listPermissionsNeeded = new ArrayList<>();
        if (permissionLocationCoarse != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        if (permissionLocationFine != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (storagePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), MY_PERMISSIONS_REQUEST);
            return false;
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST: {

                Map<String, Integer> perms = new HashMap<>();
                // Initialize the map with both permissions
                perms.put(Manifest.permission.ACCESS_COARSE_LOCATION, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
                // Fill with actual results from user
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);
                    // Check for both permissions
                    if (perms.get(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        Log.d("", "sms & location services permission granted");
                        // process the normal flow
                        //else any one or both the permissions are not granted
                    } else {
                        Log.d("", "Some permissions are not granted ask again ");
                        //permission is denied (this is the first time, when "never ask again" is not checked) so ask again explaining the usage of permission
//                        // shouldShowRequestPermissionRationale will return true
                        //show the dialog or snackbar saying its necessary and try again otherwise proceed with setup.
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                            showDialogOK("SMS and Location Services Permission required for this app",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            switch (which) {
                                                case DialogInterface.BUTTON_POSITIVE:
                                                    checkAndRequestPermissions();
                                                    break;
                                                case DialogInterface.BUTTON_NEGATIVE:
                                                    // proceed with logic by disabling the related features or quit the app.
                                                    break;
                                            }
                                        }
                                    });
                        }
                        //permission is denied (and never ask again is  checked)
                        //shouldShowRequestPermissionRationale will return false
                        else {
                            Toast.makeText(this, "Go to settings and enable permissions", Toast.LENGTH_LONG)
                                    .show();
                            //                            //proceed with logic by disabling the related features or quit the app.
                        }
                    }
                }
            }
        }

    }

    private void showDialogOK(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", okListener)
                .create()
                .show();
    }

    // save to file
    void saveToFile(double lat, double lon, String path) {

        // create an array list of note
        ArrayList<MarkerInfo> notes = new ArrayList<>();

        // create a note with current data
        MarkerInfo myMarker = new MarkerInfo(lat, lon, path);
        // add note to the arraylist
        notes.add(myMarker);

        // getting all the input object from input stream
        try {
            //Open the file
            FileInputStream fis = this.openFileInput(LOCAL_FILE_NAME);
            //Open a wrapper class to read serialized objects from that file
            ObjectInputStream is = new ObjectInputStream(fis);

            Object obj = null;
            // if there is object inobject input stream, if will add to the arraylist
            while ((obj = is.readObject()) != null) {
                if (obj instanceof MarkerInfo) {
                    notes.add(((MarkerInfo) obj));
                }
            }

            // close the streams
            is.close();
            fis.close();
        } catch (java.io.FileNotFoundException e) {
            Log.e("open", "File Not Found. FileNotFoundException: " + e.toString());
        } catch (java.io.IOException e) {
            Log.e("open", "Cannot open file, IOException" + e.toString());
        } catch (java.lang.ClassNotFoundException e) {
            Log.e("open", "Cannot open file, Class does not exist: " + e.toString());
        }


        try {
            FileOutputStream fos = this.openFileOutput(LOCAL_FILE_NAME, Context.MODE_PRIVATE);
            //ObjectOutputStream - Takes serializeable objects and prints them to the file
            ObjectOutputStream os = new ObjectOutputStream(fos);
            //Print serialized object to file
            for (int i = 0; i < notes.size(); i++)
                os.writeObject(notes.get(i));

            os.close();
            fos.close();
        } catch (java.io.FileNotFoundException e) {
            Log.e("File", "File Not Found. FileNotFoundException: " + e.toString());
        } catch (java.io.IOException e) {
            Log.e("File", "Cannot open file, IOException" + e.toString());
        }

    }

    void updateMap()
    {

        ArrayList<MarkerInfo> markerInfos = new ArrayList<>();

        // getting all the input object from input stream
            try {
                //Open the file
                FileInputStream fis = this.openFileInput(LOCAL_FILE_NAME);
                //Open a wrapper class to read serialized objects from that file
                ObjectInputStream is = new ObjectInputStream(fis);

                Object obj = null;
                // if there is object inobject input stream, if will add to the arraylist
                while ((obj = is.readObject()) != null) {
                    if (obj instanceof MarkerInfo) {
                        markerInfos.add(((MarkerInfo) obj));
                    }
                }


                // close the streams
                is.close();
                fis.close();
            } catch (java.io.FileNotFoundException e) {
                Log.e("open", "File Not Found. FileNotFoundException: " + e.toString());
            } catch (java.io.IOException e) {
                Log.e("open", "Cannot open file, IOException" + e.toString());
            } catch (java.lang.ClassNotFoundException e) {
                Log.e("open", "Cannot open file, Class does not exist: " + e.toString());
            }

            for(int i = 0; i < markerInfos.size(); i++)
            {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                //Load the Bitmap from the Image file created by the camera
                final Bitmap imageBitmap = BitmapFactory.decodeFile(markerInfos.get(i).getPath(), options);
                final Bitmap resizedBitmap = Bitmap.createScaledBitmap(
                        imageBitmap, 100, 100, false);

                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(markerInfos.get(i).getLat(), markerInfos.get(i).getLon())));

                mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

                    @Override
                    public View getInfoWindow(Marker marker) {
                        return null;
                    }

                    @Override
                    public View getInfoContents(Marker marker) {
                        View v = getLayoutInflater().inflate(R.layout.window, null);

                        ImageView imageView = v.findViewById(R.id.img_1);
                        TextView lat = (TextView) v.findViewById(R.id.tv_lat);
                        TextView lon = (TextView) v.findViewById(R.id.tv_long);
                        //TextView time = (TextView) v.findViewById(R.id.tv_time);

                        imageView.setImageBitmap(resizedBitmap);
                        LatLng ll = marker.getPosition();
                        lat.setText("Latitude: " + ll.latitude);
                        lon.setText("Longitude: " + ll.longitude);

                        return v;
                    }
                });

                mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick(Marker marker) {
                        // to view big size of picture
                    }
                });
            }
    }
}
