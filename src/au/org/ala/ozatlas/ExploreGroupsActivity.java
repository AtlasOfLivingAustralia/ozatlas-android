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
		
		LatLng latlng = null;
		if(getIntent() != null && getIntent().getExtras() != null)
			latlng = (LatLng) getIntent().getExtras().get("latlng");
		if(latlng == null){
			//this is for debug purposes only
			latlng = new LatLng(-37.1,149.1);
		}
		gst.execute(latlng.latitude + "", latlng.longitude + "", "10");		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.explore_groups, menu);
		return true;
	}

	@Override
	public void load(String name) {
        System.out.println("Starting ExploreGroupActivity activity.....");
    	Intent myIntent = new Intent(this, ExploreGroupActivity.class);
    	myIntent.putExtra("groupName", name);
    	startActivity(myIntent);
	}
}
