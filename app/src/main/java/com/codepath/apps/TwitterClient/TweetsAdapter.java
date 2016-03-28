package com.codepath.apps.TwitterClient;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.codepath.apps.TwitterClient.Models.Tweet;

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
        View contactView = inflater.inflate(R.layout.tweet_item, parent, false);

        // Return a new holder instance
        return new ViewHolder(contactView);
    }

    @Override
    public void onBindViewHolder(TweetsAdapter.ViewHolder holder, int position) {
        Tweet tweet = mTweets.get(position);
        holder.mBodyMessage.setText(tweet.getmBody());
        holder.mCreatedAt.setText(tweet.getmCreateAt());
        holder.mScreenName.setText("@" + tweet.getmUser().getmScreenName());
        holder.mUserName.setText(tweet.getmUser().getmName());
        final ImageView profileImageView = holder.mProfileImageView;
        Glide.with(mContext)
                .load(tweet.getmUser().getmImageUrl())
                .asBitmap().into(new BitmapImageViewTarget(profileImageView) {
            @Override
            protected void setResource(Bitmap resource) {
                RoundedBitmapDrawable circularBitmapDrawable =
                        RoundedBitmapDrawableFactory.create(mContext.getResources(), resource);
                circularBitmapDrawable.setCornerRadius(6);
                profileImageView.setImageDrawable(circularBitmapDrawable);
            }
        });
        final ImageView tweetImageView = holder.mTweetImage;
        Glide.with(mContext)
                .load(tweet.getmImageUrl())
                .asBitmap()
                .into(tweetImageView);
//        Glide.with(mContext)
//                .load(tweet.getmImageUrl())
//                .asBitmap().into(new BitmapImageViewTarget(tweetImageView) {
//            @Override
//            protected void setResource(Bitmap resource) {
//                RoundedBitmapDrawable circularBitmapDrawable =
//                        RoundedBitmapDrawableFactory.create(mContext.getResources(), resource);
//                circularBitmapDrawable.setCornerRadius(0);
//                tweetImageView.setImageDrawable(circularBitmapDrawable);
//            }
//        });
    }

    @Override
    public int getItemCount() {
        return mTweets != null ? mTweets.size() : 0;
    }
}