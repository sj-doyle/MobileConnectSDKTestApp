package com.gsma.android.oneapi.discovery;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONObject;

import com.gsma.android.oneapi.utilsDiscovery.HttpUtils;
import com.gsma.android.oneapi.utilsDiscovery.JsonUtils;

import android.util.Log;

/**
 * Makes a petition to the discovery service. 
 */
public class ProcessDiscoveryToken {

	private static final String TAG = "ProcessDiscoveryToken";

	/**
	 * Constructor 
	 * @param mccmnc
	 * @param consumerKey
	 * @param serviceUri
	 * @return JSONObject
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static JSONObject start(String mccmnc, String consumerKey, String serviceUri) {

		JSONObject errorResponse = null;

		try {

			String phase2Uri = serviceUri + "?mcc_mnc=" + mccmnc;
			Log.d(TAG, "mccmnc = " + mccmnc);
			Log.d(TAG, "phase2Uri = " + phase2Uri);

			HttpClient httpClient = HttpUtils.getHttpClient(phase2Uri, consumerKey);

			HttpGet httpRequest = new HttpGet(phase2Uri);
			httpRequest.addHeader("Accept", "application/json");
			HttpResponse httpResponse = httpClient.execute(httpRequest);

			HashMap<String, String> headerMap = HttpUtils.getHeaders(httpResponse);

			String contentType = headerMap.get("content-type");

			HttpEntity httpEntity = httpResponse.getEntity();
			InputStream is = httpEntity.getContent();

			errorResponse = DiscoveryProcessEndpoints.start(contentType, httpResponse, is);

		} catch (ClientProtocolException e) {
			errorResponse = JsonUtils.simpleError("ClientProtocolException",
					"ClientProtocolException - " + e.getMessage());
		} catch (IOException e) {
			errorResponse = JsonUtils.simpleError("IOException",
					"IOException - " + e.getMessage());
		}
		return errorResponse;
	}

}
