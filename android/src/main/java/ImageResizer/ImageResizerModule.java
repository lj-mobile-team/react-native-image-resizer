package ImageResizer;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;

import java.util.Map;
import java.io.ByteArrayOutputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.provider.MediaStore;
import android.net.Uri;
import java.io.IOException;
import java.io.FileNotFoundException;

public class ImageResizerModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;

    public ImageResizerModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return "ImageResizer";
    }
    
    @ReactMethod
	public void resizedBase64(String uri, int width, int height, final Promise promise)
	{
		try
		{
			Bitmap image = MediaStore.Images.Media.getBitmap(this.context.getContentResolver(), Uri.parse(uri));
			if (image == null)
				// callback.invoke("FAIL : uri: " + uri);
                promise.resolve(null);
			else
                promise.resolve(makeConversion(image, width, height));
				//callback.invoke(null, makeConversion(image, width, height));
		}
		catch (IOException e)
		{
		}
    }
    
    @ReactMethod
	public void resizeImage(String uri, int width, int height, final Promise promise)
	{
		try
		{
			Bitmap image = MediaStore.Images.Media.getBitmap(this.context.getContentResolver(), Uri.parse(uri));
			if (image == null) {
				// callback.invoke("FAIL : uri: " + uri);
                promise.resolve(null);
                return;
            }
            
            Bitmap bitmap;
            
            if (width != 0 && height != 0)
                bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);
		    else
			    bitmap = Bitmap.createBitmap(bitmap);
            
            File file = new File(uri);
            
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            
            out.flush();
            out.close();

            promise.resolve(null);
		}
		catch (IOException e) {
             e.printStackTrace();
		}
    }
    
    
    private String makeConversion(Bitmap bitmap, int width, int height) {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

		if (width != 0 && height != 0)
			bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);
		else
			bitmap = Bitmap.createBitmap(bitmap);

		bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
		byte[] byteArray = byteArrayOutputStream.toByteArray();
		return Base64.encodeToString(byteArray, Base64.DEFAULT);
	}
}