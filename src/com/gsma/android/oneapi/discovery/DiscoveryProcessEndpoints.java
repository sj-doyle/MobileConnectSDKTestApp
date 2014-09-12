package com.gsma.android.oneapi.discovery;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.json.JSONException;
import org.json.JSONObject;

import com.gsma.android.oneapi.utilsDiscovery.HttpUtils;
import com.gsma.android.oneapi.utilsDiscovery.JsonUtils;

import android.util.Log;

/**
 * Makes a connection to the discovery service. 
 */
public class DiscoveryProcessEndpoints {

	private static final String TAG = "DiscoveryProcessEndpoints";

	/**
	 * Constructor 
	 * @param contentType
	 * @param httpResponse
	 * @param inputStream
	 * @return JSONObject
	 * @throws IOException
	 * @throws JSONException
	 */
	public static JSONObject start(String contentType, HttpResponse httpResponse, InputStream inputStream) {

		JSONObject errorResponse = null;

		try {
			String contents = HttpUtils.getContentsFromInputStream(inputStream);

			Log.d(TAG, "Read " + contents);

			if (contentType != null && contentType.toLowerCase().startsWith("application/json")) {
				Log.d(TAG, "Read JSON content");

				Object rawJSON = JsonUtils.convertContent(contents, contentType);
				if (rawJSON != null) {
					Log.d(TAG, "Have read the json data");

					if (rawJSON instanceof JSONObject) {
						JSONObject json = (JSONObject) rawJSON;
						errorResponse = json;

					}
				}
			}

		} catch (IOException e) {
			errorResponse = JsonUtils.simpleError("IOException",
					"IOException - " + e.getMessage());
		} catch (JSONException e) {
			errorResponse = JsonUtils.simpleError("JSONException",
					"JSONException - " + e.getMessage());
		}

		return errorResponse;
	}

}
