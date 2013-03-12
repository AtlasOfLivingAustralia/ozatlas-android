package au.org.ala.ozatlas;

import java.io.InputStream;
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
import android.util.Log;

/**
 * Retrieve a list of species groups within a radius. 
 * 
 * @author davemartin
 */
public class GroupsSearchTask extends AsyncListLoader {

	
	public String lat;
	public String lon;
	public String radius;	

	public GroupsSearchTask(Context context, String lat, String lon, String radius) {
		super(context);
		this.lat = lat;
		this.lon = lon;
		this.radius = radius;
	}
	
	@Override
	public List<Map<String,Object>> loadInBackground() {
		List<Map<String,Object>> results = new ArrayList<Map<String,Object>>();
		try{
			
			HttpClient http = HttpUtil.getTolerantClient();
			final String searchUrl = "https://m.ala.org.au/searchByMultiRanks?lat="+lat+"&lon="+lon+"&radius="+radius; 
			Log.i("GroupsSearchTask", "Loading from: "+searchUrl);
			HttpGet get = new HttpGet(searchUrl);
			HttpResponse response = http.execute(get);
			InputStream input = response.getEntity().getContent();
			
			//parse JSON
			ObjectMapper om = new ObjectMapper();
			JsonNode node = om.readTree(input);
			
			Iterator<JsonNode> groups = node.getElements();
			while(groups.hasNext()){
				JsonNode group = groups.next();
				
				String groupName = group.get("groupName").getTextValue();
				JsonNode resultNode = group.get("groups");
				Iterator<JsonNode> iter = resultNode.getElements();
	
				while(iter.hasNext()){
					JsonNode result = iter.next();
					Map<String,Object> map = new HashMap<String,Object>();
					map.put("groupName", groupName); 
					map.put("scientificName", ((JsonNode) result.get("scientificName")).getTextValue());
					map.put("count", String.format("%,d", ((IntNode) result.get("recordCount")).asInt())  + " records");
					if(result.get("imageUrl")!=null)  map.put("smallImageUrl", ((JsonNode) result.get("imageUrl")).getTextValue());
					if(result.get("commonName")!=null) map.put("commonName", ((JsonNode) result.get("commonName")).getTextValue());					
					if(result.get("rankId")!=null) map.put("rankID", ((JsonNode) result.get("rankId")).asInt());
					results.add(map);
				}
			}

		} catch(Exception e){
			e.printStackTrace();
		}		
		return results;
	}
}
