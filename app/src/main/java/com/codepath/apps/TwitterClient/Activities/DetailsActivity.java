package com.codepath.apps.TwitterClient.activities;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.util.Linkify;
import android.view.MenuItem;
import android.view.View;
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
    @Bind(R.id.reply_button)
    TextView mReplyButton;
    @Bind(R.id.toolbar)

    Toolbar toolbar;
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
