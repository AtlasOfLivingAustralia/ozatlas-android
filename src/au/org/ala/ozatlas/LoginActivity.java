package au.org.ala.ozatlas;

import java.io.IOException;

import javax.net.ssl.SSLException;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import au.org.ala.mobile.ozatlas.R;

import com.actionbarsherlock.app.SherlockActivity;

/**
 * Displays a login form to the user and initiates the login process.
 */
public class LoginActivity extends SherlockActivity implements OnClickListener {

	private ProgressDialog pd;
	private boolean dialogShowing = false;
	private LoginTask loginTask;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_login);
		
		Button button = (Button) findViewById(R.id.loginBtn);
		button.setOnClickListener(this);
		
		Button cancel = (Button)findViewById(R.id.cancelBtn);
		cancel.setOnClickListener(this);
		
		if (savedInstanceState != null) {
			restoreProgressDialog(savedInstanceState);
		}
		
		loginTask = (LoginTask)getLastNonConfigurationInstance();
		if (loginTask != null) {
			loginTask.attach(this);
		}
	}

	@Override
	@Deprecated
	public Object onRetainNonConfigurationInstance() {
		return loginTask;
	}



	private void restoreProgressDialog(Bundle savedInstanceState) {
		dialogShowing = savedInstanceState.getBoolean("dialogVisible"); 
		if (dialogShowing) {
			
			showProgressDialog();
			
		}
	}
	
	private void showProgressDialog() {
		pd = ProgressDialog.show(LoginActivity.this, "", 
				getResources().getString(R.string.login_progress_message), true, false, null);
		dialogShowing = true;
	}
	
	private void dismissProgressDialog() {
		dialogShowing = false;
		if (pd != null) {
			pd.dismiss();
			pd = null;
		}
	}
	
	@Override
	public void onPause() {
		super.onPause();
		if (pd != null && pd.isShowing()) {
			pd.dismiss();
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle b) {
		super.onSaveInstanceState(b);
		
		b.putBoolean("dialogVisible", dialogShowing);
	}

	public void onClick(View v) {
		if (v.getId() == R.id.loginBtn) {
			
			showProgressDialog();
			
			EditText usernameField = (EditText) findViewById(R.id.username);
			EditText passwordField = (EditText) findViewById(R.id.userPassword);
			String username = usernameField.getText().toString();
			String password = passwordField.getText().toString();
			
			loginTask = new LoginTask(this, username, password);
			loginTask.execute();
		}
		else if (v.getId() == R.id.cancelBtn) {
			setResult(RESULT_CANCELED);
			finish();
		}
	}
	
	public void loginFailed(Exception e) {
		dismissProgressDialog();
		
		AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);

		builder.setTitle(R.string.login_failed_title);
		builder.setMessage(R.string.login_failed_message);
		builder.setNegativeButton(R.string.close, new Dialog.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.show();
	}
	
	public void loginSucceeded() {
		dismissProgressDialog();
		setResult(RESULT_OK);
		finish();
	}
	
	static class LoginTask extends AsyncTask<Void, Integer, Boolean> {
		
		private LoginActivity ctx;
		private String username;
		private String password;
		private Exception e;

		public LoginTask(LoginActivity ctx, String username, String password) {
			this.ctx = ctx;
			this.username = username;
			this.password = password;
		}
		
		public Boolean doInBackground(Void... args) {
			boolean success = false;
			try {
				try{
					success = login();
				} catch(SSLException e) {
					// We seem to be getting random connection resets from the 
					// server when using SSL.  Trying again will normally work.
					Log.d("LoginActivity", "Got SSL error, retrying");
					success = login();
				}
				
		        
			} catch (Exception e) {
				this.e = e;
				Log.e("LoginActivity", "Login failed, ",e);
			}
			return success;
		}

		private boolean login() throws IOException, JsonProcessingException {
			boolean success = false;
			RestTemplate template = new RestTemplate(); 
			template.getMessageConverters().add(new FormHttpMessageConverter());
			template.getMessageConverters().add(new StringHttpMessageConverter());
			MultiValueMap<String, Object> params = new LinkedMultiValueMap<String, Object>();
			params.add("userName", username);
			params.add("password", password);
			String response = template.postForObject(HttpUtil.getLoginUrl(), params, String.class);
			Log.d("LoginActivity", response.toString());
			ObjectMapper om = new ObjectMapper();
			JsonNode node = om.readTree(response);
			JsonNode keyNode = node.get("authKey");
			if (keyNode != null) {
				saveCredentials(username, keyNode.getValueAsText());
				success = true;
			}
			return success;
		}
		
		

		private void saveCredentials(String username, String authToken) {
			CredentialsStorage storage = new CredentialsStorage(ctx);
			storage.saveCredentials(username, authToken);
		}
		
		void attach(LoginActivity ctx) {
			this.ctx = ctx;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			
			if (result) {
				ctx.loginSucceeded();
			}
			else {
				ctx.loginFailed(e);
			}
		}
	
	}
}
