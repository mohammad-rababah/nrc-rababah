package com.openDC.loclookup.nrclocation;


/**
 * Created by hp on 09/12/2016.
 */

public interface LocationListener {
    void onSuccessRead(String regionName);

    void onFailRead(String failName);
}
