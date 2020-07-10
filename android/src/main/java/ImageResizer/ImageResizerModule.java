package ImageResizer;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;

import java.io.EOFException;
import java.io.FilenameFilter;
import java.io.ByteArrayOutputStream;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

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
    public void clean(final Promise promise) {
        final File folder = reactContext.getCacheDir();
        final File[] files = folder.listFiles( new FilenameFilter() {
            @Override
            public boolean accept(final File dir,
                                   final String name) {
                return name.contains( "resizer_");
            }
        });

        WritableArray result = new WritableNativeArray();

        for (final File file : files) {
            WritableMap fileMap = new WritableNativeMap();

            if (!file.delete()) {
                fileMap.putBoolean(file.getAbsolutePath(), false);
            } else {
                fileMap.putBoolean(file.getAbsolutePath(), true);
            }

            result.pushMap(fileMap);
        }

        promise.resolve(result);
    }

    @ReactMethod
	public void resizeImage(String uri, int width, int height, final Promise promise)
	{
	    float newWidth = width;
	    float newHeight = height;
	    Uri parsedUri = Uri.parse(uri);

	    try {
			Bitmap bitmap = MediaStore.Images.Media.getBitmap(reactContext.getContentResolver(), parsedUri);
			if (bitmap == null) {
                promise.resolve(null);
                return;
            }

            float originalWidth = bitmap.getWidth();
            float originalHeight = bitmap.getHeight();

			if(originalWidth <= newWidth && originalHeight <= newHeight) {
                promise.resolve(null);
                return;
            }

            if(originalWidth < originalHeight) {
                newWidth = width * (originalWidth / originalHeight);
            } else if(originalHeight < originalWidth) {
                newHeight = height * (originalHeight / originalWidth);
            }

            if (newWidth != 0 && newHeight != 0)
                bitmap = Bitmap.createScaledBitmap(bitmap, (int)newWidth, (int)newHeight, false);
		    else
			    bitmap = Bitmap.createBitmap(bitmap);

            String mimeType = getMimeType(reactContext, parsedUri);
            Bitmap.CompressFormat compressFormat = mimeType == "png" ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG;

            File path = reactContext.getCacheDir();
            File oldFile = new File(uri);

            File file = saveImage(bitmap, path, oldFile, compressFormat, 80);


            try {
                ExifHelper.copyExif(oldFile.getCanonicalPath().replace("/file:", ""), file.getCanonicalPath());
            } catch (EOFException e) {
                e.printStackTrace();
            }

            bitmap.recycle();
            promise.resolve(file.getCanonicalPath());
		}
		catch (IOException e) {
            promise.resolve(null);
            e.printStackTrace();
		}
    }

    private String getMimeType(Context context, Uri uri) {
        String extension;

        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            final MimeTypeMap mime = MimeTypeMap.getSingleton();
            extension = mime.getExtensionFromMimeType(context.getContentResolver().getType(uri));
        } else {
            extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(new File(uri.getPath())).toString());
        }

        return extension;
    }

    private File saveImage(Bitmap bitmap, File saveDirectory, File oldFile,
                                 Bitmap.CompressFormat compressFormat, int quality)
            throws IOException {
        if (bitmap == null) {
            throw new IOException("The bitmap couldn't be resized");
        }

        File newFile = new File(saveDirectory, "resizer_" + oldFile.getName());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(compressFormat, quality, outputStream);
        byte[] bitmapData = outputStream.toByteArray();

        outputStream.flush();
        outputStream.close();

        FileOutputStream fos = new FileOutputStream(newFile);
        fos.write(bitmapData);
        fos.flush();
        fos.close();

        return newFile;
    }
}
