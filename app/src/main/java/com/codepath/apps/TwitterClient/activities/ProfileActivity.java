package com.codepath.apps.TwitterClient.activities;

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
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.astuetz.PagerSlidingTabStrip;
import com.codepath.apps.TwitterClient.R;
import com.codepath.apps.TwitterClient.fragments.UserTimelineFragment;
import com.codepath.apps.TwitterClient.models.User;
import com.codepath.apps.TwitterClient.utils.TwitterApplication;
import com.codepath.apps.TwitterClient.utils.TwitterClient;
import com.codepath.apps.TwitterClient.utils.Utils;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONObject;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ProfileActivity extends AppCompatActivity {

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

    TwitterClient client;
    User user;

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
        if (user.getmBackgroundUrl() != null)
            Utils.inflateImage(this, user.getmBackgroundUrl(), backgroundImage);

        if (user.getmImageUrl() != null) {
            Utils.inflateImage(this, user.getmImageUrl(), mProfileImageView);
        }
        mScreenName.setText(user.getmScreenName());
        mUserName.setText(user.getmName());
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
                    collapsingToolbarLayout.setContentScrimColor(getResources().getColor(R.color.colorPrimaryTransparent2));
                    isShow = true;
                } else if (isShow) {
                    collapsingToolbarLayout.setTitle("");
                    collapsingToolbarLayout.setContentScrimColor(getResources().getColor(R.color.colorPrimaryTransparent));
                    isShow = false;
                }
            }
        });
    }

    public void getData() {
        mScreenName.setText(getIntent().getStringExtra("screen_name"));
        if (mScreenName.toString().equals("")) {
            client.getUserInfo(new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    user = User.fromJSON(response);
                    Log.d("DEBUG-EEE", response.toString());
                    setupViews();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    Log.d("DEBUG-EEE", errorResponse.toString());
                }
            });
        } else {
            client.getOtherUserInfo(mScreenName.toString(), new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    user = User.fromJSON(response);
                    Log.d("DEBUG-EEE", response.toString());
                    setupViews();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    Log.d("DEBUG-EEE", errorResponse.toString());
                }
            });
        }
    }

    public class TweetsPagerAdapter extends FragmentPagerAdapter {
        private String tabTitles[] = {"Tweet"};//{"Tweets", "Photos", "Favorites"};

        public TweetsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0)
                return UserTimelineFragment.newInstance(mScreenName.toString());
//            else if (position == 1)
//                return new MentionTimelineFragment();
            else
                return null;
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
