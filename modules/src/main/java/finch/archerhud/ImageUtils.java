package finch.archerhud;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageUtils {
    public static int getGreenAlpha(int pixel) {
        final int alpha = (pixel >> 24) & 0xff;
        final int green = ((pixel >> 8) & 0xff);
        final int green_alpha = (green * alpha) >> 8;
        return green_alpha;
    }


    public static void toBinaryImage(Bitmap bitmap, int treatAsWhite) {
        for (int h = 0; h < bitmap.getHeight(); h++) {
            for (int w = 0; w < bitmap.getWidth(); w++) {
                int p = bitmap.getPixel(w, h);
                final int green_alpha = getGreenAlpha(p);
                bitmap.setPixel(w, h, green_alpha > treatAsWhite ? 0xffffffff : 0);
            }
        }

    }

    public static Bitmap removeAlpha(Bitmap originalBitmap) {
        // lets create a new empty bitmap
        Bitmap newBitmap = Bitmap.createBitmap(originalBitmap.getWidth(), originalBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        // create a canvas where we can draw on
        Canvas canvas = new Canvas(newBitmap);
        // create a paint instance with alpha
        Paint alphaPaint = new Paint();
        alphaPaint.setAlpha(255);
        // now lets draw using alphaPaint instance
        canvas.drawBitmap(originalBitmap, 0, 0, alphaPaint);
        return newBitmap;
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (null == drawable) {
            return null;
        }
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static Context context;

    public static boolean storeBitmap(Bitmap image, String filename) {
        return storeBitmap(image, null, filename);
    }


    public static boolean storeBitmapQ(Bitmap bmp, String filename) {
        File cacheFile = new File(context.getCacheDir(), filename);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(cacheFile);
            final boolean compress_result = bmp.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean storeBitmap(Bitmap bmp, String dirname, String filename) {
        //remove permission check for AndroidQ
        //need use  NEW scoped storage to replace.
//        if (null == context || ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                != PackageManager.PERMISSION_GRANTED) {
//            // Permission is not granted
//            return false;
//        }

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(null != dirname ? dirname + "/" + filename : filename);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public static Bitmap getScaleBitmap(Bitmap bitmap, int newWidth,
                                        int newHeight) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        Matrix matrix = new Matrix();
        float scaleW = (float) newWidth / w;
        float scaleH = (float) newHeight / h;
        // scale = scale < scale2 ? scale : scale2;
        matrix.postScale(scaleW, scaleH);
        Bitmap bmp = Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
//        if (bitmap != null && !bitmap.equals(bmp) && !bitmap.isRecycled()) {
//            bitmap.recycle();
//            bitmap = null;
//        }
        return bmp;// Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);

    }

    /**
     * <p>This method combines two images into one by rendering them side by side.</p>
     *
     * @param left The image that goes on the left side of the combined image.
     * @param right The image that goes on the right side of the combined image.
     * @return The combined image.
     */
    public static  Bitmap combineBitmaps(final Bitmap left, final Bitmap right){
        // Get the size of the images combined side by side.
        int width = left.getWidth() + right.getWidth();
        int height = left.getHeight() > right.getHeight() ? left.getHeight() : right.getHeight();

        // Create a Bitmap large enough to hold both input images and a canvas to draw to this
        // combined bitmap.
        Bitmap combined = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(combined);

        // Render both input images into the combined bitmap and return it.
        canvas.drawBitmap(left, 0f, 0f, null);
        canvas.drawBitmap(right, left.getWidth(), 0f, null);

        return combined;
    }
}
