package lin.hung.pin.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import lin.hung.pin.util.CommonUtil;

import java.util.LinkedList;
import java.util.List;
import lin.hung.pin.R;


/**
 * Created by Lxq on 2017/11/6.
 */
public class DragGridView extends GridView implements View.OnClickListener{

    /**
     * DragGridView的item長按響應的時間， 默認是1000毫秒，也可以自行設置
     */
    private long dragResponseMS = 1000;

    /**
     * 是否可以拖拽，默認不可以
     */
    private boolean isDrag = false;

    private int mDownX;
    private int mDownY;
    private int moveX;
    private int moveY;
    /**
     * 正在拖拽的position
     */
    private int mDragPosition;

    /**
     * 剛開始拖拽的item對應的View
     */
    private View mStartDragItemView = null;

    /**
     * 用於拖拽的鏡像，這里直接用一個ImageView
     */
    private ImageView mDragImageView;

    /**
     * 震動器
     */
    private Vibrator mVibrator;

    private WindowManager mWindowManager;
    /**
     * item鏡像的佈局參數
     */
    private WindowManager.LayoutParams mWindowLayoutParams;

    /**
     * 我們拖拽的item對應的Bitmap
     */
    private Bitmap mDragBitmap;

    /**
     * 按下的點到所在item的上邊緣的距離
     */
    private int mPoint2ItemTop ;

    /**
     * 按下的點到所在item的左邊緣的距離
     */
    private int mPoint2ItemLeft;

    /**
     * DragGridView距離屏幕頂部的偏移量
     */
    private int mOffset2Top;

    /**
     * DragGridView距離屏幕左邊的偏移量
     */
    private int mOffset2Left;

    /**
     * 狀態欄的高度
     */
    private int mStatusHeight;

    /**
     * DragGridView自動向下滾動的邊界值
     */
    private int mDownScrollBorder;

    /**
     * DragGridView自動向上滾動的邊界值
     */
    private int mUpScrollBorder;
    /**
     * DragGridView自動滾動的速度
     */
    private static final int speed = 20;

    private boolean mAnimationEnd = true;

    private DragGridListener mDragAdapter;
    private int mNumColumns;
    private int mColumnWidth;
    private boolean mNumColumnsSet;
    private int mHorizontalSpacing;

    private Bitmap background;
    private Bitmap bookshelf_dock;
    private boolean touchable = true;
    private ImageButton mDeleteButton;
    private static boolean isShowDeleteButton = false;
    private static boolean isMove = false;
    private Context mcontext;
    private View firtView;
    private TextView firstItemTextView;
    private final int[] firstLocation = new int[2];
    private int i = 0;

    public DragGridView(Context context) {
        this(context, null);
    }

    public DragGridView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mStatusHeight = getStatusHeight(context);

