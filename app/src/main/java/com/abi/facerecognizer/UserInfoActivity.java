package com.abi.facerecognizer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

public class UserInfoActivity extends AppCompatActivity {

    private EditText txtUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        this.txtUsername = (EditText) findViewById(R.id.txtUsername);
    }

    public void onStartClicked(View view) {

        String username = txtUsername.getText().toString().trim();
        Intent intent = new Intent(this, TrainActivity.class);
        intent.putExtra("username", username);
        startActivity(intent);
    }
}
