package com.codepath.apps.TwitterClient.Models;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by duyvu on 3/25/16.
 */
public class User {

    private String mName;
    private long mId;
    private String mScreenName;
    private String mImageUrl;

    public String getmName() {
        return mName;
    }

    public long getmId() {
        return mId;
    }

    public String getmScreenName() {
        return mScreenName;
    }

    public String getmImageUrl() {
        return mImageUrl;
    }

    public static User fromJSON(JSONObject jsonObject) {
        User user = new User();
        try {
            user.mName = jsonObject.getString("name");
            user.mId = jsonObject.getLong("id");
            user.mScreenName = jsonObject.getString("screen_name");
            user.mImageUrl = jsonObject.getString("profile_image_url");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return user;
    }
}
