package au.org.ala.ozatlas;

import com.google.android.gms.maps.model.LatLng;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.widget.ListView;

public class ExploreGroupActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_explore_group);
		String groupName = (String) getIntent().getExtras().get("groupName");
		System.out.println("Loading group: " + groupName);
		GroupSpeciesTask gst = new GroupSpeciesTask();
		gst.setListView((ListView) findViewById(android.R.id.list));
		gst.setContext(this);

		String lat = "-37.1";
		String lon = "149.1";	
		String radius = "100";
		String placeName = "";
		
		if(getIntent() != null && getIntent().getExtras() != null){
			lat = (String) getIntent().getExtras().get("lat");
			lon = (String) getIntent().getExtras().get("lon");
			placeName = (String) getIntent().getExtras().get("placeName");
			radius = (String) getIntent().getExtras().get("radius");
		}
		
		if(lat == null){
			radius = "100";
			lat = "-37.1";
			lon = "149.1";	
			placeName = "DEFAULT PLACE";
			//this is for debug purposes only
		}		
		
		gst.execute(groupName, lat, lon, radius);
		if(placeName != null)
			setTitle(groupName +" in the area of : " + placeName);
		else
			setTitle(groupName +" in the area of : " + lat + ", " + lon);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.explore_group, menu);
		return true;
	}
}
