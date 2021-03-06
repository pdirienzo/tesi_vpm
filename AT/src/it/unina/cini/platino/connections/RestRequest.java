package it.unina.cini.platino.connections;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * An class representing a REST request
 * 
 * <p> 
 * Copyright (C) 2014 University of Naples. All Rights Reserved.
 * <p>
 * This program is distributed under GPL Version 2.0, WITHOUT ANY WARRANTY
 * 
 * @author <a href="mailto:p.dirienzo@studenti.unina.it">p.dirienzo@studenti.unina.it</a>, 
 * <a href="mailto:enr.demaio@studenti.unina.it">enr.demaio@studenti.unina.it</a>
 * @version 1.0
 *//**
 * An class representing an Hypervisor row in the Database.
 * 
 * <p> 
 * Copyright (C) 2014 University of Naples. All Rights Reserved.
 * <p>
 * This program is distributed under GPL Version 2.0, WITHOUT ANY WARRANTY
 * 
 * @author <a href="mailto:p.dirienzo@studenti.unina.it">p.dirienzo@studenti.unina.it</a>, 
 * <a href="mailto:enr.demaio@studenti.unina.it">enr.demaio@studenti.unina.it</a>
 * @version 1.0
 */
public class RestRequest {
	private static final int TIMEOUT = 3000;
	
	private final static HttpClient client = createHttpClient();

	private static HttpClient createHttpClient(){
		RequestConfig config = RequestConfig.custom()
			    .setSocketTimeout(TIMEOUT)
			    .setConnectTimeout(TIMEOUT)
			    .build();
		
		HttpClientBuilder hcBuilder = HttpClients.custom();
		hcBuilder.setDefaultRequestConfig(config);
		
		return hcBuilder.build();
	}
	
	public static JSONObject getJson(String url) throws ClientProtocolException, IOException {
		HttpGet getRequest = new HttpGet(url);
		
		HttpResponse resp = client.execute(getRequest);
		BufferedReader rd = new BufferedReader(new InputStreamReader(
				resp.getEntity().getContent()));
		
		JSONObject json = null;
		String s = null;
		StringBuilder sb = new StringBuilder();
		while((s=rd.readLine())!= null)
			sb.append(s);

		s = sb.toString();
		
		
		json = new JSONObject(sb.toString());
		
		rd.close();
		
		return json;
	}
	
	public static JSONArray getJSonArray(String url) throws IOException {
		HttpGet getRequest = new HttpGet(url);
		HttpResponse resp = client.execute(getRequest);
		BufferedReader rd = new BufferedReader(new InputStreamReader(
				resp.getEntity().getContent()));
		
		JSONArray json = null;
		String s = null;
		StringBuilder sb = new StringBuilder();
		while((s=rd.readLine())!= null){
			sb.append(s);
		}
		
		json = new JSONArray(sb.toString());
		
		rd.close();
		
		return json;
	}
	
	public static JSONObject postJson(String url, JSONObject data) throws IOException{
		return new JSONObject(post(url,data.toString()));
	}

	public static String post(String url, String data) throws IOException{
		/* POST Method */
		final HttpPost post = new HttpPost(url);
		post.setEntity(new StringEntity(data));
		String res = EntityUtils.toString(client.execute(post).getEntity());
		return res;
	}
	
	public static String put(String url, String data) {
		/* PUT Method */
		final HttpPut put = new HttpPut(url);
		try {
			put.setEntity(new StringEntity(data));
			return EntityUtils.toString(client.execute(put).getEntity());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static JSONObject deleteJson(String url, JSONObject data){
		return new JSONObject(delete(url, data.toString()));
	}
	
	public static String delete(String url, String data) {
		/* DELETE Method */
		final HttpDeleteWithBody delete = new HttpDeleteWithBody(url);
		try {
			delete.setEntity(new StringEntity(data));
			return EntityUtils.toString(client.execute(delete).getEntity());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Workaround in order to implement a Delete with body request by making use
	 * of apache APIs as this request type is not explicitly supported
	 * 
	 * @author <a href="mailto:p.dirienzo@studenti.unina.it">Pasquale Di Rienzo</a>, 
	 * <a href="mailto:enr.demaio@studenti.unina.it">Enrico De Maio</a>
	 *
	 */
	private static class HttpDeleteWithBody extends HttpPost {
		public HttpDeleteWithBody(String uri) {
			super(uri);
		}
		public String getMethod() {
			return "DELETE";
		}
	}
}
