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
import android.os.AsyncTask;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

/**
 * Retrieve a list of species groups within a radius. 
 * 
 * @author davemartin
 */
public class GroupsSearchTask extends AsyncTask<String, String, List<Map<String,Object>>> {

	protected ListView listView;
	protected Context context;
	protected LoadGroup loadGroup;
	
	String lat;
	String lon;
	String radius;	

	public GroupsSearchTask(LoadGroup loadGroup){
		this.loadGroup = loadGroup;
	}
	
	@Override
	protected List<Map<String,Object>> doInBackground(String... args) {
		List<Map<String,Object>> results = new ArrayList<Map<String,Object>>();
		try{
			
			this.lat = args[0];
			this.lon = args[1];
			this.radius = args[2];			
			
			HttpClient http = HttpUtil.getTolerantClient();
			final String searchUrl = "https://m.ala.org.au/searchByMultiRanks?lat="+lat+"&lon="+lon+"&radius="+radius; 
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
	
    @Override
    // Once the image is downloaded, associates it to the imageView
    protected void onPostExecute(List<Map<String,Object>> results) {
    	String[] from = {"commonName", "scientificName", "count", "groupName"};
    	int[] to = {R.id.commonName, R.id.scientificName, R.id.count, R.id.groupName};
    	SpeciesListSectionAdapter adapter = new SpeciesListSectionAdapter(context, results, R.layout.group_results, from, to);
        // Setting the adapter to the listView
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new OnItemClickListener(){
		    @Override public void onItemClick(AdapterView<?> listView, View view, int position, long arg3){ 
		    	System.out.println("Position: " + position + ", arg3: " + arg3 + ", view : " + view);
		    	@SuppressWarnings("unchecked")
				Map<String,Object> li =  (Map<String,Object>) listView.getItemAtPosition(position);
		    	String groupName = (String) li.get("commonName");
		    	GroupsSearchTask.this.loadGroup.load(groupName, GroupsSearchTask.this.lat, 
		    			GroupsSearchTask.this.lon, GroupsSearchTask.this.radius);
		    }
		});		        
    }

	public void setContext(Context context) {
		this.context = context;
	}
	
	public void setListView(ListView listView) {
		this.listView = listView;
	}	
}
