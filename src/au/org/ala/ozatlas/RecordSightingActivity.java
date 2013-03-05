package au.org.ala.ozatlas;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar.LayoutParams;
import com.actionbarsherlock.app.SherlockActivity;

/**
 * Displays a form to collect details of a sighting of a species
 * to be submitted to the ALA.
 * Accepts a species lsid and scientific name as input.
 */
public class RecordSightingActivity extends SherlockActivity implements RenderPage {

	public static final String LSID_KEY = "lsid";
	public static final String SCIENTIFIC_NAME_KEY = "scientificName";
	public static final String COMMON_NAME_KEY = "commonName";
	public static final String IMAGE_URL_KEY = "speciesImageUrl";
	
	private static final String PHOTO_KEY = "photo";
	private static final String LOCATION_KEY = "location";
	private static final String DATE_KEY = "date";
	
	private static final String[] BUNDLE_KEYS = {LSID_KEY, SCIENTIFIC_NAME_KEY, COMMON_NAME_KEY, IMAGE_URL_KEY};
	
	/** Used to identify a request to the Camera when a result is returned */
	public static final int TAKE_PHOTO_REQUEST = 10;

	
	/**
	 * Used to identify a request to the Image Gallery when a result is returned
	 */
	public static final int SELECT_FROM_GALLERY_REQUEST = 20;

	private String lsid;
	private Uri cameraFileUri;
	private Location location;
	private Date date;
	
