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
import android.os.AsyncTask;
import android.widget.ListView;

/**
 * Retrieve a list of species within a radius and within a group. 
 * 
 * @author davemartin
 */
public class GroupSpeciesTask extends AsyncTask<String, String, List<Map<String,Object>>> {

	protected ListView listView;
	protected Context context;
	
	public void setListView(ListView listView) {
		this.listView = listView;
	}

	@Override
	protected List<Map<String,Object>> doInBackground(String... args) {
		List<Map<String,Object>> results = new ArrayList<Map<String,Object>>();
		try{
			
			HttpClient http = HttpUtil.getTolerantClient();		
			String groupParam = URLEncoder.encode("\""+ args[0] + "\"", "utf-8");
			//todo - move the root url to properties
			final String searchUrl = "https://m.ala.org.au/exploreSubgroup?lat=-37.5&lon=149.1&radius=100&subgroup=" + groupParam;
			
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
				map.put("commonName", ((JsonNode) result.get("commonNameSingle")).getTextValue());
				map.put("smallImageUrl", ((JsonNode) result.get("smallImageUrl")).getTextValue());
				map.put("count", String.format("%,d", ((IntNode) result.get("recordCount")).asInt()) + " records");
				results.add(map);
			}

		} catch(Exception e){
			e.printStackTrace();
		}		
		return results;
	}
	
    @Override
    // Once the image is downloaded, associates it to the imageView
    protected void onPostExecute(List<Map<String,Object>> results) {
    	String[] from = { "commonName", "scientificName",  "count"};
    	int[] to = {R.id.commonName, R.id.scientificName, R.id.count};
    	ImageListAdapter adapter = new ImageListAdapter(context, results, R.layout.species_results, from, to);
        // Setting the adapter to the listView
        listView.setAdapter(adapter);
//        listView.setOnItemClickListener(new OnItemClickListener(){
//		    @Override public void onItemClick(AdapterView<?> listView, View view, int position, long arg3){ 
//		    	//load a species details
//		    }
//		});		        
    }

	public void setContext(Context context) {
		this.context = context;
	}
}
