package au.org.ala.ozatlas;

import java.util.List;
import java.util.Map;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ExploreGroupsActivity extends GroupActivity implements LoadGroup {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_explore_groups);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
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
		
		Bundle params = new Bundle();
		params.putString(LAT_KEY, lat);
		params.putString(LON_KEY, lon);
		params.putString(RADIUS_KEY, radius);
		getSupportLoaderManager().initLoader(0, params, this);
		
		setTitle("Area - radius: " + radius + "km, " + lat + ", "+ lon);
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
	
	@Override
	public Loader<List<Map<String, Object>>> onCreateLoader(int arg0, Bundle params) {		
		return new GroupsSearchTask(
				this, params.getString(LAT_KEY), 
				params.getString(LON_KEY), params.getString(RADIUS_KEY));		
	}

	@Override
	public void onLoadFinished(Loader<List<Map<String, Object>>> loader,
			List<Map<String, Object>> results) {
		
		findViewById(R.id.progress).setVisibility(View.GONE);
		
		ListView listView = getListView();
		
		if (results.size() > 0) {
			final GroupsSearchTask groupsLoader = (GroupsSearchTask)loader;
			
			String[] from = {"commonName", "scientificName", "count", "groupName"};
	    	int[] to = {R.id.commonName, R.id.scientificName, R.id.count, R.id.groupName};
	    	SpeciesListSectionAdapter adapter = new SpeciesListSectionAdapter(this, results, R.layout.group_results, from, to);
	        // Setting the adapter to the listView
	        listView.setAdapter(adapter);
	        listView.setOnItemClickListener(new OnItemClickListener(){
			    @Override public void onItemClick(AdapterView<?> listView, View view, int position, long arg3){ 
			    	System.out.println("Position: " + position + ", arg3: " + arg3 + ", view : " + view);
			    	@SuppressWarnings("unchecked")
					Map<String,Object> li =  (Map<String,Object>) listView.getItemAtPosition(position);
			    	String groupName = (String) li.get("commonName");
			    	load(groupName, groupsLoader.lat, groupsLoader.lon, groupsLoader.radius);
			    }
			});		      
		}
		else {
			listView.setAdapter(new ArrayAdapter<String>(
					this, android.R.layout.simple_list_item_1, android.R.id.text1, new String[]{"No results found."}));
			
		}
        listView.setVisibility(View.VISIBLE);
        restoreListPosition();
	}
	
	@Override
	protected ListView getListView() {
		return (ListView) findViewById(R.id.groupsList);
	}
	
}
