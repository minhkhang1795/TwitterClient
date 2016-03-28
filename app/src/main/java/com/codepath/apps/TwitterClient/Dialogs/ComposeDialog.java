package com.codepath.apps.TwitterClient.Dialogs;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.codepath.apps.TwitterClient.R;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ComposeDialog extends DialogFragment {

    @Bind(R.id.messege_text)
    EditText mComposeText;
    @Bind(R.id.tweet_button)
    Button mTweetButton;
    @Bind(R.id.counter_text_view)
    TextView mCounterTextView;
    @Bind(R.id.fragment_bottom_layout)
    RelativeLayout bottomLayout;
    @Bind(R.id.profile_image_view)
    ImageView mProfileImageView;
    @Bind(R.id.close_button)
    ImageButton mCloseButton;
    long replyID = -1;

    public ComposeDialog() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
    }

    public static ComposeDialog newInstance() {
        ComposeDialog frag = new ComposeDialog();
        Bundle args = new Bundle();
        frag.setArguments(args);
        return frag;
    }

    // Defines the listener interface
    public interface ComposeDialogListener {
        void onFinishEditDialog(String inputText, long replyID);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_compose, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setOnClickListener();
        addTextWatcher();

        String screenName = getArguments().getString("screen_name", "");
        if (screenName.length() > 0) {
            mComposeText.setText("@" + screenName + " ");
            mComposeText.setSelection(mComposeText.getText().length());
            replyID = getArguments().getLong("user_reply_id");
        }
        //TODO: Set user's profile image (Week 4)
//        Glide.with(getContext())
//                .load(url)
//                .asBitmap().into(new BitmapImageViewTarget(mProfileImageView) {
//            @Override
//            protected void setResource(Bitmap resource) {
//                RoundedBitmapDrawable circularBitmapDrawable =
//                        RoundedBitmapDrawableFactory.create(getContext().getResources(), resource);
//                circularBitmapDrawable.setCornerRadius(6);
//                mProfileImageView.setImageDrawable(circularBitmapDrawable);
//            }
//        });
    }

    @Override
    public void onResume() {
        makeFullScreen();
        // Call super onResume after sizing
        super.onResume();
        // After resize screen, show soft keyboard automatically and request focus to field
        showSoftKeyboard(mComposeText);
    }

    private void showSoftKeyboard(final View view) {
        view.requestFocus();
        // Show keyboard by simulating a tap on the TextField. Is there any other way!?
        (new Handler()).postDelayed(new Runnable() {
            public void run() {
                view.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, 0, 0, 0));
                view.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, 0, 0, 0));
                mComposeText.setSelection(mComposeText.getText().length());
            }
        }, 200);
    }

    private void makeFullScreen() {
        // Get existing layout params for the window
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        // Assign window properties to fill the parent
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
    }

    private void onCompose() {
        // Notice the use of `getTargetFragment` which will be set when the dialog is displayed
        ComposeDialogListener listener = (ComposeDialogListener) getActivity();
        listener.onFinishEditDialog(mComposeText.getText().toString(), replyID);
        dismiss();
    }

    private void setOnClickListener() {
        mCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        mTweetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCompose();
            }
        });
        bottomLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSoftKeyboard(mComposeText);
            }
        });
        mTweetButton.setClickable(false);
    }

    private void addTextWatcher() {
        mComposeText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mCounterTextView.setText(String.valueOf((140 - s.length())));
                if (s.length() > 140) mCounterTextView.setTextColor(Color.RED);
                else mCounterTextView.setTextColor(R.color.grayDark);
                int index = start - before;
                Log.d("DEBU", String.valueOf(index));
                if (isTextInRange(s) && isTextNotNull(s, index)) {
                    mTweetButton.setClickable(true);
                    mTweetButton.setBackgroundResource(R.drawable.tweet_button_enabled);
                } else {
                    mTweetButton.setClickable(false);
                    mTweetButton.setBackgroundResource(R.drawable.tweet_button_unenabled);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private Boolean isTextNotNull(final CharSequence s, final int index) {
        String string = s.toString();
        return !string.trim().isEmpty();
    }

    private Boolean isTextInRange(final CharSequence s) {
        return s.length() > 0 && s.length() <= 140;
    }
}