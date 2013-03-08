package au.org.ala.ozatlas;

import java.util.Map;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import android.app.Activity;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

/**
 * Handles uploading sighting data to the ALA.
 */
public class UploadSightingService extends IntentService {

	private static final String UPLOAD_URL = "https://m.ala.org.au/submit/recordMultiPart";
	
	public static final String STRING_PARAMS_KEY = "stringParams";
	public static final String FILE_PARAM_KEY = "attribute_file_1";
	public static final String CALLBACK_KEY = "callback";
	
	public UploadSightingService() {
		super("UploadSightingService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {

		Map<String, Object> params = (Map<String, Object>)intent.getSerializableExtra(STRING_PARAMS_KEY);
		LinkedMultiValueMap<String, Object> postData = new LinkedMultiValueMap<String, Object>();
		postData.setAll(params);
		Uri fileUri = (Uri)intent.getParcelableExtra(FILE_PARAM_KEY);
		if (fileUri != null) {
			postData.add(FILE_PARAM_KEY, new FileSystemResource(fileUri.getPath()));
		}
		int result = uploadSighting(postData);
		handleResult(intent, result);
	}

	private void handleResult(Intent intent, int result) {
		
		PendingIntent callback = intent.getParcelableExtra(CALLBACK_KEY);
		if (callback != null) {
			try {
				callback.send(this, result, null);
			}
			catch (Exception e) {
				Log.i("UploadSightingService", "Callback to activity failed.");
			}
		}
	}
	
	private int uploadSighting(MultiValueMap<String, Object> params) {
		boolean success = false;
		RestTemplate template = new RestTemplate(); 
        template.getMessageConverters().add(new FormHttpMessageConverter());
        template.getMessageConverters().add(new StringHttpMessageConverter());
        
        try {
        	String response = template.postForObject(UPLOAD_URL, params, String.class);
        	Log.d("RecordSightingActivity", response.toString());
        	ObjectMapper om = new ObjectMapper();
			JsonNode node = om.readTree(response);
			JsonNode successNode = node.get("success");
        	success = successNode.getBooleanValue();
        }
        catch (Exception e) {
        	Log.e("RecordSightingActivity", "Error recording sighting: ", e);
        }
		return success ? Activity.RESULT_OK : Activity.RESULT_CANCELED;
	}
		

}
