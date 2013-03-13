package au.org.ala.ozatlas;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
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
import com.actionbarsherlock.app.SherlockFragmentActivity;

/**
 * Displays a form to collect details of a sighting of a species
 * to be submitted to the ALA.
 * Accepts a species lsid and scientific name as input.
 */
public class RecordSightingActivity extends SherlockFragmentActivity implements RenderPage, LoaderCallbacks<Cursor> {

	public static final String LSID_KEY = "lsid";
	public static final String SCIENTIFIC_NAME_KEY = "scientificName";
	public static final String COMMON_NAME_KEY = "commonName";
	public static final String IMAGE_URL_KEY = "speciesImageUrl";
	
	private static final String PHOTO_KEY = "photo";
	private static final String LOCATION_KEY = "location";
	private static final String DATE_KEY = "date";
	private static final String PHOTO_LOAD_KEY = "loadInProgress";
	
	private static final String[] BUNDLE_KEYS = {LSID_KEY, SCIENTIFIC_NAME_KEY, COMMON_NAME_KEY, IMAGE_URL_KEY};
	
	/** Used to identify a request to the Camera when a result is returned */
	public static final int TAKE_PHOTO_REQUEST = 10;

	
	/**
	 * Used to identify a request to the Image Gallery when a result is returned
	 */
	public static final int SELECT_FROM_GALLERY_REQUEST = 20;
	
	public static final int SELECT_LOCATION_REQUEST = 30;
	
	public static final int LOGIN_REQUEST = 40;
	
	public static final int UPLOAD_REQUEST = 50;

	private String lsid;
	private Uri cameraFileUri;
	private Location location;
	private Date date;
	/** 
	 * Used to save the parameters passed to the photo loading task so it
	 * can be restored correctly.
	 */
	private Bundle photoLoadParams;
	
