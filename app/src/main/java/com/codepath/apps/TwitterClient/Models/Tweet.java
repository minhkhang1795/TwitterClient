package com.codepath.apps.TwitterClient.models;

import android.content.res.Resources;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.codepath.apps.TwitterClient.R;

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
public class Tweet extends Model implements Parcelable {

    public Boolean getmIsRetweeted() {
        return mIsRetweeted;
    }

    public void setmIsRetweeted(Boolean mIsRetweeted) {
        this.mIsRetweeted = mIsRetweeted;
    }

    public Boolean getmIsFavorited() {
        return mIsFavorited;
    }

    public void setmIsFavorited(Boolean mIsFavorited) {
        this.mIsFavorited = mIsFavorited;
    }

    public int getmRetweetCount() {
        return mRetweetCount;
    }

    public void setmRetweetCount(int mRetweetCount) {
        this.mRetweetCount = mRetweetCount;
    }

    public int getmFavoriteCount() {
        return mFavoriteCount;
    }

    public void setmFavoriteCount(int mFavoriteCount) {
        this.mFavoriteCount = mFavoriteCount;
    }

    public void setmBody(String mBody) {
        this.mBody = mBody;
    }

    public void setmId(long mId) {
        this.mId = mId;
    }

    public void setmUser(User mUser) {
        this.mUser = mUser;
    }

    public void setmCreatedAt(String mCreatedAt) {
        this.mCreatedAt = mCreatedAt;
    }

    public void setmImageUrl(String mImageUrl) {
        this.mImageUrl = mImageUrl;
    }

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

    public String getmCreatedAt() {
        return mCreatedAt;
    }

    @Column(name = "body")
    private String mBody;

    @Column(name = "remote_id", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    private long mId; // unique ID for a tweet

    @Column(name = "User", onUpdate = Column.ForeignKeyAction.CASCADE, onDelete = Column.ForeignKeyAction.CASCADE)
    private User mUser;

    @Column(name = "created_at")
    private String mCreatedAt;

    @Column(name = "image_url")
    private String mImageUrl = "";

    @Column(name = "video_url")
    private String mVideoUrl = "";

    @Column(name = "favorite_count")
    private int mFavoriteCount;

    @Column(name = "retweet_count")
    private int mRetweetCount;

    @Column(name = "is_retweeted")
    private Boolean mIsRetweeted;

    @Column(name = "is_favorited")
    private Boolean mIsFavorited;



    public String getRelativeTimeAgo(String rawJsonDate) {
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
        relativeDate = relativeDate.replaceAll("in ", "");
        relativeDate = relativeDate.replaceAll(" minutes", "m");
        relativeDate = relativeDate.replaceAll(" minute", "m");
        relativeDate = relativeDate.replaceAll(" hours ago", "h");
        relativeDate = relativeDate.replaceAll(" hour ago", "h");
        relativeDate = relativeDate.replaceAll(" ago", "");
        return relativeDate;
    }

    public String getmVideoUrl() {
        return mVideoUrl;
    }

    public void setmVideoUrl(String mVideoUrl) {
        this.mVideoUrl = mVideoUrl;
    }

    public static Tweet fromJSON(JSONObject jsonObject) {
        Log.d("DEBUG", jsonObject.toString());
        Tweet tweet = new Tweet();
        try {
            if (jsonObject.has("retweeted_status"))
                jsonObject = jsonObject.getJSONObject("retweeted_status");
            String idString = jsonObject.getString("id_str");
            tweet.mUser = User.findOrCreateFromJson(jsonObject.getJSONObject("user"));
            tweet.mId = Long.valueOf(idString);
            tweet.mCreatedAt = jsonObject.getString("created_at");
            tweet.mFavoriteCount = jsonObject.getInt("favorite_count");
            tweet.mRetweetCount = jsonObject.getInt("retweet_count");
            tweet.mIsFavorited = jsonObject.getBoolean("favorited");
            tweet.mIsRetweeted = jsonObject.getBoolean("retweeted");

            tweet.mBody = jsonObject.getString("text");
            tweet.mBody = clearUrlInString(tweet.mBody);

            JSONObject mediaJSONObject;
            if (jsonObject.getJSONObject("entities").has("media")) {
                if(jsonObject.getJSONObject("entities").getJSONArray("media").length() != 0) {
                    mediaJSONObject = jsonObject.getJSONObject("entities").getJSONArray("media").getJSONObject(0);
                    if (mediaJSONObject.getString("type").equals("photo"))
                        tweet.mImageUrl = mediaJSONObject.getString("media_url");
                }
            }

            if (jsonObject.has("extended_entities")) {
                JSONObject media = jsonObject.getJSONObject("extended_entities")
                        .getJSONArray("media")
                        .getJSONObject(0);
                if (!media.getString("type").equals("photo")) {
                    tweet.mVideoUrl = media.getJSONObject("video_info")
                            .getJSONArray("variants").getJSONObject(0).getString("url");
                }
            }

            String correctURLString = "";
            if (jsonObject.getJSONObject("entities").getJSONArray("urls").length() != 0)
                correctURLString = jsonObject.getJSONObject("entities").getJSONArray("urls").getJSONObject(0).getString("display_url");
            tweet.mBody += correctURLString;
        } catch (JSONException e) {
            e.printStackTrace();
        }


        tweet.save();
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
            return Tweet.fromJSON(jsonObject);
        } else {
            // create and return new user
            Tweet tweet = Tweet.fromJSON(jsonObject);
            tweet.save();
            return tweet;
        }
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

    private static String clearUrlInString(String bodyString) {
        return bodyString.replaceAll("https:\\//t.co\\/\\w*", "");
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
        dest.writeInt(mFavoriteCount);
        dest.writeInt(mRetweetCount);
        dest.writeByte((byte) (mIsFavorited ? 1 : 0));
        dest.writeByte((byte) (mIsRetweeted ? 1 : 0));
        dest.writeParcelable(mUser, flags);
    }

    protected Tweet(Parcel in) {
        this.mBody = in.readString();
        this.mId = in.readLong();
        this.mCreatedAt = in.readString();
        this.mImageUrl = in.readString();
        this.mFavoriteCount = in.readInt();
        this.mRetweetCount = in.readInt();
        this.mIsFavorited = in.readByte() != 0;
        this.mIsRetweeted = in.readByte() != 0;
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

    public static void setFavoriteBtnOn(ImageButton button, TextView text, Resources resources) {
        button.setBackground(resources.getDrawable(R.drawable.favorite_on));
        text.setTextColor(resources.getColor(R.color.colorYellow));
    }

    public static void setFavoriteBtnOff(ImageButton button, TextView text, Resources resources) {
        button.setBackground(resources.getDrawable(R.drawable.favorite_off));
        text.setTextColor(resources.getColor(R.color.grayDark));
    }

    public static void setRetweetBtnOn(ImageButton button, TextView text, Resources resources) {
        button.setBackground(resources.getDrawable(R.drawable.retweet_on));
        text.setTextColor(resources.getColor(R.color.colorGreen));
    }

    public static void setRetweetBtnOff(ImageButton button, TextView text, Resources resources) {
        button.setBackground(resources.getDrawable(R.drawable.retweet_off));
        text.setTextColor(resources.getColor(R.color.grayDark));
    }
}
