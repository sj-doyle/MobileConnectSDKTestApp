package com.gsma.android.mobileconnect.userinfo;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

/**
 * Main class with library methods. Implements OpenIDConnectCallbackUserinfoReciever
 */
public class Userinfo implements OpenIDConnectCallbackUserinfoReciever {
	private static final String TAG = "Userinfo";

	RetrieveUserinfoTask initialRetrieveUserinfoTask;
	UserinfoListener listener;

	/**
	 * Constructor
	 */
	public Userinfo() {
	}

	/**
	 * Application can optionally use the access token to request access to
	 * stored user information via the OpenID Connect 'userinfo' service.
	 * 
	 * @param userinfoUri
	 *            userinfo endpoint.
	 * @param accessToken
	 * @param listener
	 *            It is necessary to implement userinfoResponse(JSONObject
	 *            response) and errorUserinfo(JSONObject error) to manage the
	 *            response.
	 */
	public void userinfo(String userinfoUri, String accessToken,
			UserinfoListener listener) {
		this.listener = listener;
		Log.d(TAG, "Calling RetrieveUserinfoTask");
		try {
			initialRetrieveUserinfoTask = new RetrieveUserinfoTask(userinfoUri,
					accessToken, this);
			initialRetrieveUserinfoTask.execute();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Internal use. Method from the OpenIDConnectCallbackUserinfoReciever
	 * interface to wait the response.
	 * 
	 * @param response
	 * @throws JSONException
	 * @throws NullPointerException
	 */
	@Override
	public void processUserinfoResponse(JSONObject response) {
		if (initialRetrieveUserinfoTask != null) {
			initialRetrieveUserinfoTask.cancel(true);
			initialRetrieveUserinfoTask = null;
		}
		Log.d(TAG, "processUserinfoResponse");
		if (response != null) {
			try {
				Log.d(TAG, "userinfoResponse");
				UserinfoData userData = new UserinfoData(response);
				listener.userinfoResponse(userData);
			} catch (JSONException e) {
				JSONObject json = new JSONObject();
				try {
					json.put("Exception", "JSONException");
					json.put("Message", e.getMessage());
				} catch (JSONException ex) {
				}
				Log.d(TAG, "errorUserinfo exception: "+json);
				try{
					listener.errorUserinfo(json);
				}catch(NullPointerException ex){
					Log.d(TAG, "NullPointerException=" + ex.getMessage());
				}
			} catch (NullPointerException e){
				Log.d(TAG, "NullPointerException: "+e.getMessage());
			}
		} else {
			JSONObject json = new JSONObject();
			try {
				json.put("Exception", "JSONException");
				json.put("Message", "Null response");
			} catch (JSONException ex) {
			}
			Log.d(TAG, "errorUserinfo with null response");
			try{
				listener.errorUserinfo(json);
			}catch(NullPointerException ex){
				Log.d(TAG, "NullPointerException=" + ex.getMessage());
			}
		}
	}

}
