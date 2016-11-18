package com.example.jose.fingerunlock;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    private String gameSlug;
    private String gamePositions;

    public final static String POSITION_MESSAGE = "com.example.fingering.POSITION";


    private class Request extends ApiRequest {

        @Override
        protected void onPostExecute(String result) {
            gamePositions = result;
            unlockReq(gamePositions);
        }
    }

    public void unlockReq(String positions){
        Intent intent = new Intent(this, PlayActivity.class);
        intent.putExtra(POSITION_MESSAGE, positions);
        startActivity(intent);
        finish();
    }

    public void getId(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter ID");
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        builder.setPositiveButton("Unlock", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String m_Text = input.getText().toString();
                gameSlug = m_Text;
                String request_Url = String.format(
                        "http://fingering.public.ndev.tech/fingering/getGame/%s/", gameSlug);
                new Request().execute(request_Url);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

}