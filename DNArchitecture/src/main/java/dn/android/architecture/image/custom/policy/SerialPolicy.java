package dn.android.architecture.image.custom.policy;

import dn.android.architecture.image.custom.request.BitmapRequest;

public class SerialPolicy implements LoaderPolicy{
    @Override
    public int compareTo(BitmapRequest request1, BitmapRequest request2) {
        // 先进先出
        return request1.getSerialNo() -request2.getSerialNo();
    }
}
