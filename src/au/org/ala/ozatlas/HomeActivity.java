
package au.org.ala.ozatlas;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.actionbarsherlock.app.SherlockActivity;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class HomeActivity extends SherlockActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle("OzAtlas - Atlas of Living Australia");
		setContentView(R.layout.activity_home);
		final Button exploreYourAreaButton = (Button) findViewById(R.id.exploreYourArea); 
		exploreYourAreaButton.setOnClickListener(new View.OnClickListener() {
	        public void onClick(View view) {                 
	            System.out.println("Starting ExploreYourAreaActivity activity.....");
	        	Intent myIntent = new Intent(HomeActivity.this, ExploreYourAreaActivity.class);
	        	HomeActivity.this.startActivity(myIntent);
	        }
	    });	
	    
		final Button speciesSearchButton = (Button) findViewById(R.id.speciesSearch); 
		speciesSearchButton.setOnClickListener(new View.OnClickListener() {
	        public void onClick(View view) {                 
	            System.out.println("Starting Species search activity.....");
	        	Intent myIntent = new Intent(HomeActivity.this, SpeciesSearchActivity.class);
	        	myIntent.putExtra("followupActivity", SpeciesSearchActivity.SPECIES_PAGE);
	        	HomeActivity.this.startActivity(myIntent);
	        }
	    });	
		
		final Button recordSightingButton = (Button) findViewById(R.id.recordSighting); 
		recordSightingButton.setOnClickListener(new View.OnClickListener() {
	        public void onClick(View view) {                 
	            System.out.println("Starting Species record sighting activity.....");
	        	Intent myIntent = new Intent(HomeActivity.this, SpeciesSearchActivity.class);
	        	myIntent.putExtra("followupActivity", SpeciesSearchActivity.RECORD_SIGHTING);
	        	HomeActivity.this.startActivity(myIntent);
	        }
	    });			
	}
}
