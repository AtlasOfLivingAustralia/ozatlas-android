package au.org.ala.ozatlas;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import au.org.ala.mobile.ozatlas.R;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;

public class ExploreYourAreaActivity extends SearchableMapActivity {
	
	private Marker marker;
	private int radiusInPixels = -1;
	private int radiusInMeters = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore_your_area);        
		final Button exploreYourAreaButton = (Button) findViewById(R.id.exploreGroups); 
		exploreYourAreaButton.setOnClickListener(new View.OnClickListener() {
	        public void onClick(View view) {                 
	            System.out.println("Starting ExploreYourAreaActivity activity.....");
	            Intent myIntent = new Intent(ExploreYourAreaActivity.this, ExploreGroupsActivity.class);
	        	
	        	//get the latitude/longitude
	        	LatLng position = ExploreYourAreaActivity.this.map.getCameraPosition().target;
	        	myIntent.putExtra("lat", Double.toString(position.latitude));
	        	myIntent.putExtra("lon", Double.toString(position.longitude));
	        	myIntent.putExtra("radius", convertToKm(radiusInMeters));
	        	//get the place name
	        	myIntent.putExtra("placeName", "My Default Place");
	        	
	        	ExploreYourAreaActivity.this.startActivity(myIntent);
	        }

			private String convertToKm(int radiusInMeters) {
				return String.valueOf(Math.floor(((double) radiusInMeters) * 10)/10000);
			}
	    });	   
		
//		final EditText searchText = (EditText) findViewById(R.id.locationSearchInput);
//		searchText.setOnKeyListener(new OnKeyListener()
//		{
//		    public boolean onKey(View v, int keyCode, KeyEvent event)
//		    {
//		        if (event.getAction() == KeyEvent.ACTION_DOWN)
//		        {
//		            switch (keyCode)
//		            {
//		                case KeyEvent.KEYCODE_DPAD_CENTER:
//		                case KeyEvent.KEYCODE_ENTER:
//		                	ExploreYourAreaActivity.this.geocode(searchText.getText().toString());
//		                    return true;
//		                default:
//		                    break;
//		            }
//		        }
//		        return false;
//		    }
//		});			
        
		initialiseMap();
    }
    
    @Override
    protected void initialiseMap() {
    	super.initialiseMap();
    	 MarkerOptions markerOptions = new MarkerOptions();
         


         Display display = getWindowManager().getDefaultDisplay();
         int width = display.getWidth();
         int height = display.getHeight();
         
         //min screen dimension
//         double maxRadiusInDeg = latitudeRange > longitudeRange ? longitudeRange / 2 : latitudeRange /2;   
//         int maxRadiusInMeters = convertDecimalDegreesToMeters(maxRadiusInDeg); 
         //int radiusInPixels = convertMetersToPixels(location.getLatitude(), location.getLongitude(), maxRadiusInMeters);

         this.radiusInPixels = height > width ? (width * 75)/200 : (height * 75)/200;
         
         //convert to meters...
         
         
         
//         int radiusInPixels = convertMetersToPixels(-23.7, 133.87, maxRadiusInMeters);
         //convert decimal degrees to meters
         
         //get the width & height of the map in pixels
         
         //get the width & height of the map in degs lat/long
         
         
//         int radius = 350; //350 units of what ?? must be in pixels ....                
         
         markerOptions.icon(BitmapDescriptorFactory.fromBitmap(getBitmap(radiusInPixels)));
         markerOptions.anchor(0.5f,0.5f);
         markerOptions.draggable(true);
         
         this.marker = this.map.addMarker(markerOptions
             .position(new LatLng(0, 0))
             .title("Current position"));
         
//         this.map.setOnCameraChangeListener(new OnCameraChangeListener() {
//				@Override
//				public void onCameraChange(CameraPosition position) {
//					updateTheRadius();
//				}
//			});
         
         this.map.setOnMarkerDragListener(new OnMarkerDragListener(){

				@Override
				public void onMarkerDrag(Marker marker) {
					ExploreYourAreaActivity.this.updateTheRadius();
				}

				@Override
				public void onMarkerDragEnd(Marker marker) {
					ExploreYourAreaActivity.this.map.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
					//calculate the radius
	                ExploreYourAreaActivity.this.updateTheRadius();
				}

				@Override
				public void onMarkerDragStart(Marker marker) {
					ExploreYourAreaActivity.this.updateTheRadius();
				}
         });
         
         this.map.setOnCameraChangeListener(new OnCameraChangeListener(){
         	
				@Override
				public void onCameraChange(CameraPosition position) {
					ExploreYourAreaActivity.this.marker.setPosition(position.target);
					ExploreYourAreaActivity.this.updateTheRadius();
				}
         });
    }
    
    boolean firstFixFired = false;
    
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

        String context = Context.LOCATION_SERVICE;
        LocationManager locationManager = (LocationManager) getSystemService(context);
        Location location = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        if (location != null) {
        	this.map.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(),location.getLongitude())));
        	LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
        	ExploreYourAreaActivity.this.firstFixFired = true;
        	ExploreYourAreaActivity.this.map.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(latLng, 11, 0f, 0f)));
        }
	}

	@TargetApi(8)
	private void updateTheRadius() {
		VisibleRegion visibleRegion = ExploreYourAreaActivity.this.map.getProjection().getVisibleRegion();
        
        double longitudeRange = visibleRegion.farLeft.longitude - visibleRegion.farRight.longitude;
        if(longitudeRange<0){ 
        	longitudeRange = longitudeRange * -1;
        }
        
        //screen width in meters
        double lonRangeInMeters = convertDecimalDegreesToMeters(longitudeRange);
        
        //get window width in decimal degrees
        Display display = getWindowManager().getDefaultDisplay();
        
        int rotation;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
        	rotation = display.getRotation();
        }
        else {
        	rotation = display.getOrientation();
        }
        double width = - 1;
        if(rotation == 0 || rotation == 180){
        	width = (double) display.getWidth();
        } else {
        	width = (double) display.getHeight();
        }

        double metersPerPixel = lonRangeInMeters/width;
        
        double radiusInMeters = metersPerPixel * radiusInPixels;
        
        //get window width in meters
        //get window width in pixels
        	
        TextView radiusView = (TextView) ExploreYourAreaActivity.this.findViewById(R.id.radius);
        radiusView.setText(formatTheRadius(radiusInMeters));
        radiusView.setShadowLayer(20, 0, 0, Color.WHITE);
        this.radiusInMeters = (int) radiusInMeters;
	}    
    
	private String formatTheRadius(double radiusInMeters){
		if(radiusInMeters < 1000){
			return ((int) radiusInMeters) +"m";
		} else {
			int radiusInKm = (int) radiusInMeters/1000;
			return String.format("%,d", radiusInKm) + "km";
		}
	}
	
