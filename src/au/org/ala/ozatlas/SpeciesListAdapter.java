package au.org.ala.ozatlas;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import au.org.ala.mobile.ozatlas.R;

import com.fedorvlasov.lazylist.ImageLoader;

public class SpeciesListAdapter extends SimpleAdapter {

	ImageLoader imageLoader = null;
	int resourceID = -1;
	Context context = null;
	List<? extends Map<String, ?>> data;
	
	public SpeciesListAdapter(Context context,
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
		imageLoader.DisplayImage((String) properties.get("smallImageUrl"), imageView);
		
		if(properties.get("rankID") !=null){
			TextView textView = (TextView) view.findViewById(R.id.scientificName);
			Integer rankID = (Integer) properties.get("rankID");
			if(textView != null & rankID !=null && rankID>=6000){
				textView.setTypeface(null, Typeface.ITALIC);
			}
		}
		
		String commonName = (String) properties.get("commonName");
		if(commonName == null || commonName.trim().length()==0){
			TextView commonNameView = (TextView) view.findViewById(R.id.commonName);
			commonNameView.setVisibility(TextView.GONE);
		}
		
		return view;
	}
}
