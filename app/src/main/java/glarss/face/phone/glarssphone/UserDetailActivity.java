package glarss.face.phone.glarssphone;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.loopj.android.http.*;

import cz.msebera.android.httpclient.Header;

public class UserDetailActivity extends AppCompatActivity {
    private final static int REQUEST_DONE = 3;

    public static final String KEY_PHOTO_URI = "PHOTO_URI";
    public static final String KEY_FACE_OFFSET_X = "FACE_OFFSET_X";
    public static final String KEY_FACE_OFFSET_Y = "FACE_OFFSET_Y";
    public static final String KEY_FACE_WIDTH = "FACE_WIDTH";
    public static final String KEY_FACE_HEIGHT = "FACE_HEIGHT";
    private final static String SERVER_URL = "http://52.32.152.75:8080/users";

    private float mFaceOffsetX = 0;
    private float mFaceOffsetY = 0;
    private float mFaceWidth = 0;
    private float mFaceHeight = 0;
    private static String BITMAP_URI;
    private static Uri FACE_PNG_URI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_user_detail);

        ImageView photoView = (ImageView)findViewById(R.id.face_view);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mFaceOffsetX = extras.getFloat(KEY_FACE_OFFSET_X);
            mFaceOffsetY = extras.getFloat(KEY_FACE_OFFSET_Y);
            mFaceWidth = extras.getFloat(KEY_FACE_WIDTH);
            mFaceHeight = extras.getFloat(KEY_FACE_HEIGHT);

            BITMAP_URI = extras.getString(KEY_PHOTO_URI);

            Bitmap faceBitmp = cropBitmap(Uri.parse(BITMAP_URI));

            photoView.setImageBitmap(faceBitmp);

            try {
                writeFaceFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Button submitButton = (Button)findViewById(R.id.btn_submit);
        assert submitButton != null;
        submitButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (((EditText) findViewById(R.id.input_name)).getText().toString().trim().length() > 0) {
                    try {
                        submitPerson();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
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

    private void submitPerson() throws IOException {
        String name = ((EditText)findViewById(R.id.input_name)).getText().toString();
        String email = ((EditText)findViewById(R.id.input_email)).getText().toString();
        String comment = ((EditText)findViewById(R.id.input_comment)).getText().toString();

        RequestParams params = new RequestParams();
        params.put("user_name", name);
        params.put("user_email", email);
        params.put("user_comment", comment);

        FileInputStream fileInput = new FileInputStream(FACE_PNG_URI.getPath());
        params.put("image", fileInput, "user_farss.png");

        AsyncHttpClient client = new AsyncHttpClient();
        client.post(SERVER_URL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
                //Log.d("NETWORK", "OnStart");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                Log.d("NETWORK", "Success");
                Intent intent = new Intent(getBaseContext(), DoneActivity.class);
                startActivity(intent);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                Log.d("NETWORK", "Failure: " + String.valueOf(statusCode));
                Toast toast = Toast.makeText(getApplicationContext(), "Network error.", Toast.LENGTH_SHORT);
                toast.show();
            }

            @Override
            public void onRetry(int retryNo) {
                //Log.d("NETWORK", "onRetry");
            }
        });
    }

    private void writeFaceFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "PNG_FACE_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".png",
                storageDir
        );

        Bitmap faceBitmp = cropBitmap(Uri.parse(BITMAP_URI));
        FileOutputStream stream = new FileOutputStream(image);
        faceBitmp.compress(Bitmap.CompressFormat.PNG, 100, stream);

        FACE_PNG_URI = Uri.fromFile(image);
    }
}
