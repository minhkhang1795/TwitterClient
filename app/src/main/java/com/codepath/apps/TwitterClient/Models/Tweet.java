package com.codepath.apps.TwitterClient.Models;

import android.text.format.DateUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by duyvu on 3/25/16.
 */
public class Tweet {
    private String mBody;
    private long mId; // unique ID for a tweet
    private User mUser;
    private String mCreateAt;
    private String mImageUrl;

    public String getmImageUrl() {
        return mImageUrl;
    }

    public User getmUser() {
        return mUser;
    }

    public String getmBody() {
        return mBody;
    }

    public long getmId() {
        return mId;
    }

    public String getmCreateAt() {
        return mCreateAt;
    }

    public static Tweet fromJSON(JSONObject jsonObject) {
        Tweet tweet = new Tweet();
        try {
            tweet.mBody = jsonObject.getString("text");
            tweet.mId = jsonObject.getLong("id");
            tweet.mCreateAt = getRelativeTimeAgo(jsonObject.getString("created_at"));
            tweet.mUser = User.fromJSON(jsonObject.getJSONObject("user"));
            tweet.mImageUrl = jsonObject.getJSONObject("entities").getJSONArray("media").getJSONObject(0).getString("media_url");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return tweet;
    }

    public static ArrayList<Tweet> fromJSONArray(JSONArray jsonArray) {
        ArrayList<Tweet> tweets = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {

            try {
                JSONObject tweetJson = jsonArray.getJSONObject(i);
                Tweet tweet = Tweet.fromJSON(tweetJson);
                if (tweet != null) {
                    tweets.add(tweet);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                continue;
            }
        }
        return tweets;
    }

    private static String getRelativeTimeAgo(String rawJsonDate) {
        String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
        sf.setLenient(true);
        String relativeDate = "";
        try {
            long dateMillis = sf.parse(rawJsonDate).getTime();
            relativeDate = DateUtils.getRelativeTimeSpanString(dateMillis,
                    System.currentTimeMillis(), DateUtils.FORMAT_ABBREV_ALL).toString();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return relativeDate;
    }
}
