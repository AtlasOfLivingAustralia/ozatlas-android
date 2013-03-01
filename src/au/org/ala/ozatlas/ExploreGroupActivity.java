package au.org.ala.ozatlas;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.widget.ListView;

public class ExploreGroupActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_explore_group);
		String groupName = (String) getIntent().getExtras().get("groupName");
		System.out.println("Loading group: " + groupName);
		GroupSpeciesTask gst = new GroupSpeciesTask();
		gst.setListView((ListView) findViewById(android.R.id.list));
		gst.setContext(this);
		gst.execute(groupName);
		setTitle("Species in this area");		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.explore_group, menu);
		return true;
	}
}
