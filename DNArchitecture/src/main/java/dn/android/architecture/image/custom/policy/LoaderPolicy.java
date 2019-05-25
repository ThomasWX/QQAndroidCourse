package dn.android.architecture.image.custom.policy;

import dn.android.architecture.image.custom.request.BitmapRequest;

/**
 * 加载策略
 */
public interface LoaderPolicy {
    /**
     * 两个BitmapRequest进行优先级比较
     * @return
     */
    int compareTo(BitmapRequest request1,BitmapRequest request2);
}