	private SubmitSightingTask submitSightingTask;
	private ProgressDialog progressDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_record_sighting);
		buildCustomActionBar();
		
		// Read the species that was sighted from the intent.
		Intent i = getIntent();
		render(bundleToMap(i.getExtras()));
		if (savedInstanceState != null) {
			cameraFileUri = savedInstanceState.getParcelable(PHOTO_KEY);
			location = savedInstanceState.getParcelable(LOCATION_KEY);
			long dateInMillis = savedInstanceState.getLong(DATE_KEY);
			if (dateInMillis > 0) {
				date = new Date(dateInMillis);
			}
		}
		
		updateDateTime();
		updateLocation();
		
		// We have to do this later (hence the post) as the ImageView won't 
		// have been layed out yet so will have zero size.
		ImageView photoView = (ImageView)findViewById(R.id.photoView);
		photoView.post(new Runnable() {
			public void run() {
				updatePhoto();
			}
		});
		
		submitSightingTask = (SubmitSightingTask)getLastNonConfigurationInstance();
		if (submitSightingTask != null) {
			submitSightingTask.attach(this);
			showProgressDialog();
		}
		
		addEventHandlers();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle out) {
		super.onSaveInstanceState(out);
		if (cameraFileUri != null) {
			out.putParcelable(PHOTO_KEY, cameraFileUri);
		}
		if (location != null) {
			out.putParcelable(LOCATION_KEY, location);
		}
		if (date != null) {
			out.putLong(DATE_KEY, date.getTime());
		}
	}
	
	@Override
	public Object onRetainNonConfigurationInstance() {
		return submitSightingTask;
	}
	
	@Override
	public void onPause() {
		super.onPause();
		if (progressDialog != null) {
			progressDialog.dismiss();
		}
	}
	
	private void showProgressDialog() {
		progressDialog = ProgressDialog.show(this, "", "Recording your sighting...");
	}
	
	private void buildCustomActionBar() {
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		getSupportActionBar().setDisplayShowHomeEnabled(false);

		View customNav = LayoutInflater.from(this).inflate(R.layout.cancel_done, null);

		customNav.findViewById(R.id.action_done).setOnClickListener(new OnClickListener() {
			
			@SuppressWarnings("unchecked")
			@Override
			public void onClick(View v) {
				showProgressDialog();
				new SubmitSightingTask(RecordSightingActivity.this).execute(getRecordData("chris.godwin.ala@gmail.com", ""));	
			}
		});
		customNav.findViewById(R.id.action_cancel).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT, Gravity.FILL_HORIZONTAL);
		getSupportActionBar().setCustomView(customNav, params);
		getSupportActionBar().setDisplayShowCustomEnabled(true);
	}
	
	private void addEventHandlers() {
		Button button = (Button)findViewById(R.id.takePhotoButton);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				takePhoto();
			}
		});
		
		button = (Button)findViewById(R.id.pickPhotoButton);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				selectFromGallery();
			}
		});
		
		final Button dateButton = (Button)findViewById(R.id.dateDisplay);
		dateButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				OnDateSetListener listener = new OnDateSetListener() {
					
					@Override
					public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
						Calendar cal = Calendar.getInstance();
						cal.setTime(date);
						cal.set(Calendar.YEAR, year);
						cal.set(Calendar.MONTH, monthOfYear);
						cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
						date = cal.getTime();
						updateDateTime();
					}
				};
				try {
					Calendar cal = Calendar.getInstance();
					cal.setTime(date);
					new DatePickerDialog(RecordSightingActivity.this, listener, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		final Button timeButton = (Button)findViewById(R.id.timeDisplay);
		timeButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				OnTimeSetListener listener = new OnTimeSetListener() {
					
					@Override
					public void onTimeSet(TimePicker picker, int hour, int minute) {
						Calendar cal = Calendar.getInstance();
						cal.setTime(date);
						cal.set(Calendar.HOUR, hour);
						cal.set(Calendar.MINUTE, minute);
						date = cal.getTime();
						updateDateTime();
					}
				};
				try {
					Calendar cal = Calendar.getInstance();
					cal.setTime(date);
					new TimePickerDialog(RecordSightingActivity.this, listener, cal.get(Calendar.HOUR), cal.get(Calendar.MINUTE), false).show();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	/**
	 * Callback from the SubmitRecordTask when it completes.
	 * @param success true if the submit was successful.
	 */
	public void recordSubmitComplete(boolean success) {
		if (progressDialog != null) {
			progressDialog.dismiss();
		}
		if (success) {
			Toast.makeText(this, "Your sighting has been submitted", Toast.LENGTH_LONG).show();
			finish();
		}
		else {
			Toast.makeText(this, "There was an error recording your sighting.\n Please try again later.", Toast.LENGTH_LONG).show();
			
		}
	}
	
	public MultiValueMap<String, Object> getRecordData(String userName, String authKey) {
	    Log.i("SubmitSightingTask", "Constructing data object for POST...");
	    MultiValueMap<String, Object> params = new LinkedMultiValueMap<String, Object>();
	    params.add("surveyId", "1");
//	    params.kingdom = $("SubmitSightingTask", "#kingdom").val();
//	    params.family = $("SubmitSightingTask", "#family").val();
	    params.add("taxonID", lsid);
	    TextView scientificName = (TextView)findViewById(R.id.scientificNameLabel);
	    params.add("scientificName", scientificName.getText().toString());

	    TextView commonName = (TextView)findViewById(R.id.commonNameLabel);
	    params.add("commonName", commonName.getText().toString());
	    
//	    params.locationName = $("SubmitSightingTask", "#locality").val();
//	    Log.i("SubmitSightingTask", "locationName: " + params.locationName);
	    if (location != null) {
	    	params.add("latitude", Double.toString(location.getLatitude()));
	    	params.add("longitude", Double.toString(location.getLongitude()));
	        params.add("accuracyInMeters", Float.toString(location.getAccuracy()));
	    }
	    params.add("deviceName", android.os.Build.MODEL);
	    
	    params.add("deviceId", android.os.Build.SERIAL);
	    params.add("devicePlatform", "android");
	    params.add("deviceVersion", new android.os.Build.VERSION().toString());

	    //	    params.user = userName.toLowerCase();
//	    params.userName = userName.toLowerCase();
//	    Log.i("SubmitSightingTask", "user: " + params.user);
//	    params.authenticationKey = authKey;
//	    Log.i("SubmitSightingTask", "auth key: " + params.authenticationKey);
	    Button date = (Button)findViewById(R.id.dateDisplay);
	    params.add("date", date.getText().toString());
	    Button time = (Button)findViewById(R.id.timeDisplay);
	    params.add("time", time.getText().toString());
	    EditText notes = (EditText)findViewById(R.id.notesField);
	    params.add("notes", notes.getText().toString());
	    if (cameraFileUri != null) {
	    	params.add("attribute_1", new FileSystemResource(cameraFileUri.getPath()));
	    }
	    EditText number = (EditText)findViewById(R.id.howManyField);
	    params.add("number", number.getText().toString());
	    return params;
	}
	
	public static class SubmitSightingTask extends AsyncTask<MultiValueMap<String, Object>, Void, Boolean> {

		private RecordSightingActivity ctx;
		
		public SubmitSightingTask(RecordSightingActivity ctx) {
			attach(ctx);
		}
		
		@Override
		protected Boolean doInBackground(MultiValueMap<String, Object>... params) {
			boolean success = false;
			String url = "http://152.83.195.62:8082/mobileauth/proxy/submitRecordMultipart";
			RestTemplate template = new RestTemplate(); // FormHttpMessageConverter is configured by default MultiValueMap<String,
	        template.getMessageConverters().add(new FormHttpMessageConverter());
	        try {
	        	Map<String, Object> response = template.postForObject(url, params[0], Map.class);
	        	Log.d("RecordSightingActivity", response.toString());
	        	success = (Boolean)response.get("success");
	        }
	        catch (Exception e) {
	        	Log.e("RecordSightingActivity", "Error recording sighting: ", e);
	        }
			return success;
		}
		
		
	
		@Override
		protected void onPostExecute(Boolean result) {
			ctx.recordSubmitComplete(result);
		}
		
		void attach(RecordSightingActivity ctx) {
			this.ctx = ctx;
		}
	}
	
	private Map<String, Object> bundleToMap(Bundle bundle) {
		Map<String, Object> data = new HashMap<String, Object>();
		for (String key : BUNDLE_KEYS) {
			data.put(key, bundle.get(key));
		}
		return data;
	}

	@Override
	public void render(Map<String, Object> data) {
		this.lsid = (String)data.get(LSID_KEY);
		ImageView speciesImage = (ImageView) findViewById(R.id.speciesImageView);
		String speciesImageUrl = (String) data.get(IMAGE_URL_KEY);
		
		LoadImageTask lit = new LoadImageTask();
		lit.setImageView(speciesImage);
		lit.execute(speciesImageUrl);
		
		TextView scientificName = (TextView) findViewById(R.id.scientificNameLabel);
		scientificName.setText((String) data.get(SCIENTIFIC_NAME_KEY));
		
		TextView commonName = (TextView) findViewById(R.id.commonNameLabel);
		commonName.setText((String) data.get(COMMON_NAME_KEY));
		
	}
	
	/**
	 * Launches the default camera application to take a photo and store the
	 * result for the supplied attribute.
	 */
	public void takePhoto() {
		if (ImageHelper.canWriteToExternalStorage()) {
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			cameraFileUri = ImageHelper.getOutputMediaFileUri(ImageHelper.MEDIA_TYPE_IMAGE);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraFileUri);
			// Unfortunately, this URI isn't being returned in the
			// result as expected so we have to save it somewhere it can
			// survive an activity restart.
			//surveyViewModel.setTempValue(attribute, fileUri.toString());
			startActivityForResult(intent, TAKE_PHOTO_REQUEST);
		} else {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Cannot take photo")
					.setMessage("Please ensure you have mounted your SD card and it is writable")
					.setPositiveButton("OK", null).show();
		}
	}

	/**
	 * Launches the default gallery application to allow the user to select an
	 * image to be attached to the supplied attribute.
	 */
	public void selectFromGallery() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");
		// Just saving the attribute we are working with.
		startActivityForResult(Intent.createChooser(intent, "Select Photo"),
				SELECT_FROM_GALLERY_REQUEST);
	}
	
	/**
	 * Callback made to this activity after the camera, gallery or map activity
	 * has finished.
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == TAKE_PHOTO_REQUEST) {
			if (resultCode == RESULT_OK) {
				readPhotoMetadata(cameraFileUri);		
			}
		} else if (requestCode == SELECT_FROM_GALLERY_REQUEST) {
			if (resultCode == RESULT_OK) {
				Uri selected = data.getData();
				if (selected != null) {
					queryPhotoMetadata(selected, null, null);
				} else {
					Log.e("CollectSurveyData", "Null data returned from gallery intent!" + data);
				}
			}
		}
	}
	
	private void queryPhotoMetadata(Uri photoUri, String selection, String[] selectionArgs) {

		if (selection != null) {
		Log.d("", selection);
		Log.d("", selectionArgs[0]);
		}
		String[] columns = {
		        MediaStore.Images.ImageColumns.LATITUDE,
		        MediaStore.Images.ImageColumns.LONGITUDE,
		        MediaStore.Images.ImageColumns.DATE_TAKEN,
		        MediaStore.Images.ImageColumns.DATA
		    };


		Cursor cursor = getContentResolver().query(photoUri, columns, selection, selectionArgs, null);
		if (cursor.moveToFirst()) {
			String lat = cursor.getString(0);
			String lon = cursor.getString(1);
			String dateTaken = cursor.getString(2);
			cameraFileUri = Uri.fromFile(new File(cursor.getString(3)));
			if (StringUtils.hasLength(lat)) {
				location = new Location("EXIF");
				location.setLatitude(Double.parseDouble(lat));
				location.setLongitude(Double.parseDouble(lon));
				updateLocation();
			}
			try {
				date = new Date(Long.parseLong(dateTaken));
			}
			catch (NumberFormatException e) {
				Log.e("RecordSightingActivity", "Invalid date in photo metadata: "+date);
			}
			
			updatePhoto();
		}
		else {
			Log.d("", "no meatdata!");
		}
	}

	@SuppressLint("SimpleDateFormat")
	private void readPhotoMetadata(Uri photoUri) {
		try {
			ExifInterface exif = new ExifInterface(photoUri.getPath());
			float[] latlong= new float[2];
			boolean hasLatLong = exif.getLatLong(latlong);
			if (hasLatLong) {
				location = new Location("EXIF");
				location.setLatitude(latlong[0]);
				location.setLongitude(latlong[1]);
				updateLocation();
			}
			else {
				Log.i("", "No lat long in photo EXIF data");
			}
			DateFormat exifFormat = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
			try {
				date = exifFormat.parse(exif.getAttribute(ExifInterface.TAG_DATETIME));
				updateDateTime();
			}
			catch (ParseException e) {
				Log.e("RecordSightingActivity", "Error reading date from EXIF for file: "+photoUri, e);
			}
			updatePhoto();
			
		}
		catch (IOException e) {
			Log.e("RecordSightingActivity", "Error reading EXIF for file: "+photoUri, e);
		}
	}
	
	private void updatePhoto() {
		
		if (cameraFileUri != null) {
			ImageView photo = (ImageView)findViewById(R.id.photoView);
			photo.setImageBitmap(ImageHelper.bitmapFromFile(cameraFileUri, photo.getWidth(), photo.getHeight()));
		}
	}
	
	private void updateLocation() {
		Button locationButton = (Button)findViewById(R.id.locationButton);
		
		if (location != null) {
			locationButton.setText(String.format("Lat: %f, Lon: %f", location.getLatitude(), location.getLongitude()));
		}
		else {
			locationButton.setText("Unknown");
		}
	}
	
	private void updateDateTime() {
		
		if (date == null) {
			date = new Date();
		}
		
		Button dateButton = (Button)findViewById(R.id.dateDisplay);
		Button timeButton = (Button)findViewById(R.id.timeDisplay);
		
		dateButton.setText(android.text.format.DateFormat.getDateFormat(this).format(date));
		
		timeButton.setText(android.text.format.DateFormat.getTimeFormat(this).format(date));
	}
	
}