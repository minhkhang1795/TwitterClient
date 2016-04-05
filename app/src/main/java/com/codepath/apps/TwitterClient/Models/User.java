package com.codepath.apps.TwitterClient.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by duyvu on 3/25/16.
 */
@Table(name = "Users")
public class User extends Model implements Parcelable {

    public Long getmFollowersCount() {
        return mFollowersCount;
    }

    public void setmFollowersCount(Long mFollowersCount) {
        this.mFollowersCount = mFollowersCount;
    }

    public Long getmFriendsCount() {
        return mFriendsCount;
    }

    public void setmFriendsCount(Long mFriendsCount) {
        this.mFriendsCount = mFriendsCount;
    }

    public void setmBackgroundUrl(String mBackgroundUrl) {
        this.mBackgroundUrl = mBackgroundUrl;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }

    public void setmId(long mId) {
        this.mId = mId;
    }

    public void setmScreenName(String mScreenName) {
        this.mScreenName = mScreenName;
    }

    public void setmImageUrl(String mImageUrl) {
        this.mImageUrl = mImageUrl;
    }

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

    @Column(name = "name")
    private String mName;

    @Column(name = "remote_id", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    private long mId;

    @Column(name = "screen_name")
    private String mScreenName;

    @Column(name = "image_url")
    private String mImageUrl;

    @Column(name = "background_url")
    private String mBackgroundUrl;

    @Column(name = "followers_count")
    private Long mFollowersCount;

    @Column(name = "friends_count")
    private Long mFriendsCount;

    public String getmBackgroundUrl() {
        return mBackgroundUrl;
    }

    public static User fromJSON(JSONObject jsonObject) {
        User user = new User();
        try {
            user.mName = jsonObject.getString("name");
            user.mId = jsonObject.getLong("id");
            user.mScreenName = jsonObject.getString("screen_name");
            user.mImageUrl = jsonObject.getString("profile_image_url");
            user.mBackgroundUrl = jsonObject.optString("profile_banner_url");
            if (user.mBackgroundUrl != null) user.mBackgroundUrl += "/600x200";
            user.mFollowersCount = jsonObject.getLong("followers_count");
            user.mFriendsCount = jsonObject.getLong("friends_count");
            user.save();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return user;
    }

    // Finds existing user based on remoteId or creates new user and returns
    public static User findOrCreateFromJson(JSONObject jsonObject) {
        long rId = 0; // get just the remote id
        try {
            rId = jsonObject.getLong("id");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        User existingUser =
                new Select().from(User.class).where("remote_id = ?", rId).executeSingle();
        if (existingUser != null) {
            // found and return existing
            return User.fromJSON(jsonObject);
        } else {
            // create and return new user
            User user = User.fromJSON(jsonObject);
            user.save();
            return user;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mName);
        dest.writeLong(mId);
        dest.writeString(mScreenName);
        dest.writeString(mImageUrl);
    }

    protected User(Parcel in) {
        mName = in.readString();
        mId = in.readLong();
        mScreenName = in.readString();
        mImageUrl = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };


}
