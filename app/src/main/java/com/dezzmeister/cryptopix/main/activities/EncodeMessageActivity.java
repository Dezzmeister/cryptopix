package com.dezzmeister.cryptopix.main.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import com.dezzmeister.cryptopix.R;
import com.dezzmeister.cryptopix.main.dialogs.DialogArgs;
import com.dezzmeister.cryptopix.main.secret.PackageHandler;
import com.dezzmeister.cryptopix.main.secret.PackageHeader;
import com.dezzmeister.cryptopix.main.session.SessionObject;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

/**
 * An activity to hide a file in an image.
 *
 * @author Joe Desmond
 * @since 1.0.0
 */
public class EncodeMessageActivity extends AppCompatActivity {
    private SessionObject sessionObject;
    private PackageHeader packageHeader;
    private PackageHandler packageHandler;



    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent intent = getIntent();
        sessionObject = (SessionObject) intent.getSerializableExtra(DialogArgs.SESSION_OBJECT_KEY);
        packageHeader = (PackageHeader) intent.getSerializableExtra(DialogArgs.PACKAGE_HEADER_KEY);
        packageHandler = (PackageHandler) intent.getSerializableExtra(DialogArgs.PACKAGE_HANDLER_KEY);

        setContentView(R.layout.activity_encode_message);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {

            }
        });

        final AdView adView = findViewById(R.id.adView);
        final AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }
}
