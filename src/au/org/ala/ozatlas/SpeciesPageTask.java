package au.org.ala.ozatlas;

import java.io.InputStream;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.codehaus.jackson.JsonNode;
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
			final String searchUrl = "https://m.ala.org.au/species/" + guid;
			System.out.println("PAGE URL : " + searchUrl);
			
			HttpGet get = new HttpGet(searchUrl);
			HttpResponse response = http.execute(get);
			InputStream input = response.getEntity().getContent();
			
			//parse JSON
			ObjectMapper om = new ObjectMapper();
			JsonNode node = om.readTree(input);
			
			JsonNode images = node.findValue("images");

			JsonNode taxonConcept = node.get("taxonConcept");
			
			map.put("guid", node.get("taxonConcept").get("guid").getTextValue()); 
			map.put("scientificName", taxonConcept.get("guid").getTextValue());
			map.put("speciesImage", images.get(0).get("largeImageUrl").getTextValue());
				
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
