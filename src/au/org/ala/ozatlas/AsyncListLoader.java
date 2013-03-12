package au.org.ala.ozatlas;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

/**
 * Extends AsyncTaskLoader to cache search results and deliver existing
 * results if they exist.
 */
public abstract class AsyncListLoader extends AsyncTaskLoader<List<Map<String, Object>>> {

	protected List<Map<String, Object>> results;
	
	public AsyncListLoader(Context context) {
		super(context);
		
	}
	
	@Override
	  protected void onStartLoading() {
	    if (results!=null) {
	      deliverResult(results);
	    }
	    
	    if (takeContentChanged() || results==null) {
	      forceLoad();
	    }
	  }

}