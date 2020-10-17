package com.codepath.apps.restclienttemplate;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONException;
import org.parceler.Parcels;

import java.util.Locale;

import okhttp3.Headers;

import static com.codepath.apps.restclienttemplate.models.Tweet.*;

public class ComposeActivity extends AppCompatActivity {

    public static final String TAG="ComposeActivity";
    public static final int MAX_TWEET_LENGTH = 280;


    EditText etCompose;
    Button btnTweet;
    TextView tvCharCount;

    TwitterClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);

        client = TwitterApp.getRestClient(this);

        etCompose = findViewById(R.id.etCompose);
        btnTweet = findViewById(R.id.btnTweet);
        tvCharCount = findViewById(R.id.tvCharCount);
        etCompose.setFilters(new InputFilter[] { new InputFilter.LengthFilter(MAX_TWEET_LENGTH) });

        etCompose.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int remChars = 280 - s.toString().length();
                tvCharCount.setText(String.format(Locale.getDefault(), "%s", remChars));
                if(remChars == 0) {
                    tvCharCount.setTextColor(ContextCompat.getColor(ComposeActivity.this, R.color.red));
                } else {
                    tvCharCount.setTextColor(
                            ContextCompat.getColor(ComposeActivity.this, R.color.gray));
                }

                if(remChars == 280 || remChars == 0) {
                    btnTweet.setEnabled(false);
                }
                else{
                    btnTweet.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });


        btnTweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tweetContent = etCompose.getText().toString();

                if(tweetContent.isEmpty()){
                    Toast.makeText(ComposeActivity.this,"Sorry, your tweet cannot be empty",Toast.LENGTH_SHORT).show();
                    return;
                }

                client.publishTweet(tweetContent, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                        Log.i(TAG,"onSuccess to publish tweet");
                        try {
                            Tweet tweet = fromJson(json.jsonObject);
                            Log.i(TAG,"Published tweet says: "+tweet.body);
                            Intent intent = new Intent();
                            intent.putExtra("tweet", Parcels.wrap(tweet));
                            setResult(RESULT_OK,intent);
                            finish();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                            Log.e(TAG,"onFailure to publish tweet",throwable);
                    }
                });
            }
        });
    }
}