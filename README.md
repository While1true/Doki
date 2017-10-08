# Doki
仿腾讯视频Doki
仿腾讯视频Doki页

## 1.效果
1.腾讯视频效果
![2017-10-08-16-28-27.gif](http://upload-images.jianshu.io/upload_images/6456519-d4962b92a77acb7d.gif?imageMogr2/auto-orient/strip)
2.实现的效果

![2017-10-08-16-50-49.gif](http://upload-images.jianshu.io/upload_images/6456519-000d3b0886483dc3.gif?imageMogr2/auto-orient/strip)


使用
```
   dokiView.setAdapter(new DokiView.DokiAdapter(list) {
            @Override
            public void bindview(int position, T t, DokiView.ViewBean viewBean) {
                viewBean.tv.setText();
                viewBean.iv.setImageDrawable(getRounddrawable(R.drawable.a5)
            }
        })
        .setonDokiClickListener(new DokiView.onDokiClickListener() {
            @Override
            public void singleClick(int position, View view) {
                Toast.makeText(view.getContext(), "singleClick", 0).show();
            }

            @Override
            public void doubleClick(int position, View view) {
                Toast.makeText(view.getContext(), "doubleClick", 0).show();
            }
        })
        .setupWithViewPager(vp);
```


## 2.实现
> 先打开腾讯视频，用studio device monitor查看是一个水平scrollView
内面是TabWidget，我这边就用Linealayout了

![aa.jpg.png](http://upload-images.jianshu.io/upload_images/6456519-27236daa7eb77811.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


1. 自定义View继承horizontalScrollView

2. 添加一个水平的LinearLayout用于添加各个View

```
contentView = new LinearLayout(getContext());
addView(contentView, getContentLayoutParams());
```

3. 改版Baseadapter用于生产view

```
 public static abstract class DokiAdapter<T> extends BaseAdapter {
        public ViewBean getView(int orentation, int i, ViewGroup viewGroup) {
            ViewBean viewBean = sparseArray.get(i);
            if (viewBean == null) {
                viewBean = new ViewBean(orentation, viewGroup);
                sparseArray.put(i, viewBean);
            }
            bindview(i, list.get(i), viewBean);
            return viewBean;
        }
        public abstract void bindview(int position, T item, ViewBean viewBean);
    }
```

4. view 单个子包装到ViewBean

```
static class ViewBean {
        ImageView iv;
        TextView tv;
        LinearLayout itemview;

        public ViewBean(int orentation, ViewGroup viewGroup) {
            itemview = new LinearLayout(viewGroup.getContext());
            itemview.setClipChildren(true);
            itemview.setOrientation(orentation);
            itemview.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);

            iv = new ImageView(viewGroup.getContext());

            tv = new TextView(viewGroup.getContext());
            tv.setTextColor(0xffffffff);
            tv.setPadding(2 * margin, 0, 2 * margin, 0);
            tv.getPaint().setFakeBoldText(true);
            tv.setSingleLine();

            itemview.addView(iv);
            itemview.addView(tv);
        }
    }
```

5. 添加view到LinearLayout

```
 private void Layout() {
        if (adapter == null || adapter.getCount() == 0) {
            return;
        }
        contentView.removeAllViews();
        contentView.setOrientation(orentation);
        for (int i = 0; i < adapter.getCount(); i++) {
            ViewBean viewBean = getView(i);
            if (listener != null) {
                final int position = i;
                viewBean.itemview.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (checked != position) {
                            /**
                             * viewPager不为null调viewpager
                             * 否则调动画
                             */
                            if (viewPager != null) {
                                viewPager.setCurrentItem(position, false);
                            } else {
                                doAnimator(checked, position);
                                checked = position;
                            }
                            listener.singleClick(position, v);
                        } else {
                            if (System.currentTimeMillis() - lastclicktime < 600) {
                                listener.doubleClick(position, v);
                                lastclicktime = 0;
                            } else {
                                lastclicktime = System.currentTimeMillis();
                            }
                        }
                    }
                });
            }
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

```

6. 过度动画
显示名字的view渐隐，选中的渐现，若在边缘位置判断让左右的view平移到屏幕中
```
 private void doAnimator(final int lastChecked, final int position) {
        final ViewBean lastbean = getView(lastChecked);
        final ViewBean checkbean = getView(position);
        if (set != null)
            set.end();
        set = new AnimatorSet();
        
        final float checkX = checkbean.itemview.getX();
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
            if (expect > getWidth() - getPaddingLeft() - getPaddingRight()) {
                smoothScrollBy((int) (expect - getWidth()), 0);
            }
        } else {
        /**
             * 左边点击时若还有item在屏幕外右移view
             */
            Log.i("11", "doAnimator: " + (checkX - getScrollX() - ivWidth - 4 * margin));
            if (checkX - getScrollX() < ivWidth + 4 * margin) {
                smoothScrollBy((int) (checkX - getScrollX() - ivWidth - 4 * margin), 0);
            }

        }

    }
```
[github地址](https://github.com/While1true/Doki)
