package au.org.ala.ozatlas;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.EditText;
import android.widget.ListView;

public class SpeciesSearchActivity extends ListActivity  {

	public static final String RECORD_SIGHTING = "recordSighting";
	public static final String SPECIES_PAGE = "speciesPage";
	
	protected Class followupActivityClass = SpeciesPageActivity.class; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_species_search);
		
		//whats the followup action for this activity ?
		Intent intent = getIntent();
		if(intent != null && intent.getExtras() != null){
			String followupActivity = intent.getExtras().getString("followupActivity");
			if(followupActivity != null && RECORD_SIGHTING.equals(followupActivity)){
				this.followupActivityClass = RecordSightingActivity.class;
			}
		}
		
		final EditText searchText = (EditText) findViewById(R.id.speciesSearchInput);
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
	}

	/** Called when the user touches the button */
	public void search(View view) {
		EditText editText = (EditText) findViewById(R.id.speciesSearchInput);
		System.out.println(editText.getText().toString());
		search(editText.getText().toString());
	}
	
	/** Called when the user touches the button */
	public void search(String text) {
		SpeciesSearchTask sst = new SpeciesSearchTask();
		sst.setListView((ListView) findViewById(android.R.id.list));
		sst.setContext(this);
		sst.setFollowupActivity(this.followupActivityClass);
		sst.execute(text);
	}	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.species_search, menu);
		return true;
	}
}
