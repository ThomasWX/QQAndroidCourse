package qq.android.common;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public abstract class BaseActivity extends AppCompatActivity {

    private Fragment defaultFragment;
    private Fragment currentFragment;

    private LinearLayout mMenuContainer;
    private LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams
            .WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            showFragment(defaultFragment);
        }
    }

    private void addTabButton(final int label, final Fragment target) {
        Button button = new Button(this);
        button.setLayoutParams(params);
        button.setText(label);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFragment(target);
            }
        });
        mMenuContainer.addView(button);
    }

    /*
    ArrayList与LinkedList: 大数据遍历操作：ArrayList 宜用 for循环 , LinkedList宜用迭代器; forEach两者都不讨好
     */

    private void showFragment(Fragment target) {
        if (currentFragment == target) return;

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (currentFragment != null && currentFragment.isAdded()) {
            transaction.hide(currentFragment);
        }
        if (target.isAdded()) {
            transaction.show(target);
        } else {
            transaction.add(R.id.content, target);
        }
        transaction.commit();
        currentFragment = target;
    }
}
