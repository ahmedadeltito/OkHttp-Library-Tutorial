# OkHttp-Library-Tutorial

 In this tutorial we are having the OkHttp Android Library Tutorial which is an HTTP client that’s efficient by default.
 
 OkHttp perseveres when the network is troublesome: it will silently recover from common connection problems. If your service has multiple IP addresses OkHttp will attempt alternate addresses if the first connect fails. Using OkHttp is easy. Its request/response API is designed with fluent builders and immutability. It supports both synchronous blocking calls and async calls with callbacks.
 
# Our Application:
Our application will use OkHttp Library to make a sample application has a single button “London Current Weather”. Once the user is going to click on this button, London current weather data are appeared on defined TextViews  by integrating with REST web API from OpenWeatherMap.

# Main Steps:
1. First of all, you have to create a free account on OpenWeatherMap to get a free Application ID to be able to access OpenWeatherMap APIs,the creating account steps are very sample.
2. Create an Android Application.
3. Add permission to your Manifest.xml and build.gradle file.
4. Start to access OpenWeatherMap APIs to get London current weather data.

# 1. Setup

* Then OkHttp Library dependency and Gson library dependency should be added in the build.gradle of OkHttp Example Application:

```xml
compile 'com.squareup.okhttp3:okhttp:3.2.0'
compile 'com.google.code.gson:gson:2.6.2'
```

* Internet permission should be added in the Manifest.xml file.

```xml
<uses-permission android:name="android.permission.INTERNET"/>
```

# 2. Sending and Receiving Network Requests

* First, you must instantiate an OkHttpClient and create a Request object.

```java
private static final String API_URL = "http://api.openweathermap.org/data/2.5/weather?q=London&amp;units=metric&amp;appid=c6afdab60aa89481e297e0a4f19af055";
OkHttpClient client = new OkHttpClient();
Request request = new Request.Builder()
 .url(API_URL)
 .build();
 ```
 
* If there are any query parameters that need to be added, the HttpUrl class provided by OkHttp can be leveraged to construct the URL:

```java
HttpUrl.Builder urlBuilder = HttpUrl.parse("https://ajax.googleapis.com/ajax/services/search/images").newBuilder();
urlBuilder.addQueryParameter("v", "1.0");
urlBuilder.addQueryParameter("q", "android");
urlBuilder.addQueryParameter("rsz", "8");
String url = urlBuilder.build().toString();
 
Request request = new Request.Builder()
                     .url(url)
                     .build();
```

* If there are any authenticated query parameters, headers can be added to the request too:

```java
Request request = new Request.Builder()
    .header("Authorization", "token abcd")
    .url("https://api.github.com/users/codepath")
    .build();
```

# 3. Synchronous Network Calls

* You can create a Call object and dispatch the network request synchronously:

```java
Response response = client.newCall(request).execute();
```

* Because Android disallows network calls on the main thread, you can only make synchronous calls if you do so on a separate thread or a background service. You can use also use AsyncTask for lightweight network calls.

# 4. Asynchronous Network Calls

* You can also make asynchronous network calls too by creating a Call object, using the enqueue() method, and passing an anonymous Callback object that implements both onFailure() and onResponse().

```java
// Get a handler that can be used to post to the main thread
client.newCall(request).enqueue(new Callback() {
    @Override
    public void onFailure(Call call, IOException e) {
        e.printStackTrace();
    }
 
    @Override
    public void onResponse(Call call, final Response response) throws IOException {
        if (!response.isSuccessful()) {
            throw new IOException("Unexpected code " + response);
        }
    }
}
```

* OkHttp normally creates a new worker thread to dispatch the network request and uses the same thread to handle the response. It is built primarily as a Java library so does not handle the Android framework limitations that only permit views to be updated on the main UI thread. If you need to update any views, you will need to use runOnUiThread() or post the result back on the main thread. See this guide for more context.

```java
client.newCall(request).enqueue(new Callback() {
    @Override
    public void onResponse(Call call, final Response response) throws IOException {
        // ... check for failure using `isSuccessful` before proceeding
 
        // Read data on the worker thread
        final String responseData = response.body().string();
 
        // Run view-related code back on the main thread
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    TextView myTextView = (TextView) findViewById(R.id.myTextView);
                    myTextView.setText(responseData);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
       }
    }
});
```

# 5. Processing Network Responses

