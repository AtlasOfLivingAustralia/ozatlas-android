package au.org.ala.ozatlas;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;


public class ImageHelper {

	
	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;

	/** Create a file Uri for saving an image or video */
	public static Uri getOutputMediaFileUri(int type){
	      return Uri.fromFile(getOutputMediaFile(type));
	}
	
	public static boolean canWriteToExternalStorage() {
		String state = Environment.getExternalStorageState();
		 return Environment.MEDIA_MOUNTED.equals(state);
	}
	
	/** Create a File for saving an image or video */
	@SuppressLint("SimpleDateFormat")
	@TargetApi(8)
	private static File getOutputMediaFile(int type){
		
		if (!canWriteToExternalStorage()) {
			throw new RuntimeException("External storage is not writable!");
		}
		
		File mediaStorageDir = null;
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
			mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
	              Environment.DIRECTORY_PICTURES), "FieldData");
		}
		else {
			mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "FieldData");
		}
	    // This location works best if you want the created images to be shared
	    // between applications and persist after your app has been uninstalled.

	    // Create the storage directory if it does not exist
	    if (! mediaStorageDir.exists()){
	        if (! mediaStorageDir.mkdirs()){
	            Log.e("StorageManager", "Failed to create directory to store photos.");
	            return null;
	        }
	    }

	    // Create a media file name
	    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	    File mediaFile;
	    if (type == MEDIA_TYPE_IMAGE){
	        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
	        "IMG_"+ timeStamp + ".jpg");
	    } else if(type == MEDIA_TYPE_VIDEO) {
	        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
	        "VID_"+ timeStamp + ".mp4");
	    } else {
	        return null;
	    }

	    return mediaFile;
	}
	
	public static Bitmap bitmapFromFile(Uri path, int targetW, int targetH) {
		
			
		BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		bmOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path.getEncodedPath(), bmOptions);
		int photoW = bmOptions.outWidth;
		int photoH = bmOptions.outHeight;
	
		// Determine how much to scale down the image
		int scaleFactor = Math.min(photoW / targetW, photoH / targetH);
	
		// Decode the image file into a Bitmap sized to fill the View
		bmOptions.inJustDecodeBounds = false;
		bmOptions.inSampleSize = scaleFactor;
		bmOptions.inPurgeable = true;
	
		return BitmapFactory.decodeFile(path.getEncodedPath(), bmOptions);
		
	}
	
	
}