// 1. some variables:

//    private static final double EARTH_RADIUS = 6378100.0;
//    private int offset;
////
////// 2. convert meters to pixels between 2 points in current zoom:
////
//    private int convertMetersToPixels(double lat, double lng, double radiusInMeters) {
//    	if(lat == 0) lat = 1;
//    	if(lng == 0) lng = 1;
//    	
//         double lat1 = radiusInMeters / EARTH_RADIUS;
//         double lng1 = radiusInMeters / (EARTH_RADIUS * Math.cos((Math.PI * lat / 180)));
//
//         double lat2 = lat + lat1 * 180 / Math.PI;
//         double lng2 = lng + lng1 * 180 / Math.PI; 
//
//         Point p1 = this.map.getProjection().toScreenLocation(new LatLng(lat, lng));
//         Point p2 = this.map.getProjection().toScreenLocation(new LatLng(lat2, lng2));
//
//         return Math.abs(p1.x - p2.x);
//    }

    /**
     * WARNING: This conversion isnt accurate..
     * @return
     */
    public Double convertMetresToDecimalDegrees(Float metres){
        //0.01 degrees is approximately 1110 metres
        //0.00001 1.11 m
        return ( metres / 1.11 ) * 0.00001;
    }   
    
    /**
     * WARNING: This conversion isnt accurate..
     * @return
     */
    public double convertDecimalDegreesToMeters(double degrees){
        //0.01 degrees is approximately 1110 metres
        //0.00001 1.11 m
        return(degrees * 111000d);
    }       
    
    private Bitmap getBitmap(int radiusInPixels) {
        Paint paint1 = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint1.setColor(0x110000FF);
        paint1.setStyle(Style.FILL);

        // stroke color
        Paint paint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint2.setColor(0xFF0000FF);
        paint2.setStyle(Style.STROKE);

        // create empty bitmap
        Bitmap b = Bitmap.createBitmap(radiusInPixels * 2, radiusInPixels * 2, Config.ARGB_8888);
        Canvas c = new Canvas(b);
        c.drawCircle(radiusInPixels, radiusInPixels, radiusInPixels, paint1);
        c.drawCircle(radiusInPixels, radiusInPixels, radiusInPixels, paint2);
        return b;
    }

}