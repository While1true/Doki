package com.example.ck.doki;

import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewPager vp = (ViewPager) findViewById(R.id.vp);
        DokiView dokiView = (DokiView) findViewById(R.id.dokiView);
        final List<String> list = new ArrayList<>(50);
        for (int i = 0; i < 12; i++) {
            list.add("小戏骨" + i);
        }
        dokiView.setAdapter(new DokiView.DokiAdapter<String>(list) {
            @Override
            public void bindview(int position, String str, DokiView.ViewBean viewBean) {
                viewBean.tv.setText(str);
                int i = position % 5;
                viewBean.iv.setLayoutParams(new LinearLayout.LayoutParams(dp2px(45), dp2px(45)));
                viewBean.iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
                if (i == 0)
                    viewBean.iv.setImageDrawable(getRounddrawable(R.drawable.a1));
                else if (i == 1)
                    viewBean.iv.setImageDrawable(getRounddrawable(R.drawable.a2));
                else if (i == 2)
                    viewBean.iv.setImageDrawable(getRounddrawable(R.drawable.a3));
                else if (i == 3)
                    viewBean.iv.setImageDrawable(getRounddrawable(R.drawable.a4));
                else if (i == 4)
                    viewBean.iv.setImageDrawable(getRounddrawable(R.drawable.a5));
            }
        }).setonDokiClickListener(new DokiView.onDokiClickListener() {
            @Override
            public void singleClick(int position, View view) {
                Toast.makeText(view.getContext(), "singleClick", 0).show();
            }

            @Override
            public void doubleClick(int position, View view) {
                Toast.makeText(view.getContext(), "doubleClick", 0).show();
            }
        }).setupWithViewPager(vp);
        vp.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return list.size();
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                ImageView iv = new ImageView(container.getContext());
                int i = position % 5;
                if (i == 0)
                    iv.setImageDrawable(getdrawable(R.drawable.a1));
                else if (i == 1)
                    iv.setImageDrawable(getdrawable(R.drawable.a2));
                else if (i == 2)
                    iv.setImageDrawable(getdrawable(R.drawable.a3));
                else if (i == 3)
                    iv.setImageDrawable(getdrawable(R.drawable.a4));
                else if (i == 4)
                    iv.setImageDrawable(getdrawable(R.drawable.a5));
                container.addView(iv);
                return iv;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView((View) object);
            }
        });
    }

    public Drawable getRounddrawable(int res) {
        RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), BitmapFactory.decodeResource(getResources(), res));
        roundedBitmapDrawable.setCircular(true);
        return roundedBitmapDrawable;
    }

    public Drawable getdrawable(int res) {
        RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), BitmapFactory.decodeResource(getResources(), res));

        return roundedBitmapDrawable;
    }

    private int dp2px(float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }
}
