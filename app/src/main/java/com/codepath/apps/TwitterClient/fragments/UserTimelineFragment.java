package com.codepath.apps.TwitterClient.fragments;

/**
 * Created by duyvu on 4/2/16.
 */

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
 * Created by duyvu on 4/2/16.
 */
public class UserTimelineFragment extends TweetsListFragment {

    private static String screenName;
    public static UserTimelineFragment newInstance(String sc) {
        UserTimelineFragment userTimelineFragment = new UserTimelineFragment();
        screenName = sc;
        return userTimelineFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isCalledFromProfile = false;
    }

    @Override
    protected void callApi(int page) {
        client.getUserTimeline(screenName, page, new JsonHttpResponseHandler() {
            // Success
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                addAll(Tweet.fromJSONArray(response));
            }

            // Failure
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                Toast.makeText(getActivity(), "Request Limit Exceeded 2", Toast.LENGTH_LONG).show();
                mPtrFrame.refreshComplete();
                hideProgressbar();
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