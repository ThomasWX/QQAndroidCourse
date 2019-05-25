package dn.android.architecture.image.custom.loader;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


import dn.android.architecture.image.custom.request.BitmapRequest;
import dn.android.architecture.image.custom.utils.BitmapDecoder;
import dn.android.architecture.image.custom.utils.ImageViewHelper;

public class UrlLoader extends AbstractLoader{












    @Override
    protected Bitmap onLoad(BitmapRequest request) {
//        HttpURLConnection connection = null;
////        HttpsURLConnection
//        InputStream inputStream = null;
//        URL url = new URL(request.getImageUrl());
//        BitmapDecoder decoder;
//        try {


//
//            connection = (HttpURLConnection) url.openConnection();
//            // 转换成BufferedInputStream
//            inputStream = new BufferedInputStream(connection.getInputStream());
//
//
//
//            // 为何不用 BitmapFactory.decodeStream(inputStream) ?
//            // 因为 需要做自适应宽高且不失真，需等比例缩放
//            inputStream.mark(inputStream.available()); // 标记inputStream.reset时，回位到哪里
//
//            // 即 第一次读到边界信息位置，第二次只需从边界位置开始读即可，不用从头读
//
//
//            decoder = new BitmapDecoder() {
//                @Override
//                protected Bitmap decodeBitmapWithOptions(BitmapFactory.Options options) {
//                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream,null,options);
//
//                    // 确定是第一次读流
//                    if (options.inJustDecodeBounds){
//                        try {
//                            //TODO （可能不需要） 因为InputStream 读流的过程，读一次指针就往前移动，第一次读取后，指针处于边界信息位置，所以要重置一下，让下一次正确读取。
//                            inputStream.reset();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    } else {
//                        inputStream.close();
//                    }
//                    return bitmap;
//                }
//            };

            // 先下载再读取





//            return decoder.decodeBitmap(ImageViewHelper.getImageViewWidth(request.getImageView()),ImageViewHelper.getImageViewHeight(request.getImageView()));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        return null;
    }
}
