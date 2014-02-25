package org.at.floodlight;

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

@SuppressWarnings("deprecation")
public class RestRequest {
	
	private final static HttpClient http = new DefaultHttpClient();

	public static String get(String url) {
		/* GET Method */
		final HttpGet get = new HttpGet(url);
		try {
			HttpEntity entity = http.execute(get).getEntity();
			if(entity==null)
				return "";
			return EntityUtils.toString(entity);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String post(String url, String data) {
		/* POST Method */
		final HttpPost post = new HttpPost(url);
		try {
			post.setEntity(new StringEntity(data));
			return EntityUtils.toString(http.execute(post).getEntity());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static String put(String url, String data) {
		/* PUT Method */
		final HttpPut put = new HttpPut(url);
		try {
			put.setEntity(new StringEntity(data));
			return EntityUtils.toString(http.execute(put).getEntity());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static String delete(String url, String data) {
		/* DELETE Method */
		final HttpDeleteWithBody delete = new HttpDeleteWithBody(url);
		try {
			delete.setEntity(new StringEntity(data));
			return EntityUtils.toString(http.execute(delete).getEntity());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private static class HttpDeleteWithBody extends HttpPost {
		public HttpDeleteWithBody(String uri) {
			super(uri);
		}
		public String getMethod() {
			return "DELETE";
		}
	}
}
