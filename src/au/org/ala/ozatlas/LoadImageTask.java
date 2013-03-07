package au.org.ala.ozatlas;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

public class LoadImageTask extends AsyncTask<String, Integer, Bitmap> {

	protected ImageView imageView;

	private boolean scaleToWidth;
	private int targetWidth;
	
	public LoadImageTask() {
		scaleToWidth = false;
	}
	
	public LoadImageTask(boolean scaleToWidth) {
		this.scaleToWidth = scaleToWidth;
	}
	
	@Override
	protected Bitmap doInBackground(String... params) {
		Bitmap bitmap = loadBitmap(params[0]);
		if (bitmap != null && scaleToWidth) {
			double scaleFactor = (double)targetWidth/(double)bitmap.getWidth();
			
			bitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, (int)(bitmap.getHeight()*scaleFactor), true);
		}
		return bitmap;
	}

	@Override
	protected void onPostExecute(Bitmap bm) {
		this.imageView.setImageBitmap(bm);
	}

	public static Bitmap loadBitmap(String url) {
	    Bitmap bitmap = null;
	    InputStream in = null;
	    BufferedOutputStream out = null;

	    try {
	        in = new BufferedInputStream(new URL(url).openStream(), 1024);

	        final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
	        out = new BufferedOutputStream(dataStream, 1024);
	        int read = 0;
	        byte[] buff = new byte[1024];
	        while((read = in.read(buff))>0){
	        	out.write(buff, 0, read);
	        }
	        out.flush();

	        final byte[] data = dataStream.toByteArray();
	        BitmapFactory.Options options = new BitmapFactory.Options();
	        bitmap = BitmapFactory.decodeByteArray(data, 0, data.length,options);
	        
	   
	    } catch (Exception e) {
	        e.printStackTrace();
	    } finally {
	    	try{
		        if(in !=null) {in.close(); };
		        if(out !=null) {out.close(); };
	    	} catch (Exception e){
	    		e.printStackTrace();
	    	}
	    }

	    return bitmap;
	}
	
	public void setImageView(ImageView imageView) {
		this.imageView = imageView;
		if (scaleToWidth) {
			targetWidth = imageView.getWidth();
		}
	}
}
