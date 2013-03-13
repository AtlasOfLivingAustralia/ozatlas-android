package au.org.ala.ozatlas;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;

import android.os.AsyncTask;
import android.util.Log;

/**
 * Render a species page
 * 
 * @author davemartin
 */
public class SpeciesPageTask extends AsyncTask<String, String, Map<String,Object>> {

	protected RenderPage renderPage;

	@Override
	protected Map<String,Object> doInBackground(String... args) {
		Map<String,Object> map = new HashMap<String,Object>();
		String guid = args[0];
		try {
			
			try{
				loadSpeciesData(map, guid);	
			} catch(SSLException e) {
				// We seem to be getting random connection resets from the 
				// server when using SSL.  Trying again will normally work.
				Log.d("SpeciesPageTask", "Got SSL error, retrying");
				loadSpeciesData(map, guid);
			}	
		}
		catch (Exception e) {
			Log.e("SpeciesPageTask", "Error loading species page", e);
		}
		return map;
	}



	private void loadSpeciesData(Map<String, Object> map, String guid) throws IOException, SSLException,
			ClientProtocolException, JsonProcessingException, UnsupportedEncodingException {
		HttpClient http = HttpUtil.getTolerantClient();
		final String searchUrl = HttpUtil.SERVER_URL+"/species/" + guid + ".json";
		Log.i("SpeciesPageTask", "Species page URL : " + searchUrl);
		
		HttpGet get = new HttpGet(searchUrl);
		HttpResponse response = http.execute(get);
		InputStream input = response.getEntity().getContent();
		Log.i("SpeciesPageTask", "response code: "+response.getStatusLine().getStatusCode());
		
		//parse JSON
		ObjectMapper om = new ObjectMapper();
		om.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		
		JsonNode node = om.readTree(input);
		
		JsonNode taxonConcept = node.get("taxonConcept");			
		JsonNode classification = node.findValue("classification");
		JsonNode commonNames = node.get("commonNames");
		JsonNode synonyms = node.get("synonyms");
		JsonNode conservationStatuses = node.findValue("conservationStatuses");
		JsonNode categories = node.findValue("categories");
		JsonNode images = node.findValue("images");			
		
		map.put("guid", taxonConcept.get("guid").getTextValue()); 
		map.put("scientificName", taxonConcept.get("nameString").getTextValue());
		map.put("authorship", taxonConcept.get("author").getTextValue());
		map.put("infoSourceName", taxonConcept.get("infoSourceName").getTextValue());
		
		if(commonNames != null){
			Iterator<JsonNode> iter = commonNames.getElements();
			if(iter.hasNext()){
				map.put("commonName", iter.next().get("nameString").getTextValue());
			}
		}
		
		if(taxonConcept.get("rankID") != null)
			map.put("rankID", taxonConcept.get("rankID").getIntValue());
		
		
		if(images !=null && images.getElements().hasNext()){
			List<ImageDTO> imageDTOs = om.convertValue(images, om.getTypeFactory().constructCollectionType(ArrayList.class, ImageDTO.class));
			map.put("speciesImages", imageDTOs);
		}
			
		if(classification !=null){
			ClassificationDTO classificationDTO = om.convertValue(classification, om.getTypeFactory().constructType(ClassificationDTO.class));
			map.put("classification", classificationDTO);
		}
		
		if(synonyms !=null){
			List<SynonymDTO> synonymDTOs = om.convertValue(synonyms, om.getTypeFactory().constructCollectionType(ArrayList.class, SynonymDTO.class));
			map.put("synonyms", synonymDTOs);
		}			

		if(categories !=null){
			List<CategoryDTO> categoryDTOs = om.convertValue(categories, om.getTypeFactory().constructCollectionType(ArrayList.class, CategoryDTO.class));
			map.put("categories", categoryDTOs);
		}	
		
		if(conservationStatuses !=null){
			List<ConservationStatusDTO> conservationStatusDTOs = om.convertValue(conservationStatuses, om.getTypeFactory().constructCollectionType(ArrayList.class, ConservationStatusDTO.class));
			map.put("conservationStatuses", conservationStatusDTOs);
		}				
		
		// + taxonConcept.get("guid").getTextValue()
		map.put("speciesMap", "http://biocache.ala.org.au/ws/density/map?q=lsid:%22" +  URLEncoder.encode(guid, "UTF-8") +"%22%20AND%20geospatial_kosher:true");
		
		System.out.println("http://biocache.ala.org.au/ws/density/map?q=lsid:%22" +  URLEncoder.encode(guid, "UTF-8") +"%22%20AND%20geospatial_kosher:true");
		System.out.println("Loaded");
	}
	
	
	
    @Override
    // Once the image is downloaded, associates it to the imageView
    protected void onPostExecute(Map<String,Object> speciesData) {
    	this.renderPage.render(speciesData);
    }

    public void setRenderPage(RenderPage renderPage) {
		this.renderPage = renderPage;
	}
}
