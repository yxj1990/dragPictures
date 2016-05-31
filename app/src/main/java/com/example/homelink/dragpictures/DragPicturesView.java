package com.example.homelink.dragpictures;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;



import java.util.ArrayList;
import java.util.List;

/**
 * Created by homelink on 2016/5/27.
 */
public class DragPicturesView extends ScrollView {
    //private List<DragPictureImageView> delViews = new ArrayList<>();  //删除的图片

    public DragPicturesView(Context context) {
        super(context);
        initView();
    }

    public DragPicturesView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public DragPicturesView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

//    public DragPicturesView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
//        super(context, attrs, defStyleAttr, defStyleRes);
//        initView();
//    }




    int mWidth;
    int mHeight;
    int gap;

    public void initView() {

        super.setVerticalScrollBarEnabled(false);
        mWidth =  ScreenUtils.getScreenWidth(getContext());
        mHeight = ScreenUtils.getScreenHeight(getContext());
        gap =dp2px(getContext(),20) ;
        //setOnLongClickListener(onLongClickListener);
        //初始化显示被拖动item的image view
        dragImageView = new ImageView(getContext());
        dragImageView.setTag(DRAG_IMG_NOT_SHOW);
        //初始化用于设置dragImageView的参数对象
        dragImageViewParams = new WindowManager.LayoutParams();
        //获取窗口管理对象，用于后面向窗口中添加dragImageView
        windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        ll = new RelativeLayout(getContext());
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ScreenUtils.getScreenWidth(getContext()), ScreenUtils.getScreenWidth(getContext()) * 2);

//        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ScreenUtils.getScreenWidth(getContext()), RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT);
        ll.setLayoutParams(lp);
        this.addView(ll);


        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(dm);

    }

    private RelativeLayout ll;


    private static final int DRAG_IMG_SHOW = 1;
    private static final int DRAG_IMG_NOT_SHOW = 0;
    private static final String LOG_TAG = "DragGridView";
    private static float AMP_FACTOR = 1.2f;

    private ImageView dragImageView;
    private WindowManager.LayoutParams dragImageViewParams;
    private WindowManager windowManager;
    private boolean isViewOnDrag = false;
    //  private View currentDragView;

    /**
     * previous dragged over position
     */
    private int preDraggedOverPositon = AdapterView.INVALID_POSITION;
    private int downRawX;
    private int downRawY;
    private AdapterView.OnLongClickListener onLongClickListener = new OnLongClickListener() {

        @Override
        public boolean onLongClick(View view) {
            if (editType == 0 || editType == 2) return false;

            //  currentDragView = view;
            //记录长按item位置
            preDraggedOverPositon = views.indexOf(view);

            //获取被长按item的drawing cache
            view.destroyDrawingCache();
            view.setDrawingCacheEnabled(true);
            //通过被长按item，获取拖动item的bitmap
            Bitmap dragBitmap = Bitmap.createBitmap(view.getDrawingCache());

            //设置拖动item的参数
            dragImageViewParams.gravity = Gravity.TOP | Gravity.LEFT;
            //设置拖动item为原item 1.2倍
            if (preDraggedOverPositon == 0) {
                AMP_FACTOR = 0.3f;
            } else {
                AMP_FACTOR = 1.2f;
            }
            dragImageViewParams.width = (int) (AMP_FACTOR * dragBitmap.getWidth());
            dragImageViewParams.height = (int) (AMP_FACTOR * dragBitmap.getHeight());


            //设置触摸点为绘制拖动item的中心
            dragImageViewParams.x = (downRawX - dragImageViewParams.width / 2);
            dragImageViewParams.y = (downRawY - dragImageViewParams.height / 2);
            dragImageViewParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
            dragImageViewParams.format = PixelFormat.TRANSLUCENT;
            dragImageViewParams.windowAnimations = 0;

            //dragImageView为被拖动item的容器，清空上一次的显示
            if ((int) dragImageView.getTag() == DRAG_IMG_SHOW) {
                windowManager.removeView(dragImageView);
                dragImageView.setTag(DRAG_IMG_NOT_SHOW);
            }

            //设置本次被长按的item
            dragImageView.setImageBitmap(dragBitmap);

            //添加拖动item到屏幕
            windowManager.addView(dragImageView, dragImageViewParams);
            dragImageView.setTag(DRAG_IMG_SHOW);
            isViewOnDrag = true;


            return true;
        }
    };
    public static int dp2px(Context context, int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return (int) ((dp * displayMetrics.density) + 0.5);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (editType == 0 || editType == 2) return super.onTouchEvent(ev);

        //被按下时记录按下的坐标
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            //获取触摸点相对于屏幕的坐标

            downRawX = (int) ev.getRawX();
            downRawY = (int) ev.getRawY();
        }
        //dragImageView处于被拖动时，更新dragImageView位置
        else if ((ev.getAction() == MotionEvent.ACTION_MOVE) && (isViewOnDrag )) {

            //  Log.i(LOG_TAG, "" + ev.getRawX() + " " + ev.getRawY());
            //设置触摸点为dragImageView中心
            dragImageViewParams.x = (int) (ev.getRawX() - dragImageView.getWidth() / 2);
            dragImageViewParams.y = (int) (ev.getRawY() - dragImageView.getHeight() / 2);
            //更新窗口显示
            windowManager.updateViewLayout(dragImageView, dragImageViewParams);

            //获取当前触摸点的item position
            int currDraggedPosition = pointToPosition((int) ev.getX(), (int) ev.getY() + this.getScrollY());
            //如果当前停留位置item不等于上次停留位置的item，交换本次和上次停留的item
            if ((currDraggedPosition != AdapterView.INVALID_POSITION) && (currDraggedPosition != preDraggedOverPositon)) {
                swapView(preDraggedOverPositon, currDraggedPosition);
                preDraggedOverPositon = currDraggedPosition;
            }
            if (ev.getRawY() < getY() + 400 || ev.getRawY() > mHeight - 200) {
                this.smoothScrollBy(0, (int) (ev.getRawY() - downRawY));
            }

            return true;
        }
        //释放dragImageView
        else if ((ev.getAction() == MotionEvent.ACTION_UP) && (isViewOnDrag )) {

            if ((int) dragImageView.getTag() == DRAG_IMG_SHOW) {
                windowManager.removeView(dragImageView);
                dragImageView.setTag(DRAG_IMG_NOT_SHOW);

            }
            isViewOnDrag = false;

        }
        return super.onTouchEvent(ev);
    }




    //更新拖动时的gridView
    public void swapView(int draggedPos, int destPos) {
        if (draggedPos < destPos) {
            views.add(destPos + 1, views.get(draggedPos));
            views.remove(draggedPos);
        }
        //从后向前拖动，其他item依次后移
        else if (draggedPos > destPos) {
            views.add(destPos, views.get(draggedPos));
            views.remove(draggedPos + 1);
        }
        Log.e("test", draggedPos + "-" + destPos);
        redraw();
    }

    /**
     * 根据xy判断此处是第几个view
     *
     * @param x
     * @param y
     * @return
     */
    private int pointToPosition(int x, int y) {
        Rect frame = new Rect();
        for (int i = 0; i < views.size(); i++) {
            views.get(i).getHitRect(frame);
            if (frame.contains(x, y)) {
                return i;
            }
        }
        return -1;
    }


    private List<View> views = new ArrayList<View>();

    /**
     * 添加View
     */
    public void AddView(View view) {
        views.add(view);
        int t = views.size() - 1;
        int row = t / 3 + (t % 3 > 0 ? 1 : 0);
        ViewGroup.LayoutParams lp = ll.getLayoutParams();
        lp.height = (row) * (mWidth / 3) + mWidth;
        ll.setLayoutParams(lp);
    }

    public void RemoveAllViews() {
        views.clear();
        ll.removeAllViews();
    }

    public void redraw() {


        ll.removeAllViews();

        for (int i = 0; i < views.size(); i++) {
            View v = views.get(i);
            ll.addView(v);

            v.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v1) {
                    if (editType == 0 || editType == 2) return ;
                    int temp=views.indexOf(v1);
                    swapView(temp,0);
                }
            });
            int width = mWidth - gap * 2;
            if (i == 0) {

                v.getLayoutParams().width = width;
//                v.getLayoutParams().height = mWidth - gap * 2;
                v.getLayoutParams().height = width * 2 / 3;
                v.setX(gap);
                v.setY(gap);


            } else {
                v.getLayoutParams().width = (mWidth - gap * 4) / 3;
                v.getLayoutParams().height = (mWidth - gap * 4) / 3;
                v.setX(gap + ((mWidth - gap * 4) / 3 + gap) * ((i - 1) % 3));
                v.setY((i - 1) / 3 * ((mWidth - gap * 4) / 3 + gap) + mWidth - (width / 3));
            }


            ImageView img = (ImageView) v.findViewById(R.id.iv_picture_delete);
            if (editType == 2) {
                img.setVisibility(View.VISIBLE);
                shakeAnimation(v);   //抖动动画
            } else {
                img.setVisibility(View.GONE);
            }
            img.setTag(v);
            img.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v1) {

                   // delViews.add((DragPictureImageView) ((ViewGroup) v1.getParent()).findViewById(R.id.iv_picture));

                    ((View) v1.getTag()).clearAnimation();
                    views.remove(v1.getTag());

                    int t = views.size() - 1;
                    int row = t / 3 + (t % 3 > 0 ? 1 : 0);
                    ViewGroup.LayoutParams lp = ll.getLayoutParams();
                    lp.height = (row) * (mWidth / 3) + mWidth;
                    ll.setLayoutParams(lp);
                    redraw();

                }
            });
            v.setOnLongClickListener(onLongClickListener);
            v.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent ev) {
                    if (editType == 0 || editType == 2) return false;
                    if (ev.getAction() == MotionEvent.ACTION_DOWN) {
                        downRawX = (int) ev.getRawX();
                        downRawY = (int) ev.getRawY();
                        System.gc();
                    } else if ((ev.getAction() == MotionEvent.ACTION_UP) && (isViewOnDrag)) {
                        if ((int) dragImageView.getTag() == DRAG_IMG_SHOW) {
                            windowManager.removeView(dragImageView);
                            dragImageView.setTag(DRAG_IMG_NOT_SHOW);
                        }
                        isViewOnDrag = false;
                        return true;
                    }

                    return false;
                }
            });
        }
        addHintView();
    }


    /**
     * 添加提示信息
     */
    private void addHintView() {
        TextView txt = new TextView(getContext());
        txt.setText("建议使用横图作为默认图");
        txt.setTextColor(Color.parseColor("#ababab"));
        txt.setTextSize(15);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        txt.setGravity(Gravity.CENTER_HORIZONTAL);
        txt.setLayoutParams(lp);
        txt.setY(ll.getLayoutParams().height - mWidth / 4);
        ll.addView(txt);
    }

    private int editType = 0;   // 0 正常状态    1.拖动   2.编辑


    public void setEditType(int type) {
        editType = type;
        redraw();
    }


    private static final int ICON_WIDTH = 80;
    private static final int ICON_HEIGHT = 94;
    private static final float DEGREE_0 = 1.8f;
    private static final float DEGREE_1 = -2.0f;
    private static final float DEGREE_2 = 2.0f;
    private static final float DEGREE_3 = -1.5f;
    private static final float DEGREE_4 = 1.5f;
    private static final int ANIMATION_DURATION = 80;
    float mDensity;
    int mCount = 0;

    private void shakeAnimation(final View v) {
        float rotate = 0;
        int c = mCount++ % 5;
        if (c == 0) {
            rotate = DEGREE_0;
        } else if (c == 1) {
            rotate = DEGREE_1;
        } else if (c == 2) {
            rotate = DEGREE_2;
        } else if (c == 3) {
            rotate = DEGREE_3;
        } else {
            rotate = DEGREE_4;
        }
        final RotateAnimation mra = new RotateAnimation(rotate / 20, -rotate / 20, ICON_WIDTH * mDensity / 2, ICON_HEIGHT * mDensity / 2);
        final RotateAnimation mrb = new RotateAnimation(-rotate / 20, rotate / 20, ICON_WIDTH * mDensity / 2, ICON_HEIGHT * mDensity / 2);

        mra.setDuration(ANIMATION_DURATION);
        mrb.setDuration(ANIMATION_DURATION);

        mra.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                if (editType == 2) {
                    mra.reset();
                    v.startAnimation(mrb);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

            @Override
            public void onAnimationStart(Animation animation) {

            }

        });

        mrb.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                if (editType == 2) {
                    mrb.reset();
                    v.startAnimation(mra);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

            @Override
            public void onAnimationStart(Animation animation) {

            }

        });
        v.startAnimation(mra);
    }


//    public DragPictureImageView getDefaultView() {
//
//        if (views != null && views.size() > 0) {
//
//            return (DragPictureImageView) views.get(0).findViewById(R.id.iv_picture);
//        }
//        return null;
//
//    }
//
//
//    public List<DragPictureImageView> getDelViews() {
//
//        return delViews;
//
//    }

}
