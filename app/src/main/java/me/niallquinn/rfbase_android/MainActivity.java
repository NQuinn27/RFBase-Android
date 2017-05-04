package me.niallquinn.rfbase_android;

import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

import com.basecamp.turbolinks.TurbolinksAdapter;
import com.basecamp.turbolinks.TurbolinksSession;
import com.basecamp.turbolinks.TurbolinksView;

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
                case R.id.navigation_social:
                    loc = POST_PATH;
                    setTitle("News");
                    break;
            }
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

}
