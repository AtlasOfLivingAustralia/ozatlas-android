package au.org.ala.ozatlas;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;


public class ImageHelper {

	private static final String TMP_FILE_PREFIX = "ozatlas_";

	private static File cacheFolder;
	
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

	    return createFile(mediaStorageDir, "", type);
	}
	
	private static File createFile(File path, String prefix, int type) {
		// Create a media file name
	    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmssSSS").format(new Date());
	    File mediaFile;
	    if (type == MEDIA_TYPE_IMAGE){
	        mediaFile = new File(path + File.separator +
	        prefix+"IMG_"+ timeStamp + ".jpg");
	    } else if(type == MEDIA_TYPE_VIDEO) {
	        mediaFile = new File(path + File.separator +
	        prefix+"VID_"+ timeStamp + ".mp4");
	    } else {
	        return null;
	    }
	    return mediaFile;
	
	}
	
	public static Bitmap bitmapFromFile(Uri path, int targetW, int targetH) {
		
			
		BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		bmOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path.getPath(), bmOptions);
		int photoW = bmOptions.outWidth;
		int photoH = bmOptions.outHeight;
	
		// Determine how much to scale down the image
		int scaleFactor = (int)Math.min((double)photoW / targetW, (double)photoH / targetH);
	
		// Decode the image file into a Bitmap sized to fill the View
		bmOptions.inJustDecodeBounds = false;
		bmOptions.inSampleSize = scaleFactor;
		bmOptions.inPurgeable = true;
	
		return BitmapFactory.decodeFile(path.getPath(), bmOptions);
		
	}
	
	public static Uri downloadImage(Uri photoUri, File cacheDir, ContentResolver contentResolver) throws FileNotFoundException, IOException {
		
		InputStream in = contentResolver.openInputStream(photoUri);
		
		if (cacheFolder == null) {
			File imageTmp = new File(cacheDir, "attached_photos");
			if (!imageTmp.mkdir()) {
				imageTmp = cacheDir;
			}
			cacheFolder = imageTmp;
		}
		
		File temp = createFile(cacheFolder, TMP_FILE_PREFIX, ImageHelper.MEDIA_TYPE_IMAGE);
		FileOutputStream tmpOut = new FileOutputStream(temp);
		byte[] buffer = new byte[8012];
		int count = in.read(buffer);
		while (count > 0) {
			tmpOut.write(buffer, 0, count);
			count = in.read(buffer);
		}
		
		tmpOut.close();
		in.close();
		return Uri.fromFile(temp);
	}

	public static void deleteCachedFiles() {
		if (cacheFolder == null) {
			return;
		}
		File[] all = cacheFolder.listFiles();
		for (File file : all) {
			if (!file.isDirectory() && file.getName().startsWith(TMP_FILE_PREFIX)) {
				try {
					file.delete();
				}
				catch (Exception e) {
					Log.e("ImageHelper", "Failed to delete file from cache: "+file, e);
				}
			}
		}
	}
	
}
