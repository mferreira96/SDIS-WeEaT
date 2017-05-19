package com.example.josemartins.sdis_weeat.gui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.josemartins.sdis_weeat.R;
import com.example.josemartins.sdis_weeat.logic.Utils;
import network.Client;
import network.NotificationsWebSocket;
import network.messaging.Message;
import network.messaging.distributor.server.ServerDistributor;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;


import org.json.JSONObject;
import org.w3c.dom.Text;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{

    Context context;
    private SignInButton signIn;
    private GoogleApiClient googleApiClient;
    private static final int REQUEST_CODE = 9001;
    private static final String TAG = "SignInActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.context = this;

        try {
            Utils.client = new Client(context);
        } catch (Exception e) {
            e.printStackTrace();
        }

        signIn = (SignInButton) findViewById(R.id.signInButton);
        signIn.setOnClickListener(signInListener);

        customizeSignInButton();

        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().requestIdToken("1077664049472-kcih82jenig0b27oge2ubekqqk5414qp.apps.googleusercontent.com").build();
        googleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(this,this).addApi(Auth.GOOGLE_SIGN_IN_API,signInOptions).build();


    }


    View.OnClickListener signInListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent i = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
            startActivityForResult(i,REQUEST_CODE);
        }
    };

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }
    

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_CODE){
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleResult(result);
        }
    }


    private void handleResult(GoogleSignInResult result){

        if(result.isSuccess()){
            GoogleSignInAccount account = result.getSignInAccount();

            Utils.client.setAccount(account);

            findViewById(R.id.signInButton).setVisibility(View.INVISIBLE);
            //findViewById(R.id.logout).setVisibility(View.VISIBLE);

            request();
        }
    }


    public void logout(View v){
        Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(status -> {
                findViewById(R.id.signInButton).setVisibility(View.VISIBLE);
                findViewById(R.id.logout).setVisibility(View.INVISIBLE);
                Utils.client.setAccount(null);
            }
        );
    }

    public void request(){

        try {
            JSONObject jsonUser = new JSONObject();
            jsonUser.put("token",Utils.client.getToken());
            Utils.client.makeRequest(Utils.serverUrl,"POST",new Message(ServerDistributor.ADD_USER, jsonUser.toString()));


        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d("debug","activity-----");
        Intent i = new Intent(this,ChooseLocal.class);
        startActivity(i);
    }


    private void customizeSignInButton(){
        for(int i = 0; i < signIn.getChildCount(); i++){
            View v = signIn.getChildAt(i);

            if(v instanceof TextView){
                TextView tv = (TextView)v;
                tv.setAllCaps(true);
            }
        }
    }
}
