package com.example.papago;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private PaPaGo papago;
    private TextView txtResult;
    private EditText txtInput;
    private Button btnTranslate;
    private Button btnVoiceTranslate;
    private static final int RESULT_SPEECH = 1234;
    InputMethodManager imm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        txtResult = findViewById(R.id.txt_result);
        txtInput = findViewById(R.id.txt_input);
        btnTranslate = findViewById(R.id.btn_translate);
        btnVoiceTranslate = findViewById(R.id.btn_voice);

        btnVoiceTranslate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new
                        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                startActivityForResult(intent, RESULT_SPEECH);


            }
        });

        btnTranslate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                papago = new PaPaGo();
                papago.execute(txtInput.getText().toString());
                imm.hideSoftInputFromWindow(txtInput.getWindowToken(), 0);

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        papago = new PaPaGo();
        if (resultCode == RESULT_OK && (requestCode == RESULT_SPEECH)) {

            ArrayList<String> sstResulrt = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String result = sstResulrt.get(0);
            txtInput.setText(result);
            papago.execute(result);
        } else {
            txtInput.setText("음성 인식에 실패하였습니다.");
            papago.execute("음성 인식에 실패하였습니다.");
        }
    }

    public class PaPaGo extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            String result = null;
            String clientId = "OEVoxjpBu8gfvdFX8vCE";
            String clientSecret = "jdxNHgTxyc";

            try {
                String text = URLEncoder.encode(strings[0], "UTF-8");
                String apiURL = "https://openapi.naver.com/v1/papago/n2mt";
                URL url = new URL(apiURL);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("X-Naver-Client-Id", clientId);
                con.setRequestProperty("X-Naver-Client-Secret", clientSecret);

                String postParams = "source=ko&target=en&text=" + text;
                con.setDoOutput(true);
                DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                wr.writeBytes(postParams);
                wr.flush();
                wr.close();
                int responseCode = con.getResponseCode();
                BufferedReader br;
                Log.i("runTranslate", String.valueOf(responseCode));
                if (responseCode == 200) {
                    br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                } else {
                    br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                }
                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = br.readLine()) != null) {
                    response.append(inputLine);
                }
                br.close();
                Log.i("runTranslate", response.toString());

                Gson gson = new GsonBuilder().create();
                JsonParser parser = new JsonParser();
                JsonElement rootObj = parser.parse(response.toString()).getAsJsonObject().get("message").getAsJsonObject().get("result").getAsJsonObject().get("translatedText");

                result = rootObj.toString().replace("\"", "");
                Log.i("runTranslate", "runTranslate" + result);

            } catch (Exception e) {
                Log.i("runTranslate", e.getMessage());
            }
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            txtResult.setText(s);
        }
    }
}