* Assuming the request is not cancelled and there are no connectivity issues, the onResponse() method will be fired. It passes a Response object that can be used to check the status code, the response body, and any headers that were returned. Calling isSuccessful() for instance if the code returned a status code of 2XX (i.e. 200, 201, etc.)

```java
if (!response.isSuccessful()) {
    throw new IOException("Unexpected code " + response);
}
```

* The header responses are also provided as a list:

```java
Headers responseHeaders = response.headers();
for (int i = 0; i < responseHeaders.size(); i++) {
  Log.d("DEBUG", responseHeaders.name(i) + ": " + responseHeaders.value(i));
}
```

* The headers can also be access directly using response.body():

```java
String header = response.header("Date");
```

* You can also get the response data by calling response.body() and then calling string() to read the entire payload. Note that response.body() can only be run once and should be done on a background thread.

```java
Log.d("DEBUG", response.body().string());
```

# 6. Processing JSON data

* Suppose you make a call to the GitHub API, which returns JSON-based data:

```java
Request request = new Request.Builder()
             .url("https://api.github.com/users/codepath")
             .build();
```

* You can also decode the data by converting it to a JSONObject or JSONArray, depending on the response data:

```java
client.newCall(request).enqueue(new Callback() {
    @Override
    public void onResponse(Call call, final Response response) throws IOException {  
        try {
            String responseData = response.body().string();
            JSONObject json = new JSONObject(responseData);
            final String owner = json.getString("name");
        } catch (JSONException e) {
 
        }
    }
});
```

# 7. Processing JSON data with GSON

* To use the Gson library, you first must declare a class that maps directly to the JSON response:

```java
static class GitUser {
    String name;
    String url;
    int id;
}
```

* You can then use the Gson parser to convert the data directly to a Java model:

```java
// Create new gson object
final Gson gson = new Gson();
// Get a handler that can be used to post to the main thread
client.newCall(request).enqueue(new Callback() {
    // Parse response using gson deserializer
    @Override
    public void onResponse(Call call, final Response response) throws IOException {
        // Process the data on the worker thread
        GitUser user = gson.fromJson(response.body().charStream(), GitUser.class);
        // Access deserialized user object here
    }
}
```

# 8. Caching Network Responses

* You can setup network caching by passing in a cache when building the OkHttpClient:

```java
int cacheSize = 10 * 1024 * 1024; // 10 MiB
Cache cache = new Cache(getApplication().getCacheDir(), cacheSize);
OkHttpClient client = new OkHttpClient.Builder().cache(cache).build();
```

* You can control whether to retrieve a cached response by setting the cacheControl property on the request. For instance, if you wish to only retrieve the request if data is cached, you could construct the Request object as follows:

```java
Request request = new Request.Builder()
                .url("http://publicobject.com/helloworld.txt")
                .cacheControl(new CacheControl.Builder().onlyIfCached().build())
                .build();
```

* You can also force a network response by using noCache() for the request:

```java
.cacheControl(new CacheControl.Builder().noCache().build())
```

* You can also specify a maximum staleness age for the cached response:

```java
 .cacheControl(new CacheControl.Builder().maxStale(365, TimeUnit.DAYS).build())
 ```
 
* To retrieve the cached response, you can simply call cacheResponse() on the Response object:

```java
Call call = client.newCall(request);
call.enqueue(new Callback() {
  @Override
  public void onFailure(Call call, IOException e) {
 
  }
 
  @Override
  public void onResponse(Call call, final Response response) throws IOException
  {
     final Response text = response.cacheResponse();
     // if no cached object, result will be null
     if (text != null) {
        Log.d("here", text.toString());
     }
  }
});
```

# Creating OkHttp Example Project:

