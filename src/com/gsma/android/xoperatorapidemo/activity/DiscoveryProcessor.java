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
	}

	@Override
	public void errorDiscoveryInfo(JSONObject error) {
		// TODO Auto-generated method stub
		
		Log.d(TAG, "received error");
	}

}
