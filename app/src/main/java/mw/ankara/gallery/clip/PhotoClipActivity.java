package mw.ankara.gallery.clip;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

import java.io.ByteArrayOutputStream;
import java.net.URLDecoder;

import mw.ankara.gallery.R;

/**
 * http://blog.csdn.net/lmj623565791/article/details/39761281
 *
 * @author zhy
 */
public class PhotoClipActivity extends AppCompatActivity {

    public final static String KEY_PHOTO = "photo";

    public final static String KEY_RESULT = "bitmap";

    private AnClipView mACVLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_clip);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mACVLayout = (AnClipView) findViewById(R.id.clip_acv_layout);

        Uri uri = getIntent().getData();
        String path = uri == null ? getIntent().getStringExtra(KEY_PHOTO)
            : uri.getQueryParameter(KEY_PHOTO);
        if (!TextUtils.isEmpty(path)) {
            mACVLayout.setImagePath(URLDecoder.decode(path));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.send, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.clip_send) {
            Bitmap bitmap = mACVLayout.clip();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] bytes = baos.toByteArray();

            Intent intent = new Intent();
            intent.putExtra(KEY_RESULT, bytes);
            setResult(RESULT_OK, intent);
            finish();
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}
