package com.micronautics.util;

import java.util.concurrent.Callable;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

public class HttpGetter implements Callable<String> {
    private DefaultHttpClient httpClient = new DefaultHttpClient();
    private BasicResponseHandler brh = new BasicResponseHandler();
    private String urlStr;
    
    public HttpGetter(String urlStr) {
        this.urlStr = urlStr;
    }
    
    @Override public String call() throws Exception {
        org.apache.http.client.methods.HttpGet httpGet = new org.apache.http.client.methods.HttpGet(urlStr);
        try {
            return httpClient.execute(httpGet, brh);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
