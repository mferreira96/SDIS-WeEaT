package com.example.josemartins.sdis_weeat.gui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.josemartins.sdis_weeat.R;
import com.example.josemartins.sdis_weeat.logic.Utils;
import network.Client;
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


public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{


    private SignInButton signIn;
    private GoogleApiClient googleApiClient;
    private static final int REQUEST_CODE = 9001;
    private static final String TAG = "SignInActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Utils.context = this;


        signIn = (SignInButton) findViewById(R.id.signInButton);
        signIn.setOnClickListener(signInListener);

        Button settingBtn= (Button) findViewById(R.id.settings_btn);
        settingBtn.setOnClickListener((View v) -> settingsHandler());


        customizeSignInButton();

        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().requestIdToken("1077664049472-kcih82jenig0b27oge2ubekqqk5414qp.apps.googleusercontent.com").build();
        googleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(this,this).addApi(Auth.GOOGLE_SIGN_IN_API,signInOptions).build();

        try {
            Utils.client = new Client(Utils.context,googleApiClient);
        } catch (Exception e) {
            e.printStackTrace();
        }
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

            Intent i = new Intent(this,ChooseServer.class);
            startActivity(i);
        }
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

    public void settingsHandler(){

        LayoutInflater li = LayoutInflater.from(Utils.context);
        View prompt = li.inflate(R.layout.start_dialog,null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Utils.context);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(prompt);
        alertDialogBuilder.setTitle("Finding a new experience");
        alertDialogBuilder.setPositiveButton("Go", (dialog, id) -> {
            String ip = ((EditText) prompt.findViewById(R.id.ipValue)).getText().toString();
            String port= ((EditText) prompt.findViewById(R.id.portValue)).getText().toString();


            StringBuilder sbLoadBalancer = new StringBuilder();
            sbLoadBalancer.append("https://");
            sbLoadBalancer.append(ip);
            sbLoadBalancer.append(":");
            sbLoadBalancer.append(port);

            Log.d("debug", "url-> " + Utils.loadBalancerUrl + " newurl-> " + sbLoadBalancer.toString());

            Utils.loadBalancerUrl = sbLoadBalancer.toString();
        });

        alertDialogBuilder.setNegativeButton("Cancel", (dialog, id) -> dialog.cancel());

        signIn = (SignInButton) findViewById(R.id.signInButton);
        signIn.setOnClickListener(signInListener);

        alertDialogBuilder.show();
    }


}
