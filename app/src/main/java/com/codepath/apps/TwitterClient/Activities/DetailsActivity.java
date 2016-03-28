package com.codepath.apps.TwitterClient.Activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.text.util.Linkify;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.codepath.apps.TwitterClient.Dialogs.ComposeDialog;
import com.codepath.apps.TwitterClient.Java.TwitterApplication;
import com.codepath.apps.TwitterClient.Java.TwitterClient;
import com.codepath.apps.TwitterClient.Models.Tweet;
import com.codepath.apps.TwitterClient.R;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;

public class DetailsActivity extends AppCompatActivity implements ComposeDialog.ComposeDialogListener {

    Tweet tweet;
    @Bind(R.id.profile_image_view)
    ImageView mProfileImageView;
    @Bind(R.id.tweet_image_view)
    ImageView mTweetImage;
    @Bind(R.id.screen_name_text)
    TextView mScreenName;
    @Bind(R.id.user_name_text)
    TextView mUserName;
    @Bind(R.id.body_text)
    TextView mBodyMessage;
    @Bind(R.id.created_at_text)
    TextView mCreatedAt;
    @Bind(R.id.reply_button)
    TextView mReplyButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        ButterKnife.bind(this);
        Bundle bundle = this.getIntent().getExtras();
        tweet = bundle.getParcelable("tweet");
        setupView();
    }

    private void setupView() {
        mBodyMessage.setText(tweet.getmBody());
        Linkify.addLinks(mBodyMessage, Linkify.WEB_URLS);
        String time = null;
        time = setTime(tweet.getmCreatedAt());
        mCreatedAt.setText(time);
        mScreenName.setText("@" + tweet.getmUser().getmScreenName());
        mUserName.setText(tweet.getmUser().getmName());
        final ImageView profileImageView = mProfileImageView;
        Glide.with(this)
                .load(tweet.getmUser().getmImageUrl())
                .asBitmap().into(new BitmapImageViewTarget(profileImageView) {
            @Override
            protected void setResource(Bitmap resource) {
                RoundedBitmapDrawable circularBitmapDrawable =
                        RoundedBitmapDrawableFactory.create(getResources(), resource);
                circularBitmapDrawable.setCornerRadius(6);
                profileImageView.setImageDrawable(circularBitmapDrawable);
            }
        });
        if (tweet.getmImageUrl() != null) {
            mTweetImage.setVisibility(View.VISIBLE);
            final ImageView tweetImageView = mTweetImage;
            Glide.with(this)
                    .load(tweet.getmImageUrl())
                    .into(tweetImageView);
        } else {
            mTweetImage.setVisibility(View.GONE);
        }
    }

    private static String setTime(String rawJsonDate) {
        Date date = null;
        try {
            date = new SimpleDateFormat("EEE MMM dd HH:mm:ss ZZZZZ yyyy").parse(rawJsonDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new SimpleDateFormat("HH:mm - dd MMM, yyyy").format(date);
    }

    public void onReply(View view) {
        FragmentManager fm = getSupportFragmentManager();
        ComposeDialog composeDialog = ComposeDialog.newInstance();
        Bundle args = new Bundle();
        args.putString("screen_name", tweet.getmUser().getmScreenName());
        args.putLong("user_reply_id", tweet.getmUser().getmId());
        composeDialog.setArguments(args);
        composeDialog.setStyle(DialogFragment.STYLE_NORMAL, R.style.Dialog_FullScreen);
        composeDialog.show(fm, "fragment_compose");
    }

    @Override
    public void onFinishEditDialog(String inputText, long replyID) {
        if (isOnline()) {
            TwitterClient client = TwitterApplication.getRestClient();
            client.postTweet(inputText, replyID, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                }
                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                    Toast.makeText(getBaseContext(), "Request Limit Exceeded", Toast.LENGTH_LONG).show();
                }
            });
        } else {
            Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();
        }
    }

    public boolean isOnline() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }
}
