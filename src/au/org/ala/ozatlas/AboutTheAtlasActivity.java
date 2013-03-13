package au.org.ala.ozatlas;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

/**
 * Displays information about the atlas.
 */
public class AboutTheAtlasActivity extends Activity implements OnClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about_the_atlas);
		
		findViewById(R.id.imageView1).setOnClickListener(this);
		((TextView)findViewById(R.id.textView1)).setText(Html.fromHtml(getResources().getString(R.string.about_ala_header)));
		
		TextView alaLink = (TextView)findViewById(R.id.textView5);
		
		alaLink.setText(Html.fromHtml(getResources().getString(R.string.about_ala_url)));
		alaLink.setMovementMethod(LinkMovementMethod.getInstance());
		
	}
	
	@Override
	public void onClick(View v) {
		goToAtlas();
	}
	
	private void goToAtlas() {
		Intent alaIntent = new Intent(Intent.ACTION_VIEW);
		alaIntent.setData(Uri.parse("http://www.ala.org.au"));
		startActivity(alaIntent);
	}
}
