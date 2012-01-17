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

    /** @urlStr web page URL to fetch */
    public HttpGetter(String urlStr) {
        this.urlStr = urlStr;
    }

    /** @return web page contents */
    @Override public String call() throws Exception {
        HttpGet httpGet = new HttpGet(urlStr);
        try {
            return httpClient.execute(httpGet, brh);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
