package dn.android.architecture.image.custom.loader;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.File;

import dn.android.architecture.image.custom.request.BitmapRequest;
import dn.android.architecture.image.custom.utils.BitmapDecoder;
import dn.android.architecture.image.custom.utils.ImageViewHelper;

public class LocalLoader extends AbstractLoader {
    @Override
    protected Bitmap onLoad(BitmapRequest request) {
        final String path = Uri.parse(request.getImageUrl()).getPath();
        File file = new File(path);
        if (!file.exists()) return null;
        BitmapDecoder decoder = new BitmapDecoder() {
            @Override
            protected Bitmap decodeBitmapWithOptions(BitmapFactory.Options options) {
                return BitmapFactory.decodeFile(path);
            }
        };
        return decoder.decodeBitmap(ImageViewHelper.getImageViewWidth(request.getImageView()),
                ImageViewHelper.getImageViewHeight(request.getImageView()));
    }
}
