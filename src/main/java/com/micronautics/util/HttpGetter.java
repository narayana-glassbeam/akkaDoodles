package com.micronautics.util;

import java.util.concurrent.Callable;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;


/** Called when a future is about to be computed.
 * @author Mike Slinn*/
public class HttpGetter implements Callable<String> {
    private DefaultHttpClient httpClient = new DefaultHttpClient();
    private BasicResponseHandler brh = new BasicResponseHandler();
    private String urlStr;
    private String searchString = null;

    
    /** @param urlStr web page URL to fetch */
    public HttpGetter(String urlStr) { this.urlStr = urlStr; }
    
    /** @param urlStr web page URL to fetch
     * @param searchString web page must contain searchString in order to be returned */
    public HttpGetter(String urlStr, String searchString) { 
    	this.urlStr = urlStr;
    	this.searchString = searchString;
    }
    
    /** @return web page contents if search string not specified or search string found in contents; 
     * otherwise return empty string */
    @Override public String call() throws Exception {
        HttpGet httpGet = new HttpGet(urlStr);
        try {
            String result = httpClient.execute(httpGet, brh);
            if (searchString==null || result.indexOf(searchString)>=0)
            	return result;
            return "";
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
