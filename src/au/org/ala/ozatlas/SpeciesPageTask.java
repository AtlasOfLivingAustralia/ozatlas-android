package au.org.ala.ozatlas;

import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;

import android.os.AsyncTask;

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
		try{
			String guid = args[0];
			
			HttpClient http = HttpUtil.getTolerantClient();
			final String searchUrl = "https://m.ala.org.au/species/" + guid + ".json";
			System.out.println("PAGE URL : " + searchUrl);
			
			HttpGet get = new HttpGet(searchUrl);
			HttpResponse response = http.execute(get);
			InputStream input = response.getEntity().getContent();
			
			//parse JSON
			ObjectMapper om = new ObjectMapper();
			om.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			
			
			JsonNode node = om.readTree(input);
			
			JsonNode images = node.findValue("images");

			JsonNode taxonConcept = node.get("taxonConcept");
			JsonNode commonNames = node.get("commonNames");
			
			map.put("guid", taxonConcept.get("guid").getTextValue()); 
			map.put("scientificName", taxonConcept.get("nameString").getTextValue());
			map.put("authorship", taxonConcept.get("author").getTextValue());			
			
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
				
			// + taxonConcept.get("guid").getTextValue()
			map.put("speciesMap", "http://biocache.ala.org.au/ws/density/map?q=lsid:%22" +  URLEncoder.encode(guid, "UTF-8") +"%22%20AND%20geospatial_kosher:true");
			
			System.out.println("http://biocache.ala.org.au/ws/density/map?q=lsid:%22" +  URLEncoder.encode(guid, "UTF-8") +"%22%20AND%20geospatial_kosher:true");
			System.out.println("Loaded");	
			
			
		} catch(Exception e){
			e.printStackTrace();
		}		
		return map;
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
