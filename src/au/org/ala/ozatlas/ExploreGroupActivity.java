package au.org.ala.ozatlas;

import android.os.Bundle;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;

public class ExploreGroupActivity extends SherlockActivity {

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
		getSupportMenuInflater().inflate(R.menu.explore_group, menu);
		return true;
	}
}
