package glarss.face.phone.glarssphone;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class UserDetailActivity extends AppCompatActivity {
    public static final String KEY_PHOTO_URI = "PHOTO_URI";
    public static final String KEY_FACE_OFFSET_X = "FACE_OFFSET_X";
    public static final String KEY_FACE_OFFSET_Y = "FACE_OFFSET_Y";
    public static final String KEY_FACE_WIDTH = "FACE_WIDTH";
    public static final String KEY_FACE_HEIGHT = "FACE_HEIGHT";

    private float mFaceOffsetX = 0;
    private float mFaceOffsetY = 0;
    private float mFaceWidth = 0;
    private float mFaceHeight = 0;
    Bitmap mFaceBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_user_detail);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mFaceOffsetX = extras.getFloat(KEY_FACE_OFFSET_X);
            mFaceOffsetY = extras.getFloat(KEY_FACE_OFFSET_Y);
            mFaceWidth = extras.getFloat(KEY_FACE_WIDTH);
            mFaceHeight = extras.getFloat(KEY_FACE_HEIGHT);

            mFaceBitmap = cropBitmap(Uri.parse(extras.getString(KEY_PHOTO_URI)));
            ((ImageView)findViewById(R.id.face_view)).setImageBitmap(mFaceBitmap);
        }
    }

    private Bitmap cropBitmap(Uri bmpUri) {
        InputStream inputStream = null;
        try {
            inputStream = getContentResolver().openInputStream(bmpUri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Bitmap bMap = BitmapFactory.decodeStream(inputStream);

        Bitmap bmOverlay = Bitmap.createBitmap((int)mFaceWidth, (int)mFaceHeight, Bitmap.Config.RGB_565);

        Paint p = new Paint();
        p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        Canvas c = new Canvas(bmOverlay);
        c.drawBitmap(bMap, -mFaceOffsetX, -mFaceOffsetY, null);

        return bmOverlay;
    }
}
