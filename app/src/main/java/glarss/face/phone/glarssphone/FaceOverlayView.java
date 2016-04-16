package glarss.face.phone.glarssphone;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;

public class FaceOverlayView extends View {

    private Bitmap mBitmap;
    private SparseArray<Face> mFaces;

    private float mFaceOffsetX = 0;
    private float mFaceOffsetY = 0;
    private float mFaceWidth = 0;
    private float mFaceHeight = 0;

    public FaceOverlayView(Context context) {
        this(context, null);
    }

    public FaceOverlayView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FaceOverlayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public boolean faceDetected() {
        return mFaces.size() > 0;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if ((mBitmap != null) && (mFaces != null)) {
            if (mFaces.size() > 0) {
                double scale = drawBitmap(canvas);
                drawFaceBox(canvas, scale);
            }
        }
    }

    public float getmFaceOffsetX() {
        return mFaceOffsetX;
    }

    public float getmFaceOffsetY() {
        return mFaceOffsetY;
    }

    public float getmFaceWidth() {
        return mFaceWidth;
    }

    public float getmFaceHeight() {
        return mFaceHeight;
    }

    private double drawBitmap(Canvas canvas) {
        double viewWidth = canvas.getWidth();
        double viewHeight = canvas.getHeight();
        double imageWidth = mBitmap.getWidth();
        double imageHeight = mBitmap.getHeight();
        double scale = Math.min( viewWidth / imageWidth, viewHeight / imageHeight );

        Rect destBounds = new Rect(0, 0, (int) ( imageWidth * scale ), (int) ( imageHeight * scale ) );
        canvas.drawBitmap( mBitmap, null, destBounds, null );
        return scale;
    }

    private void drawFaceBox(Canvas canvas, double scale) {
        Paint paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);

        float left = 0;
        float top = 0;
        float right = 0;
        float bottom = 0;

        Face face = mFaces.valueAt(0);

        left = (float) ( face.getPosition().x * scale );
        top = (float) ( face.getPosition().y * scale );
        right = (float) scale * ( face.getPosition().x + face.getWidth() );
        bottom = (float) scale * ( face.getPosition().y + face.getHeight() );

        mFaceWidth = face.getWidth();
        mFaceHeight = face.getHeight();
        mFaceOffsetX = face.getPosition().x;
        mFaceOffsetY = face.getPosition().y;

        canvas.drawRect(left, top, right, bottom, paint);
    }

    public void setBitmap(Bitmap bitmap) {
        mBitmap = bitmap;

        FaceDetector detector = new FaceDetector.Builder( getContext() )
                .setTrackingEnabled(false)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .setMode(FaceDetector.FAST_MODE)
                .build();

        if (!detector.isOperational()) {
            //Handle contingency
        } else {
            Frame frame = new Frame.Builder().setBitmap(bitmap).build();
            mFaces = detector.detect(frame);
            Log.d("FACES", "Faces detected:" + String.valueOf(mFaces.size()));
            detector.release();
        }

        invalidate();
    }
}
