package au.org.ala.ozatlas;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleAdapter;

import com.fedorvlasov.lazylist.ImageLoader;

public class ImageListAdapter extends SimpleAdapter {

	ImageLoader imageLoader = null;
	int resourceID = -1;
	Context context = null;
	List<? extends Map<String, ?>> data;
	
	public ImageListAdapter(Context context,
			List<? extends Map<String, ?>> data, int resource, String[] from,
			int[] to) {
		super(context, data, resource, from, to);
		this.context = context;
		this.data = data;
		imageLoader = new ImageLoader(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		View view = super.getView(position, convertView, parent);
		
		ImageView imageView = (ImageView) view.findViewById(R.id.image);
		
		@SuppressWarnings("unchecked")
		Map<String,Object> properties = (Map<String,Object>) data.get(position);
		
		System.out.println("loading image: " + (String) properties.get("smallImageUrl"));
		
//		ImageView imageView = getLayoutInflater().inflate(R.layout.item_list_image, parent, false);		
		imageLoader.DisplayImage((String) properties.get("smallImageUrl"), imageView);
		
		return view;
	}
}
