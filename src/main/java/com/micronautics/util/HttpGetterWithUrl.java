package com.micronautics.util;

import java.util.concurrent.Callable;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;


/** Called when a future is about to be computed.
 * @author Mike Slinn*/
public class HttpGetterWithUrl implements Callable<UrlAndContents> {
    private DefaultHttpClient httpClient = new DefaultHttpClient();
    private BasicResponseHandler brh = new BasicResponseHandler();
    private String urlStr;
    private String searchString = null;

    
    /** @param urlStr web page URL to fetch */
    public HttpGetterWithUrl(String urlStr) { this.urlStr = urlStr; }
    
    /** @param urlStr web page URL to fetch
     * @param searchString web page must contain searchString in order to be returned */
    public HttpGetterWithUrl(String urlStr, String searchString) { 
    	this.urlStr = urlStr;
    	this.searchString = searchString;
    }
    
    /** @return new UrlAndContents() containing web page contents if search string not specified or search string found in contents; 
     * otherwise return new UrlAndContents(urlStr, result) with result set to null */
    @Override public UrlAndContents call() throws Exception {
        HttpGet httpGet = new HttpGet(urlStr);
        try {
            String result = httpClient.execute(httpGet, brh);
            if (searchString==null || result.indexOf(searchString)>=0)
            	return new UrlAndContents(urlStr, result);
            return new UrlAndContents(urlStr, null);
        } catch (Exception e) {
            e.printStackTrace();
            return new UrlAndContents(urlStr, null);
        }
    }
}
