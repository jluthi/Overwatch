package something.overwatch;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

public class InfoPlayerActivity extends AppCompatActivity {

    private ProgressDialog progDialog;
    private ArrayList<String> favorites = null;
    private String query = "";
    private String region = "";
    private Boolean isFavorited = false;
    private int y = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_player);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_player);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            TypedValue tv = new TypedValue();
            if(getTheme().resolveAttribute(android.R.attr.actionBarSize,tv,true))
                y = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
        }

        handleIntent(getIntent());

        //get stuff from intent
        query = getIntent().getStringExtra(SearchManager.QUERY);
        region = getIntent().getStringExtra("region");
        favorites = getIntent().getStringArrayListExtra("favoriteslist");
        isFavorited = favorites != null && favorites.contains(query + ";" + region);
        //progDialog = new ProgressDialog(this);
        //progDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        //progDialog.show();
        //progDailog.setCancelable(false);

        String currentUrl;
        // PSN allows alphanumeric, hyphen, and underscore
        // Xbox allows alphanumeric and white space
        // Battlenet allows alphanumeric, foreign words, but NO symbols like hyphen
        // Blizzard's search is bugged for "-" currently. revert this change after they fix it.
        if(region.equals("Console") && query.contains("-")){
            currentUrl = "https://playoverwatch.com/en-us/career/psn/" + query.replace(" ", "%20");
        } else {
            currentUrl = "https://playoverwatch.com/en-us/search?q=" + query.replace("#", "-").replace(" ", "%20");
        }

        WebView webView = (WebView)findViewById(R.id.webview_player);
        // required to make blizzard's website work
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setWebViewClient(new WebViewClient(){
            private boolean isRedirected = false;

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                //check if this is loading the redirected url
                if (url.contains("career")){
                    //provide some feedback to make sure the user know it's not frozen
                    Toast t = Toast.makeText(getApplicationContext(), "Player found. Redirecting...", Toast.LENGTH_SHORT);
                    t.setGravity(Gravity.CENTER,0,0);
                    t.show();
                }
            }

            @Override
            public void onPageCommitVisible(WebView view, String url) {
                //deletes top blizzard bar
                view.loadUrl("javascript:(function() { " +
                        "document.getElementsByClassName('navbars')[0].style.display = 'none'; " +
                        "})()");
                super.onPageCommitVisible(view, url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                // deletes bottom blizzard bar
                view.loadUrl("javascript:(function() { " +
                        "document.getElementsByClassName('navbars')[0].style.display = 'none'; " +
                        "document.getElementById('footer').style.display = 'none'; " +
                        //"document.getElementsByClassName('bootstrap-footer')[0].style.display = 'none'; " +
                        "document.getElementById('Page-footer').style.display = 'none'; " +
                        "})()");
                // add this line if u want to hide platform buttons
                //"document.getElementById('profile-platforms').style.display = 'none'; " +
                //view.setVisibility(View.VISIBLE);
                //if(progDialog.isShowing()) progDialog.dismiss();
            }
        });
        webView.loadUrl(currentUrl);
    }

    private void handleIntent(Intent intent){
        if(Intent.ACTION_SEARCH.equals(intent.getAction())){
            String query = intent.getStringExtra(SearchManager.QUERY);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_player_favorite, menu);
        MenuItem favButton = menu.findItem(R.id.action_favorite);
        if(isFavorited) {
            favButton.setIcon(getResources().getDrawable(R.drawable.ic_star_on_86dp));
        } else {
            favButton.setIcon(getResources().getDrawable(R.drawable.ic_star_off_86dp));
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_favorite) {
            if(isFavorited)
                item.setIcon(getResources().getDrawable(R.drawable.ic_star_off_86dp));
            else
                item.setIcon(getResources().getDrawable(R.drawable.ic_star_on_86dp));
            favorite(query, region);
            isFavorited = !isFavorited;
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void saveInfo(String v){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPref.edit();
        String result = "";
        if (favorites == null)
            favorites = new ArrayList<>();
        favorites.add(v);
        for(int i=0; i<favorites.size(); i++){
            result = result + "," + favorites.get(i);
        }
        result = result.substring(1);
        editor.putString("favorites", result);
        editor.commit();
        Toast toast = Toast.makeText(getApplicationContext(), "Favorited", Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP|Gravity.END,0,y);
        toast.show();
    }
    public void removeInfo(String v){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPref.edit();
        String result = "";
        favorites.remove(v);
        for(int i=0; i<favorites.size(); i++){
            result = result + "," + favorites.get(i);
        }
        if(!result.equals(""))
            result = result.substring(1);
        editor.putString("favorites", result);
        editor.commit();
        Toast toast = Toast.makeText(getApplicationContext(), "Unfavorited", Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP|Gravity.END,0,y);
        toast.show();
    }

    // Favorites/Unfavorite current player the current player
    private void favorite(String query, String region){
        String v = query + ";" + region;
        if(isFavorited)
            removeInfo(v);
        else
            saveInfo(v);
    }
}
