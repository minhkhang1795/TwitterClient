package com.codepath.apps.TwitterClient.Models;

import android.os.Parcel;
import android.os.Parcelable;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by duyvu on 3/25/16.
 */
@Table(name = "Tweets")
public class Tweet extends Model implements Parcelable {
    @Column(name = "body")
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
//            tweet.mId = jsonObject.getLong("id");
            tweet.mCreatedAt = jsonObject.getString("created_at");
            tweet.mUser = User.findOrCreateFromJson(jsonObject.getJSONObject("user"));
            if (jsonObject.getJSONObject("entities").getJSONArray("media").getJSONObject(0).getString("type").equals("photo")) {
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

    // Finds existing user based on remoteId or creates new user and returns
    public static Tweet findOrCreateFromJson(JSONObject jsonObject) {
        long rId = 0; // get just the remote id
        try {
            rId = jsonObject.getLong("id");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Tweet existingTweet =
                new Select().from(Tweet.class).where("remote_id = ?", rId).executeSingle();
        if (existingTweet != null) {
            // found and return existing
            return existingTweet;
        } else {
            // create and return new user
            Tweet tweet = Tweet.fromJSON(jsonObject);
            tweet.save();
            return tweet;
        }
    }

    private static String clearUrlInString(String mBodyUrl) {
        return mBodyUrl.replaceAll("https:\\//t.co\\/\\w*", "");
    }

    public static ArrayList<Tweet> fromJSONArray(JSONArray jsonArray) {
        ArrayList<Tweet> tweets = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {

            try {
                JSONObject tweetJson = jsonArray.getJSONObject(i);
                Tweet tweet = Tweet.findOrCreateFromJson(tweetJson);
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mBody);
        dest.writeLong(mId);
        dest.writeString(mCreatedAt);
        dest.writeString(mImageUrl);
        dest.writeString(mBodyUrl);
        dest.writeParcelable(mUser, flags);
    }

    protected Tweet(Parcel in) {
        this.mBody = in.readString();
        this.mId = in.readLong();
        this.mCreatedAt = in.readString();
        this.mImageUrl = in.readString();
        this.mBodyUrl = in.readString();
        this.mUser = in.readParcelable(User.class.getClassLoader());
    }

    public static final Parcelable.Creator CREATOR =
            new Parcelable.Creator() {
                public Tweet createFromParcel(Parcel in) {
                    return new Tweet(in);
                }

                public Tweet[] newArray(int size) {
                    return new Tweet[size];
                }
            };
}
