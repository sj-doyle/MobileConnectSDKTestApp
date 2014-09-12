package com.gsma.android.xoperatorapidemo.activity;

import org.json.JSONObject;

import android.util.Log;

import com.gsma.android.oneapi.discovery.DiscoveryItem;
import com.gsma.android.oneapi.discovery.DiscoveryListener;

public class DiscoveryProcessor implements DiscoveryListener {
	private static String TAG="DiscoveryProcessor";
	
	@Override
	public void discoveryInfo(DiscoveryItem di) {
		// TODO Auto-generated method stub
		Log.d(TAG, "received discoveryInfo");
		if (di.getResponse()!=null) {
			Log.d(TAG, "have response");
		}
		if (di.getError()!=null) {
			Log.d(TAG, "have error");
		}
	}

	@Override
	public void errorDiscoveryInfo(JSONObject error) {
		// TODO Auto-generated method stub
		
		Log.d(TAG, "received error");
	}

}
