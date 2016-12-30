package com.humanplus.facebooklogin;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.StrictMode;
import android.support.annotation.RequiresPermission;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.internal.Utility;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

import static android.R.attr.dashGap;
import static android.R.attr.data;

public class MainActivity extends AppCompatActivity {
    private LoginButton loginButton;
    private CallbackManager callbackManager;
    private ImageView mImageView;
    private AccessToken token;
    private Bitmap fb_img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        FacebookSdk.sdkInitialize(this);
        AppEventsLogger.activateApp(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageView = (ImageView)findViewById(R.id.imageView);

        loginButton = (LoginButton)findViewById(R.id.login_button);
        loginButton.setReadPermissions("email");
        token = AccessToken.getCurrentAccessToken();

        // Set callback
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(final LoginResult login_result) {
                        GraphRequest request = GraphRequest.newMeRequest(login_result.getAccessToken(),
                                new GraphRequest.GraphJSONObjectCallback() {
                                    @Override
                                    public void onCompleted(
                                            JSONObject object,
                                            GraphResponse response) {

                                        token= login_result.getAccessToken();
                                        response.getError();

                                        try {
                                            if (android.os.Build.VERSION.SDK_INT > 9) {
                                                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                                                StrictMode.setThreadPolicy(policy);
                                                String profilePicUrl = object.getJSONObject("picture").getJSONObject("data").getString("url");

                                                URL fb_url = new URL(profilePicUrl);//small | noraml | large
                                                HttpsURLConnection conn1 = (HttpsURLConnection) fb_url.openConnection();
                                                HttpsURLConnection.setFollowRedirects(true);
                                                conn1.setInstanceFollowRedirects(true);
                                                Bitmap fb_img = BitmapFactory.decodeStream(conn1.getInputStream());
                                                mImageView.setImageBitmap(fb_img);
                                            }
                                        }catch (Exception ex) {
                                            ex.printStackTrace();
                                        }
                                    }
                                });
                        Bundle parameters = new Bundle();
                        parameters.putString("fields", "id,picture");
                        request.setParameters(parameters);
                        request.executeAsync();
                    }

                    @Override
                    public void onCancel() {
                        System.out.println("Cancelled");
                    }

                    @Override
                    public void onError(FacebookException error) {
                        error.printStackTrace();
                    }
                });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode,resultCode,data);
    }

    public void onClickButton(View v) {
        switch(v.getId()) {
            case R.id.button2:
                GraphRequest graphRequest2 = GraphRequest.newMeRequest(token, new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {

                    }
                });
                break;
            case R.id.button3:
                /*
                GraphRequest graphRequest = GraphRequest.newMeRequest(token, new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        try {
                            JSONArray jsonArray = object.getJSONObject("friendlists").getJSONArray("data");
                            JSONObject friendlistObject = jsonArray.getJSONObject(0);
                            String friendListID = friendlistObject.getString("id");
                            myNewGraphReq(friendListID);
                        } catch(Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

                Bundle param = new Bundle();
                param.putString("fields", "friendlists");
                graphRequest.setParameters(param);
                graphRequest.executeAsync();
                break;
                */

                // Here is the usage.
                myNewGraphReq("me");
                break;
        }
    }

    private void myNewGraphReq(String friendlistId) {
        // Set graph path
        // will looks like https://graph.facebook.com/friendlistId/

        final String graphPath = "/"+friendlistId;
        AccessToken token = AccessToken.getCurrentAccessToken();

        // For 4th param, generally we can use .GET otherwise POST, DELETE
        GraphRequest request = new GraphRequest(token, graphPath, null, HttpMethod.GET, new GraphRequest.Callback() {
            @Override
            public void onCompleted(GraphResponse graphResponse) {

                JSONObject object = graphResponse.getJSONObject();
                try {
                    String profilePicUrl = object.getJSONObject("picture").getJSONObject("data").getString("url");
                    Log.i("fb: ", profilePicUrl);
                        try {
                            final URL fb_url = new URL(profilePicUrl); //small | noraml | large
                            new Thread() {
                                public void run() {
                                    try {
                                        HttpsURLConnection conn1 = (HttpsURLConnection) fb_url.openConnection();
                                        HttpsURLConnection.setFollowRedirects(true);
                                        conn1.setInstanceFollowRedirects(true);
                                        fb_img = BitmapFactory.decodeStream(conn1.getInputStream());
                                    }catch(Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }.start();

                            mImageView.setImageBitmap(fb_img);

                            // In here, we set profile image to mImageView defined in xml file
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                }
                catch(JSONException e){
                        e.printStackTrace();
                }
            }
        });

        Bundle param = new Bundle();
        param.putString("fields", "picture");
        request.setParameters(param);
        request.executeAsync();
    }

    private void callFriendList() {
        GraphRequest request = GraphRequest.newMeRequest(token,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(
                            JSONObject object,
                            GraphResponse response) {

                        response.getError();

                        try {
                            if (android.os.Build.VERSION.SDK_INT > 9) {
                                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                                StrictMode.setThreadPolicy(policy);
                                String profilePicUrl = object.getJSONObject("picture").getJSONObject("data").getString("url");

                                ArrayList<String> list = new ArrayList<String>();
                                JSONArray array = object.getJSONArray("data");
                                JSONObject object1;
                                for(int i =0;i<array.length();i++) {
                                    object1 = array.getJSONObject(i);
                                    list.add(i, object1.getString("id"));
                                }
                            }
                        }catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,picture");
        request.setParameters(parameters);
        Log.i("fb: ", parameters.toString());
        request.executeAsync();
    }

}