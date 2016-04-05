package com.codepath.apps.TwitterClient.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;
import com.codepath.apps.TwitterClient.R;
import com.codepath.apps.TwitterClient.dialogs.ComposeDialog;
import com.codepath.apps.TwitterClient.fragments.HomeTimelineFragment;
import com.codepath.apps.TwitterClient.fragments.MentionTimelineFragment;
import com.codepath.apps.TwitterClient.fragments.TweetsListFragment;
import com.codepath.apps.TwitterClient.utils.SmartFragmentStatePagerAdapter;
import com.codepath.apps.TwitterClient.utils.TwitterApplication;
import com.codepath.apps.TwitterClient.utils.TwitterClient;

import butterknife.Bind;
import butterknife.ButterKnife;

public class TimelineActivity extends AppCompatActivity implements
        ComposeDialog.ComposeDialogListener, TweetsListFragment.TweetsListFragmentListener {

    @Bind(R.id.viewpager)
    ViewPager viewPager;
    @Bind(R.id.tabs)
    PagerSlidingTabStrip tabStrip;
    @Bind(R.id.fab)
    FloatingActionButton mFActButton;
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.pbProgressLoading)
    ProgressBar progressBar;

    private SmartFragmentStatePagerAdapter adapterViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);
        ButterKnife.bind(this);
        initialSetup();
    }

    private void initialSetup() {
        setSupportActionBar(toolbar);
        adapterViewPager = new TweetsPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapterViewPager);
        tabStrip.setViewPager(viewPager);
        mFActButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showComposeDialog();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_timeline, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_log_out:
                logOut();
                return true;
            case R.id.profile:
                launchProfileActivity();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void launchProfileActivity() {
        Intent i = new Intent(this, ProfileActivity.class);
        i.putExtra("screen_name", ""); // Null
        startActivity(i);
    }

    private void logOut() {
        TwitterClient client = TwitterApplication.getRestClient();
        client.clearAccessToken();
        finish();
    }

    @Override
    public void showProgressBarListener() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgressBarListener() {
        progressBar.setVisibility(View.INVISIBLE);
    }

    public class TweetsPagerAdapter extends SmartFragmentStatePagerAdapter {
        private String tabTitles[] = {"Home", "Mentions"};

        public TweetsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return (position == 0 ? new HomeTimelineFragment() : new MentionTimelineFragment());
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

    private void showComposeDialog() {
        FragmentManager fm = getSupportFragmentManager();
        ComposeDialog composeDialog = ComposeDialog.newInstance();
        composeDialog.setStyle(DialogFragment.STYLE_NORMAL, R.style.Dialog_FullScreen);
        composeDialog.show(fm, "fragment_compose");
    }

    @Override
    public void onFinishComposeDialog(String inputText, long replyID) {
        Toast.makeText(getBaseContext(), inputText, Toast.LENGTH_LONG).show();
        HomeTimelineFragment fragment = (HomeTimelineFragment) adapterViewPager.getRegisteredFragment(0);
        fragment.onFinishComposeDialog(inputText, replyID);
    }


}
