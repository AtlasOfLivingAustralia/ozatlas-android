package au.org.ala.ozatlas;

import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
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
		List<ImageDTO> speciesImages = (List<ImageDTO>) data.get("speciesImages");

		if(speciesImages != null && !speciesImages.isEmpty()){
			
			ImageDTO image = speciesImages.get(0); 
			LoadImageTask lit = new LoadImageTask();
			lit.setImageView(speciesImage);
			lit.execute(image.getLargeImageUrl());
			//add metadata
			if(image.creator != null)
				((TextView)findViewById(R.id.speciesImageCreator)).setText("Image by: " + image.creator);
			if(image.infoSourceName != null)
				((TextView)findViewById(R.id.speciesImageSource)).setText("Source: " + image.infoSourceName);
		} else {
			speciesImage.setVisibility(ImageView.GONE);
			findViewById(R.id.speciesImageCreator).setVisibility(TextView.GONE);
			findViewById(R.id.speciesImageSource).setVisibility(TextView.GONE);
		}

		ImageView speciesMap = (ImageView) findViewById(R.id.speciesMap);
		String speciesMapUrl = (String) data.get("speciesMap");
		LoadImageTask lit2 = new LoadImageTask();
		lit2.setImageView(speciesMap);
		lit2.execute(speciesMapUrl);		
		
		TextView scientificName = (TextView) findViewById(R.id.scientificName);
		scientificName.setText((String) data.get("scientificName"));
		
		if(data.get("rankID") != null){
			Integer rankID = (Integer) data.get("rankID");
			if(rankID>6000){
				scientificName.setTypeface(null, Typeface.ITALIC);
			}
		}
		
		TextView authorship = (TextView) findViewById(R.id.authorship);
		authorship.setText((String) data.get("authorship"));		
		
		TextView commonName = (TextView) findViewById(R.id.commonName);
		if(data.get("commonName") != null){
			commonName.setText((String) data.get("commonName"));
		} else {
			commonName.setVisibility(TextView.GONE);
		}
		
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
