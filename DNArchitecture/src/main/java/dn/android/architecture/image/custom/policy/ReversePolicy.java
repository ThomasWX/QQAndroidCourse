package dn.android.architecture.image.custom.policy;

import dn.android.architecture.image.custom.request.BitmapRequest;

public class ReversePolicy implements LoaderPolicy{

    @Override
    public int compareTo(BitmapRequest request1, BitmapRequest request2) {
        // 后进先出
        return request2.getSerialNo() - request1.getSerialNo();
    }
}
