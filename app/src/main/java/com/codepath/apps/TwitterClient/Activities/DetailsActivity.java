package com.codepath.apps.TwitterClient.activities;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.util.Linkify;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.codepath.apps.TwitterClient.R;
import com.codepath.apps.TwitterClient.dialogs.ComposeDialog;
import com.codepath.apps.TwitterClient.models.Tweet;
import com.codepath.apps.TwitterClient.utils.TwitterApplication;
import com.codepath.apps.TwitterClient.utils.TwitterClient;
import com.codepath.apps.TwitterClient.utils.Utils;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;

public class DetailsActivity extends AppCompatActivity implements ComposeDialog.ComposeDialogListener {

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
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.retweet_text_view) TextView mRetweetCount;
    @Bind(R.id.favorite_text_view) TextView mFavoriteCount;
    @Bind(R.id.retweet_button)
    ImageButton mRetweetButton;
    @Bind(R.id.favorite_button)
    ImageButton mFavoriteButton;
    @Bind(R.id.reply_button)
    ImageButton mReplyButton;

    Tweet tweet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        ButterKnife.bind(this);
        getData();
        setupView();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // This is the up button
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setupView() {
        mBodyMessage.setText(tweet.getmBody());
        Linkify.addLinks(mBodyMessage, Linkify.WEB_URLS);
        String time = getFormatTime(tweet.getmCreatedAt());

        mCreatedAt.setText(time);
        mScreenName.setText("@" + tweet.getmUser().getmScreenName());
        mUserName.setText(tweet.getmUser().getmName());
        mRetweetCount.setText(String.valueOf(tweet.getmRetweetCount()));
        mFavoriteCount.setText(String.valueOf(tweet.getmFavoriteCount()));
        setupFavoriteBtn();
        setupRetweetBtn();

        Utils.inflateImage(this, tweet.getmUser().getmImageUrl(), mProfileImageView);
        if (tweet.getmImageUrl() != null) {
            mTweetImage.setVisibility(View.VISIBLE);
            Utils.inflateImage(this, tweet.getmImageUrl(), mTweetImage);
        } else mTweetImage.setVisibility(View.GONE);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Tweet");
        }
    }

    private void setupFavoriteBtn() {
        if (tweet.getmIsFavorited()) Tweet.setFavoriteBtnOn(mFavoriteButton, mFavoriteCount, getResources());
        else Tweet.setFavoriteBtnOff(mFavoriteButton, mFavoriteCount, getResources());
        mFavoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFavoriteButton.setClickable(false);
                if (tweet.getmIsFavorited()) {
                    // DESTROY FAVORITE
                    TwitterApplication.getRestClient().destroyFavorite(tweet.getmId(), new JsonHttpResponseHandler() {
                        // Success
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            Tweet.setFavoriteBtnOff(mFavoriteButton, mFavoriteCount, getResources());
                            tweet.setmFavoriteCount(tweet.getmFavoriteCount() - 1);
                            mFavoriteButton.setClickable(true);
                            tweet.setmIsFavorited(false);
                            mFavoriteCount.setText(String.valueOf(tweet.getmFavoriteCount()));
                        }

                        // Failure
                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            super.onFailure(statusCode, headers, throwable, errorResponse);
                            mFavoriteButton.setClickable(true);
                            Log.d("DEBUG", errorResponse.toString());
                        }
                    });

                } else {
                    // CREATE FAVORITE
                    TwitterApplication.getRestClient().createFavorite(tweet.getmId(), new JsonHttpResponseHandler() {
                        // Success
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            Tweet.setFavoriteBtnOn(mFavoriteButton, mFavoriteCount, getResources());
                            tweet.setmFavoriteCount(tweet.getmFavoriteCount() + 1);
                            mFavoriteButton.setClickable(true);
                            tweet.setmIsFavorited(true);
                            mFavoriteCount.setText(String.valueOf(tweet.getmFavoriteCount()));
                        }

                        // Failure
                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            super.onFailure(statusCode, headers, throwable, errorResponse);
                            mFavoriteButton.setClickable(true);
                            Log.d("DEBUG", errorResponse.toString());
                        }
                    });
                }
            }
        });
    }

    private void setupRetweetBtn() {
        if (tweet.getmIsRetweeted()) Tweet.setRetweetBtnOn(mRetweetButton, mRetweetCount, getResources());
        else Tweet.setRetweetBtnOff(mRetweetButton, mRetweetCount, getResources());
        mRetweetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRetweetButton.setClickable(false);
                if (tweet.getmIsRetweeted()) {
                    // DESTROY RETWEET
                    TwitterApplication.getRestClient().destroyRetweet(tweet.getmId(), new JsonHttpResponseHandler() {
                        // Success
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            Tweet.setRetweetBtnOff(mRetweetButton, mRetweetCount, getResources());
                            tweet.setmRetweetCount(tweet.getmRetweetCount() - 1);
                            mRetweetButton.setClickable(true);
                            tweet.setmIsRetweeted(false);
                            mRetweetCount.setText(String.valueOf(tweet.getmRetweetCount()));
                        }

                        // Failure
                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            super.onFailure(statusCode, headers, throwable, errorResponse);
                            mRetweetButton.setClickable(true);
                            Log.d("DEBUG", errorResponse.toString());
                        }
                    });

                } else {
                    // CREATE RETWEET
                    TwitterApplication.getRestClient().createRetweet(tweet.getmId(), new JsonHttpResponseHandler() {
                        // Success
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            Tweet.setRetweetBtnOn(mRetweetButton, mRetweetCount, getResources());
                            tweet.setmRetweetCount(tweet.getmRetweetCount() + 1);
                            mRetweetButton.setClickable(true);
                            tweet.setmIsRetweeted(true);
                            mRetweetCount.setText(String.valueOf(tweet.getmRetweetCount()));
                        }

                        // Failure
                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            super.onFailure(statusCode, headers, throwable, errorResponse);
                            mRetweetButton.setClickable(true);
                            Log.d("DEBUG", errorResponse.toString());
                        }
                    });
                }
            }
        });
    }

    private static String getFormatTime(String rawJsonDate) {
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
        args.putLong("status_id", tweet.getmId());
        composeDialog.setArguments(args);
        composeDialog.setStyle(DialogFragment.STYLE_NORMAL, R.style.Dialog_FullScreen);
        composeDialog.show(fm, "fragment_compose");
    }

    @Override
    public void onFinishComposeDialog(String inputText, long replyID) {
        if (Utils.isOnline()) {
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

    public void getData() {
        Bundle bundle = this.getIntent().getExtras();
        tweet = bundle.getParcelable("tweet");
    }
}
