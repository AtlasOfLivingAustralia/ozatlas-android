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
import org.codehaus.jackson.node.IntNode;

import android.content.Context;

/**
 * Retrieve a list of species within a radius and within a group. 
 * 
 * @author davemartin
 */
public class GroupSpeciesTask extends AsyncListLoader {

	private String lat;
	private String lon;
	private String radius;
	private String group;
	
	public GroupSpeciesTask(Context context, String group, String lat, String lon, String radius) {
		super(context);
		this.group = group;
		this.lat = lat;
		this.lon = lon;
		this.radius = radius;
	}
	
	@Override
	public List<Map<String,Object>> loadInBackground() {
		List<Map<String,Object>> results = new ArrayList<Map<String,Object>>();
		try{
			HttpClient http = HttpUtil.getTolerantClient();		
			String groupParam = URLEncoder.encode("\""+ group + "\"", "utf-8");
			
			//todo - move the root url to properties
			final String searchUrl = "https://m.ala.org.au/exploreSubgroup?lat="+lat+"&lon="+lon+"&radius="+radius+"&subgroup=" + groupParam;
			
			System.out.println("Url used: " + searchUrl);
			HttpGet get = new HttpGet(searchUrl);
			HttpResponse response = http.execute(get);
			InputStream input = response.getEntity().getContent();
			
			//parse JSON
			ObjectMapper om = new ObjectMapper();
			JsonNode node = om.readTree(input);
			
			Iterator<JsonNode> iter = node.getElements();

			while(iter.hasNext()){
				JsonNode result = iter.next();
				Map<String,Object> map = new HashMap<String,Object>();
				map.put("guid", ((JsonNode) result.get("guid")).getTextValue());
				map.put("scientificName", ((JsonNode) result.get("name")).getTextValue()); 
				if(result.get("commonNameSingle") !=null) map.put("commonName", ((JsonNode) result.get("commonNameSingle")).getTextValue());
				if(result.get("smallImageUrl") !=null) map.put("smallImageUrl", ((JsonNode) result.get("smallImageUrl")).getTextValue());
				if(result.get("rankId") !=null) map.put("rankID", ((JsonNode) result.get("rankId")).asInt());
				map.put("count", String.format("%,d", ((IntNode) result.get("recordCount")).asInt()) + " records");
				results.add(map);
			}

		} catch(Exception e){
			e.printStackTrace();
		}		
		return results;
	}

}
