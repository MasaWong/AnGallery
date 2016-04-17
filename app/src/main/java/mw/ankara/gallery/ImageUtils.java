package mw.ankara.gallery;

import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.widget.ImageView;

/**
 * 照片展示专用
 *
 * @author masawong
 * @since 4/7/16
 */
public class ImageUtils {

    public static void display(String path, ImageView imageView, int size) {
        display(path, imageView, size, size);
    }

    public static void display(String path, ImageView imageView, int width, int height) {
        if (TextUtils.isEmpty(path) || imageView == null) {
            return;
        }

        if (width <= 0 && height <= 0) {
            imageView.setImageBitmap(BitmapFactory.decodeFile(path));
            return;
        }

        BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
        decodeOptions.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(path, decodeOptions);
        int scaleWidth = decodeOptions.outWidth / width;
        int scaleHeight = decodeOptions.outHeight / height;

        decodeOptions.inSampleSize = scaleHeight < scaleWidth ? scaleHeight : scaleWidth;
        if (decodeOptions.inSampleSize < 1) {
            decodeOptions.inSampleSize = 1;
        }
        decodeOptions.inJustDecodeBounds = false;

        imageView.setImageBitmap(BitmapFactory.decodeFile(path, decodeOptions));
    }
}
