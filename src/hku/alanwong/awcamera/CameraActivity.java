package hku.alanwong.awcamera;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.*;
import android.widget.*;

public class CameraActivity extends Activity {
	protected static final String TAG = null;
	private byte[] data;
	private Camera mCamera;
    private CameraPreview mPreview;
    private int currentSize=1, width=0, height=0;
    private Camera.Parameters cp;
    private List<Size> supportedSize;
    public static final int MEDIA_TYPE_IMAGE = 1;
    private Button captureButton, saveButton, editButton, backButton, resButton, galleryButton, hdrButton;
    private TextView resTextView;
	
    public void editButtonsToggle(boolean visible){
    	if (visible){
    		captureButton.setVisibility(View.GONE);
    		resButton.setVisibility(View.GONE);
    		resTextView.setVisibility(View.GONE);
    		editButton.setVisibility(View.VISIBLE);
            saveButton.setVisibility(View.VISIBLE);
            backButton.setVisibility(View.VISIBLE);
    	} else {
    		captureButton.setVisibility(View.VISIBLE);
    		resButton.setVisibility(View.VISIBLE);
    		resTextView.setVisibility(View.VISIBLE);
    		editButton.setVisibility(View.GONE);
    		saveButton.setVisibility(View.GONE);
    		backButton.setVisibility(View.GONE);
    	}
    }
    
    
    public boolean dispatchKeyEvent(KeyEvent event){
        switch(event.getKeyCode()){
			case KeyEvent.KEYCODE_VOLUME_DOWN:
			case KeyEvent.KEYCODE_VOLUME_UP:
				capture();
				return true;
			default:
				return super.dispatchKeyEvent(event);
		}
    }
        
    private void capture(){
    	editButtonsToggle(true);
    	mCamera.autoFocus(new Camera.AutoFocusCallback(){
    		  public void onAutoFocus(boolean success, Camera camera){
    			  camera.takePicture(
//    					  new Camera.ShutterCallback(){
//    		    			  public void onShutter(){
//    		    				  // Play your sound here.
//    		    			  }
//    		    		  },
    					  null,
    		    		  null, 
    		    		  new PictureCallback(){
    		    			  public void onPictureTaken(byte[] _data, Camera camera){
    		    				  data = _data;
    		    			  }
    		    		  }
    			  );
    		  }
    	});
    }
        
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_camera);
		
		if (this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
		    try{
		    	mCamera = Camera.open();
		    } catch (Exception e){
		    	Toast.makeText(CameraActivity.this, "Camera not accessible", Toast.LENGTH_SHORT).show();
		    }

	        // Create our Preview view and set it as the content of our activity.
	        mPreview = new CameraPreview(this, mCamera);
	        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
	        preview.addView(mPreview);
		}
		
		editButton = (Button) findViewById(R.id.button_edit);		
		
		captureButton = (Button) findViewById(R.id.button_capture);
		captureButton.setOnClickListener(
		    new View.OnClickListener(){
		        @Override
		        public void onClick(View v){
		        	capture();
		        }
		    }
		);
		
		backButton = (Button) findViewById(R.id.button_back);
		backButton.setOnClickListener(
		    new View.OnClickListener(){
		    	@Override
		        public void onClick(View v){
		        	editButtonsToggle(false);
		        	mCamera.startPreview();
		        }
		    }
		);
		
		saveButton = (Button) findViewById(R.id.button_save);
		saveButton.setOnClickListener(
		    new View.OnClickListener(){
		    	@Override
		        public void onClick(View v){
		        	File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "AWCamera");
				    
				    // Create the storage directory if it does not exist
				    if (!mediaStorageDir.exists()){
				    	Toast.makeText(CameraActivity.this, "Creating a directory named AWCamera...", Toast.LENGTH_SHORT).show();
				        if (!mediaStorageDir.mkdirs()){
				        	Toast.makeText(CameraActivity.this, "Storage not accessible", Toast.LENGTH_SHORT).show();
				        }
				    }
				    
				    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
				    File pictureFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_"+ timeStamp + ".jpg");	        

			        try {
			            FileOutputStream fos = new FileOutputStream(pictureFile);
			            fos.write(data);
			            fos.flush();
			            CameraActivity.this.getApplicationContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(pictureFile)));
			            //sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://"+ mediaStorageDir)));
			            fos.close();
			        } catch (FileNotFoundException e){
			        	Toast.makeText(CameraActivity.this, "CameraActivity onCreate - FileNotFoundException", Toast.LENGTH_SHORT).show();
			        } catch (IOException e){
			        	Toast.makeText(CameraActivity.this, "CameraActivity onCreate - IOException", Toast.LENGTH_SHORT).show();
			        } finally {
			        	Toast.makeText(CameraActivity.this, "Image saved to " + mediaStorageDir.getPath(), Toast.LENGTH_SHORT).show();
			        	editButtonsToggle(false);
			        	mCamera.startPreview();
			        }
		        }
		    }
		);
		
		cp = mCamera.getParameters();
	    supportedSize = cp.getSupportedPictureSizes();
	    width = supportedSize.get(0).width;
		height = supportedSize.get(0).height;
	    int _size = width * height / 100000;
		double size = (double)_size/10;
		resTextView = (TextView) findViewById(R.id.textView_res);
		resTextView.setText(Integer.toString(width) + " x " + Integer.toString(height));
		resButton = (Button) findViewById(R.id.button_res);
		resButton.setText(Double.toString(size) + "MP");
		cp.setPictureSize(width, height);
		mCamera.setParameters(cp);
		resButton.setOnClickListener(
		    new View.OnClickListener(){
		    	@Override
		        public void onClick(View v){
		    		while (true){
		    			if (currentSize >= supportedSize.size()) {
			    			currentSize = 0;
			    		}
			    		width = supportedSize.get(currentSize).width;
		    			height = supportedSize.get(currentSize).height;
		    			currentSize++;
		    			if ((double)height / width == 0.75){
		    				break;
		    			}
		    		}
		    		
		    		int _size = width * height / 100000;
		    		double size = (double)_size/10;
		    		
		    		resButton.setText(Double.toString(size) + "MP");
		    		resTextView.setText(Integer.toString(width) + " x " + Integer.toString(height));
		    		cp.setPictureSize(width, height);
		    		mCamera.setParameters(cp);
		        }
		    }
		);	
		
		galleryButton = (Button) findViewById(R.id.button_gallery);
		galleryButton.setOnClickListener(
		    new View.OnClickListener(){
		    	@Override
		        public void onClick(View v){
		    		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		    	    intent.setType("image/*");
		    	    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		    	    startActivity(intent);
		    	    finish();
		        }
		    }
		);
				
		editButtonsToggle(false);
	}
	
	@Override
    protected void onPause(){
        super.onPause();
        if (mCamera != null){
            mCamera.release();
            mCamera = null;
        }
    }	
}
