package com.example.client_broadcast;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;
import ua.naiksoftware.stomp.dto.StompCommand;
import ua.naiksoftware.stomp.dto.StompHeader;
import ua.naiksoftware.stomp.dto.StompMessage;

public class MainActivity extends AppCompatActivity {

    private EditText nameText, messageText;
    private Button nameButton,messButton;
    private TextView resultText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nameText = findViewById(R.id.name);
        messageText = findViewById(R.id.message);
        nameButton = findViewById(R.id.sendname);
        messButton = findViewById(R.id.sendmess);
        resultText = findViewById(R.id.show);


        StompClient stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, Const.address);
        StompUtils.lifecycle(stompClient);

        Log.i(Const.TAG, "Подключаемся к серверу...");

        stompClient.connect();

        Log.i(Const.TAG, "Подписка на события...");

        stompClient.topic(Const.broadcastResponse).subscribe(stompMessage -> {
            JSONObject jsonObject = new JSONObject(stompMessage.getPayload());
            Log.i(Const.TAG, "Get: " + stompMessage.getPayload());
            runOnUiThread(() -> {
                try {
                    resultText.append(jsonObject.getString("response") + "\n");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });
        });

        nameButton.setOnClickListener(v -> {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("type", "name");
                jsonObject.put("message", nameText.getText());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            stompClient.send(new StompMessage(
                    StompCommand.SEND,
                    Arrays.asList(
                            new StompHeader(StompHeader.DESTINATION, Const.broadcast),
                            new StompHeader("authorization", "token12345")
                    ),
                    jsonObject.toString())
            ).subscribe();
        });

        messButton.setOnClickListener(v -> {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("type", "chat");
                jsonObject.put("name", nameText.getText());
                jsonObject.put("message", messageText.getText());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            stompClient.send(new StompMessage(
                    StompCommand.SEND,
                    Arrays.asList(
                            new StompHeader(StompHeader.DESTINATION, Const.broadcast),
                            new StompHeader("authorization", "token12345")
                    ),
                    jsonObject.toString())
            ).subscribe();
            messageText.setText("");
        });


    }
}
