package com.gsma.android.oneapi.discovery;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.HashMap;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.gsma.android.utils.CookieManagement;
import com.gsma.android.utils.HttpUtils;
import com.gsma.android.utils.JsonUtils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

/**
 * This is a background task which makes an initial connection to the discovery 
 * service - it will handle a variety of initial response types. Extends AsyncTask<Void, Void, JSONObject>
 */
public class DiscoveryTask extends AsyncTask<Void, Void, JSONObject> {
	private static final String TAG = "DiscoveryTask";

	String serviceUri; 
	String consumerKey;
	String consumerSecret; 
	String sourceIP;
	boolean usingMobileData;
	String msisdn;
	String mcc;
	String mnc;
	DiscoveryCallbackReceiver getJSONListener;
	String credentials;
	String redirectUri;
	Context context;
	boolean followRedirect;

	/**
	 * Standard constructor
	 * 
	 * @param serviceUri
	 * @param consumerKey
	 * @param consumerSecret
	 * @param usingMobileData
	 * @param msisdn
	 * @param mcc
	 * @param mnc
	 * @param getJSONListener
	 * @param credentials (none, plain, sha256)
	 * @param redirectUri 
	 * @param context
	 * @param followRedirect
	 */
	public DiscoveryTask(String serviceUri, String consumerKey,
			String consumerSecret, String sourceIP, boolean usingMobileData,
			String msisdn, String mcc, String mnc,
			DiscoveryCallbackReceiver getJSONListener, String credentials, String redirectUri, Context context, boolean followRedirect) {
		Log.d(TAG, "Instantiated DiscoveryTask");
		this.serviceUri = serviceUri;
		this.consumerKey = consumerKey;
		this.consumerSecret = consumerSecret;
		this.sourceIP = sourceIP;
		this.usingMobileData = usingMobileData;
		this.msisdn = msisdn;
		this.mcc = mcc;
		this.mnc = mnc;
		this.getJSONListener = getJSONListener;
		this.credentials = credentials;
		this.redirectUri = redirectUri;
		this.context = context;
		this.followRedirect = followRedirect;
	}

