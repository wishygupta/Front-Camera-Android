package com.example.frontcam;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

public class CameraActivity extends ActionBarActivity {
	private Camera mCamera;
	private CameraPreview mPreview;
	Button captureButton;
	public static final int MEDIA_TYPE_IMAGE = 1;
	static Context con;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera);
		captureButton = (Button) findViewById(R.id.button_capture);
		FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);

		// Create an instance of Camera
		con = getApplicationContext();
		try {
			mCamera = getCameraInstance();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Create our Preview view and set it as the content of our activity.
		mPreview = new CameraPreview(this, mCamera);

		preview.addView(mPreview);

		captureButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// get an image from the camera
				mCamera.takePicture(null, null, mPicture);
			}
		});
	}

	Bitmap bitmap;
	private PictureCallback mPicture = new PictureCallback() {

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {

			System.gc();
			bitmap = null;
			BitmapWorkerTask task = new BitmapWorkerTask(data);
			task.execute(0);
		}
	};

	class BitmapWorkerTask extends AsyncTask<Integer, Void, Bitmap> {
		private final WeakReference<byte[]> dataf;
		private int data = 0;

		public BitmapWorkerTask(byte[] imgdata) {
			// Use a WeakReference to ensure the ImageView can be garbage
			// collected
			dataf = new WeakReference<byte[]>(imgdata);
		}

		// Decode image in background.
		@Override
		protected Bitmap doInBackground(Integer... params) {
			data = params[0];
			ResultActivity(dataf.get());
			return mainbitmap;
		}

		// Once complete, see if ImageView is still around and set bitmap.
		@Override
		protected void onPostExecute(Bitmap bitmap) {
			if (mainbitmap != null) {

				Intent i = new Intent();
				i.putExtra("BitmapImage", mainbitmap);
				setResult(-1, i);
				// Here I am Setting the Requestcode 1, you can put according to
				// your requirement
				finish();
			}
		}
	}

	Bitmap mainbitmap;

	public void ResultActivity(byte[] data) {
		mainbitmap = null;
		mainbitmap = decodeSampledBitmapFromResource(data, 200, 200);
		mainbitmap=RotateBitmap(mainbitmap,270);
		mainbitmap=flip(mainbitmap);
	}

	public static Bitmap decodeSampledBitmapFromResource(byte[] data,
			int reqWidth, int reqHeight) {

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		// BitmapFactory.decodeResource(res, resId, options);
		BitmapFactory.decodeByteArray(data, 0, data.length, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth,
				reqHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeByteArray(data, 0, data.length, options);
	}

	public static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {
			if (width > height) {
				inSampleSize = Math.round((float) height / (float) reqHeight);
			} else {
				inSampleSize = Math.round((float) width / (float) reqWidth);
			}
		}
		return inSampleSize;
	}

	/** A safe way to get an instance of the Camera object. */
	public static Camera getCameraInstance() {
		Camera c = null;
		Log.d("No of cameras", Camera.getNumberOfCameras() + "");
		for (int camNo = 0; camNo < Camera.getNumberOfCameras(); camNo++) {
			CameraInfo camInfo = new CameraInfo();
			Camera.getCameraInfo(camNo, camInfo);

			if (camInfo.facing == (Camera.CameraInfo.CAMERA_FACING_FRONT)) {
				c = Camera.open(camNo);
				c.setDisplayOrientation(90);
			}
		}
		return c; // returns null if camera is unavailable
	}

	@Override
	protected void onPause() {
		super.onPause();
		releaseCamera(); // release the camera immediately on pause event
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		if (mCamera == null) {
			setContentView(R.layout.activity_camera);
			captureButton = (Button) findViewById(R.id.button_capture);
			FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);

			// Create an instance of Camera
			con = getApplicationContext();
			try {
				mCamera = getCameraInstance();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// Create our Preview view and set it as the content of our
			// activity.
			mPreview = new CameraPreview(this, mCamera);

			preview.addView(mPreview);
		}
	}

	private void releaseCamera() {
		if (mCamera != null) {
			mCamera.release(); // release the camera for other applications
			mCamera = null;
		}
	}

	// rotate the bitmap to portrait
	public static Bitmap RotateBitmap(Bitmap source, float angle) {
		Matrix matrix = new Matrix();
		matrix.postRotate(angle);
		return Bitmap.createBitmap(source, 0, 0, source.getWidth(),
				source.getHeight(), matrix, true);
	}

	//the front camera displays the mirror image, we should flip it to its original
		Bitmap flip(Bitmap d)
		{
		    Matrix m = new Matrix();
		    m.preScale(-1, 1);
		    Bitmap src = d;
		    Bitmap dst = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), m, false);
		    dst.setDensity(DisplayMetrics.DENSITY_DEFAULT);
		    return dst;
		}
		
	
}
