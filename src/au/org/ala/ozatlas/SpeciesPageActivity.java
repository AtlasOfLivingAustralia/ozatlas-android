package au.org.ala.ozatlas;

import java.util.List;
import java.util.Map;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.text.style.TextAppearanceSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import au.org.ala.mobile.ozatlas.R;

import com.actionbarsherlock.app.SherlockActivity;

/**
 * Load a species page
 * 
 * @author davemartin
 */
public class SpeciesPageActivity extends SherlockActivity implements RenderPage{

	@Override
	public void render(Map<String, Object> data) {
		
		findViewById(R.id.progress).setVisibility(View.GONE);
		findViewById(R.id.recordSightingButton).setVisibility(View.VISIBLE);
		addRecordSightingListener(data);
		ImageView speciesImage = (ImageView) findViewById(R.id.speciesImage);
		List<ImageDTO> speciesImages = (List<ImageDTO>) data.get("speciesImages");

		if(speciesImages != null && !speciesImages.isEmpty()){
			ImageDTO image = speciesImages.get(0); 
			LoadImageTask lit = new LoadImageTask(true);
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
		
		TextView scientificNameView = (TextView) findViewById(R.id.scientificNameAndAuthorship);
		String scientificName = (String) data.get("scientificName"); 
		
		boolean italicise = false;
		
		if(data.get("rankID") != null){
			Integer rankID = (Integer) data.get("rankID");
			if(rankID>6000){
				italicise = true;
			}
		}
		String authorship = (String) data.get("authorship");
		
		if (scientificName != null) {
			SpannableStringBuilder builder = new SpannableStringBuilder(scientificName);
			builder.setSpan(new TextAppearanceSpan(this, android.R.style.TextAppearance_Large), 0, scientificName.length(), 0);
			if (italicise) {
				builder.setSpan(new StyleSpan(Typeface.ITALIC), 0, scientificName.length(), 0);
			}
			if (authorship != null) {
				builder.append("   "+authorship);
				builder.setSpan(new TextAppearanceSpan(this, android.R.style.TextAppearance_Small), scientificName.length(), builder.length(), 0);
			}
			
			scientificNameView.setText(builder);
		}
		
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

	private void addRecordSightingListener(final Map<String, Object> data) {
		Button recordSighting = (Button)findViewById(R.id.recordSightingButton);
		recordSighting.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent recordSightingIntent = new Intent(SpeciesPageActivity.this, RecordSightingActivity.class);
				recordSightingIntent.putExtra(RecordSightingActivity.LSID_KEY, (String)data.get("guid"));
				recordSightingIntent.putExtra(RecordSightingActivity.SCIENTIFIC_NAME_KEY, (String)data.get("scientificName"));
				recordSightingIntent.putExtra(RecordSightingActivity.COMMON_NAME_KEY, (String)data.get("commonName"));
				
				@SuppressWarnings("unchecked")
				List<ImageDTO> speciesImages = (List<ImageDTO>) data.get("speciesImages");

				if(speciesImages != null && !speciesImages.isEmpty()){
					ImageDTO image = speciesImages.get(0); 
					recordSightingIntent.putExtra(RecordSightingActivity.IMAGE_URL_KEY, (String)image.smallImageUrl);
				}
				startActivity(recordSightingIntent);
			}
		});
		
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
}
