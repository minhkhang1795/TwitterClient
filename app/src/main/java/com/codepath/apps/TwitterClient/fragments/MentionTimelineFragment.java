package com.codepath.apps.TwitterClient.fragments;

import android.os.Bundle;
import android.widget.Toast;

import com.activeandroid.query.Select;
import com.codepath.apps.TwitterClient.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by duyvu on 4/1/16.
 */
public class MentionTimelineFragment extends TweetsListFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isCalledFromProfile = false;
    }

    @Override
    protected void callApi(int page) {
        client.getMentionTimeline(page, new JsonHttpResponseHandler() {
            // Success
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                addAllItemRangeInserted(Tweet.fromJSONArray(response));
            }

            // Failure
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                Toast.makeText(getActivity(), "Request Limit Exceeded 2", Toast.LENGTH_LONG).show();
                mPtrFrame.refreshComplete();
            }
        });
    }

    @Override
    protected void loadOfflineData() {
        List<Tweet> queryResults = new Select().from(Tweet.class).execute();
        clear();
        addAll(queryResults);
        Toast.makeText(getActivity() , "No Internet Connection", Toast.LENGTH_LONG).show();
    }

}
