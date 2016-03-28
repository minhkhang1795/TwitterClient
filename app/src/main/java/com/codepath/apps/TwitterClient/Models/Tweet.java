package com.codepath.apps.TwitterClient.Models;

import android.text.format.DateUtils;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

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
@Table(name = "Tweets")
public class Tweet extends Model {
    @Column(name = "name")
    private String mBody;

    @Column(name = "remote_id", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    private long mId; // unique ID for a tweet

    @Column(name = "User", onUpdate = Column.ForeignKeyAction.CASCADE, onDelete = Column.ForeignKeyAction.CASCADE)
    private User mUser;

    @Column(name = "created_at")
    private String mCreatedAt;

    @Column(name = "image_url")
    private String mImageUrl;

    @Column(name = "body_url")
    private String mBodyUrl;

    public Tweet() {
        super();
    }

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

    public String getmBodyUrl() {
        return mBodyUrl;
    }

    public void setmBodyUrl(String mBodyUrl) {
        this.mBodyUrl = mBodyUrl;
    }

    public String getmCreatedAt() {
        return mCreatedAt;
    }

    public static Tweet fromJSON(JSONObject jsonObject) {
        Tweet tweet = new Tweet();
        try {
            tweet.mBody = jsonObject.getString("text");
            tweet.mId = jsonObject.getLong("id");
            tweet.mCreatedAt = getRelativeTimeAgo(jsonObject.getString("created_at"));
            tweet.mUser = User.fromJSON(jsonObject.getJSONObject("user"));
            if(jsonObject.getJSONObject("entities").getJSONArray("media").getJSONObject(0).getString("type").equals("photo")) {
                tweet.mImageUrl = jsonObject.getJSONObject("entities").getJSONArray("media").getJSONObject(0).getString("media_url");
            } else {
                tweet.mImageUrl = null;
            }
            tweet.mBodyUrl = jsonObject.getJSONObject("entities").getJSONArray("urls").getJSONObject(0).getString("display_url");
            tweet.mBody = clearUrlInString(tweet.mBody);
            if (tweet.mBodyUrl.length() > 0) {
                tweet.mBody += tweet.mBodyUrl;
            }
            tweet.save();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return tweet;
    }

    private static String clearUrlInString(String mBodyUrl) {
        return mBodyUrl.replaceAll("https:\\//t.co\\/\\w*", "");
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
