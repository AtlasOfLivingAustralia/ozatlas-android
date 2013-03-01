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

	@Override
	protected Bitmap doInBackground(String... params) {
		return loadBitmap(params[0]);
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
	        //options.inSampleSize = 1;

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
	}
}
