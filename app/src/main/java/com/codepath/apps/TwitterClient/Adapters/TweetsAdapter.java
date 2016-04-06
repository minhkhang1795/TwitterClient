package com.codepath.apps.TwitterClient.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.codepath.apps.TwitterClient.R;
import com.codepath.apps.TwitterClient.activities.ProfileActivity;
import com.codepath.apps.TwitterClient.dialogs.ComposeDialog;
import com.codepath.apps.TwitterClient.models.Tweet;
import com.codepath.apps.TwitterClient.utils.TwitterApplication;
import com.codepath.apps.TwitterClient.utils.Utils;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by duyvu on 3/25/16.
 */
public class TweetsAdapter extends RecyclerView.Adapter<TweetsAdapter.ViewHolder> {

    private ArrayList<Tweet> mTweets;
    private Context mContext;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.profile_image_view) ImageView mProfileImageView;
        @Bind(R.id.tweet_image_view) ImageView mTweetImage;
        @Bind(R.id.screen_name_text) TextView mScreenName;
        @Bind(R.id.user_name_text) TextView mUserName;
        @Bind(R.id.body_text) TextView mBodyMessage;
        @Bind(R.id.created_at_text) TextView mCreatedAt;
        @Bind(R.id.reply_button) ImageButton mReplyButton;
        @Bind(R.id.retweet_button) ImageButton mRetweetButton;
        @Bind(R.id.favorite_button) ImageButton mFavoriteButton;
        @Bind(R.id.retweet_text_view) TextView mRetweetCount;
        @Bind(R.id.favorite_text_view) TextView mFavoriteCount;


        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public TweetsAdapter(ArrayList<Tweet> tweets) {
        mTweets = tweets;
    }

    @Override
    public TweetsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(mContext);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.tweet_item_image, parent, false);

        // Return a new holder instance
        return new ViewHolder(contactView);
    }

    @Override
    public void onBindViewHolder(final TweetsAdapter.ViewHolder holder, int position) {
        final Tweet tweet = mTweets.get(position);
        holder.mBodyMessage.setText(tweet.getmBody());
        Linkify.addLinks(holder.mBodyMessage, Linkify.WEB_URLS);
        String createdAtString = tweet.getRelativeTimeAgo(tweet.getmCreatedAt());

        holder.mCreatedAt.setText(createdAtString);
        holder.mScreenName.setText("@" + tweet.getmUser().getmScreenName());
        holder.mUserName.setText(tweet.getmUser().getmName());

        Utils.inflateRoundedImage(mContext, tweet.getmUser().getmImageUrl(), holder.mProfileImageView);
        holder.mProfileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(mContext, ProfileActivity.class);
                i.putExtra("screen_name", tweet.getmUser().getmScreenName());
                mContext.startActivity(i);
            }
        });
        if (!tweet.getmImageUrl().equals("")) {
            holder.mTweetImage.setVisibility(View.VISIBLE);
            Utils.inflateRoundedImage(mContext, tweet.getmImageUrl(), holder.mTweetImage);
        } else {
            holder.mTweetImage.setVisibility(View.GONE);
        }
        holder.mFavoriteCount.setText(String.valueOf(tweet.getmFavoriteCount()));
        holder.mRetweetCount.setText(String.valueOf(tweet.getmRetweetCount()));
        holder.mReplyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = ((AppCompatActivity) mContext).getSupportFragmentManager();
                ComposeDialog composeDialog = ComposeDialog.newInstance();
                Bundle args = new Bundle();
                args.putString("screen_name", tweet.getmUser().getmScreenName());
                args.putLong("status_id", tweet.getmId());
                composeDialog.setArguments(args);
                composeDialog.setStyle(DialogFragment.STYLE_NORMAL, R.style.Dialog_FullScreen);
                composeDialog.show(fm, "fragment_compose");
            }
        });
        setupFavoriteBtn(holder, tweet);
        setupRetweetBtn(holder, tweet);
    }


    @Override
    public int getItemCount() {
        return mTweets != null ? mTweets.size() : 0;
    }

    private void setupFavoriteBtn(final TweetsAdapter.ViewHolder holder, final Tweet tweet) {
        if (tweet.getmIsFavorited()) Tweet.setFavoriteBtnOn(holder.mFavoriteButton, holder.mFavoriteCount, mContext.getResources());
        else Tweet.setFavoriteBtnOff(holder.mFavoriteButton, holder.mFavoriteCount, mContext.getResources());
        holder.mFavoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.mFavoriteButton.setClickable(false);
                if (tweet.getmIsFavorited()) {
                    // DESTROY FAVORITE
                    TwitterApplication.getRestClient().destroyFavorite(tweet.getmId(), new JsonHttpResponseHandler() {
                        // Success
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            Tweet.setFavoriteBtnOff(holder.mFavoriteButton, holder.mFavoriteCount, mContext.getResources());
                            tweet.setmFavoriteCount(tweet.getmFavoriteCount() - 1);
                            holder.mFavoriteButton.setClickable(true);
                            tweet.setmIsFavorited(false);
                            holder.mFavoriteCount.setText(String.valueOf(tweet.getmFavoriteCount()));
                        }

                        // Failure
                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            super.onFailure(statusCode, headers, throwable, errorResponse);
                            holder.mFavoriteButton.setClickable(true);
                            Log.d("DEBUG", errorResponse.toString());
                        }
                    });

                } else {
                    // CREATE FAVORITE
                    TwitterApplication.getRestClient().createFavorite(tweet.getmId(), new JsonHttpResponseHandler() {
                        // Success
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            Tweet.setFavoriteBtnOn(holder.mFavoriteButton, holder.mFavoriteCount, mContext.getResources());
                            tweet.setmFavoriteCount(tweet.getmFavoriteCount() + 1);
                            holder.mFavoriteButton.setClickable(true);
                            tweet.setmIsFavorited(true);
                            holder.mFavoriteCount.setText(String.valueOf(tweet.getmFavoriteCount()));
                        }

                        // Failure
                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            super.onFailure(statusCode, headers, throwable, errorResponse);
                            holder.mFavoriteButton.setClickable(true);
                            Log.d("DEBUG", errorResponse.toString());
                        }
                    });
                }
            }
        });
    }

    private void setupRetweetBtn(final TweetsAdapter.ViewHolder holder, final Tweet tweet) {
        if (tweet.getmIsRetweeted()) Tweet.setRetweetBtnOn(holder.mRetweetButton, holder.mRetweetCount, mContext.getResources());
        else Tweet.setRetweetBtnOff(holder.mRetweetButton, holder.mRetweetCount, mContext.getResources());
        holder.mRetweetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.mRetweetButton.setClickable(false);
                if (tweet.getmIsRetweeted()) {
                    // DESTROY RETWEET
                    TwitterApplication.getRestClient().destroyRetweet(tweet.getmId(), new JsonHttpResponseHandler() {
                        // Success
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            Tweet.setRetweetBtnOff(holder.mRetweetButton, holder.mRetweetCount, mContext.getResources());
                            tweet.setmRetweetCount(tweet.getmRetweetCount() - 1);
                            holder.mRetweetButton.setClickable(true);
                            tweet.setmIsRetweeted(false);
                            holder.mRetweetCount.setText(String.valueOf(tweet.getmRetweetCount()));
                        }

                        // Failure
                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            super.onFailure(statusCode, headers, throwable, errorResponse);
                            holder.mRetweetButton.setClickable(true);
                            Log.d("DEBUG", errorResponse.toString());
                        }
                    });

                } else {
                    // CREATE RETWEET
                    TwitterApplication.getRestClient().createRetweet(tweet.getmId(), new JsonHttpResponseHandler() {
                        // Success
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            Tweet.setRetweetBtnOn(holder.mRetweetButton, holder.mRetweetCount, mContext.getResources());
                            tweet.setmRetweetCount(tweet.getmRetweetCount() + 1);
                            holder.mRetweetButton.setClickable(true);
                            tweet.setmIsRetweeted(true);
                            holder.mRetweetCount.setText(String.valueOf(tweet.getmRetweetCount()));
                        }

                        // Failure
                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            super.onFailure(statusCode, headers, throwable, errorResponse);
                            holder.mRetweetButton.setClickable(true);
                            Log.d("DEBUG", errorResponse.toString());
                        }
                    });
                }
            }
        });
    }
}
