package com.codepath.apps.TwitterClient.Java;

import android.content.Context;

import com.codepath.oauth.OAuthBaseClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.scribe.builder.api.Api;
import org.scribe.builder.api.TwitterApi;

/*
 * 
 * This is the object responsible for communicating with a REST API. 
 * Specify the constants below to change the API being communicated with.
 * See a full list of supported API classes: 
 *   https://github.com/fernandezpablo85/scribe-java/tree/master/src/main/java/org/scribe/builder/api
 * Key and Secret are provided by the developer site for the given API i.e dev.twitter.com
 * Add methods for each relevant endpoint in the API.
 * 
 * NOTE: You may want to rename this object based on the service i.e TwitterClient or FlickrClient
 * 
 */
public class TwitterClient extends OAuthBaseClient {
	public static final Class<? extends Api> REST_API_CLASS = TwitterApi.class; // Change this
	public static final String REST_URL = "https://api.twitter.com/1.1"; // Change this, base API URL
	public static final String REST_CONSUMER_KEY = "8qK3WiIAwigu40GXXpI7sxZo8";       // Change this
	public static final String REST_CONSUMER_SECRET = "Wdt4L63WNINrPa5jsasMHoVlcoD3ll6HPWQzRdasLpewiaHrtg"; // Change this
	public static final String REST_CALLBACK_URL = "oauth://cpsimpletweeta"; // Change this (here and in manifest)

	public TwitterClient(Context context) {
		super(context, REST_API_CLASS, REST_URL, REST_CONSUMER_KEY, REST_CONSUMER_SECRET, REST_CALLBACK_URL);
	}

	// GET HOME TIMELINE
	public void getHomeTimeline(int page, AsyncHttpResponseHandler handler) {
		String apiUrl = getApiUrl("statuses/home_timeline.json");
		// Specify RequestParams.
		RequestParams params = new RequestParams();
		params.put("count", 20);
		params.put("since_id", 1);
		params.put("page", page);
		// Execute the request
		getClient().get(apiUrl, params, handler);
	}

	// GET HOME TIMELINE
	public void postTweet(String status, long replyID, AsyncHttpResponseHandler handler) {
		String apiUrl = getApiUrl("statuses//update.json");
		// Specify RequestParams.
		RequestParams params = new RequestParams();
		if (replyID != -1) {
			params.put("in_reply_to_status_id", replyID);
		}
		params.put("status", status);
		// Execute the request
		getClient().post(apiUrl, params, handler);
	}

	/* 1. Define the endpoint URL with getApiUrl and pass a relative path to the endpoint
	 * 	  i.e getApiUrl("statuses/home_timeline.json");
	 * 2. Define the parameters to pass to the request (query or body)
	 *    i.e RequestParams params = new RequestParams("foo", "bar");
	 * 3. Define the request method and make a call to the client
	 *    i.e client.get(apiUrl, params, handler);
	 *    i.e client.post(apiUrl, params, handler);
	 */
}