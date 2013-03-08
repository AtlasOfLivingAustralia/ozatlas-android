package au.org.ala.ozatlas;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.util.StringUtils;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.commonsware.cwac.loaderex.acl.AbstractCursorLoader;

/**
 * Loads photo metadata, (downloading the photo if necessary).
 */
public class PhotoLoader extends AbstractCursorLoader {

	public static final String[] COLUMN_NAMES = {"date", "latitude", "longitude", "fileUri"};
	private Uri photoUri;
	
	public PhotoLoader(Context ctx, Uri photoUri) {
		super(ctx);
		this.photoUri = photoUri;
	}
	
	@Override
	protected Cursor buildCursor() {
		MatrixCursor cursor = new MatrixCursor(COLUMN_NAMES, 1);
		try {
			if (ContentResolver.SCHEME_FILE.equals(photoUri.getScheme()) ) {
				readPhotoMetadata(photoUri, cursor);
			}
			else if (ContentResolver.SCHEME_CONTENT.equals(photoUri.getScheme())) {
				queryPhotoMetadata(photoUri, cursor);
			}
			else {
				cursor.close();
				throw new IllegalArgumentException("Scheme "+photoUri.getScheme()+" unsupported");
			}
		}
		catch (Exception e) {
			Log.e("PhotoLoader", "Failed to load metadata for photo: "+photoUri, e);
		}
		return cursor;
	}
	
	private void queryPhotoMetadata(Uri contentUri, MatrixCursor cursor) {

		Uri fileUri = null;
		Location location = null;
		Date date = null;
		String[] columns = {
		        MediaStore.Images.ImageColumns.LATITUDE,
		        MediaStore.Images.ImageColumns.LONGITUDE,
		        MediaStore.Images.ImageColumns.DATE_TAKEN,
		        MediaStore.Images.ImageColumns.DATA
		    };

		ContentResolver contentResolver = getContext().getContentResolver();
		
		Cursor tmpCursor = contentResolver.query(contentUri, columns, null, null, null);
		if (tmpCursor.moveToFirst()) {
			String filePath = tmpCursor.getString(3);
			
			// This can happen in the case the Gallery app integrates 
			// non-local sources, e.g. Picassa.
			if (filePath == null) {
				try {
					fileUri = ImageHelper.downloadImage(contentUri, getContext().getCacheDir(), contentResolver);
					readPhotoMetadata(fileUri, cursor);
				}
				catch (Exception e) {
					Log.e("ImageLoader", "Error downloading photo: "+photoUri, e);
				}
			} else {
				fileUri = Uri.fromFile(new File(filePath));
				String lat = tmpCursor.getString(0);
				String lon = tmpCursor.getString(1);
				String dateTaken = tmpCursor.getString(2);
				if (StringUtils.hasLength(lat)) {
					location = new Location("EXIF");
					location.setLatitude(Double.parseDouble(lat));
					location.setLongitude(Double.parseDouble(lon));
				}
				try {
					date = new Date(Long.parseLong(dateTaken));
				}
				catch (NumberFormatException e) {
					Log.e("RecordSightingActivity", "Invalid date in photo metadata: "+date);
				}
				populateCursor(cursor, fileUri, location, date);
			}
		}
		else {
			Log.d("", "no meatdata!");
		}
		tmpCursor.close();	
	}

	private void populateCursor(MatrixCursor cursor, Uri fileUri, Location location, Date date) {
		
		MatrixCursor.RowBuilder row = cursor.newRow();
		if (date != null) {
			row.add(date.getTime());
		}
		else {
			row.add(null);
		}
		if (location != null) {
			row.add(location.getLatitude());
			row.add(location.getLongitude());
		}
		else {
			row.add(null);
			row.add(null);
		}
		row.add(fileUri.toString());
	}

	
	@SuppressLint("SimpleDateFormat")
	private void readPhotoMetadata(Uri photoUri, MatrixCursor cursor) {
		Location location = null;
		Date date = null;
		try {
			ExifInterface exif = new ExifInterface(photoUri.getPath());
			float[] latlong= new float[2];
			boolean hasLatLong = exif.getLatLong(latlong);
			if (hasLatLong) {
				location = new Location("EXIF");
				location.setLatitude(latlong[0]);
				location.setLongitude(latlong[1]);
			}
			else {
				Log.i("", "No lat long in photo EXIF data");
			}
			DateFormat exifFormat = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
			try {
				String dateString = exif.getAttribute(ExifInterface.TAG_DATETIME);
				if (StringUtils.hasLength(dateString)) {
					date = exifFormat.parse(dateString);
				}
			}
			catch (ParseException e) {
				Log.e("RecordSightingActivity", "Error reading date from EXIF for file: "+photoUri, e);
			}
		}
		catch (IOException e) {
			Log.e("RecordSightingActivity", "Error reading EXIF for file: "+photoUri, e);
		}
		
		populateCursor(cursor, photoUri, location, date);
	}

	

}
