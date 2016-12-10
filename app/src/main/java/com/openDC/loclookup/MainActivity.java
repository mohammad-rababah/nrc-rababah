package com.openDC.loclookup;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import com.openDC.loclookup.nrclocation.LocationListener;
import com.openDC.loclookup.nrclocation.LocationReader;

public class MainActivity extends Activity {

    private LocationReader locationReader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        locationReader = LocationReader.getInstance(this, new LocationListener() {
            @Override
            public void onSuccessRead(final String regionName) {
                Intent intent = new Intent();
                intent.putExtra("result", regionName);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, regionName, Toast.LENGTH_LONG).show();

                    }
                });
                MainActivity.this.setResult(RESULT_OK, intent);
                MainActivity.this.finish();
            }

            @Override
            public void onFailRead(final String failName) {
                Intent intent = new Intent();
                intent.putExtra("result", failName);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, failName, Toast.LENGTH_LONG).show();

                    }
                });
                MainActivity.this.setResult(RESULT_CANCELED, intent);
                MainActivity.this.finish();

            }
        });
        locationReader.enablePermission();
        locationReader.readLocation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        if (requestCode == LocationReader.LOCATION_PERMISSION_VALUE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            locationReader.readLocation();
        }
    }

}