![alt tag](http://androidgifts.com/wp-content/uploads/2016/04/OkHttp-768x403.jpg)

* In Android Studio, create a new activity, select Empty Activity and lets name OkHttpMainActivity.

* For this Android OkHttp Example, we are showing responses in TextViews, hence a layout with the TextViews, Buttons and ProgressBar should be created:

```xml
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:paddingBottom="@dimen/activity_vertical_margin"
    android:paddingLeft="@dimen/activity_horizontal_margin"
    android:paddingRight="@dimen/activity_horizontal_margin"
    android:paddingTop="@dimen/activity_vertical_margin"
    tools:context="com.android.gifts.okhttp.OkHttpMainActivity">

    <Button
        android:id="@+id/activity_ok_http_main_get_weather_sync_btn"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_centerHorizontal="true"
        android:layout_marginTop="5dp"
        android:text="@string/get_weather_sync"
        android:textAllCaps="false" />

    <Button
        android:id="@+id/activity_ok_http_main_get_weather_async_btn"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_below="@id/activity_ok_http_main_get_weather_sync_btn"
        android:layout_centerHorizontal="true"
        android:text="@string/get_weather_async"
        android:textAllCaps="false" />

    <ProgressBar
        android:id="@+id/activity_ok_http_main_pb"
        style="?android:attr/progressBarStyleLarge"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_alignBottom="@+id/activity_ok_http_main_root_ll"
        android:layout_alignTop="@+id/activity_ok_http_main_root_ll"
        android:layout_centerHorizontal="true"
        android:layout_margin="5dp"
        android:visibility="gone" />

    <LinearLayout
        android:id="@+id/activity_ok_http_main_root_ll"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_centerInParent="true"
        android:gravity="center"
        android:orientation="vertical"
        android:visibility="invisible">

        <TextView
            android:id="@+id/activity_ok_http_main_temperature_tv"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginBottom="5dp"
            android:layout_marginTop="5dp"
            android:text="@string/temperature"
            android:textAppearance="?android:textAppearanceMedium" />

        <TextView
            android:id="@+id/activity_ok_http_main_pressure_tv"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginBottom="5dp"
            android:layout_marginTop="5dp"
            android:text="@string/pressure"
            android:textAppearance="?android:textAppearanceMedium" />

        <TextView
            android:id="@+id/activity_ok_http_main_humidity_tv"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginBottom="5dp"
            android:layout_marginTop="5dp"
            android:text="@string/humidity"
            android:textAppearance="?android:textAppearanceMedium" />

    </LinearLayout>

</RelativeLayout>
```

* Therefore we will make a POJO Class for the data we need, here we will need London current weather data. To parse the response properly you may need to define the WeatherDataBean class carefully. As it should have the same structure as JSON being returned in response. Although it is not required to define all the fields, if you don’t want to parse all of them.

**JSON that we will parse from OpenWeatherMap APIs:**

```xml
{
    "main": {
        "temp": 3.72, 
        "pressure": 1009,
        "humidity": 86,
        "temp_min": 2,
        "temp_max": 5.3
    }
}
```

**WeatherDataBean.java class:**

```java
package com.android.gifts.okhttp;

/**
 * Created by ahmedadel on 26/04/16.
 */
public class WeatherDataBean {

    private Main main;

    public Main getMain() {
        return main;
    }

    public void setMain(Main main) {
        this.main = main;
    }

    class Main {
        double temp;
        double pressure;
        int humidity;

        public double getTemp() {
            return temp;
        }

        public void setTemp(double temp) {
            this.temp = temp;
        }

        public double getPressure() {
            return pressure;
        }

        public void setPressure(double pressure) {
            this.pressure = pressure;
        }

        public int getHumidity() {
            return humidity;
        }

        public void setHumidity(int humidity) {
            this.humidity = humidity;
        }
    }
}
```

* Then we will go to onCreate() method of OkHttpMainActivity and start to handle synchronous and Asynchronous request calling.

```java
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
    private static final String API_URL = "http://api.openweathermap.org/data/2.5/weather?q=London&amp;units=metric&amp;appid=c6afdab60aa89481e297e0a4f19af055";

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

    private class GetWeatherSync extends AsyncTask&lt;Void, Void, WeatherDataBean&gt; {

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
```

# Finally The OkHttp Example Application after running should be as screenshots attached below:

Main Screen with two buttons “London Current Weather Synchronously” and “London Current Weather Asynchronously”

![alt tag](http://androidgifts.com/wp-content/uploads/2016/04/Home-Screen-1-576x1024.png)

After pressing on “London Current Weather Synchronously”

![alt tag](http://androidgifts.com/wp-content/uploads/2016/04/After-pressing-on-London-Current-Weather-Synchronously-1-576x1024.png)

After pressing on “London Current Weather Asynchronously”

![alt tag](http://androidgifts.com/wp-content/uploads/2016/04/After-pressing-on-London-Current-Weather-Asynchronously-1-576x1024.png)

Loading progress bar appears informing the user that your request is in progress

![alt tag](http://androidgifts.com/wp-content/uploads/2016/04/Loading-1-576x1024.png)

[You can check the whole article here](http://androidgifts.com/okhttp-android-library-tutorial-library-7/)
