package dn.android.architecture.image.custom.loader;

import android.graphics.Bitmap;

import dn.android.architecture.image.custom.request.BitmapRequest;

public class NullLoader extends AbstractLoader{
    @Override
    protected Bitmap onLoad(BitmapRequest request) {
        return null;
    }
}