	private ProgressDialog progressDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		checkForLogin();
		
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
			photoLoadParams = (Bundle)savedInstanceState.getParcelable(PHOTO_LOAD_KEY);
			if (photoLoadParams != null) {
				getSupportLoaderManager().initLoader(0, photoLoadParams, this);
			}
			
		}
		
		updateDateTime();
		updateLocation();
		updatePhoto();
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
		out.putParcelable(PHOTO_LOAD_KEY, photoLoadParams);
	}
		
	@Override
	public void onPause() {
		super.onPause();
		if (progressDialog != null) {
			progressDialog.dismiss();
			progressDialog = null;
		}
	}
	
	@Override
	public void finish() {
		Log.i("RecordSightingActivity", "finish called");
		ImageHelper.deleteCachedFiles();
		super.finish();
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle args) {
		Log.i("RecordSightingActivity", "onCreateLoader");
		
		return new PhotoLoader(this, (Uri)args.getParcelable(PHOTO_KEY));
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		Log.i("RecordSightingActivity", "onLoadFinished");
		photoLoadFinished();
		if (cursor.moveToFirst()) {
	    	if (!cursor.isNull(0)) {
	    		date = new Date(cursor.getLong(0));
	    		updateDateTime();
	    	}
	    	if (!cursor.isNull(1)) {
	    		location = new Location("EXIF");
	    		location.setLatitude(cursor.getFloat(1));
	    		location.setLongitude(cursor.getFloat(2));
	    		updateLocation();
	    	}
	    	if (!cursor.isNull(3)) {
	    		cameraFileUri = Uri.parse(cursor.getString(3));
	    		updatePhoto();
	    	}
	    	else {
	    		photoLoadError();
	    	}
	    	
	    }
	    else {
	    	photoLoadError();
	    }
	}
	
	private void photoLoadError() {
		showError(R.string.error_loading_photo_title, R.string.error_loading_photo_message);
		cameraFileUri = null;
		updatePhoto();
	}
	
	private void photoLoadFinished() {
		photoLoadParams = null;
	}
	
	private void photoLoadInProgress(Bundle params) {
		photoLoadParams = params;
	}
	
	private boolean isPhotoLoadInProgress() {
		return photoLoadParams != null;
	}
	
	private void setPhotoReady(boolean ready) {
		if (isPhotoLoadInProgress()) {
			return;
		}
		if (!ready) {
			findViewById(R.id.photoView).setVisibility(View.GONE);
			findViewById(R.id.photoProgress).setVisibility(View.VISIBLE);
		}
		else {
			findViewById(R.id.photoView).setVisibility(View.VISIBLE);
			findViewById(R.id.photoProgress).setVisibility(View.GONE);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		Log.i("RecordSightingActivity", "onLoaderReset");
		// don't really care - we'll keep the last lot we have for the moment.
	}

	private void showProgressDialog() {
		progressDialog = ProgressDialog.show(this, "", "Recording your sighting...");
	}
	
	private void buildCustomActionBar() {
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		getSupportActionBar().setDisplayShowHomeEnabled(false);

		View customNav = LayoutInflater.from(this).inflate(R.layout.cancel_done, null);

		customNav.findViewById(R.id.action_done).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				validateAndSubmit();	
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
		
		final Button locationButton = (Button)findViewById(R.id.locationButton);
		locationButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				recordLocation();
			}
		});
	}
	
	private void validateAndSubmit() {
		if (cameraFileUri != null) {
			showProgressDialog();
			
			Intent uploadIntent = new Intent(this, UploadSightingService.class);
			uploadIntent.putExtra(UploadSightingService.STRING_PARAMS_KEY, getRecordData());
			if (cameraFileUri != null) {
				uploadIntent.putExtra(UploadSightingService.FILE_PARAM_KEY, cameraFileUri);
		    }
			PendingIntent callback = createPendingResult(UPLOAD_REQUEST, new Intent(), PendingIntent.FLAG_ONE_SHOT);
			uploadIntent.putExtra(UploadSightingService.CALLBACK_KEY, callback);
			
			startService(uploadIntent);
		}
		else {
			showError(R.string.attach_photo_title, R.string.attach_photo_message);
		}
	}

	private void showError(int title, int message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setTitle(title);
		builder.setMessage(message);
		builder.setNegativeButton(R.string.close, new Dialog.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.show();
	}
	
	/**
	 * Callback from the SubmitRecordTask when it completes.
	 * @param success true if the submit was successful.
	 */
	public void recordSubmitComplete(boolean success) {
		if (progressDialog != null) {
			progressDialog.dismiss();
			progressDialog = null;
		}
		if (success) {
			Toast.makeText(this, getResources().getString(R.string.record_sighting_success), Toast.LENGTH_LONG).show();
			finish();
		}
		else {
			Toast.makeText(this, getResources().getString(R.string.record_sighting_failed), Toast.LENGTH_LONG).show();
			
		}
	}
	
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	public HashMap<String, String> getRecordData() {
	    Log.i("SubmitSightingTask", "Constructing data object for POST...");
	    HashMap<String, String> params = new HashMap<String, String>();
	    params.put("surveyId", "1");
	    if (lsid != null) {
	    	params.put("taxonID", lsid);
	    }
	    TextView scientificName = (TextView)findViewById(R.id.scientificNameLabel);
	    params.put("scientificName", scientificName.getText().toString());
	    params.put("survey_species_search", scientificName.getText().toString());
	    TextView commonName = (TextView)findViewById(R.id.commonNameLabel);
	    params.put("commonName", commonName.getText().toString());
	    
	    if (location != null) {
	    	params.put("latitude", Double.toString(location.getLatitude()));
	    	params.put("longitude", Double.toString(location.getLongitude()));
	    	if (location.hasAccuracy()) {
	    		params.put("accuracyInMeters", Float.toString(location.getAccuracy()));
	    	}
	    }
	    params.put("deviceName", android.os.Build.MODEL);
	    
	    if (Build.VERSION.SDK_INT >= VERSION_CODES.GINGERBREAD) {
	    	
	    	params.put("deviceId", android.os.Build.SERIAL);
	    }
	    params.put("devicePlatform", "android");
	    params.put("deviceVersion", Build.VERSION.CODENAME + Build.VERSION.SDK_INT);

	    CredentialsStorage storage = new CredentialsStorage(this);
	    params.put("userName", storage.getUsername().toLowerCase());
	    params.put("authenticationKey", storage.getAuthToken());
	    
	    DateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd");
	    if (date != null) {
	    	params.put("date", isoFormat.format(date));		    
	    }
	    Button time = (Button)findViewById(R.id.timeDisplay);
	    params.put("time", time.getText().toString());
	    EditText notes = (EditText)findViewById(R.id.notesField);
	    params.put("notes", notes.getText().toString());
	    
	    EditText number = (EditText)findViewById(R.id.howManyField);
	    params.put("number", number.getText().toString());
	    return params;
	}
	
	
	
	private void checkForLogin() {
		CredentialsStorage storage = new CredentialsStorage(this);
		if (!storage.isAuthenticated()) {
			Intent loginIntent = new Intent(this, LoginActivity.class);
			startActivityForResult(loginIntent, LOGIN_REQUEST);
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
				loadPhotoMetadata(cameraFileUri);		
			}
		} else if (requestCode == SELECT_FROM_GALLERY_REQUEST) {
			if (resultCode == RESULT_OK) {
				Uri selected = data.getData();
				if (selected != null) {
					loadPhotoMetadata(selected);
				} else {
					Log.e("CollectSurveyData", "Null data returned from gallery intent!" + data);
				}
			}
		}
		else if (requestCode == SELECT_LOCATION_REQUEST) {
			if (resultCode == RESULT_OK) {
				location = data.getParcelableExtra(RecordLocationActivity.LOCATION_KEY);
				updateLocation();
			}
		}
		else if (requestCode == LOGIN_REQUEST) {
			if (resultCode != RESULT_OK) {
				finish();
			}
		}
		else if (requestCode == UPLOAD_REQUEST) {
			recordSubmitComplete(resultCode == RESULT_OK);
		}
	}

	private void loadPhotoMetadata(Uri photoUri) {
		
		findViewById(R.id.photoView).setVisibility(View.GONE);
		findViewById(R.id.photoProgress).setVisibility(View.VISIBLE);
		
		Bundle params = new Bundle();
		params.putParcelable(PHOTO_KEY, photoUri);
		photoLoadInProgress(params);
		getSupportLoaderManager().restartLoader(0, params, this);
	}
	
	private void updatePhoto() {
		ImageView photo = (ImageView)findViewById(R.id.photoView);
		
		if (cameraFileUri != null) {
			new UpdatePhotoTask(cameraFileUri, photo.getWidth(), photo.getHeight()).execute();
		}
		else {
			photo.setImageDrawable(getResources().getDrawable(R.drawable.no_photo));
			setPhotoReady(true);
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
	
	private void recordLocation() {
		Intent recordLocationIntent = new Intent(this, RecordLocationActivity.class);
		recordLocationIntent.putExtra(RecordLocationActivity.LOCATION_KEY, location);
		
		startActivityForResult(recordLocationIntent, SELECT_LOCATION_REQUEST);
	}
	
	private class UpdatePhotoTask extends AsyncTask<String, Void, Bitmap> {

		private int width;
		private int height;
		private Uri uri;
		
		public UpdatePhotoTask(Uri photoUri, int width, int height) {
			this.uri = photoUri;
			this.width = width;
			this.height = height;
		}
		@Override
		protected Bitmap doInBackground(String... params) {
			if (height == 0 || width == 0) {
				height = 300;
				width = 300;
			}
			return ImageHelper.bitmapFromFile(uri, width, height);
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			ImageView view = (ImageView)findViewById(R.id.photoView);
			view.setImageBitmap(result);
			setPhotoReady(true);
		}
		
	}
	
}
