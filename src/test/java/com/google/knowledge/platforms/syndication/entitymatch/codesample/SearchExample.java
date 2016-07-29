package com.google.knowledge.platforms.syndication.entitymatch.codesample;

import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;

/**
 * Example of Java client calling Knowledge Graph Search API
 */
public class SearchExample {
    public static void main(String[] args) {
        try {
            HttpTransport httpTransport = new NetHttpTransport();
            HttpRequestFactory requestFactory = httpTransport.createRequestFactory();
            GenericUrl url = new GenericUrl("https://kgsearch.googleapis.com/v1/entities:search");
            url.put("query", "broadcast");
            url.put("limit", "10");
            url.put("indent", "true");
            url.put("key", "AIzaSyB1UgiRQKAELYBpjaetM4d0k3i7ZvKgNjE");
            HttpRequest request = requestFactory.buildGetRequest(url);
            HttpResponse httpResponse = request.execute();
            System.out.println(httpResponse.parseAsString());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}