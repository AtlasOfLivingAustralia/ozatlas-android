package au.org.ala.ozatlas;

import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.DefaultHttpClient;

public class HttpUtil {

	
	private static final String SERVER_URL = "https://m.ala.org.au";
	
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
	
	public static String getLoginUrl() {
		return SERVER_URL + "/mobileauth/mobileKey/generateKey";
	}
}
