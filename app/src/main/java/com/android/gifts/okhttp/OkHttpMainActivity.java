package com.android.gifts.okhttp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OkHttpMainActivity extends AppCompatActivity implements View.OnClickListener {

    /**
     * Define the OpenWeatherMap API URL
     */
    private static final String API_URL = "http://api.openweathermap.org/data/2.5/weather?q=London&units=metric&appid=c6afdab60aa89481e297e0a4f19af055";

    /**
     * Instance variables to represent the "London Current Weather Synchronously"
     * and "London Current Weather Asynchronously" buttons,
     * "Temperature", "Pressure" and "Humidity" TextViews and loadingProgressbar.
     */
    private Button getLondonCurrentWeatherSync, getLondonCurrentWeatherAsync;
    private TextView temperatureTextView, pressureTextView, humidityTextView;
    private ProgressBar loadingProgressBar;
    private LinearLayout getLondonCurrentWeatherLinearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ok_http_main);

        /**
         * Instantiate the variables we declared above using the ID values
         * we specified in the layout XML file.
         */
        getLondonCurrentWeatherSync = (Button) findViewById(R.id.activity_ok_http_main_get_weather_sync_btn);
        getLondonCurrentWeatherAsync = (Button) findViewById(R.id.activity_ok_http_main_get_weather_async_btn);
        getLondonCurrentWeatherLinearLayout = (LinearLayout) findViewById(R.id.activity_ok_http_main_root_ll);
        temperatureTextView = (TextView) findViewById(R.id.activity_ok_http_main_temperature_tv);
        pressureTextView = (TextView) findViewById(R.id.activity_ok_http_main_pressure_tv);
        humidityTextView = (TextView) findViewById(R.id.activity_ok_http_main_humidity_tv);
        loadingProgressBar = (ProgressBar) findViewById(R.id.activity_ok_http_main_pb);

        /**
         * Add a listener to getLondonCurrentWeatherSync and getLondonCurrentWeatherAsync so that we can handle presses.
         */
        getLondonCurrentWeatherSync.setOnClickListener(this);
        getLondonCurrentWeatherAsync.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            /**
             * make a synchronous background request.
             */
            case R.id.activity_ok_http_main_get_weather_sync_btn:
                getWeatherSync();
                break;
            /**
             * make an asynchronous background request.
             */
            case R.id.activity_ok_http_main_get_weather_async_btn:
                getWeatherAsync();
                break;
        }
    }

    /**
     * getWeatherAsync() method will make an asynchronous background request
     * by using OkHttpClient class, Request main class and Callback interface.
     */
    private void getWeatherAsync() {
        getLondonCurrentWeatherLinearLayout.setVisibility(View.INVISIBLE);
        loadingProgressBar.setVisibility(View.VISIBLE);

        /**
         * To make REST API call through Android OkHttp Library we may first need to build an instance of OkHttpClient class
         * and also an instance of Request class. Since Request class is the main class of OkHttp Library which executes
         * all the requests.
         */
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(API_URL)
                .build();
        /**
         * After this, call enqueue() method to make an asynchronous API request, and implement inside it
         * CallBack Interface Listener "Observer" since this Callback Interface has to methods onResponse()
         * that is fire once a successive response is returned from OpenWeatherMap API and onFailure()
         * that is fire once an error occurs
         */
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingProgressBar.setVisibility(View.GONE);
                        Toast.makeText(OkHttpMainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String responseString = response.body().string();
                    /**
                     * Parse JSON response to Gson library
                     */
                    JSONObject jsonObject = new JSONObject(responseString);
                    Gson gson = new Gson();
                    final WeatherDataBean weatherDataBean = gson.fromJson(jsonObject.toString(), WeatherDataBean.class);
                    /**
                     * Any action involving the user interface must be done in the main or UI thread, using runOnUiThread()
                     * method will run this specified action on the UI thread.
                     */
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateUI(weatherDataBean);
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * getWeatherSync() method will make a synchronous background request
     */
    private void getWeatherSync() {
        GetWeatherSync getWeatherSync = new GetWeatherSync();
        getWeatherSync.execute();
    }

    /**
     * updateUI() method will be used once a successive response is returned from OpenWeatherMap API
     *
     * @param weatherDataBean that is returned from successive OpenWeatherMap API request
     */
    private void updateUI(WeatherDataBean weatherDataBean) {
        loadingProgressBar.setVisibility(View.GONE);
        if (weatherDataBean != null) {
            getLondonCurrentWeatherLinearLayout.setVisibility(View.VISIBLE);
            temperatureTextView.setText("Temperature : " + weatherDataBean.getMain().getTemp() + " Celsius");
            pressureTextView.setText("Pressure : " + weatherDataBean.getMain().getPressure());
            humidityTextView.setText("Humidity : " + weatherDataBean.getMain().getHumidity());
        }
    }

    private class GetWeatherSync extends AsyncTask<Void, Void, WeatherDataBean> {

        OkHttpClient client = new OkHttpClient();
        Request request;

        @Override
        protected void onPreExecute() {
            getLondonCurrentWeatherLinearLayout.setVisibility(View.INVISIBLE);
            loadingProgressBar.setVisibility(View.VISIBLE);
            /**
             * To make REST API call through Android OkHttp Library we may first need to build an instance of Request class.
             * This class is the main class of OkHttp Library which executes all the requests.
             */
            request = new Request.Builder()
                    .url(API_URL)
                    .build();
        }

        @Override
        protected WeatherDataBean doInBackground(Void... params) {
            WeatherDataBean weatherDataBean = null;
            try {
                /**
                 * After this, call newCall() method to make a synchronous API request.
                 * Then Parse JSON response to Gson library
                 */
                Response response = client.newCall(request).execute();
                String responseString = response.body().string();
                JSONObject jsonObject = new JSONObject(responseString);
                Gson gson = new Gson();
                weatherDataBean = gson.fromJson(jsonObject.toString(), WeatherDataBean.class);
            } catch (final IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(OkHttpMainActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return weatherDataBean;
        }

        /**
         * Get the returned object from doInBackground() method and get from this object the
         * necessary data that we want.
         *
         * @param weatherDataBean is the returned object that came from doInBackground() merhod
         */
        @Override
        protected void onPostExecute(WeatherDataBean weatherDataBean) {
            updateUI(weatherDataBean);
        }
    }
}
