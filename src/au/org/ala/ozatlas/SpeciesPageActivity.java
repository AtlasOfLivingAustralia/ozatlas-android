package au.org.ala.ozatlas;

import java.util.Map;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;

import com.fedorvlasov.lazylist.ImageLoader;

/**
 * Load a species page
 * 
 * @author davemartin
 */
public class SpeciesPageActivity extends Activity implements RenderPage{

	@Override
	public void render(Map<String, Object> data) {
    	ImageLoader imageLoader = new ImageLoader(this);
    	//add species map
    	ImageView mapView = (ImageView) findViewById(R.id.speciesMap);
		imageLoader.DisplayImage((String) data.get("speciesMap"), mapView);
		
		ImageView speciesImage = (ImageView) findViewById(R.id.speciesImage);
		imageLoader.DisplayImage((String) data.get("speciesImage"), speciesImage);
		
		TextView scientificName = (TextView) findViewById(R.id.scientificName);
		scientificName.setText((String) data.get("scientificName"));
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_species_page);
		SpeciesPageTask spt = new SpeciesPageTask();
		spt.setRenderPage(this);
		spt.execute("urn:lsid:biodiversity.org.au:afd.taxon:31a9b8b8-4e8f-4343-a15f-2ed24e0bf1ae");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.species_page, menu);
		return true;
	}
}
