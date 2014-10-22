package com.gsma.android.mobileconnect.authorization;

/**
 * Interface AuthorizationListener
 */
public interface AuthorizationListener {

	/**
	 * Method to wait successful response.
	 * @param response
	 */
	public void authorizationCodeResponse(String state, String authorizationCode, String error, String clientId, String clientSecret, String scopes, String redirectUri);
	
	/**
	 * Method to wait error response.
	 * @param error
	 */
	public void authorizationError(String reason);

}
