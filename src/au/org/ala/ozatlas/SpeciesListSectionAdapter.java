package au.org.ala.ozatlas;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import au.org.ala.mobile.ozatlas.R;

import com.fedorvlasov.lazylist.ImageLoader;

public class SpeciesListSectionAdapter extends SimpleAdapter {

	ImageLoader imageLoader = null;
	int resourceID = -1;
	Context context = null;
	List<? extends Map<String, ?>> data;
	
	public SpeciesListSectionAdapter(Context context,
			List<? extends Map<String, ?>> data, int resource, String[] from,
			int[] to) {
		super(context, data, resource, from, to);
		this.context = context;
		this.data = data;
		imageLoader = new ImageLoader(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		String lastGroupName = null;
		
		if(position>0){
			ListView listView = (ListView) parent;
			@SuppressWarnings("unchecked")
			Map<String,Object> listViewItem = (Map<String,Object>) listView.getItemAtPosition(position - 1);
			lastGroupName = (String) listViewItem.get("groupName");
		}	
		View view = super.getView(position, convertView, parent);
		
		ImageView imageView = (ImageView) view.findViewById(R.id.image);
		
		TextView textView = (TextView) view.findViewById(R.id.groupName);
		
		if(textView!=null){
			String groupName = textView.getText().toString();
//			System.out.println("Position:" + position +", OLD: " + lastGroupName + ", NEW: " + groupName);
			if(groupName != null){
				if(lastGroupName == null){
//					System.out.println("SHOW");
					textView.setVisibility(TextView.VISIBLE);
					textView.setHeight(40);
				} else if(lastGroupName.equals(groupName)){
					//hide the textView
//					System.out.println("HIDE");
					textView.setVisibility(TextView.INVISIBLE);
					textView.setHeight(0);
				} else {
//					System.out.println("SHOW");
					textView.setVisibility(TextView.VISIBLE);
					textView.setHeight(40);
				}
			}
		}
		
		@SuppressWarnings("unchecked")
		Map<String,Object> properties = (Map<String,Object>) data.get(position);
		
//		System.out.println("loading image: " + (String) properties.get("smallImageUrl"));
		
//		ImageView imageView = getLayoutInflater().inflate(R.layout.item_list_image, parent, false);		
		imageLoader.DisplayImage((String) properties.get("smallImageUrl"), imageView);
		
		return view;
	}
}
