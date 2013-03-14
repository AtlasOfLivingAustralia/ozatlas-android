package au.org.ala.ozatlas;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import au.org.ala.mobile.ozatlas.R;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class RecordLocationActivity extends SearchableMapActivity {
	public static final String LOCATION_KEY = "location";
	
	private Marker marker;
	private Location location;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_location);  
        
        Button doneButton = (Button)findViewById(R.id.done_button);
		doneButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				Intent result = new Intent();
				result.putExtra(LOCATION_KEY, location);
				setResult(RESULT_OK, result);
				finish();
			}
		});
	
		if (savedInstanceState != null) {
			location = savedInstanceState.getParcelable(LOCATION_KEY);
		}
    }

	private void setMarkerPosition(Location newLocation) {
    	LatLng latlng = new LatLng(newLocation.getLatitude(), newLocation.getLongitude());
    	if (marker == null) {
    		MarkerOptions markerOptions = new MarkerOptions()
    		.position(latlng)
    		.draggable(true)
    		.title("Long press and drag to move the marker");
    		this.marker = this.map.addMarker(markerOptions);
    		this.map.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(latlng, 11, 0, 0)));
    		
    		map.setOnMarkerDragListener(new OnMarkerDragListener() {
				
				@Override
				public void onMarkerDragStart(Marker arg0) {}
				
				@Override
				public void onMarkerDragEnd(Marker arg0) {
					location = new Location("User selection");
					location.setLatitude(arg0.getPosition().latitude);
					location.setLongitude(arg0.getPosition().longitude);
					map.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));
					
				}
				
				@Override
				public void onMarkerDrag(Marker arg0) {}
			});
        }
   
    	else {
    		marker.setPosition(latlng);
    	}
    	
    }
	
	
	@Override
	public void onSaveInstanceState(Bundle out) {
		super.onSaveInstanceState(out);
		out.putParcelable(LOCATION_KEY, location);
	}
     
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		initialiseMap();
		Location parcelLocation = (Location)getIntent().getParcelableExtra(LOCATION_KEY);
		if (location == null) {
			if (parcelLocation != null) {
				setLocation(parcelLocation);
			}
		
			else {
				LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
				location = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
				setLocation(location);
			}
		}
		// We are restoring from saved state.
		else {
			setMarkerPosition(location);
		}
	}
	
	private void setLocation(Location location) {
		this.location = location;
		setMarkerPosition(location);
	}

	@Override
	public void geocodeCallback(List<Address> address) {
		super.geocodeCallback(address);
		if (address.size() > 0) {
			Location loc = new Location("Geocoded");
			loc.setLatitude(address.get(0).getLatitude());
			loc.setLongitude(address.get(0).getLongitude());
			setLocation(loc);
		}
		
	}

}