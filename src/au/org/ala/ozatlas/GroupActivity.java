package au.org.ala.ozatlas;

import java.util.List;
import java.util.Map;

import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragmentActivity;

public abstract class GroupActivity extends SherlockFragmentActivity implements LoaderCallbacks<List<Map<String, Object>>> {

	protected static final String GROUP_KEY = "group";
	protected static final String LAT_KEY = "lat";
	protected static final String LON_KEY = "lon";
	protected static final String RADIUS_KEY = "radius";
	protected static final String LIST_INDEX_KEY = "listIndex";
	protected static final String LIST_POSITION_KEY = "listPosition";
	
	protected int savedListIndex = -1;
	protected int savedViewOffset = -1;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			extractListPosition(savedInstanceState);
		}
	}
	/** 
	 * Subclasses should override to return the instance of their main
	 * ListView
	 */
	protected abstract ListView getListView();
	
	@Override
	public void onSaveInstanceState(Bundle out) {
		super.onSaveInstanceState(out);
		
		ListView list = getListView();
		int index = list.getFirstVisiblePosition();
		out.putInt(LIST_INDEX_KEY, index);
		View view = list.getChildAt(index);
		int top = view == null ? 0 : view.getTop();
		out.putInt(LIST_POSITION_KEY, top);
	}
	
	protected void extractListPosition(Bundle state) {
		if (state != null) {
			savedListIndex = state.getInt(LIST_INDEX_KEY);
			savedViewOffset = state.getInt(LIST_POSITION_KEY);
			
		}
	}
	
	protected void restoreListPosition(){
		if (savedListIndex >= 0) {
			getListView().setSelectionFromTop(savedListIndex, savedViewOffset);
		}
	}
	@Override
	public void onLoaderReset(Loader<List<Map<String, Object>>> loader) {
		
	}
	
}