        background = BitmapFactory.decodeResource(getResources(),
                R.mipmap.bookshelf_layer_center);
        bookshelf_dock = BitmapFactory.decodeResource(getResources(),R.mipmap.bookshelf_dock);
        if(!mNumColumnsSet){
            mNumColumns = AUTO_FIT;
        }
        mcontext = context;
    }

    private Handler mHandler = new Handler();

    //用來處理是否為長按的Runnable
    private Runnable mLongClickRunnable = new Runnable() {

        @Override
        public void run() {
            isDrag = true; //設置可以拖拽
            mVibrator.vibrate(50); //震動一下
            mStartDragItemView.setVisibility(View.INVISIBLE);//隱藏該item

            //根據我們按下的點顯示item鏡像
            createDragImage(mDragBitmap, mDownX, mDownY);

            setIsShowDeleteButton(true);
            for (int i = 0;i < getChildCount();i++) {
                final View mGridItemView = getChildAt(i);
                mDeleteButton = (ImageButton) mGridItemView.findViewById(R.id.ib_close);
                mDeleteButton.setOnClickListener(DragGridView.this);
                if(mDeleteButton.getVisibility()!=VISIBLE) {
                    //   mDeleteButton.setVisibility(VISIBLE);
                }

            }

        }
    };

    @Override
    public void setAdapter(ListAdapter adapter) {
        super.setAdapter(adapter);

        if(adapter instanceof DragGridListener){
            mDragAdapter = (DragGridListener) adapter;//回調方法的關鍵,拿到了該介面被實現後的實例
        }else{
            throw new IllegalStateException("the adapter must be implements DragGridListener");
        }
    }


    @Override
    public void setNumColumns(int numColumns) {
        super.setNumColumns(numColumns);
        mNumColumnsSet = true;
        this.mNumColumns = numColumns;
    }


    @Override
    public void setColumnWidth(int columnWidth) {
        super.setColumnWidth(columnWidth);
        mColumnWidth = columnWidth;
    }


    @Override
    public void setHorizontalSpacing(int horizontalSpacing) {
        super.setHorizontalSpacing(horizontalSpacing);
        this.mHorizontalSpacing = horizontalSpacing;
    }


    /**
     * 若設置為AUTO_FIT，計算有多少列
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mNumColumns == AUTO_FIT) {
            int numFittedColumns;
            if (mColumnWidth > 0) {
                int gridWidth = Math.max(MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft()
                        - getPaddingRight(), 0);
                numFittedColumns = gridWidth / mColumnWidth;
                if (numFittedColumns > 0) {
                    while (numFittedColumns != 1) {
                        if (numFittedColumns * mColumnWidth + (numFittedColumns - 1)
                                * mHorizontalSpacing > gridWidth) {
                            numFittedColumns--;
                        } else {
                            break;
                        }
                    }
                } else {
                    numFittedColumns = 1;
                }
            } else {
                numFittedColumns = 2;
            }
            mNumColumns = numFittedColumns;
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * 設置響應拖拽的毫秒數，默認是1000毫秒
     * @param dragResponseMS
     */
    public void setDragResponseMS(long dragResponseMS) {
        this.dragResponseMS = dragResponseMS;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch(ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                mDownX = (int) ev.getX();
                mDownY = (int) ev.getY();

                //根據按下的X,Y坐標獲取所點擊item的position
                mDragPosition = pointToPosition(mDownX, mDownY);
                // Log.d("mDagPosition is", "" + mDragPosition);

                if(mDragPosition == AdapterView.INVALID_POSITION){
                    return super.dispatchTouchEvent(ev);
                }

                //使用Handler延遲dragResponseMS執行mLongClickRunnable,
                // 大於20dp才執行mLongClickRunnable避免與Drawlayout發生沖突
                int panding = (int) CommonUtil.convertDpToPixel(mcontext,20);
                if(mDownX > panding) {
                    mHandler.postDelayed(mLongClickRunnable, dragResponseMS);
                }
                //根據position獲取該item所對應的View
                mStartDragItemView = getChildAt(mDragPosition - getFirstVisiblePosition());

                //
                mPoint2ItemTop = mDownY - mStartDragItemView.getTop();
                mPoint2ItemLeft = mDownX - mStartDragItemView.getLeft();

                mOffset2Top = (int) (ev.getRawY() - mDownY);
                mOffset2Left = (int) (ev.getRawX() - mDownX);

                //獲取DragGridView自動向上滾動的偏移量，小於這個值，DragGridView向下滾動
                mDownScrollBorder = getHeight() / 5;
                //獲取DragGridView自動向下滾動的偏移量，大於這個值，DragGridView向上滾動
                mUpScrollBorder = getHeight() * 4/5;



                //開啟mDragItemView繪圖緩存
                mStartDragItemView.setDrawingCacheEnabled(true);

                //獲取mDragItemView在緩存中的Bitmap對象
                mDragBitmap = Bitmap.createBitmap(mStartDragItemView.getDrawingCache());
                //這一步很關鍵，釋放繪圖緩存，避免出現重復的鏡像
                mStartDragItemView.destroyDrawingCache();


                break;
            case MotionEvent.ACTION_MOVE:
                int moveX = (int)ev.getX();
                int moveY = (int) ev.getY();

                //如果我們在按下的item上面移動，只要不超過item的邊界我們就不移除mRunnable
                if(!isTouchInItem(mStartDragItemView, moveX, moveY)){
                    mHandler.removeCallbacks(mLongClickRunnable);
                }
                break;
            case MotionEvent.ACTION_UP:
                mHandler.removeCallbacks(mLongClickRunnable);
                mHandler.removeCallbacks(mScrollRunnable);
                break;
        }
        return super.dispatchTouchEvent(ev);
    }


    /**
     * 是否點擊在GridView的item上面
     * @param dragView
     * @param x
     * @param y
     * @return
     */
    private boolean isTouchInItem(View dragView, int x, int y){
        if(dragView == null){
            return false;
        }
        int leftOffset = dragView.getLeft();
        int topOffset = dragView.getTop();
        if(x < leftOffset || x > leftOffset + dragView.getWidth()){
            return false;
        }

        if(y < topOffset || y > topOffset + dragView.getHeight()){
            return false;
        }

        return true;
    }



    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if(isDrag && mDragImageView != null){
            switch(ev.getAction()){
                case MotionEvent.ACTION_DOWN:
                    break;
                case MotionEvent.ACTION_MOVE:
                    moveX = (int) ev.getX();
                    moveY = (int) ev.getY();

                    //拖動item
                    onDragItem(moveX, moveY);
                    break;
                case MotionEvent.ACTION_UP:
                    onStopDrag();
                    isDrag = false;
                    break;
            }
            return true;
        }
        return super.onTouchEvent(ev);
    }

    /**
     * 創建拖動的鏡像
     * @param bitmap
     * @param downX
     * 			按下的點相對父控制項的X坐標
     * @param downY
     * 			按下的點相對父控制項的X坐標
     */
    private void createDragImage(Bitmap bitmap, int downX , int downY){
        mWindowLayoutParams = new WindowManager.LayoutParams();
        mWindowLayoutParams.format = PixelFormat.TRANSLUCENT; //圖片之外的其他地方透明
        mWindowLayoutParams.gravity = Gravity.TOP | Gravity.LEFT;
        mWindowLayoutParams.x = downX - mPoint2ItemLeft + mOffset2Left;
        mWindowLayoutParams.y = downY - mPoint2ItemTop + mOffset2Top - mStatusHeight;
        mWindowLayoutParams.alpha = 1.0f; //透明度
        // mWindowLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        //  mWindowLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mWindowLayoutParams.width = (int)(1.05*mStartDragItemView.getWidth());
        mWindowLayoutParams.height = (int)(1.05*mStartDragItemView.getHeight());
        mWindowLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE ;

        mDragImageView = new ImageView(getContext());
        mDragImageView.setImageBitmap(bitmap);
        mWindowManager.addView(mDragImageView, mWindowLayoutParams);
    }

    /**
     * 從界面上面移動拖動鏡像
     */
    private void removeDragImage(){
        if(mDragImageView != null){
            mWindowManager.removeView(mDragImageView);
            mDragImageView = null;
        }
    }

    /**
     * 拖動item，在裡面實現了item鏡像的位置更新，item的相互交換以及GridView的自行滾動
     * @param moveX
     * @param moveY
     */
    private void onDragItem(int moveX, int moveY){
        mWindowLayoutParams.x = moveX - mPoint2ItemLeft + mOffset2Left;
        mWindowLayoutParams.y = moveY - mPoint2ItemTop + mOffset2Top - mStatusHeight;
        mWindowManager.updateViewLayout(mDragImageView, mWindowLayoutParams); //更新鏡像的位置
        onSwapItem(moveX, moveY);

        //GridView自動滾動             //已知bug：當上下滾動過快時item的相互交換速度跟不上會crash
        mHandler.post(mScrollRunnable);
    }


    /**
     * 當moveY的值大於向上滾動的邊界值，觸發GridView自動向上滾動
     * 當moveY的值小於向下滾動的邊界值，觸發GridView自動向下滾動
     * 否則不進行滾動
     */
    private Runnable mScrollRunnable = new Runnable() {

        @Override
        public void run() {
            int scrollY;
            if(getFirstVisiblePosition() == 0 || getLastVisiblePosition() == getCount() - 1){
                mHandler.removeCallbacks(mScrollRunnable);
            }

            if(moveY > mUpScrollBorder){
                scrollY = speed;
                mHandler.postDelayed(mScrollRunnable, 25);
            }else if(moveY < mDownScrollBorder){
                scrollY = -speed;
                mHandler.postDelayed(mScrollRunnable, 25);
            }else{
                scrollY = 0;
                mHandler.removeCallbacks(mScrollRunnable);
            }

            smoothScrollBy(scrollY, 10);
        }
    };


    /**
     * 交換item,並且控制item之間的顯示與隱藏效果
     * @param moveX
     * @param moveY
     */
    private void onSwapItem(int moveX, int moveY){
        //獲取我們手指移動到的那個item的position
        final int tempPosition = pointToPosition(moveX, moveY);

        //假如tempPosition 改變了並且tempPosition不等於-1,則進行交換
        if(tempPosition != mDragPosition && tempPosition != AdapterView.INVALID_POSITION && mAnimationEnd ){

            mDragAdapter.setHideItem(tempPosition);

            mDragAdapter.reorderItems(mDragPosition, tempPosition);


            final ViewTreeObserver observer = getViewTreeObserver();
            observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {

                @Override
                public boolean onPreDraw() {
                    observer.removeOnPreDrawListener(this);
                    animateReorder(mDragPosition, tempPosition);
                    mDragPosition = tempPosition;  //交換結束更新mDragPosition
                    return true;
                }
            } );

        }

    }

    /**
     * 創建移動動畫
     * @param view
     * @param startX
     * @param endX
     * @param startY
     * @param endY
     * @return
     */
    private AnimatorSet createTranslationAnimations(View view, float startX,
                                                    float endX, float startY, float endY) {
        ObjectAnimator animX = ObjectAnimator.ofFloat(view, "translationX",
                startX, endX);
        ObjectAnimator animY = ObjectAnimator.ofFloat(view, "translationY",
                startY, endY);
        AnimatorSet animSetXY = new AnimatorSet();
        animSetXY.playTogether(animX, animY);
        return animSetXY;
    }


    /**
     * item的交換動畫效果
     * @param oldPosition
     * @param newPosition
     */
    private void animateReorder(final int oldPosition, final int newPosition) {
        boolean isForward = newPosition > oldPosition;
        List<Animator> resultList = new LinkedList<Animator>();
        if (isForward) {
            for (int pos = oldPosition; pos < newPosition; pos++) {
                View view = getChildAt(pos - getFirstVisiblePosition());
                // Log.d("oldPosition",""+ pos);

                //雙數
                if ((pos + 1) % mNumColumns == 0) {
                    resultList.add(createTranslationAnimations(view,
                            - view.getWidth() * (mNumColumns - 1), 0,
                            view.getHeight(), 0));
                } else {
                    resultList.add(createTranslationAnimations(view,
                            view.getWidth(), 0, 0, 0));
                }
            }
        } else {
            for (int pos = oldPosition; pos > newPosition; pos--) {
                View view = getChildAt(pos - getFirstVisiblePosition());
                if ((pos + mNumColumns) % mNumColumns == 0) {
                    resultList.add(createTranslationAnimations(view,
                            view.getWidth() * (mNumColumns - 1), 0,
                            -view.getHeight(), 0));
                } else {
                    resultList.add(createTranslationAnimations(view,
                            -view.getWidth(), 0, 0, 0));
                }
            }
        }

        AnimatorSet resultSet = new AnimatorSet();
        resultSet.playTogether(resultList);
        resultSet.setDuration(300);
        resultSet.setInterpolator(new AccelerateDecelerateInterpolator());
        resultSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mAnimationEnd = false;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mAnimationEnd = true;
            }
        });

        resultSet.start();

    }

    /**
     * 停止拖拽我們將之前隱藏的item顯示出來，並將鏡像移除
     */
    private void onStopDrag(){
        View view = getChildAt(mDragPosition - getFirstVisiblePosition());
        if(view != null){
            view.setVisibility(View.VISIBLE);
        }
        mDragAdapter.setHideItem(-1);
        removeDragImage();
    }

    /**
     * 獲得狀態欄的高度
     *
     * @param context
     * @return
     */
    public static int getStatusHeight(Context context)
    {
        int statusHeight = -1;
        try
        {
            Class<?> clazz = Class.forName("com.android.internal.R$dimen");
            Object object = clazz.newInstance();
            int height = Integer.parseInt(clazz.getField("status_bar_height")
                    .get(object).toString());
            statusHeight = context.getResources().getDimensionPixelSize(height);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return statusHeight;
    }


    @Override
    protected void dispatchDraw(Canvas canvas) {
        i++;
        int backgroundHeightPanding = (int) CommonUtil.convertDpToPixel(mcontext,4);
        int dockHightPanding = (int) CommonUtil.convertDpToPixel(mcontext,3);
        int count = getChildCount();
        int top = count > 0 ? getChildAt(0).getTop() : 0;
        int bottom = getChildAt(0).getBottom();
        int backgroundWidth = background.getWidth();
        int backgroundHeight = background.getHeight()-backgroundHeightPanding;
        int dockWith = bookshelf_dock.getWidth();
        int dockHight = bookshelf_dock.getHeight();
        int width = getWidth();
        int height = getHeight();

        for (int y = top; y < height; y += backgroundHeight) {
            for (int x = 0; x < width; x += backgroundWidth) {
                canvas.drawBitmap(background, x, y, null);
            }
            if(y > top) {
                canvas.drawBitmap(bookshelf_dock, 0 , y-dockHightPanding, null);
            }
        }
        if(i == 1) {
            firtView = getChildAt(0);
            firstItemTextView = (TextView) firtView.findViewById(R.id.tv_name);
            firstItemTextView.getLocationInWindow(firstLocation);
        }

        super.dispatchDraw(canvas);
    }

    @Override
    public void onClick(View v) {
        // Log.d("deleteImageButton","ok");
        mDragAdapter.removeItem(mDragPosition);
    }

    public static boolean getShowDeleteButton () {
        return isShowDeleteButton;
    }

    public static void setIsShowDeleteButton (boolean a) {
        isShowDeleteButton = a;
    }

    public void setTouchable(boolean isable) {
        this.touchable = isable;
    }

    public boolean getTouchable () {
        return touchable;
    }

    public static void setNoMove (boolean ismove) {
        isMove = ismove;
    }

    private ImageView getmDragImageView() {
        return mDragImageView;
    }

    public int[] getFirstLocation() {
        return firstLocation;
    }

}
