package com.codepath.apps.TwitterClient.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.codepath.apps.TwitterClient.R;
import com.codepath.apps.TwitterClient.models.Tweet;
import com.codepath.apps.TwitterClient.utils.Utils;

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
        Tweet tweet = mTweets.get(position);
        holder.mBodyMessage.setText(tweet.getmBody());
        Linkify.addLinks(holder.mBodyMessage, Linkify.WEB_URLS);
        String createdAtString = tweet.getRelativeTimeAgo(tweet.getmCreatedAt());

        holder.mCreatedAt.setText(createdAtString);
        holder.mScreenName.setText("@" + tweet.getmUser().getmScreenName());
        holder.mUserName.setText(tweet.getmUser().getmName());

        Utils.inflateImage(mContext, tweet.getmUser().getmImageUrl(), holder.mProfileImageView);

        if (tweet.getmImageUrl() != null) {
            holder.mTweetImage.setVisibility(View.VISIBLE);
            Utils.inflateImage(mContext, tweet.getmImageUrl(), holder.mTweetImage);
        } else {
            holder.mTweetImage.setVisibility(View.GONE);
        }
    }


    @Override
    public int getItemCount() {
        return mTweets != null ? mTweets.size() : 0;
    }
}
