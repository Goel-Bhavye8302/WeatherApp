package com.ai.game.wheatherapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    public EditText editText;
    public TextView textView;
    public boolean toast = false;
    public String message = "";
    boolean weatherFound = false;

    public String capitalizeWord(String str){
        String words[] = str.split("\\s");
        String capitalizeWord = "";
        for(String w:words){
            String first = w.substring(0,1);
            String afterfirst = w.substring(1);
            capitalizeWord += first.toUpperCase()+afterfirst+" ";
        }
        return capitalizeWord.trim();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editText = findViewById(R.id.editText);
        textView = findViewById(R.id.resultView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.share_menu && weatherFound){
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            String sharingMessage = "Weather Info : \nLocation : " + editText.getText().toString().toUpperCase() + "\n\n" + message;
            shareIntent.putExtra(Intent.EXTRA_TEXT, sharingMessage);
            shareIntent.setType("text/plain");
            startActivity(shareIntent);
        }
        else{
            Toast.makeText(getApplicationContext(), "Invalid Location!", Toast.LENGTH_SHORT).show();
            return super.onOptionsItemSelected(item);
        }
        return true;
    }

    public void getWeather(View view) throws UnsupportedEncodingException {
        try {
            DownloadTask task = new DownloadTask();
            String encodedCityName = URLEncoder.encode(editText.getText().toString(), "UTF-8");
            task.execute("https://api.openweathermap.org/data/2.5/weather?q=" + editText.getText().toString() + "&appid=e0a5733ad5612192502aa5b912dfd678");
            InputMethodManager mgr = (InputMethodManager) getSystemService((Context.INPUT_METHOD_SERVICE));
            mgr.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        }
        catch (Exception e){
            e.printStackTrace();
            Toast.makeText(MainActivity.this, "Could Not Find Weather!", Toast.LENGTH_SHORT).show();
            weatherFound = false;
        }
    }

    public class DownloadTask extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... urls) {
            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;
            try {
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();

                while (data != -1) {
                    char current = (char) data;
                    result = result.concat(Character.toString(current));
                    data = reader.read();
                }

                return result;

            } catch (Exception e) {
                e.printStackTrace();
                toast = true;
                return null;
            }
        }

        @Override
        protected void onPostExecute(java.lang.String s) {
            super.onPostExecute(s);

            try {
                JSONObject jsonObject = new JSONObject(s);
                String weatherInfo = jsonObject.getString("weather");
                JSONObject addInfo = jsonObject.getJSONObject("main");
                JSONObject windInfo = jsonObject.getJSONObject("wind");

                Log.i("Weather content", weatherInfo);
                JSONArray arr = new JSONArray(weatherInfo);

                message =  "";
                for (int i=0; i < arr.length(); i++) {
                    JSONObject jsonPart = arr.getJSONObject(i);
                    String main = jsonPart.getString("main");
                    String description = jsonPart.getString("description");
                    if(!main.isEmpty() && !description.isEmpty()){
                        message += main + ": " + capitalizeWord(description) + "\r\n";
                    }
                }
                double temp = Double.parseDouble(addInfo.getString("temp"));
                temp = temp - 273.15;
                message += "Temperature : " + String.format("%.2f", temp) + " °C\n";
                message += "Pressure : " + addInfo.getString("pressure") + " hPa\n";
                message += "Humidity : " + addInfo.getString("humidity") + " %\n";
                message += "Wind Speed : " + windInfo.getString("speed") + " m/s\n";

                weatherFound = true;
                if(!message.equals("")){
                    textView.setText(message);
                }
            } catch (Exception e) {
                e.printStackTrace();
                toast = true;
            }
            if(toast) {
                Toast.makeText(getApplicationContext(), "Could Not Find Weather!", Toast.LENGTH_SHORT).show();
                toast = !toast;
                weatherFound = false;
            }
        }
    }
}