	/**
	 * The doInBackground function does the actual background processing.
	 * @see android.os.AsyncTask#doInBackground(Params[])
	 * 
	 * @param params
	 * @return JSONObject
	 * @throws UnsupportedEncodingException
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws JSONException
	 */
	@Override
	protected JSONObject doInBackground(Void... params) {
//		android.os.Debug.waitForDebugger();

		JSONObject response = null;
		String cookie = null;
		String phase1Uri=serviceUri;

		/*
		 * sets up the HTTP request with a redirect_uri parameter - in practice
		 * we're looking for mcc/mnc added to the redirect_uri if this step is
		 * necessary
		 */
		if(redirectUri!=null && redirectUri.trim().length() > 0){
			phase1Uri = phase1Uri + "?redirect_uri="+redirectUri;//http://gsma.com/oneapi
			if (mcc != null && mcc.trim().length() > 0 && mnc != null && mnc.trim().length() > 0) {
				phase1Uri = phase1Uri + "&mcc_mnc=" + mcc + "_" + mnc;
			} else if ((cookie = CookieManagement.getCookie("mcc_mnc")) != null) {
				phase1Uri = phase1Uri + "&mcc_mnc=" + cookie;
			}
		}else if (mcc != null && mcc.trim().length() > 0 && mnc != null && mnc.trim().length() > 0) {
			phase1Uri = phase1Uri + "?mcc_mnc=" + mcc + "_" + mnc;
		} else if ((cookie = CookieManagement.getCookie("mcc_mnc")) != null) {
			phase1Uri = phase1Uri + "?mcc_mnc=" + cookie;
		}
		
		if(msisdn!=null && msisdn.trim().length() > 0) {
			if (phase1Uri.indexOf("?")== -1) {
				phase1Uri = phase1Uri + "?msisdn=" + msisdn;
			} else {
				phase1Uri = phase1Uri + "&msisdn=" + msisdn;
			}
		}
		
		Log.d(TAG, "Started discovery process via " + phase1Uri);
		
		HttpGet httpRequest = new HttpGet(phase1Uri);

		if (usingMobileData || (mcc != null && mcc.trim().length() > 0 && mnc != null && mnc.trim().length() > 0 && (msisdn == null || msisdn.trim().length() <= 0)) || cookie != null) {
			httpRequest.addHeader("Accept", "application/json");
			Log.d(TAG, "Add application/json");
		} else {
			httpRequest.addHeader("Accept", "text/html");// msisdn and redirect cases
			Log.d(TAG, "Add text/html");
		}

		if (sourceIP != null) {
			httpRequest.addHeader("x-source-ip", sourceIP);
			Log.d(TAG, "Add x-source-ip " + sourceIP);
		}

		try {
			HostnameVerifier hostnameVerifier = org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
			SchemeRegistry registry = new SchemeRegistry();
			SSLSocketFactory socketFactory = SSLSocketFactory.getSocketFactory();
			socketFactory.setHostnameVerifier((X509HostnameVerifier) hostnameVerifier);
			registry.register(new Scheme("https", socketFactory, 443));
			HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);

			HttpClient httpClient = HttpUtils.getHttpAuthorizationClient(phase1Uri, consumerKey, consumerSecret, credentials, httpRequest);
			HttpParams httpParams = httpRequest.getParams();
			httpParams.setParameter(ClientPNames.HANDLE_REDIRECTS,Boolean.FALSE);
			httpRequest.setParams(httpParams);

			Log.d(TAG, "Add consumerKey and consumerSecret; " + consumerKey
					+ " - " + consumerSecret);

			Log.d(TAG, "Making " + httpRequest.getMethod() + " request to " + httpRequest.getURI());
			
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.DATE, 1);
			if (mcc != null && mcc.trim().length() > 0 && mnc != null && mnc.trim().length() > 0)
				CookieManagement.addCookieExpireOneDay("mcc_mnc", mcc + "_" + mnc);
			CookieManagement.updateCookieStore(httpClient);
			HttpResponse httpResponse = httpClient.execute(httpRequest);
			
			Log.d(TAG, "Request executed and completed with status=" + httpResponse.getStatusLine().getStatusCode());

			HashMap<String, String> headerMap = HttpUtils.getHeaders(httpResponse);
			
			Log.d(TAG, "headerMap =" + headerMap);
			
			String contentType = headerMap.get("content-type");
			String location = headerMap.get("location");

			int statusCode = httpResponse.getStatusLine().getStatusCode();
			
			Log.d(TAG, "status=" + statusCode + " CT=" + contentType + " Loc="
					+ location + " JSON?" + HttpUtils.isJSON(contentType)
					+ " HTML?" + HttpUtils.isHTML(contentType));

			if (statusCode == HttpStatus.SC_OK) {

				if (HttpUtils.isJSON(contentType)) {

					HttpEntity httpEntity = httpResponse.getEntity();
					InputStream is = httpEntity.getContent();
					Log.d(TAG, "Converting discovery data OK (JSON)");
					response = JsonUtils.readJSON(is);

				} else if (HttpUtils.isHTML(contentType)) {
					Log.d(TAG,"Have OK HTML content - needs to be handled through the browser");
					// msisdn
				}

			} else if (statusCode == HttpStatus.SC_MOVED_TEMPORARILY && location != null) {
				Log.d(TAG, "302 response " + location);
				
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ie) {
				}

