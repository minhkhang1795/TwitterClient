package com.codepath.apps.TwitterClient.Models;

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

    @Column(name = "name")
    private String mName;

    @Column(name = "remote_id", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    private long mId;

    @Column(name = "screen_name")
    private String mScreenName;

    @Column(name = "image_url")
    private String mImageUrl;

    public User() {
        super();
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
//            user.mId = jsonObject.getLong("id");
            user.mScreenName = jsonObject.getString("screen_name");
            user.mImageUrl = jsonObject.getString("profile_image_url");
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
            return existingUser;
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
}
