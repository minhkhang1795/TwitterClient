package com.codepath.apps.TwitterClient.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

import com.codepath.apps.TwitterClient.utils.TwitterClient;
import com.codepath.apps.TwitterClient.R;
import com.codepath.oauth.OAuthLoginActionBarActivity;

import butterknife.Bind;
import butterknife.ButterKnife;

public class LoginActivity extends OAuthLoginActionBarActivity<TwitterClient> {

    @Bind(R.id.twitter_logo_image)
    ImageView twitterImage;
    TranslateAnimation animationDown, animationUp;
    Float originalY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        originalY = twitterImage.getY();
        setupAnimation();
    }

    private void setupAnimation() {
        animationDown = new TranslateAnimation(0.0f, 0.0f,
                0.0f, 400.0f);          //  new TranslateAnimation(xFrom,xTo, yFrom,yTo)animationUp
        animationUp = new TranslateAnimation(0.0f, 0.0f,
                400.0f, 00.0f);
        animationDown.setDuration(1000);  // animation duration
        animationUp.setDuration(1000);
        animationUp.setStartOffset(1000);
    }


    // Inflate the menu; this adds items to the action bar if it is present.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }

    // OAuth authenticated successfully, launch primary authenticated activity
    // i.e Display application "homepage"
    @Override
    public void onLoginSuccess() {
        final Intent i = new Intent(this, TimelineActivity.class);
        twitterImage.startAnimation(animationDown);  // start animation
        animationDown.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                startActivity(i);
                animation.setFillAfter(true);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    // OAuth authentication flow failed, handle the error
    // i.e Display an error dialog or toast
    @Override
    public void onLoginFailure(Exception e) {
        e.printStackTrace();
    }

    // Click handler method for the button used to start OAuth flow
    // Uses the client to initiate OAuth authorization
    // This should be tied to a button used to login
    public void loginToRest(View view) {
        getClient().connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (twitterImage.getY() != originalY) {
            twitterImage.startAnimation(animationUp);
        }
    }
}
