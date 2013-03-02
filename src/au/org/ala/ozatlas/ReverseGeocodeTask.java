package au.org.ala.ozatlas;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;

public class ReverseGeocodeTask extends AsyncTask<String, Integer, List<Address>> {

	protected Context context;
	protected GeocodeCallback callback;
	
	@Override
	protected void onPostExecute(List<Address> result) {
		super.onPostExecute(result);
		this.callback.geocodeCallback(result);
	}

	@Override
	protected List<Address> doInBackground(String... params) {
		
		String searchString = params[0];
		Geocoder geocoder = new Geocoder(this.context);
		try {
			return geocoder.getFromLocationName(searchString + ", Australia", 1);
		} catch(Exception e){
			e.printStackTrace();
		}
		return new ArrayList<Address>();
	}

	public void setContext(Context context) {
		this.context = context;
	}

	public void setCallback(GeocodeCallback callback) {
		this.callback = callback;
	}	
}
