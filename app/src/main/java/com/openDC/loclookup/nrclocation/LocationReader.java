package com.openDC.loclookup.nrclocation;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.location.*;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import diewald_shapeFile.files.shp.shapeTypes.ShpPolygon;
import diewald_shapeFile.shapeFile.ShapeFile;


/**
 * Created by hp on 09/12/2016.
 */
public class LocationReader implements android.location.LocationListener {
    private static final LocationReader LOCATION_READER = new LocationReader();
    private static final long MAXIMUM_READ_TIME = 20000;
    public static final int LOCATION_PERMISSION_VALUE = 0;
    private Activity resultActivity;
    private LocationListener resultLocationListener;
    private ProgressDialog locationProgressDialog;
    private Thread timerThread;
    double x = 41.382994;
    double y = 0.256985;

    /**
     * this function to call instance for this library
     *
     * @param resultActivity       the activity that call this library
     * @param resultLocationReader interface that retrun the rigon name
     * @return
     */
    public static LocationReader getInstance(Activity resultActivity, LocationListener resultLocationReader) {
        if (resultActivity == null)
            throw new RuntimeException("Activity that you snd is Null");
        if (resultLocationReader == null)
            throw new RuntimeException("location read listener  that you snd is Null");
        LOCATION_READER.setResultActivity(resultActivity);
        LOCATION_READER.setResultLocationListener(resultLocationReader);
        LOCATION_READER.copyAssets();
        return LOCATION_READER;
    }

    /**
     * private construct used only by this class
     */
    private LocationReader() {

    }

    /**
     * Activity Setter
     *
     * @param resultActivity the activity that call this library
     */
    public void setResultActivity(Activity resultActivity) {
        this.resultActivity = resultActivity;
    }

    /**
     * Activity Getter
     * return resultActivity parameter
     */
    public Activity getResultActivity() {
        return resultActivity;
    }

    /**
     * to read current location
     */
    public void readLocation() {
        showLocationProgressDialog();
        Criteria nrcCriteria = new Criteria();
        nrcCriteria.setPowerRequirement(Criteria.NO_REQUIREMENT);
        nrcCriteria.setAccuracy(Criteria.ACCURACY_COARSE);
        LocationManager nrcLocationManager = (LocationManager) resultActivity.getSystemService(resultActivity.LOCATION_SERVICE);
        if (checkPermission()) {
            nrcLocationManager.requestSingleUpdate(nrcCriteria, this, resultActivity.getMainLooper());
            setTimer();
        } else
            resultLocationListener.onFailRead("Location permission not set");

    }

    /**
     * time out function for location read
     */
    private void setTimer() {
        timerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(MAXIMUM_READ_TIME);
                    resultLocationListener.onFailRead("Location could not read");
                    locationProgressDialog.dismiss();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        timerThread.start();
    }

    /**
     * show wait  progress dialog
     */
    private void showLocationProgressDialog() {
        locationProgressDialog = new ProgressDialog(resultActivity);
        locationProgressDialog.setTitle("Please Wait");
        locationProgressDialog.setMessage("Reading Location");
        locationProgressDialog.show();
    }

    /**
     * enable the location permission
     */
    public void enablePermission() {
        if (!checkPermission()) {
            ActivityCompat.requestPermissions(resultActivity,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_VALUE);
        }

    }

    /**
     * set the result listener
     *
     * @param resultLocationListener set the listener value
     */
    public void setResultLocationListener(LocationListener resultLocationListener) {
        this.resultLocationListener = resultLocationListener;
    }

    /**
     * @return if the location permission is activate
     */
    private boolean checkPermission() {
        if (ActivityCompat.checkSelfPermission(resultActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return true;
    }

    @Override
    public void onLocationChanged(Location location) {
        locationProgressDialog.dismiss();
        String regionName = readRegion(location);
        if (regionName != null)
            resultLocationListener.onSuccessRead(regionName);
        else
            resultLocationListener.onFailRead("region not found in your location");

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

    private void copyAssets() {
        AssetManager assetManager = resultActivity.getAssets();
        String[] files = null;
        try {
            files = assetManager.list("");
        } catch (IOException e) {
            Log.e("tag", "Failed to get asset file list.", e);
        }
        if (files != null) for (String filename : files) {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = assetManager.open(filename);
                File outFile = new File(resultActivity.getExternalFilesDir(null), filename);
                Log.e("path", outFile.getAbsolutePath());
                out = new FileOutputStream(outFile);
                copyFile(in, out);
            } catch (IOException e) {
                Log.e("tag", "Failed to copy asset file: " + filename, e);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        // NOOP
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        // NOOP
                    }
                }
            }
        }
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    private String readRegion(Location location) {
        try {
            ShapeFile shapeFile = new ShapeFile("/storage/emulated/0/Android/data/com.openDC.loclookup/files/", "som_polbnda_adm2_1m").READ();
            int recordId = -1;
            for (int i = 0; i < shapeFile.getSHP_shapeCount(); i++) {
                ShpPolygon shpPolygon = shapeFile.getSHP_shape(i);
                double maxX = Math.max(shpPolygon.getBoundingBox()[0][0], shpPolygon.getBoundingBox()[0][1]);
                double maxY = Math.max(shpPolygon.getBoundingBox()[1][0], shpPolygon.getBoundingBox()[1][1]);
                double minX = Math.min(shpPolygon.getBoundingBox()[0][0], shpPolygon.getBoundingBox()[0][1]);
                double minY = Math.min(shpPolygon.getBoundingBox()[1][0], shpPolygon.getBoundingBox()[1][1]);
                y = location.getLatitude();
                x = location.getLongitude();
                if (x >= minX && x <= maxX && y >= minY && y <= maxY) {
                    recordId = shpPolygon.getRecordNumber();
                    break;
                }
            }
            if (recordId > -1)
                return shapeFile.getDBF_record(recordId, 4).trim() + ";" + shapeFile.getDBF_record(recordId, 3).replaceAll(",", ";");
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("error", e.getMessage());
        }
        return null;

    }
}
