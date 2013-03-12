package au.org.ala.ozatlas;

import java.util.List;

import android.app.SearchManager;
import android.content.Context;
import android.location.Address;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

/**
 * An activity that supports geocoding via a search item in the action bar.
 * 
 * Subclasses must use a layout that contains a SupportMapFragment with
 * id = "map".
 *
 */
public abstract class SearchableMapActivity extends SherlockFragmentActivity implements GeocodeCallback {

	protected GoogleMap map;
	
	protected void initialiseMap() {
		if (this.map == null) {
        	SupportMapFragment mf = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map));
            // Check if we were successful in obtaining the map.
            if (mf != null) {
            	this.map = mf.getMap();
            	this.map.setMyLocationEnabled(true);
       	
            }
        }
		// Start off showing australia
		if (this.map != null) {
			this.map.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(-23.7, 133.87)));
		}
	}
	
	@Override
    public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
    	getSupportMenuInflater().inflate(R.menu.map_search_item, menu);
        
    	SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
    	final MenuItem searchItem = (MenuItem) menu.findItem(R.id.menu_search);
        SearchView searchView = (SearchView)searchItem.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false);   
        
        SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
            public boolean onQueryTextChange(String searchText) {
            	return true;
            }

            public boolean onQueryTextSubmit(String query) {
            	SearchableMapActivity.this.geocode(query);
            	searchItem.collapseActionView();
            	return true;
            }
        };
        searchView.setOnQueryTextListener(queryTextListener);
    	
    	return super.onCreateOptionsMenu(menu);
     }
	
	protected void geocode(String str) {
		ReverseGeocodeTask rgt = new ReverseGeocodeTask();
		rgt.setCallback(this);
		rgt.setContext(this);
		rgt.execute(str);	
	}

	@Override
	public void geocodeCallback(List<Address> address) {
		if(!address.isEmpty()){
			LatLng latLng = new LatLng(address.get(0).getLatitude(), address.get(0).getLongitude());
			this.map.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(latLng, 11, 0f, 0f)));
		} else {
			Toast.makeText(this, getResources().getString(R.string.no_location_found), Toast.LENGTH_SHORT).show();
		}
	}	
}
