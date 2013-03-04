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
import org.springframework.web.client.RestTemplate;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Intent;
import android.database.Cursor;
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
	public static final String IMAGE_URL_KEY = "imageUrl";
	
	private static final String[] BUNDLE_KEYS = {LSID_KEY, SCIENTIFIC_NAME_KEY, COMMON_NAME_KEY, IMAGE_URL_KEY};
	
	/** Used to identify a request to the Camera when a result is returned */
	public static final int TAKE_PHOTO_REQUEST = 10;

	
	/**
	 * Used to identify a request to the Image Gallery when a result is returned
	 */
	public static final int SELECT_FROM_GALLERY_REQUEST = 20;

	private String lsid;
	private String scientificName;
	private Uri cameraFileUri;
	private float latitude;
	private float longitude;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_record_sighting);
		buildCustomActionBar();
		
		// Read the species that was sighted from the intent.
		Intent i = getIntent();
		render(bundleToMap(i.getExtras()));
		
		updateDateTime(new Date());
		
		addEventHandlers();
	}
	
	private void buildCustomActionBar() {
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		getSupportActionBar().setDisplayShowHomeEnabled(false);

		View customNav = LayoutInflater.from(this).inflate(R.layout.cancel_done, null);

		customNav.findViewById(R.id.action_done).setOnClickListener(new OnClickListener() {
			
			@SuppressWarnings("unchecked")
			@Override
			public void onClick(View v) {
				new SubmitSightingTask().execute(getRecordData("chris.godwin.ala@gmail.com", ""));	
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
						cal.set(Calendar.YEAR, year);
						cal.set(Calendar.MONTH, monthOfYear);
						cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
						dateButton.setText(android.text.format.DateFormat.getDateFormat(RecordSightingActivity.this).format(cal.getTime()));
					}
				};
				String currentDate = dateButton.getText().toString();
				try {
					Date date = android.text.format.DateFormat.getDateFormat(RecordSightingActivity.this).parse(currentDate);
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
						cal.set(Calendar.HOUR, hour);
						cal.set(Calendar.MINUTE, minute);
						timeButton.setText(android.text.format.DateFormat.getTimeFormat(RecordSightingActivity.this).format(cal.getTime()));
					}
				};
				String currentTime = timeButton.getText().toString();
				try {
					Date date = android.text.format.DateFormat.getTimeFormat(RecordSightingActivity.this).parse(currentTime);
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
	
	public MultiValueMap<String, Object> getRecordData(String userName, String authKey) {
	    Log.i("SubmitSightingTask", "Constructing data object for POST...");
	    MultiValueMap<String, Object> params = new LinkedMultiValueMap<String, Object>();
	    params.add("surveyId", "1");
//	    params.kingdom = $("SubmitSightingTask", "#kingdom").val();
//	    Log.i("SubmitSightingTask", "Kingdom: " + params.kingdom);
//	    params.family = $("SubmitSightingTask", "#family").val();
//	    Log.i("SubmitSightingTask", "Family: " + params.family);
	    params.add("taxonID", lsid);
//	    Log.i("SubmitSightingTask", "TaxonID: " + params.taxonID);
	    params.add("scientificName", scientificName);
//	    Log.i("SubmitSightingTask", "Scientific name: " + params.scientificName);
//	    params.survey_species_search = $("SubmitSightingTask", "#scientificName").val();
//	    Log.i("SubmitSightingTask", "survey_species_search: " + params.survey_species_search);
//	    params.commonName = $("SubmitSightingTask", "#commonName").val();
//	    Log.i("SubmitSightingTask", "common name: " + params.commonName);
//	    params.locationName = $("SubmitSightingTask", "#locality").val();
//	    Log.i("SubmitSightingTask", "locationName: " + params.locationName);
	    params.add("latitude", Float.toString(latitude));
//	    Log.i("SubmitSightingTask", "latitude: " + params.latitude);
	    params.add("longitude", Float.toString(longitude));
//	    Log.i("SubmitSightingTask", "longitude: " + params.longitude);
//	    params.deviceName = device.name;
//	    Log.i("SubmitSightingTask", "device name: " + params.deviceName);
//	    params.deviceId = device.uuid;
//	    params.devicePlatform = device.platform;
//	    params.deviceVersion = device.version;
//	    params.user = userName.toLowerCase();
//	    params.userName = userName.toLowerCase();
//	    Log.i("SubmitSightingTask", "user: " + params.user);
//	    params.authenticationKey = authKey;
//	    Log.i("SubmitSightingTask", "auth key: " + params.authenticationKey);
//	    params.date = $("SubmitSightingTask", "#occurrenceDate").val();
//	    params.time = "12:00";
	    params.add("notes", "");
//	    Log.i("SubmitSightingTask", "notes: " + params.notes);
	    params.add("attribute_1", new FileSystemResource(cameraFileUri.getPath()));
//	    params.number = $("SubmitSightingTask", "#individualCount").val();
//	    params.coordinatePrecision = 1.0; //TODO fix this
//	    params.accuracyInMeters = 100; //TODO fix this
	    return params;
	}
	
	public static class SubmitSightingTask extends AsyncTask<MultiValueMap<String, Object>, Void, Void> {

		@Override
		protected Void doInBackground(MultiValueMap<String, Object>... params) {
//			HttpClient http = HttpUtil.getTolerantClient();
//			final String searchUrl = "https://m.ala.org.au/search.json?pageSize=30&q="  + URLEncoder.encode("koala", "utf-8");
//			System.out.println("SEARCH URL : " + searchUrl);
//			
//			HttpGet get = new HttpGet(searchUrl);
//			HttpResponse response = http.execute(get);
//			InputStream input = response.getEntity().getContent();
			post("http://192.168.0.4:8082/mobileauth/proxy/submitRecordMultipart", params[0]);
			
			return null;
		}
	
		public void post(String url, MultiValueMap<String, Object> formData) {
			RestTemplate template = new RestTemplate(); // FormHttpMessageConverter is configured by default MultiValueMap<String,
	        template.getMessageConverters().add(new FormHttpMessageConverter());
			// String> form =
			
			 template.postForLocation(url, formData);
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
//				Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
//				String selection = MediaStore.Images.ImageColumns.DATA +"=?";
//				String[] args = {cameraFileUri.getPath()};
//				queryPhotoMetadata(uri, selection, args);
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
			Log.d("", cursor.getString(0)+", "+cursor.getString(1)+", "+cursor.getString(2)+", "+cursor.getString(3));
			photoUri = Uri.fromFile(new File(cursor.getString(3)));
			updateFromPhoto(photoUri, new Date(Long.parseLong(cursor.getString(2))), Float.parseFloat(cursor.getString(0)),Float.parseFloat(cursor.getString(1)));
		}
		else {
			Log.d("", "no meatdata!");
		}
		//ExifInterface exif = new ExifInterface(photoUri)
	}

	@SuppressLint("SimpleDateFormat")
	private void readPhotoMetadata(Uri photoUri) {
		try {
			ExifInterface exif = new ExifInterface(photoUri.getPath());
			float[] latlong= new float[2];
			boolean hasLatLong = exif.getLatLong(latlong);
			if (hasLatLong) {
				Log.d("", latlong[0]+", "+latlong[1]+", "+exif.getAttribute(ExifInterface.TAG_DATETIME));
			}
			else {
				Log.i("", "No lat long in photo EXIF data");
			}
			DateFormat exifFormat = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
			updateFromPhoto(photoUri, exifFormat.parse(exif.getAttribute(ExifInterface.TAG_DATETIME)), latlong[0], latlong[1]);
			
		}
		catch (IOException e) {
			Log.e("RecordSightingActivity", "Error reading EXIF for file: "+photoUri, e);
		}
		catch (ParseException e) {
			
		}
	}
	
	private void updateFromPhoto(Uri photoUri, Date dateTaken, float lat, float lon) {
		
		ImageView photo = (ImageView)findViewById(R.id.photoView);
		photo.setVisibility(View.VISIBLE);
		
		photo.setImageBitmap(ImageHelper.bitmapFromFile(photoUri, photo.getWidth(), photo.getHeight()));
		
		updateDateTime(dateTaken);
		
		EditText locationField = (EditText)findViewById(R.id.locationField);
		locationField.setText(String.format("Lat: %f, Lon: %f", lat, lon));
	}
	
	private void updateDateTime(Date date) {
		Button dateButton = (Button)findViewById(R.id.dateDisplay);
		Button timeButton = (Button)findViewById(R.id.timeDisplay);
		
		dateButton.setText(android.text.format.DateFormat.getDateFormat(this).format(date));
		
		timeButton.setText(android.text.format.DateFormat.getTimeFormat(this).format(date));
	}
	
}
