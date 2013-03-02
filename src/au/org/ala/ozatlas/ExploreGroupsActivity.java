package au.org.ala.ozatlas;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.widget.ListView;

import com.google.android.gms.maps.model.LatLng;

public class ExploreGroupsActivity extends Activity implements LoadGroup {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_explore_groups);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		GroupsSearchTask gst = new GroupsSearchTask(this);
		ListView listView = (ListView) findViewById(R.id.groupsList);
		gst.setListView(listView);
		gst.setContext(this);
		String lat = "";
		String lon = "";
		String radius = "";
		if(getIntent() != null && getIntent().getExtras() != null){
			lat = (String) getIntent().getExtras().get("lat");
			lon = (String) getIntent().getExtras().get("lon");
			radius = (String) getIntent().getExtras().get("radius");
		}
		//this is for debug purposes only		
		if(lat == null){
			lat = "-37.1";
			lon = "149.1";
			radius = "10";
		}
		gst.execute(lat,lon,radius);
		setTitle("Area - radius: " + radius + "km, " + lat + ", "+ lon);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.explore_groups, menu);
		return true;
	}

	@Override
	public void load(String name, String lat, String lon, String radius) {
        System.out.println("Starting ExploreGroupActivity activity.....");
    	Intent myIntent = new Intent(this, ExploreGroupActivity.class);
    	myIntent.putExtra("groupName", name);
    	myIntent.putExtra("lat", lat);
    	myIntent.putExtra("lon", lon);
    	myIntent.putExtra("radius", radius);
    	startActivity(myIntent);
	}
}