				if (location.indexOf("mcc_mnc") > -1) {
					String[] parts = location.split("mcc_mnc", 2);
					if (parts.length == 2) {
						String mcc_mnc = parts[1].replaceFirst("=", "").trim();
						Log.d(TAG, "To try again with mcc_mnc = " + mcc_mnc);
						response = ProcessDiscoveryToken.start(mcc_mnc,consumerKey, serviceUri);
					}
				} else {
					Log.d(TAG, "Redirect requested to " + location);
					if(followRedirect){
						Intent intent = new Intent(Intent.ACTION_VIEW);
						intent.setData(Uri.parse(location));
						String title = "Choose a browser";
						Intent chooser = Intent.createChooser(intent, title);
						context.startActivity(chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
					}
					response = new JSONObject();
					response.put("Location", location);
				}
			} else if (statusCode == HttpStatus.SC_ACCEPTED) {

				Log.d(TAG, "202 response ");
				
				HttpEntity httpEntity = httpResponse.getEntity();
				InputStream is = httpEntity.getContent();
				JSONObject jo = JsonUtils.readJSON(is);
				Link[] link = null;
				String href = "";
				if (jo != null) {
					JSONArray linkArray = jo.getJSONArray("links");
					if (linkArray != null) {
						link = new Link[linkArray.length()];
						for (int i = 0; i < linkArray.length(); i++) {
							link[i] = new Link(linkArray.getJSONObject(i));
						}
					}
				}
				if (link[0] != null)
					href = link[0].getHref();

				try {
					Thread.sleep(1000);
				} catch (InterruptedException ie) {
				}

				if (href.indexOf("mcc_mnc") > -1) {
					String[] parts = href.split("mcc_mnc", 2);
					if (parts.length == 2) {
						String mcc_mnc = parts[1].replaceFirst("=", "").trim();
						Log.d(TAG, "To try again with mcc_mnc = " + mcc_mnc);
						response = ProcessDiscoveryToken.start(mcc_mnc, consumerKey, serviceUri);
					}
				} else {
					Log.d(TAG, "Redirect requested to " + href);
					if(followRedirect){
						Intent intent = new Intent(Intent.ACTION_VIEW);
						intent.setData(Uri.parse(href));
						String title = "Choose a browser";
						Intent chooser = Intent.createChooser(intent, title);
						context.startActivity(chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
					}
					response = new JSONObject();
					response.put("operatorSelection", href);
				}

			} else if (statusCode >= HttpStatus.SC_BAD_REQUEST) {
				Log.d(TAG, "Bad request response " + statusCode);

				HttpEntity httpEntity = httpResponse.getEntity();
				InputStream is = httpEntity.getContent();
				String contents = HttpUtils.getContentsFromInputStream(is);

				if (HttpUtils.isJSON(contentType)) {
					Log.d(TAG, "Bad request response " + contentType);
					Object rawJSON = JsonUtils.convertContent(contents, contentType);
					if (rawJSON != null && rawJSON instanceof JSONObject) {
						response = (JSONObject) rawJSON;
					}
				} else {
					Log.d(TAG, "Bad request response " + contentType);
					response = JsonUtils.simpleError("HTTP " + statusCode, "HTTP " + statusCode);
				}

			}

		} catch (UnsupportedEncodingException e) {
			Log.d(TAG, "UnsupportedEncodingException=" + e.getMessage());
			response = JsonUtils.simpleError("UnsupportedEncodingException",
					"UnsupportedEncodingException - " + e.getMessage());
		} catch (ClientProtocolException e) {
			Log.d(TAG, "ClientProtocolException=" + e.getMessage());
			response = JsonUtils.simpleError("ClientProtocolException",
					"ClientProtocolException - " + e.getMessage());
		} catch (IOException e) {
			Log.d(TAG, "IOException=" + e.getMessage());
			response = JsonUtils.simpleError("IOException", "IOException - "
					+ e.getMessage());
		} catch (JSONException e) {
			Log.d(TAG, "JSONException=" + e.getMessage());
			response = JsonUtils.simpleError("JSONException",
					"JSONException - " + e.getMessage());
		}

		return response;
	}

	/**
	 * On completion of this background task either this task has started the
	 * next part of the process or an error has occurred.
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	 * 
	 * @param response
	 */
	@Override
	protected void onPostExecute(JSONObject response) {
//		android.os.Debug.waitForDebugger();
		Log.d(TAG, "onPostExecute for " + response);
		getJSONListener.receiveDiscoveryData(response);
	}

}
