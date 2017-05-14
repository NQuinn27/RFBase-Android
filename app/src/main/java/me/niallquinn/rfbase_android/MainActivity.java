package me.niallquinn.rfbase_android;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.basecamp.turbolinks.TurbolinksAdapter;
import com.basecamp.turbolinks.TurbolinksSession;
import com.basecamp.turbolinks.TurbolinksView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements TurbolinksAdapter {

    private TextView mTextMessage;
    private TurbolinksView turbolinksView;
    private String location;

    private static String BASE_URL = "http://rfbase.herokuapp.com/api/v1";
    private static String POST_PATH = "/post";
    private static String BIO_PATH = "/bio";
    private static String MEDIA_PATH = "/media";
    private static String CALENDAR_PATH = "/calendar";
    private static String API_KEY = "?apiKey=97fc5679-63be-4981-9121-68735c8927f0";
    private static final String INTENT_URL = "intentUrl";

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            String loc = POST_PATH;
            switch (item.getItemId()) {
                case R.id.navigation_news:
                    loc = POST_PATH;
                    setTitle("News");
                    break;
                case R.id.navigation_bio:
                    loc = BIO_PATH;
                    setTitle("Bio");
                    break;
                case R.id.navigation_media:
                    loc = MEDIA_PATH;
                    setTitle("Media");
                    break;
                case R.id.navigation_calendar:
                    loc = CALENDAR_PATH;
                    setTitle("Calendar");
                    break;
            }

//            FetchThemeTask theme = new FetchThemeTask();
//            theme.execute();

            System.out.print(loc);
            location = BASE_URL + loc + API_KEY;
            TurbolinksSession.getDefault(MainActivity.this)
                    .activity(MainActivity.this)
                    .adapter(MainActivity.this)
                    .view(turbolinksView)
                    .visit(location);
            return true;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        turbolinksView = (TurbolinksView) findViewById(R.id.turbolinks_view);
        TurbolinksSession.getDefault(this).setDebugLoggingEnabled(true);
        location = getIntent().getStringExtra(INTENT_URL) != null ? getIntent().getStringExtra(INTENT_URL) : BASE_URL + POST_PATH + API_KEY;
        setTitle("News");
        TurbolinksSession.getDefault(this)
                .activity(this)
                .adapter(this)
                .view(turbolinksView)
                .visit(location);

        FetchThemeTask theme = new FetchThemeTask();
        theme.execute();

    }

    @Override
    protected void onRestart() {
        super.onRestart();

        // Since the webView is shared between activities, we need to tell Turbolinks
        // to load the location from the previous activity upon restarting
        TurbolinksSession.getDefault(this)
                .activity(this)
                .adapter(this)
                .restoreWithCachedSnapshot(true)
                .view(turbolinksView)
                .visit(location);
    }

    // -----------------------------------------------------------------------
    // TurbolinksAdapter interface
    // -----------------------------------------------------------------------

    @Override
    public void onPageFinished() {

    }

    @Override
    public void onReceivedError(int errorCode) {
        handleError(errorCode);
    }

    @Override
    public void pageInvalidated() {

    }

    @Override
    public void requestFailedWithStatusCode(int statusCode) {
        handleError(statusCode);
    }

    @Override
    public void visitCompleted() {

    }

    // The starting point for any href clicked inside a Turbolinks enabled site. In a simple case
    // you can just open another activity, or in more complex cases, this would be a good spot for
    // routing logic to take you to the right place within your app.
    @Override
    public void visitProposedToLocationWithAction(String location, String action) {
        if (!location.contains(BASE_URL)) {
            //Open this in android browser
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(location));
            startActivity(browserIntent);
            return;
        }
        FetchThemeTask theme = new FetchThemeTask();
        theme.execute();

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(INTENT_URL, location);

        this.startActivity(intent);
    }

    // -----------------------------------------------------------------------
    // Private
    // -----------------------------------------------------------------------

    // Simply forwards to an error page, but you could alternatively show your own native screen
    // or do whatever other kind of error handling you want.
    private void handleError(int code) {
        if (code == 404) {
            TurbolinksSession.getDefault(this)
                    .activity(this)
                    .adapter(this)
                    .restoreWithCachedSnapshot(false)
                    .view(turbolinksView)
                    .visit(BASE_URL + "/error");
        }
    }

    public void processTheme(String json) {
        JSONObject mainObject = null;
        try {
            mainObject = new JSONObject(json);
            String  primaryColor = (String) mainObject.get("primary_color");
            String primaryInverseColor = (String) mainObject.get("primary_inverse_color");
            String primaryFontColor = (String) mainObject.get("primary_font_color");
            String secondaryFontColor = (String) mainObject.get("secondary_font_color");

            SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("primaryColor", primaryColor);
            editor.putString("primaryInverseColor", primaryInverseColor);
            editor.putString("primaryFontColor", primaryFontColor);
            editor.putString("secondaryFontColor", secondaryFontColor);
            editor.commit();
            applyLocalTheme();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @MainThread
    public void applyLocalTheme() {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        final String primaryColor =  sharedPref.getString("primaryColor", "");
        final String primaryInverseColor =  sharedPref.getString("primaryInverseColor", "");

        if (primaryColor.length() > 0) {
            getWindow().setNavigationBarColor(Color.parseColor(primaryColor));
            final Toolbar actionBarToolbar = (Toolbar)this.findViewById(R.id.action_bar);
            if (actionBarToolbar != null)
                this.runOnUiThread(new Runnable() {
                    public void run() {
                        actionBarToolbar.setBackgroundColor(Color.parseColor(primaryColor));
                        actionBarToolbar.setTitleTextColor(Color.parseColor(primaryInverseColor));
                        Window window = getWindow();
                        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                        window.setStatusBarColor(Color.parseColor(primaryColor));
                    }
                });
        }
    }

    public class FetchThemeTask extends AsyncTask<Void, Void, Void> {

        private final String LOG_TAG = FetchThemeTask.class.getSimpleName();
        String forecastJsonStr = null;

        @Override
        protected Void doInBackground(Void... params) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                String fetchurl = BASE_URL + "/theme" + API_KEY;
                URL url = new URL(fetchurl);

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                forecastJsonStr = buffer.toString();
                processTheme(forecastJsonStr);

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
            return null;
        }
    }
}

