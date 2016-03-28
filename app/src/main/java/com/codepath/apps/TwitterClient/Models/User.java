package com.codepath.apps.TwitterClient.Models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by duyvu on 3/25/16.
 */
@Table(name = "Users")
public class User extends Model {

    @Column(name = "name")
    private String mName;

    @Column(name = "remote_id", unique = true)
    private long mId;

    @Column(name = "screen_name")
    private String mScreenName;

    @Column(name = "image_url")
    private String mImageUrl;

    public User() {
        super();
    }

    public List<Tweet> tweets() {
        return getMany(Tweet.class, "User");
    }

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
            user.save();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return user;
    }
}
