package com.codepath.apps.TwitterClient.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.codepath.apps.TwitterClient.R;
import com.codepath.apps.TwitterClient.activities.DetailsActivity;
import com.codepath.apps.TwitterClient.adapters.TweetsAdapter;
import com.codepath.apps.TwitterClient.dialogs.ComposeDialog;
import com.codepath.apps.TwitterClient.models.Tweet;
import com.codepath.apps.TwitterClient.utils.DividerItemDecoration;
import com.codepath.apps.TwitterClient.utils.EndlessRecyclerViewScrollListener;
import com.codepath.apps.TwitterClient.utils.ItemClickSupport;
import com.codepath.apps.TwitterClient.utils.TwitterApplication;
import com.codepath.apps.TwitterClient.utils.TwitterClient;
import com.codepath.apps.TwitterClient.utils.Utils;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import in.srain.cube.views.ptr.PtrClassicFrameLayout;
import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;

/**
 * Created by duyvu on 4/1/16.
 */
public abstract class TweetsListFragment extends Fragment implements ComposeDialog.ComposeDialogListener {

    @Bind(R.id.store_house_ptr_frame)
    PtrClassicFrameLayout mPtrFrame;
    @Bind(R.id.recycler_view)
    RecyclerView mRecyclerView;

    private LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
    protected Boolean isCalledFromProfile;
    protected TwitterClient client;
    protected ArrayList<Tweet> mTweets = new ArrayList<>();
    protected TweetsAdapter mTweetsAdapter = new TweetsAdapter(mTweets);

    protected abstract void callApi(int page);
    protected abstract void loadOfflineData();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        client = TwitterApplication.getRestClient();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = null;
        if (isCalledFromProfile) {
            view = inflater.inflate(R.layout.fragment_tweet_list_profile, container, false);
        } else {
            view = inflater.inflate(R.layout.fragment_tweet_list, container, false);
        }
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupPullToRefresh();
        populateTimeline(1);
        setupRecyclerView();
    }

    private void setupRecyclerView() {
        mRecyclerView.setAdapter(mTweetsAdapter);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity()));
        ItemClickSupport.addTo(mRecyclerView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                Intent i = new Intent(getActivity(), DetailsActivity.class);
                Tweet tweet = mTweets.get(position);
                i.putExtra("tweet", tweet);
                startActivityForResult(i, 200);
            }
        });
        mRecyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                populateTimeline(page + 1);
            }
        });
    }

    public void addAll(List<Tweet> tweets) {
        mTweets.addAll(tweets);
        mTweetsAdapter.notifyDataSetChanged();
        mPtrFrame.refreshComplete();
    }

    public void addAllItemRangeInserted(List<Tweet> tweets) {
        mTweets.addAll(tweets);
        mTweetsAdapter.notifyItemRangeInserted(mTweetsAdapter.getItemCount(), mTweets.size() - 1);
        mPtrFrame.refreshComplete();
    }

    public void clear() {
        mTweets.clear();
        mTweetsAdapter.notifyDataSetChanged();
    }

    private void setupPullToRefresh() {
        mPtrFrame.setLastUpdateTimeRelateObject(this);
        mPtrFrame.setPtrHandler(new PtrDefaultHandler() {
            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                populateTimeline(1);
            }

            @Override
            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
                return PtrDefaultHandler.checkContentCanBePulledDown(frame, content, header);
            }
        });
    }

    @Override
    public void onFinishComposeDialog(String inputText, long replyID) {
        if (Utils.isOnline()) {
            client.postTweet(inputText, replyID, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    mTweets.add(0, Tweet.findOrCreateFromJson(response));
                    mTweetsAdapter.notifyItemInserted(0);
                    mRecyclerView.scrollToPosition(0);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                    Toast.makeText(getActivity(), "Request Limit Exceeded", Toast.LENGTH_LONG).show();
                }
            });
        } else {
            Toast.makeText(getActivity(), "No Internet Connection", Toast.LENGTH_LONG).show();
        }
    }

    protected void populateTimeline(int page){
        if (Utils.isOnline()) {
            if (page == 1) {
                clear();
            }
            callApi(page);
        } else {
            loadOfflineData();
        }
    }
}
