package au.org.ala.ozatlas;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

/**
 * Manages persistence of user credentials.
 */
public class CredentialsStorage {

	private static final String TOKEN_KEY = "authToken";
	private static final String USERNAME_KEY = "username";
	
	private Context ctx;
	
	public CredentialsStorage(Context ctx) {
		this.ctx = ctx;
	}
	
	public boolean isAuthenticated() {
		return getAuthToken() != null;
	}
	
	public String getAuthToken() {
		return PreferenceManager.getDefaultSharedPreferences(ctx).getString(TOKEN_KEY, null);
	}
	
	public String getUsername() {
		return PreferenceManager.getDefaultSharedPreferences(ctx).getString(USERNAME_KEY, null);
	}
	
	public void saveCredentials(String username, String authToken) {
		
		if (username == null) {
			throw new IllegalArgumentException("Username cannot be null");
		}
		if (authToken == null) {
			throw new IllegalArgumentException("Authentication token cannot be null");
		}
		Editor editor = PreferenceManager.getDefaultSharedPreferences(ctx).edit();
		editor.putString(USERNAME_KEY, username);
		editor.putString(TOKEN_KEY, authToken);
		editor.commit();
	}
}
