package com.getmeashop.realestate.partner;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import com.squareup.picasso.Picasso;


public class ThemeView extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme_view);
        TouchImageView touchImageView = (TouchImageView) findViewById(R.id.imgv);

        Log.e("image zoom", getIntent().getStringExtra("image"));
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Picasso.with(ThemeView.this)
                .load(getIntent().getStringExtra("image").replace(".png",".jpg"))
                .error(R.drawable.icon_no_image)
                .into(touchImageView);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }
}
