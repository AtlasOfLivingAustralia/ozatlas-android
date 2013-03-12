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
import org.codehaus.jackson.map.ObjectMapper;

import android.content.Context;

/**
 * Species search 
 * 
 * @author davemartin
 */
public class SpeciesSearchTask extends AsyncListLoader {

	protected String searchText;
	
	public SpeciesSearchTask(Context context, String searchText) {
		super(context);
		this.searchText = searchText;
	}
	
	
	@Override
	public List<Map<String,Object>> loadInBackground() {
		results = new ArrayList<Map<String,Object>>();
		try{
			HttpClient http = HttpUtil.getTolerantClient();
			final String searchUrl = "http://bie.ala.org.au/ws/search.json?pageSize=30&fq=idxtype:TAXON&q="  + URLEncoder.encode(searchText, "utf-8");
			System.out.println("SEARCH URL : " + searchUrl);
			
			HttpGet get = new HttpGet(searchUrl);
			HttpResponse response = http.execute(get);
			InputStream input = response.getEntity().getContent();
			
			//parse JSON
			ObjectMapper om = new ObjectMapper();
			JsonNode node = om.readTree(input);
			
			JsonNode resultNode = node.findValue("searchResults").findValue("results");
			Iterator<JsonNode> iter = resultNode.getElements();

			while(iter.hasNext()){
				JsonNode result = iter.next();
				Map<String,Object> map = new HashMap<String,Object>();
				map.put("guid", result.get("guid").getTextValue()); 
				map.put("scientificName", result.get("name").getTextValue());
				if(result.get("commonNameSingle")!=null) map.put("commonName", result.get("commonNameSingle").getTextValue()); 
				if(result.get("smallImageUrl")!=null) map.put("smallImageUrl", result.get("smallImageUrl").getTextValue());
				if(result.get("rankId") != null) map.put("rankID", result.get("rankId").getIntValue());
				results.add(map);
			}

		} catch(Exception e){
			e.printStackTrace();
		}		
		return results;
	}
		
	
	 
}
