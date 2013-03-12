package au.org.ala.ozatlas;

import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;

public class SpeciesSearchFragment extends SherlockListFragment implements LoaderCallbacks<List<Map<String, Object>>>  {

	public static final String RECORD_SIGHTING = "recordSighting";
	public static final String SPECIES_PAGE = "speciesPage";
	
	private static final String SEARCH_TEXT_KEY = "searchText";
	private static final String SEARCH_PERFORMED_KEY = "searchPerformed";
	
	protected Class followupActivityClass = SpeciesPageActivity.class; 
	/** 
	 * Records whether a search has been performed or not so we can
	 * restore the results on config change.
	 */
	private boolean searchPerformed;
	
	@Override
	public void onAttach(Activity parent) {
		super.onAttach(parent);
		//whats the followup action for this activity ?
		Intent intent = parent.getIntent();
		if(intent != null && intent.getExtras() != null){
			String followupActivity = intent.getExtras().getString("followupActivity");
			if(followupActivity != null && RECORD_SIGHTING.equals(followupActivity)){
				this.followupActivityClass = RecordSightingActivity.class;
			}
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setHasOptionsMenu(true);
	}
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		View view = inflater.inflate(R.layout.fragment_species_search, container);
		
		if (savedInstanceState != null) {
			searchPerformed = savedInstanceState.getBoolean(SEARCH_PERFORMED_KEY);
			if (searchPerformed) {
				// Don't need to pass an argument as we want the existing 
				// instance in this case.
				getLoaderManager().initLoader(0, null, this);
			}
		}
		
		final EditText searchText = (EditText) view.findViewById(R.id.speciesSearchInput);
		searchText.setOnKeyListener(new OnKeyListener(){
		    public boolean onKey(View v, int keyCode, KeyEvent event){
		        if (event.getAction() == KeyEvent.ACTION_DOWN){
		            switch (keyCode){
		                case KeyEvent.KEYCODE_DPAD_CENTER:
		                case KeyEvent.KEYCODE_ENTER:
		                	search(searchText.getText().toString());
		                    return true;
		                default:
		                    break;
		            }
		        }
		        return false;
		    }
		});		
		
		final Button goButton = (Button)view.findViewById(R.id.goButton);
		goButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				search(v);	
			}
		});
		return view;
	}
	
	private String getSearchText() {
		EditText editText = (EditText) getView().findViewById(R.id.speciesSearchInput);
		return editText.getText().toString();
	}

	/** Called when the user touches the button */
	public void search(View view) {
		search(getSearchText());
	}
	
	/** Called when the user touches the button */
	public void search(String text) {
		Bundle params = new Bundle();
		params.putString(SEARCH_TEXT_KEY, text);
		getLoaderManager().restartLoader(0, params, this);
	}	
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.species_search, menu);
	}
	
	@Override
	public void onSaveInstanceState(Bundle out) {
		super.onSaveInstanceState(out);
		out.putBoolean(SEARCH_PERFORMED_KEY, searchPerformed);
	}

	@Override
	public Loader<List<Map<String, Object>>> onCreateLoader(int arg0, Bundle params) {
		String searchText = params.getString(SEARCH_TEXT_KEY);
		searchPerformed = true;
		showProgress(true);
		
		return new SpeciesSearchTask(getActivity(), searchText);
	}

	@Override
	public void onLoadFinished(Loader<List<Map<String, Object>>> loader,
			List<Map<String, Object>> results) {
		String[] from = {"scientificName", "commonName", "rankID"};
    	int[] to = {R.id.scientificName, R.id.commonName};
    	SpeciesListAdapter adapter = new SpeciesListAdapter(getActivity(), results, R.layout.listview_thumbnails, from, to);
        // Setting the adapter to the listView
        getListView().setAdapter(adapter);	
        getListView().setOnItemClickListener(new OnItemClickListener(){
		    @Override public void onItemClick(AdapterView<?> listView, View view, int position, long arg3){ 
		    	Intent myIntent = new Intent(getActivity(), followupActivityClass);
				Map<String,Object> li =  (Map<String,Object>) listView.getItemAtPosition(position);
		    	myIntent.putExtra("guid", (String) li.get("guid"));
		    	myIntent.putExtra("scientificName", (String) li.get("scientificName"));
		    	myIntent.putExtra("commonName", (String) li.get("commonName"));
		    	myIntent.putExtra("speciesImageUrl", (String) li.get("smallImageUrl"));
		    	getActivity().startActivity(myIntent);
		    }
		});	        
		showProgress(false);
		if (results.isEmpty()) {
			showNoResultsMessage();
		}
	}

	private void showNoResultsMessage() {
		String searchText = getSearchText();
		String message;
		if (searchText == null || searchText.length() == 0) {
			message = getResources().getString(R.string.no_search_results_no_query_message);
		}
		else {
		    message = String.format(getResources().getString(R.string.no_search_results_message), searchText);
		}
		Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onLoaderReset(Loader<List<Map<String, Object>>> arg0) {
		// Don't need to do anything here.
		
	}
	
	
	private void showProgress(boolean progress) {
		View progressBar = getView().findViewById(R.id.searchProgress);
		
		if (progress) {
			progressBar.setVisibility(View.VISIBLE);
			getListView().setVisibility(View.GONE);
		}
		else {
			progressBar.setVisibility(View.GONE);
			getListView().setVisibility(View.VISIBLE);
		}
	}
	
	
}
