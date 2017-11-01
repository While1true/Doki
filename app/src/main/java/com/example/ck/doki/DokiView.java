package com.example.ck.doki;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.database.DataSetObserver;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

/**
 * Created by ck on 2017/10/8.
 */

public class DokiView extends HorizontalScrollView {

    private static int margin, ivWidth;

    ViewPager viewPager;

    private DataSetObserver observer;

    private DokiAdapter adapter;

    int orentation = LinearLayout.HORIZONTAL;

    private LinearLayout contentView;

    int checked = 0;

    long lastclicktime = 0;

    private AnimatorSet set;

    public DokiView(Context context) {
        this(context, null);
    }

    public DokiView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DokiView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        margin = dp2px(getContext(), 2);
        ivWidth = dp2px(getContext(), 45);
        contentView = new LinearLayout(getContext());
        addView(contentView, getContentLayoutParams());
        observer = new DataSetObserver() {
            @Override
            public void onChanged() {
                Layout();
            }

            @Override
            public void onInvalidated() {
                super.onInvalidated();
            }
        };
    }

    private LinearLayout.LayoutParams getContentLayoutParams() {
        return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    /**
     * 排列方向
     *待实现
     * @param orentation
     */
    private DokiView setOrentation(int orentation) {
        this.orentation = orentation;
        Layout();
        return this;
    }

    public DokiView setupWithViewPager(ViewPager viewPager) {
        this.viewPager = viewPager;
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                doAnimator(checked, position);
                checked = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        return this;
    }

    /**
     * 设置适配器
     *
     * @param adapter
     */
    public DokiView setAdapter(DokiAdapter adapter) {
        this.adapter = adapter;
        this.adapter.registerDataSetObserver(observer);
        Layout();
        return this;
    }

    private void Layout() {
        if (adapter == null || adapter.getCount() == 0) {
            return;
        }
        contentView.removeAllViews();
        contentView.setOrientation(orentation);
        for (int i = 0; i < adapter.getCount(); i++) {
            ViewBean viewBean = getView(i);
                final int position = i;
                viewBean.itemview.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (checked != position) {
                            /**
                             * viewPager部位null掉viewpager
                             * 否则掉动画
                             */
                            if (viewPager != null) {
                                viewPager.setCurrentItem(position, false);
                            } else {
                                doAnimator(checked, position);
                                checked = position;
                            }
                            if (listener != null) {
                                listener.singleClick(position, v);
                            }
                        } else {
                            if (System.currentTimeMillis() - lastclicktime < 600) {
                                if (listener != null) {
                                    listener.doubleClick(position, v);
                                }
                                lastclicktime = 0;
                            } else {
                                lastclicktime = System.currentTimeMillis();
                            }
                        }
                    }
                });
            ViewGroup.LayoutParams layoutParams = getView(i).itemview.getLayoutParams();
            Log.i("qqq", "Layout: " + (layoutParams == null));
            if (layoutParams == null) {
                /**
                 * 未选中的隐藏文字
                 */
                LinearLayout.LayoutParams contentLayoutParams = getContentLayoutParams();
                contentLayoutParams.leftMargin = margin;
                contentLayoutParams.rightMargin = margin;
                contentLayoutParams.topMargin = margin;
                contentLayoutParams.bottomMargin = margin;
                if (checked != i)
                    contentLayoutParams.width = ivWidth + 2 * margin;
                contentView.addView(viewBean.itemview, contentLayoutParams);
            } else {
                contentView.addView(viewBean.itemview, layoutParams);
            }
        }
    }

    private void doAnimator(final int lastChecked, final int position) {
        final ViewBean lastbean = getView(lastChecked);
        final ViewBean checkbean = getView(position);
        if (set != null)
            set.end();
        set = new AnimatorSet();
        final float checkX = checkbean.itemview.getX();
        Log.i("sss", "doAnimator: " + checkX + "--" + getScrollX());
        checkbean.itemview.measure(0, 0);
        final int measuredWidth = lastbean.itemview.getMeasuredWidth();
        final int measuredWidth2 = checkbean.itemview.getMeasuredWidth();
        ObjectAnimator animator = ObjectAnimator.ofInt(lastbean, "width", measuredWidth, ivWidth).setDuration(200);
        ObjectAnimator animator2 = ObjectAnimator.ofInt(checkbean, "width", ivWidth, measuredWidth2).setDuration(200);
        set.playTogether(animator, animator2);
        set.start();

        if (position > lastChecked) {
            /**
             * 右边点击时若还有item在屏幕外左移view
             */
            float expect = checkX + measuredWidth2 - measuredWidth + 2 * ivWidth + 4 * margin - getScrollX();
            if (expect > getWidth()) {
                smoothScrollBy((int) (expect - getWidth()), 0);
                /**
                 * viewpager滑动时 选中在屏幕左端外范围
                 */
            }else if(checkX<getScrollX()+ivWidth+4*margin+measuredWidth){
                smoothScrollBy((int) (checkX-(getScrollX()+4*margin+measuredWidth)),0);
            }
        } else {
            Log.i("11", "doAnimator: " + (checkX - getScrollX() - ivWidth - 4 * margin));
            if (checkX - getScrollX() < ivWidth + 4 * margin) {
                smoothScrollBy((int) (checkX - getScrollX() - ivWidth - 4 * margin), 0);
                /**
                 * viewpager滑动 选中在屏幕右端
                 */
            }else if(checkX-getScrollX()>getWidth()-measuredWidth2-ivWidth - 4 * margin){
                smoothScrollBy((int) (checkX - getScrollX() -getWidth()+measuredWidth2+ ivWidth + 4 * margin), 0);
            }

        }

    }

    private ViewBean getView(int position) {
        return adapter.getView(orentation, position, contentView);
    }


    onDokiClickListener listener;

    public DokiView setonDokiClickListener(onDokiClickListener listener) {
        this.listener = listener;
        Layout();
        return this;
    }


    public interface onDokiClickListener {
        void singleClick(int position, View view);

        void doubleClick(int position, View view);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.adapter.unregisterDataSetObserver(observer);
    }

    public static abstract class DokiAdapter<T> extends BaseAdapter {
        SparseArray<ViewBean> sparseArray;
        List<T> list;

        public DokiAdapter(List<T> list) {
            this.list = list;
            sparseArray = new SparseArray<>(getCount());
        }

        public List<T> getList() {
            return list;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        public ViewBean getView(int orentation, int i, ViewGroup viewGroup) {
            ViewBean viewBean = sparseArray.get(i);
            if (viewBean == null) {
                viewBean = new ViewBean(orentation, viewGroup);
                sparseArray.put(i, viewBean);
            }
            bindview(i, list.get(i), viewBean);
            return viewBean;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            return null;
        }

        public abstract void bindview(int position, T item, ViewBean viewBean);


    }

    static class ViewBean {
        ImageView iv;
        TextView tv;
        LinearLayout itemview;

        public int getWidth() {
            return itemview.getLayoutParams().width;
        }

        public void setWidth(int width) {
            itemview.getLayoutParams().width = width;
            itemview.requestLayout();
        }

        public ViewBean(int orentation, ViewGroup viewGroup) {
            itemview = new LinearLayout(viewGroup.getContext());
            itemview.setClipChildren(true);
            itemview.setOrientation(orentation);
            itemview.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);

            LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(ivWidth,ivWidth);
            iv = new ImageView(viewGroup.getContext());

            tv = new TextView(viewGroup.getContext());
            tv.setTextColor(0xffffffff);
            tv.setPadding(2 * margin, 0, 2 * margin, 0);
            tv.getPaint().setFakeBoldText(true);
            tv.setSingleLine();

            itemview.addView(iv,params);
            itemview.addView(tv);
        }
    }

    private static int dp2px(Context c, float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, c.getResources().getDisplayMetrics());
    }
}
