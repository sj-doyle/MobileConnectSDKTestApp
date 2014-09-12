package com.gsma.android.xoperatorapidemo.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * simple utilities which help with processing of JSON data
 */
public class JsonUtils {
	private static final String TAG = "JsonUtils";
	
	/**
	 * convert JSON data to a Java object representing the JSON, uses the
	 * in-built Android libraries
	 * 
	 * @param content
	 * @param contentType
	 * @return
	 * @throws JSONException
	 */
	public static Object convertContent(String content, String contentType)
			throws JSONException {
		String trimmed = content != null ? content.trim() : null;
		Object result = null;

		if (contentType != null
				&& contentType.toLowerCase().startsWith("application/json")
				&& trimmed != null && trimmed.length() > 0) {
			JSONTokener tokener = new JSONTokener(trimmed);
			result = tokener.nextValue();
		}
		return result;
	}

	/**
	 * gets a named JSON String object from a root JSON object
	 * 
	 * @param object
	 * @param identifier
	 * @return
	 */
	public static String getJSONStringElement(JSONObject object,
			String identifier) {
		String value = null;
		if (object != null) {
			try {
				value = object.getString(identifier);
			} catch (JSONException e) {
			}
		}
		return value;
	}

	/**
	 * gets a named JSON object from a root JSON object
	 * 
	 * @param object
	 * @param identifier
	 * @return
	 */
	public static JSONObject getJSONObject(JSONObject object, String identifier) {
		JSONObject value = null;
		if (object != null) {
			try {
				value = object.getJSONObject(identifier);
			} catch (JSONException e) {
			}
		}
		return value;
	}

	/**
	 * gets a named JSON array from a root JSON object
	 * 
	 * @param object
	 * @param identifier
	 * @return
	 */
	public static JSONArray getJSONArray(JSONObject object, String identifier) {
		JSONArray value = null;
		if (object != null) {
			try {
				value = object.getJSONArray(identifier);
			} catch (JSONException e) {
			}
		}
		return value;
	}

	/**
	 * creates a simple error object from an error and error_description field
	 * 
	 * @param error
	 * @param error_description
	 * @return
	 */
	public static JSONObject simpleError(String error, String error_description) {
		JSONObject container = new JSONObject();
		try {
			container.put("error", error);
		} catch (JSONException e) {
		}
		try {
			container.put("error_description", error_description);
		} catch (JSONException e) {
		}
		return container;
	}
	

	public static String readString(InputStream is) throws IOException, JSONException { 
		StringBuffer buf=new StringBuffer();

		if (is!=null) {
			final Reader in = new InputStreamReader(is, "UTF-8");
			
			char[] b=new char[1024];
			int n;
			while ((n=in.read(b)) != -1) {
				buf.append(b, 0, n);
			}
			
			in.close();
		}
		
		return buf.toString();
	}
	
}
