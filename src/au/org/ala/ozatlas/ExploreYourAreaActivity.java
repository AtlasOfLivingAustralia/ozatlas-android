package au.org.ala.ozatlas;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class ExploreYourAreaActivity extends Activity {
	
	private GoogleMap map;
	private Marker marker;

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
	        	myIntent.putExtra("latlng", position);
	        	ExploreYourAreaActivity.this.startActivity(myIntent);
	        }
	    });	        
        
        if (this.map == null) {
        	MapFragment mf = ((MapFragment) getFragmentManager().findFragmentById(R.id.map));
            // Check if we were successful in obtaining the map.
            if (mf != null) {
            	this.map = mf.getMap();
                // The Map is verified. It is now safe to manipulate the map.
            	this.map.getUiSettings().setMyLocationButtonEnabled(true);
            	this.map.setMyLocationEnabled(true);
            	//23.7000° S, 133.8700° E
            	this.map.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(-23.7, 133.87)));
//            	this.map.animateCamera(CameraUpdateFactory.zoomBy(12f, (new LatLng(-23.7, 133.87)).));
                System.out.println("Location enabled:" + this.map.getUiSettings().isMyLocationButtonEnabled());
                
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(getBitmap()));
                markerOptions.anchor(0.5f,0.5f);
                markerOptions.draggable(true);
                
                this.marker = this.map.addMarker(markerOptions
	                .position(new LatLng(0, 0))
	                .title("Current position"));
                
                this.map.setOnMarkerDragListener(new OnMarkerDragListener(){

					@Override
					public void onMarkerDrag(Marker marker) {}

					@Override
					public void onMarkerDragEnd(Marker marker) {
						ExploreYourAreaActivity.this.map.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
					}

					@Override
					public void onMarkerDragStart(Marker marker) {}
                });
                
                this.map.setOnCameraChangeListener(new OnCameraChangeListener(){

					@Override
					public void onCameraChange(CameraPosition position) {
						ExploreYourAreaActivity.this.marker.setPosition(position.target);
					}
                });
            }
        }
    }
    
 // 1. some variables:

//    private static final double EARTH_RADIUS = 6378100.0;
//    private int offset;
//
//// 2. convert meters to pixels between 2 points in current zoom:
//
//    private int convertMetersToPixels(double lat, double lng, double radiusInMeters) {
//    	if(lat == 0) lat = 1;
//    	if(lng == 0) lng = 1;
//    	
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

    private Bitmap getBitmap() {
        Paint paint1 = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint1.setColor(0x110000FF);
        paint1.setStyle(Style.FILL);

        // stroke color
        Paint paint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint2.setColor(0xFF0000FF);
        paint2.setStyle(Style.STROKE);

        int radius = 350;

        // create empty bitmap
        Bitmap b = Bitmap.createBitmap(radius * 2, radius * 2, Config.ARGB_8888);
        Canvas c = new Canvas(b);
        c.drawCircle(radius, radius, radius, paint1);
        c.drawCircle(radius, radius, radius, paint2);
        return b;
    }
}