package dn.android.architecture.image.glide;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.bumptech.glide.Glide;


public class GlideTestActivity extends AppCompatActivity {
    ImageView imageView;
    String imageUrl = "http://www.xinhuanet.com/photo/2019-04/13/c_1124361148.htm";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Glide.with(imageView).load(imageUrl);
    }
}
