package com.codepath.apps.TwitterClient.Activities;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.activeandroid.query.Select;
import com.codepath.apps.TwitterClient.ComposeDialog;
import com.codepath.apps.TwitterClient.DividerItemDecoration;
import com.codepath.apps.TwitterClient.EndlessRecyclerViewScrollListener;
import com.codepath.apps.TwitterClient.Models.Tweet;
import com.codepath.apps.TwitterClient.R;
import com.codepath.apps.TwitterClient.TweetsAdapter;
import com.codepath.apps.TwitterClient.TwitterApplication;
import com.codepath.apps.TwitterClient.TwitterClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import in.srain.cube.views.ptr.PtrClassicFrameLayout;
import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;

public class TimelineActivity extends AppCompatActivity implements ComposeDialog.ComposeDialogListener {

    private TwitterClient client;
    private ArrayList<Tweet> mTweets = new ArrayList<>();
    private TweetsAdapter mTweetsAdapter = new TweetsAdapter(mTweets);
    private int mSinceId = 1;
    @Bind(R.id.store_house_ptr_frame) PtrClassicFrameLayout mPtrFrame;
    @Bind(R.id.recycler_view) RecyclerView mRecyclerView;
    @Bind(R.id.fab) FloatingActionButton mFActButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);
        ButterKnife.bind(this);
        client = TwitterApplication.getRestClient(); // singleton client
        setupView();
        dataHandler();
    }

    private void dataHandler() {
        populateTimeline(0);
    }

    private void setupView() {
        mRecyclerView.setAdapter(mTweetsAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this));
        mPtrFrame.setLastUpdateTimeRelateObject(this);
        mPtrFrame.setPtrHandler(new PtrDefaultHandler() {
            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                populateTimeline(0);
            }

            @Override
            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
                return PtrDefaultHandler.checkContentCanBePulledDown(frame, content, header);
            }
        });
        mRecyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                populateTimeline(page + 1);
            }
        });
        mFActButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showComposeDialog();
            }
        });
    }

    private void showComposeDialog() {
        FragmentManager fm = getSupportFragmentManager();
        ComposeDialog composeDialog = ComposeDialog.newInstance();
        composeDialog.setStyle(DialogFragment.STYLE_NORMAL, R.style.Dialog_FullScreen);
        composeDialog.show(fm, "fragment_compose");
    }

    // Send API request to get the timeline json
    // Fill the recycler view by creating a tweet object from the json
    private void populateTimeline(int page) {
        if (isOnline()) {
            if (page == 1) {
                mTweets.clear();
                mTweetsAdapter.notifyDataSetChanged();
            }
            client.getHomeTimeline(page, new JsonHttpResponseHandler() {
                // Success
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                    Log.d("DEBUG", response.toString());
                    mTweets.addAll(Tweet.fromJSONArray(response));
                    mTweetsAdapter.notifyItemRangeInserted(mTweetsAdapter.getItemCount(), mTweets.size() - 1);
                    mPtrFrame.refreshComplete();
                }

                // Failure
                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                    Toast.makeText(getBaseContext(), "Request Limit Exceeded 2", Toast.LENGTH_LONG).show();
                    mPtrFrame.refreshComplete();
                }
            });
        } else {
            List<Tweet> queryResults = new Select().from(Tweet.class).execute();
            mTweets.clear();
            mTweets.addAll(queryResults);
            mTweetsAdapter.notifyDataSetChanged();
            Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();
            mPtrFrame.refreshComplete();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_timeline, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
//        switch (id) {
//            case R.id.action_setting:
//                return true;
//                break;
//            default:
//                return true;
//                break;
//        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFinishEditDialog(String inputText) {
        if (isOnline()) {
            client.postTweet(inputText, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    mTweets.add(0, Tweet.fromJSON(response));
                    mTweetsAdapter.notifyItemInserted(0);
                    mRecyclerView.scrollToPosition(0);
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
