package au.org.ala.ozatlas;

import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.DefaultHttpClient;

public class HttpUtil {

	public static DefaultHttpClient getTolerantClient() {
	    DefaultHttpClient client = new DefaultHttpClient();
	    SSLSocketFactory sslSocketFactory = (SSLSocketFactory) client
	            .getConnectionManager().getSchemeRegistry().getScheme("https")
	            .getSocketFactory();
	    final X509HostnameVerifier delegate = sslSocketFactory.getHostnameVerifier();
	    if(!(delegate instanceof MyVerifier)) {
	        sslSocketFactory.setHostnameVerifier(new MyVerifier(delegate));
	    }
	    return client;
	}
}
