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
import android.os.AsyncTask;
import android.widget.ListView;

/**
 * Species search 
 * 
 * @author davemartin
 */
public class SpeciesSearchTask extends AsyncTask<String, String, List<Map<String,Object>>> {

	protected Context context;	
	protected ListView listView;
	
	@Override
	protected List<Map<String,Object>> doInBackground(String... args) {
		List<Map<String,Object>> results = new ArrayList<Map<String,Object>>();
		try{
			HttpClient http = HttpUtil.getTolerantClient();
			final String searchUrl = "https://m.ala.org.au/search.json?pageSize=30&q="  + URLEncoder.encode(args[0], "utf-8");
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
    	String[] from = { "scientificName","commonName"};
    	int[] to = {R.id.scientificName, R.id.commonName};
    	ImageListAdapter adapter = new ImageListAdapter(context, results, R.layout.listview_thumbnails, from, to);
        // Setting the adapter to the listView
        listView.setAdapter(adapter);		
    }

	public void setContext(Context context) {
		this.context = context;
	}
	
	public void setListView(ListView listView) {
		this.listView = listView;
	}
}
