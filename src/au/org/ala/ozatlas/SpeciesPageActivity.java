package au.org.ala.ozatlas;

import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Load a species page
 * 
 * @author davemartin
 */
public class SpeciesPageActivity extends Activity implements RenderPage{

	@Override
	public void render(Map<String, Object> data) {
		
		ImageView speciesImage = (ImageView) findViewById(R.id.speciesImage);
		String speciesImageUrl = (String) data.get("speciesImage");

		ImageView speciesMap = (ImageView) findViewById(R.id.speciesMap);
		String speciesMapUrl = (String) data.get("speciesMap");
		
		
		LoadImageTask lit = new LoadImageTask();
		lit.setImageView(speciesImage);
		lit.execute(speciesImageUrl);

		LoadImageTask lit2 = new LoadImageTask();
		lit2.setImageView(speciesMap);
		lit2.execute(speciesMapUrl);		
		
		TextView scientificName = (TextView) findViewById(R.id.scientificName);
		scientificName.setText((String) data.get("scientificName"));
		
		TextView authorship = (TextView) findViewById(R.id.authorship);
		authorship.setText((String) data.get("authorship"));		
		
		TextView commonName = (TextView) findViewById(R.id.commonName);
		commonName.setText((String) data.get("commonName"));
		
		setTitle((String) data.get("scientificName"));
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_species_page);
		SpeciesPageTask spt = new SpeciesPageTask();
		spt.setRenderPage(this);
		Intent intent = getIntent();
		if(intent != null && intent.getExtras() != null){
			spt.execute((String) intent.getExtras().get("guid"));
		} else {
			spt.execute("urn:lsid:biodiversity.org.au:afd.taxon:31a9b8b8-4e8f-4343-a15f-2ed24e0bf1ae");
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.species_page, menu);
		return true;
	}
}
