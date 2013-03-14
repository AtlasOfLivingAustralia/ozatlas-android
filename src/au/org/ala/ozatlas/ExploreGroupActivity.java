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
import au.org.ala.mobile.ozatlas.R;

public class ExploreGroupActivity extends GroupActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_explore_group);
		String groupName = (String) getIntent().getExtras().get("groupName");
		
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
		Bundle params = new Bundle();
		params.putString(GROUP_KEY, groupName);
		params.putString(LAT_KEY, lat);
		params.putString(LON_KEY, lon);
		params.putString(RADIUS_KEY, radius);
		getSupportLoaderManager().initLoader(0, params, this);
		if(placeName != null)
			setTitle(groupName +" in the area of : " + placeName);
		else
			setTitle(groupName +" in the area of : " + lat + ", " + lon);
	}
	
	@Override
	public Loader<List<Map<String, Object>>> onCreateLoader(int arg0, Bundle params) {		
		return new GroupSpeciesTask(
				this, params.getString(GROUP_KEY), params.getString(LAT_KEY), 
				params.getString(LON_KEY), params.getString(RADIUS_KEY));		
	}

	@Override
	public void onLoadFinished(Loader<List<Map<String, Object>>> loader,
			List<Map<String, Object>> results) {
		
		findViewById(R.id.progress).setVisibility(View.GONE);
		
		ListView listView = getListView();
	    if (results.size() > 0) {	
			String[] from = {"guid", "commonName", "scientificName",  "count"};
	    	int[] to = {R.id.guid, R.id.commonName, R.id.scientificName, R.id.count};
	    	SpeciesListAdapter adapter = new SpeciesListAdapter(this, results, R.layout.species_results, from, to);
	        // Setting the adapter to the listView
	        listView.setAdapter(adapter);
	        listView.setOnItemClickListener(new OnItemClickListener(){
			    @Override public void onItemClick(AdapterView<?> listView, View view, int position, long arg3){ 
			    	Intent myIntent = new Intent(ExploreGroupActivity.this, SpeciesPageActivity.class);
					@SuppressWarnings("unchecked")
					Map<String,Object> li =  (Map<String,Object>) listView.getItemAtPosition(position);
			    	String guid = (String) li.get("guid");		    	
			    	myIntent.putExtra("guid", guid);
			    	startActivity(myIntent);
			    }
			});		
		}
		else {
			listView.setAdapter(new ArrayAdapter<String>(
					this, android.R.layout.simple_list_item_1, android.R.id.text1, new String[]{"No results found."}));
		}
        listView.setVisibility(View.VISIBLE);
	}
	
	@Override
	protected ListView getListView() {
		return (ListView) findViewById(android.R.id.list);
	}
}
