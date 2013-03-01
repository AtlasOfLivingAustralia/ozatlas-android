package au.org.ala.ozatlas;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.EditText;
import android.widget.ListView;

public class SpeciesSearchActivity extends ListActivity  {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_species_search);
		final EditText searchText = (EditText) findViewById(R.id.speciesSearchInput);
		searchText.setOnKeyListener(new OnKeyListener()
		{
		    public boolean onKey(View v, int keyCode, KeyEvent event)
		    {
		        if (event.getAction() == KeyEvent.ACTION_DOWN)
		        {
		            switch (keyCode)
		            {
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
		sst.execute(text);
	}	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.species_search, menu);
		return true;
	}
}
