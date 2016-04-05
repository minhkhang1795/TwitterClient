package com.codepath.apps.TwitterClient.activities;

import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;
import com.codepath.apps.TwitterClient.R;
import com.codepath.apps.TwitterClient.dialogs.ComposeDialog;
import com.codepath.apps.TwitterClient.fragments.FavoriteTimelineFragment;
import com.codepath.apps.TwitterClient.fragments.TweetsListFragment;
import com.codepath.apps.TwitterClient.fragments.UserTimelineFragment;
import com.codepath.apps.TwitterClient.models.User;
import com.codepath.apps.TwitterClient.utils.TwitterApplication;
import com.codepath.apps.TwitterClient.utils.TwitterClient;
import com.codepath.apps.TwitterClient.utils.Utils;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ProfileActivity extends AppCompatActivity implements
        ComposeDialog.ComposeDialogListener, TweetsListFragment.TweetsListFragmentListener {

    @Bind(R.id.viewpager)
    ViewPager viewPager;
    @Bind(R.id.tabs)
    PagerSlidingTabStrip tabStrip;
    @Bind(R.id.background_image_view)
    ImageView backgroundImage;
    @Bind(R.id.toolbar_transparent)
    Toolbar toolbar;
    @Bind(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbarLayout;
    @Bind(R.id.appBarLayout)
    AppBarLayout appBarLayout;
    @Bind(R.id.user_name_text)
    TextView mUserName;
    @Bind(R.id.screen_name_text)
    TextView mScreenName;
    @Bind(R.id.profile_image_view)
    ImageView mProfileImageView;
    @Bind(R.id.follower_text)
    TextView mFollowersCount;
    @Bind(R.id.following_text)
    TextView mFriendsCount;
    @Bind(R.id.follower_count_text)
    TextView mFollowersText;
    @Bind(R.id.following_count_text)
    TextView mFriendsText;
    @Bind(R.id.pbProgressLoading)
    ProgressBar progressBar;

    TwitterClient client;
    User user = new User();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);

        client = TwitterApplication.getRestClient();
        getData();
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

    private void setupViews() {
        setupToolbar();
        resizeBackgroundImageLayout();
        Utils.inflateImagePicasso(this, user.getmBackgroundUrl(), backgroundImage);
        Utils.inflateImage(this, user.getmImageUrl(), mProfileImageView);
        mScreenName.setText("@" + user.getmScreenName());
        mUserName.setText(user.getmName());
        mFollowersCount.setText(String.valueOf(user.getmFollowersCount()));
        mFriendsCount.setText(String.valueOf(user.getmFriendsCount()));
        mFollowersText.setText("FOLLOWERS");
        mFriendsText.setText("FRIENDS");
        viewPager.setAdapter(new TweetsPagerAdapter(getSupportFragmentManager()));
        tabStrip.setViewPager(viewPager);
    }

    private void resizeBackgroundImageLayout() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        ViewGroup.LayoutParams params = backgroundImage.getLayoutParams();
        params.width = size.x;
        params.height = size.x / 3;
        backgroundImage.setLayoutParams(params);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    if (user == null) collapsingToolbarLayout.setTitle("Profile");
                    else collapsingToolbarLayout.setTitle(user.getmName());
                    collapsingToolbarLayout.setCollapsedTitleTextColor(getResources().getColor(R.color.white));
                    toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    isShow = true;
                } else if (isShow) {
                    collapsingToolbarLayout.setTitle("");
                    toolbar.setBackgroundColor(Color.TRANSPARENT);
                    isShow = false;
                }
            }
        });
    }

    public void getData() {
        user.setmScreenName(getIntent().getStringExtra("screen_name"));
        if (user.getmScreenName().equals("")) {
            client.getUserInfo(new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    user = User.fromJSON(response);
                    Log.d("DEBUG-SSS", response.toString());
                    setupViews();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    Log.d("DEBUG-EEE", errorResponse.toString());
                }
            });
        } else {
            // User already existed
            client.getOtherUserInfo(user.getmScreenName(), new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    user = User.fromJSON(response);
                    Log.d("DEBUG-S", response.toString());
                    setupViews();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    Log.d("DEBUG-E", errorResponse.toString());
                }
            });
        }
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

    @Override
    public void showProgressBarListener() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgressBarListener() {
        progressBar.setVisibility(View.INVISIBLE);
    }

    public class TweetsPagerAdapter extends FragmentPagerAdapter {
        private String tabTitles[] = {"Tweets", "Favorites"};

        public TweetsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0)
                return UserTimelineFragment.newInstance(user.getmScreenName());
            else
                return FavoriteTimelineFragment.newInstance(user.getmScreenName());
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabTitles[position];
        }

        @Override
        public int getCount() {
            return tabTitles.length;
        }
    }

}
