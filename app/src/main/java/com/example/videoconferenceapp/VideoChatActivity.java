package com.example.videoconferenceapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.renderscript.Sampler;
import android.util.Log;
import android.util.Pair;
import android.view.Surface;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.videoconferenceapp.model.CameraVideoCapturer;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;

import org.json.JSONException;
import org.json.JSONObject;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class VideoChatActivity extends AppCompatActivity implements Session.SessionListener, PublisherKit.PublisherListener {

    private static String API_KEY;
    private static String SESSION_ID;
    private static String TOKEN;
    private static final String LOG_TAG = VideoChatActivity.class.getSimpleName();
    private static final int RC_VIDEO_APP_PERM = 124;

    private FrameLayout mPublisherViewController;
    private FrameLayout mSubscriberViewController;
    private Session mSession;
    private Publisher mPublisher;
    private Subscriber mSubscriber;

    private ImageView closeVideoChatBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_chat);

        closeVideoChatBtn = findViewById(R.id.close_video_chat_btn);
        closeVideoChatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeVideoCallChat();
            }
        });
        requestPermissions();
    }

    private void closeVideoCallChat(){
        mSession.unpublish(mPublisher);
        mSession.disconnect();

        Intent intent = new Intent(VideoChatActivity.this, CallsActivity.class);
        intent.putExtra("callEnded", "true");
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @AfterPermissionGranted(RC_VIDEO_APP_PERM)
    private void requestPermissions(){
        String perms[] = {Manifest.permission.INTERNET, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};

        if(EasyPermissions.hasPermissions(this, perms)){
            mPublisherViewController = findViewById(R.id.publisher_container);
            mSubscriberViewController = findViewById(R.id.subscriber_container);

            fetchSessionConnectionData();
        }else{
            EasyPermissions.requestPermissions(this, "Hey this app needs Camera, Please allow.", RC_VIDEO_APP_PERM, perms);
        }
    }

    public void fetchSessionConnectionData() {
        SessionAsyncTask sessionAsyncTask = new SessionAsyncTask(VideoChatActivity.this);
        sessionAsyncTask.execute();
    }

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {
        Log.d("VideoChat", " onStreamCreated method");
    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {
        Log.d("VideoChat " ,"onStreamDestroyed method");
        if(mPublisher != null){
            mPublisher = null;
            mPublisherViewController.removeAllViews();
            setSessionExpire(mSession.getSessionId());
        }

    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {

    }

    @Override
    public void onConnected(Session session) {
        Log.i(LOG_TAG, "Session Connected");

        mPublisher = new Publisher.Builder(this).build();
        mPublisher.setPublisherListener(this);
        mPublisher.setCapturer(new CameraVideoCapturer(VideoChatActivity.this, Publisher.CameraCaptureResolution.MEDIUM, Publisher.CameraCaptureFrameRate.FPS_30));
        mPublisherViewController.addView(mPublisher.getView());

        if(mPublisher.getView() instanceof GLSurfaceView){
            ((GLSurfaceView) mPublisher.getView()).setZOrderOnTop(true);
        }

        mSession.publish(mPublisher);
    }

    @Override
    public void onDisconnected(Session session) {
        Log.i(LOG_TAG, "Stream Disconnected");
        //mSession.disconnect();
    }

    @Override
    public void onStreamReceived(Session session, Stream stream) {
        Log.i(LOG_TAG, "Stream Received");
        if(mSubscriber == null){
            mSubscriber = new Subscriber.Builder(this, stream).build();
            mSession.subscribe(mSubscriber);
            mSubscriberViewController.addView(mSubscriber.getView());
        }
    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {
        Log.i(LOG_TAG, "Stream Dropped");

        if(mSubscriber != null){
            mSubscriber = null;
            mSubscriberViewController.removeAllViews();
        }
    }

    @Override
    public void onError(Session session, OpentokError opentokError) {
        Log.i(LOG_TAG, "Stream Error");
        Log.i(LOG_TAG, opentokError.getMessage());
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    void processValue(Session executedSession) {
        mSession = executedSession;
    }

    private void setSessionExpire(String sessionId){
        RequestQueue reqQueue = Volley.newRequestQueue(this);
        reqQueue.add(new JsonObjectRequest(Request.Method.GET,
                //192.168.0.14
                "http://10.0.2.2:8080/sessions/" + sessionId,
                null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                try {
                    String sessionId = response.getString("sessionId");
                    String update = response.getString("updated");
                    String expire = response.getString("expire");
                    Log.d("SessionId", sessionId);
                    Log.d("Update", update);
                    Log.d("Expire", expire);

                } catch (JSONException error) {
                    Log.e(LOG_TAG, "Web Service error: " + error.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(LOG_TAG, "Web Service error: " + error.getMessage());
            }
        }));
    }

    public class SessionAsyncTask extends AsyncTask<Void, Void, Void> {

        private Context context;
        private Session session;

        public SessionAsyncTask(Context videoChatActivity){
            this.context = videoChatActivity;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            RequestQueue reqQueue = Volley.newRequestQueue(context);
            reqQueue.add(new JsonObjectRequest(Request.Method.GET,
                    //10.0.2.2
                    "http://10.0.2.2:8080/sessions",
                    null, new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {
                    try {
                        String API_KEY = response.getString("apiKey");
                        String SESSION_ID = response.getString("sessionId");
                        String TOKEN = response.getString("token");

                        Log.i("API_KEY: ", API_KEY);
                        Log.i("SESSION_ID: ", SESSION_ID);
                        Log.i("TOKEN: ", TOKEN);

                        session = new Session.Builder(context, API_KEY, SESSION_ID).build();
                        session.setSessionListener((Session.SessionListener) context);
                        session.connect(TOKEN);
                        processValue(session);
                        Log.d("SESSION_IN_ASYNC", session.toString());

                    } catch (JSONException error) {
                        Log.e("Web Service error: ", error.getMessage());
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("Web Service error: ", error.getMessage());
                }
            }));
            return null;
        }
    }

}
