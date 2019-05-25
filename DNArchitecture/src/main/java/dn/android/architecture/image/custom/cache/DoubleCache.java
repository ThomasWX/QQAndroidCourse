package dn.android.architecture.image.custom.cache;

import android.graphics.Bitmap;

import dn.android.architecture.image.custom.request.BitmapRequest;

public class DoubleCache implements BitmapCache {
    @Override
    public void put(BitmapRequest request, Bitmap bitmap) {

    }

    @Override
    public Bitmap get(BitmapRequest request) {
        return null;
    }

    @Override
    public void remove(BitmapRequest request) {

    }
}
