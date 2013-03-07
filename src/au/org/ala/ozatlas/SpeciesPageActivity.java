package au.org.ala.ozatlas;

import java.util.List;
import java.util.Map;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;

/**
 * Load a species page
 * 
 * @author davemartin
 */
public class SpeciesPageActivity extends SherlockActivity implements RenderPage{

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
			if(image.creator != null){
				((TextView)findViewById(R.id.speciesImageCreator)).setText("Image by: " + image.creator);
			} else {
				findViewById(R.id.speciesImageCreator).setVisibility(TextView.GONE);				
			}
			if(image.infoSourceName != null){
				((TextView)findViewById(R.id.speciesImageSource)).setText("Source: " + image.infoSourceName);
			} else {
				findViewById(R.id.speciesImageSource).setVisibility(TextView.GONE);
			}
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
		
		boolean italicise = false;
		
		if(data.get("rankID") != null){
			Integer rankID = (Integer) data.get("rankID");
			if(rankID>6000){
				scientificName.setTypeface(null, Typeface.ITALIC);
				italicise = true;
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
		
		final String nameSource = (String) data.get("infoSourceName");
		TextView namesource = (TextView) findViewById(R.id.nameSource);
		namesource.setText("Source: " + nameSource);		
		
		//conservation status
		List<ConservationStatusDTO> csList = (List<ConservationStatusDTO>) data.get("conservationStatuses"); 
		if(csList != null && !csList.isEmpty()){
			LinearLayout conservationStatusList =  (LinearLayout) findViewById(R.id.conservationStatusLayout);
			conservationStatusList.setVisibility(LinearLayout.VISIBLE);
			for(ConservationStatusDTO cs : csList){
				TextView tv = new TextView(this);
				tv.setText(cs.getRawStatus());
				tv.setId(1);
				tv.setTextSize(18);
				tv.setLayoutParams(new LayoutParams(
			            LayoutParams.FILL_PARENT,
			            LayoutParams.WRAP_CONTENT));				
				conservationStatusList.addView(tv);

				if(cs.getRegion() !=null){
					TextView region = new TextView(this);
					region.setText("Region: " + cs.getRegion());
					region.setId(5);
					region.setTextSize(13);
					region.setLayoutParams(new LayoutParams(
				            LayoutParams.FILL_PARENT,
				            LayoutParams.WRAP_CONTENT));	
					conservationStatusList.addView(region);	
				}
				
				if(cs.getSystem() !=null){
					TextView system = new TextView(this);
					system.setText(cs.getSystem());
					system.setId(5);
					system.setTextSize(13);
					system.setLayoutParams(new LayoutParams(
				            LayoutParams.FILL_PARENT,
				            LayoutParams.WRAP_CONTENT));	
					conservationStatusList.addView(system);	
				}				
				
				TextView source = new TextView(this);
				source.setText("Source: " + cs.infoSourceName);
				source.setId(5);
				source.setTextSize(12);
				source.setPadding(0, 5, 0, 15);
				source.setLayoutParams(new LayoutParams(
			            LayoutParams.FILL_PARENT,
			            LayoutParams.WRAP_CONTENT));	
				conservationStatusList.addView(source);					
			}
		}
		
		//categories
		List<CategoryDTO> catList = (List<CategoryDTO>) data.get("categories");
		if(catList != null && !catList.isEmpty()){
			LinearLayout catLayout =  (LinearLayout) findViewById(R.id.categoriesLayout);
			catLayout.setVisibility(LinearLayout.VISIBLE);
			for(CategoryDTO cs : catList){
				TextView tv = new TextView(this);
				tv.setText(cs.getCategory());
				tv.setId(1);
				tv.setTextSize(18);
				tv.setLayoutParams(new LayoutParams(
			            LayoutParams.FILL_PARENT,
			            LayoutParams.WRAP_CONTENT));				
				catLayout.addView(tv);

				TextView reference = new TextView(this);
				reference.setText(cs.authority);
				reference.setId(5);
				reference.setTextSize(13);
				reference.setLayoutParams(new LayoutParams(
			            LayoutParams.FILL_PARENT,
			            LayoutParams.WRAP_CONTENT));	
				catLayout.addView(reference);	
				
				TextView source = new TextView(this);
				source.setText("Source: " +cs.infoSourceName);
				source.setId(5);
				source.setTextSize(12);
				source.setPadding(0, 5, 0, 15);
				source.setLayoutParams(new LayoutParams(
			            LayoutParams.FILL_PARENT,
			            LayoutParams.WRAP_CONTENT));	
				catLayout.addView( source);					
			}
		}
		
		//synonyms
		List<SynonymDTO> synonymList = (List<SynonymDTO>) data.get("synonyms");
		if(synonymList != null && !synonymList.isEmpty()){
			LinearLayout synonymLayout =  (LinearLayout) findViewById(R.id.synonymLayout);
			synonymLayout.setVisibility(LinearLayout.VISIBLE);			
			for(SynonymDTO syn : synonymList){
				TextView name = new TextView(this);
				name.setText(syn.nameString + " " + syn.author);
				name.setId(5);
				name.setTextSize(18);
				if(italicise) 
					name.setTypeface(null, Typeface.ITALIC);
				name.setLayoutParams(new LayoutParams(
			            LayoutParams.FILL_PARENT,
			            LayoutParams.WRAP_CONTENT));	
				synonymLayout.addView(name);		

				if(syn.publishedIn !=null && syn.publishedIn.trim().length()>0){
					TextView reference = new TextView(this);
					reference.setText(syn.publishedIn);
					reference.setId(5);
					reference.setTextSize(13);
					reference.setLayoutParams(new LayoutParams(
				            LayoutParams.FILL_PARENT,
				            LayoutParams.WRAP_CONTENT));	
					synonymLayout.addView(reference);	
				}
				
				TextView source = new TextView(this);
				String synonymSource = syn.getInfoSourceName() !=null ? syn.getInfoSourceName() : nameSource; 
				source.setText("Source: " + synonymSource);
				source.setId(5);
				source.setTextSize(12);
				source.setPadding(0, 5, 0, 15);
				source.setLayoutParams(new LayoutParams(
			            LayoutParams.FILL_PARENT,
			            LayoutParams.WRAP_CONTENT));	
				synonymLayout.addView(source);					
			}
		}		
		
		//common names
		List<CommonNameDTO> commonNameList = (List<CommonNameDTO>) data.get("commonNames");
		if(commonNameList != null && !commonNameList.isEmpty()){
			LinearLayout commonNamesLayout =  (LinearLayout) findViewById(R.id.commonNamesLayout);
			for(CommonNameDTO addCommonName : commonNameList){
				TextView tv = new TextView(this);
				tv.setText(addCommonName.getNameString());
				tv.setId(5);
				tv.setTextSize(15);
				tv.setLayoutParams(new LayoutParams(
			            LayoutParams.FILL_PARENT,
			            LayoutParams.WRAP_CONTENT));				
				commonNamesLayout.addView(tv);		
				
				TextView source = new TextView(this);
				source.setText(addCommonName.infoSourceName);
				source.setId(5);
				source.setTextSize(12);
				source.setPadding(0, 5, 0, 15);
				source.setLayoutParams(new LayoutParams(
			            LayoutParams.FILL_PARENT,
			            LayoutParams.WRAP_CONTENT));	
				commonNamesLayout.addView(source);					
			}
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
			spt.execute("urn:lsid:biodiversity.org.au:afd.taxon:8d061243-c39f-4b81-92a9-c81f4419e93c");
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.species_page, menu);
		return true;
	}
}
