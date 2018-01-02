package lin.hung.pin.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.util.AttributeSet;
import android.widget.ImageView;
import lin.hung.pin.R;


/**
 * 流程式控制制的比較嚴謹，比如setup函數的使用
 * updateShaderMatrix保證圖片損失度最小和始終繪制圖片正中央的那部分
 * 作者思路是畫圓用渲染器位圖填充，而不是把Bitmap重繪切割成一個圓形圖片。
 */
public class CircleImageView extends ImageView {
    //縮放類型
    private static final ScaleType SCALE_TYPE = ScaleType.CENTER_CROP;
    private static final Bitmap.Config BITMAP_CONFIG = Bitmap.Config.ARGB_8888;
    private static final int COLORDRAWABLE_DIMENSION = 2;
    // 默認邊界寬度
    private static final int DEFAULT_BORDER_WIDTH = 0;
    // 默認邊界顏色
    private static final int DEFAULT_BORDER_COLOR = Color.BLACK;
    private static final boolean DEFAULT_BORDER_OVERLAY = false;

    private final RectF mDrawableRect = new RectF();
    private final RectF mBorderRect = new RectF();

    private final Matrix mShaderMatrix = new Matrix();
    //這個畫筆最重要的是關聯了mBitmapShader 使canvas在執行的時候可以切割原圖片(mBitmapShader是關聯了原圖的bitmap的)
    private final Paint mBitmapPaint = new Paint();
    //這個描邊，則與本身的原圖bitmap沒有任何關聯，
    private final Paint mBorderPaint = new Paint();
    //這里定義了 圓形邊緣的默認寬度和顏色
    private int mBorderColor = DEFAULT_BORDER_COLOR;
    private int mBorderWidth = DEFAULT_BORDER_WIDTH;

    private Bitmap mBitmap;
    private BitmapShader mBitmapShader; // 位圖渲染
    private int mBitmapWidth;   // 位圖寬度
    private int mBitmapHeight;  // 位圖高度

    private float mDrawableRadius;// 圖片半徑
    private float mBorderRadius;// 帶邊框的的圖片半徑

    private ColorFilter mColorFilter;
    //初始false
    private boolean mReady;
    private boolean mSetupPending;
    private boolean mBorderOverlay;
    //構造函數
    public CircleImageView(Context context) {
        super(context);
        init();
    }
    //構造函數
    public CircleImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    /**
     * 構造函數
     */
    public CircleImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        //通過obtainStyledAttributes 獲得一組值賦給 TypedArray（數組） , 這一組值來自於res/values/attrs.xml中的name="CircleImageView"的declare-styleable中。
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CircleImageView, defStyle, 0);
        //通過TypedArray提供的一系列方法getXXXX取得我們在xml里定義的參數值；
        // 獲取邊界的寬度
        mBorderWidth = a.getDimensionPixelSize(R.styleable.CircleImageView_border_width, DEFAULT_BORDER_WIDTH);
        // 獲取邊界的顏色
        mBorderColor = a.getColor(R.styleable.CircleImageView_border_color, DEFAULT_BORDER_COLOR);
        mBorderOverlay = DEFAULT_BORDER_OVERLAY;
        //調用 recycle() 回收TypedArray,以便後面重用
        a.recycle();
        System.out.println("CircleImageView -- 構造函數");
        init();
    }
    /**
     * 作用就是保證第一次執行setup函數里下麵代碼要在構造函數執行完畢時調用
     */
    private void init() {
        //在這里ScaleType被強制設定為CENTER_CROP，就是將圖片水平垂直居中，進行縮放。
        super.setScaleType(SCALE_TYPE);
        mReady = true;

        if (mSetupPending) {
            setup();
            mSetupPending = false;
        }
    }

    @Override
    public ScaleType getScaleType() {
        return SCALE_TYPE;
    }
    /**
     * 這里明確指出 此種imageview 只支持CENTER_CROP 這一種屬性
     *
     * @param scaleType
     */
    @Override
    public void setScaleType(ScaleType scaleType) {
        if (scaleType != SCALE_TYPE) {
            throw new IllegalArgumentException(String.format("ScaleType %s not supported.", scaleType));
        }
    }

    @Override
    public void setAdjustViewBounds(boolean adjustViewBounds) {
        if (adjustViewBounds) {
            throw new IllegalArgumentException("adjustViewBounds not supported.");
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //如果圖片不存在就不畫
        if (getDrawable() == null) {
            return;
        }
        //繪制內圓形 圖片 畫筆為mBitmapPaint
        canvas.drawCircle(getWidth() / 2, getHeight() / 2, mDrawableRadius, mBitmapPaint);
        //如果圓形邊緣的寬度不為0 我們還要繪制帶邊界的外圓形 邊界畫筆為mBorderPaint
        if (mBorderWidth != 0) {
            canvas.drawCircle(getWidth() / 2, getHeight() / 2, mBorderRadius, mBorderPaint);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        setup();
    }

    public int getBorderColor() {
        return mBorderColor;
    }

    public void setBorderColor(int borderColor) {
        if (borderColor == mBorderColor) {
            return;
        }

        mBorderColor = borderColor;
        mBorderPaint.setColor(mBorderColor);
        invalidate();
    }

    public void setBorderColorResource(@ColorRes int borderColorRes) {
        setBorderColor(getContext().getResources().getColor(borderColorRes));
    }

    public int getBorderWidth() {
        return mBorderWidth;
    }

    public void setBorderWidth(int borderWidth) {
        if (borderWidth == mBorderWidth) {
            return;
        }

        mBorderWidth = borderWidth;
        setup();
    }

    public boolean isBorderOverlay() {
        return mBorderOverlay;
    }

    public void setBorderOverlay(boolean borderOverlay) {
        if (borderOverlay == mBorderOverlay) {
            return;
        }

        mBorderOverlay = borderOverlay;
        setup();
    }

    /**
     * 以下四個函數都是
     * 復寫ImageView的setImageXxx()方法
     * 註意這個函數先於構造函數調用之前調用
     * @param bm
     */
    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        mBitmap = bm;
        setup();
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        mBitmap = getBitmapFromDrawable(drawable);
        System.out.println("setImageDrawable -- setup");
        setup();
    }

    @Override
    public void setImageResource(@DrawableRes int resId) {
        super.setImageResource(resId);
        mBitmap = getBitmapFromDrawable(getDrawable());
        setup();
    }

    @Override
    public void setImageURI(Uri uri) {
        super.setImageURI(uri);
        mBitmap = getBitmapFromDrawable(getDrawable());
        setup();
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        if (cf == mColorFilter) {
            return;
        }

        mColorFilter = cf;
        mBitmapPaint.setColorFilter(mColorFilter);
        invalidate();
    }
    /**
     * Drawable轉Bitmap
     * @param drawable
     * @return
     */
    private Bitmap getBitmapFromDrawable(Drawable drawable) {
        if (drawable == null) {
            return null;
        }

        if (drawable instanceof BitmapDrawable) {
            //通常來說 我們的代碼就是執行到這里就返回了。返回的就是我們最原始的bitmap
            return ((BitmapDrawable) drawable).getBitmap();
        }

        try {
            Bitmap bitmap;

            if (drawable instanceof ColorDrawable) {
                bitmap = Bitmap.createBitmap(COLORDRAWABLE_DIMENSION, COLORDRAWABLE_DIMENSION, BITMAP_CONFIG);
            } else {
                bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), BITMAP_CONFIG);
            }

            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        } catch (OutOfMemoryError e) {
            return null;
        }
    }
    /**
     * 這個函數很關鍵，進行圖片畫筆邊界畫筆(Paint)一些重繪參數初始化：
     * 構建渲染器BitmapShader用Bitmap來填充繪制區域,設置樣式以及內外圓半徑計算等，
     * 以及調用updateShaderMatrix()函數和 invalidate()函數；
     */
    private void setup() {
        //因為mReady默認值為false,所以第一次進這個函數的時候if語句為真進入括號體內
        //設置mSetupPending為true然後直接返回，後面的代碼並沒有執行。
        if (!mReady) {
            mSetupPending = true;
            return;
        }
        //防止空指針異常
        if (mBitmap == null) {
            return;
        }
        // 構建渲染器，用mBitmap位圖來填充繪制區域 ，參數值代表如果圖片太小的話 就直接拉伸
        mBitmapShader = new BitmapShader(mBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        // 設置圖片畫筆反鋸齒
        mBitmapPaint.setAntiAlias(true);
        // 設置圖片畫筆渲染器
        mBitmapPaint.setShader(mBitmapShader);
        // 設置邊界畫筆樣式
        mBorderPaint.setStyle(Paint.Style.STROKE);//設畫筆為空心
        mBorderPaint.setAntiAlias(true);
        mBorderPaint.setColor(mBorderColor);    //畫筆顏色
        mBorderPaint.setStrokeWidth(mBorderWidth);//畫筆邊界寬度
        //這個地方是取的原圖片的寬高
        mBitmapHeight = mBitmap.getHeight();
        mBitmapWidth = mBitmap.getWidth();
        // 設置含邊界顯示區域，取的是CircleImageView的佈局實際大小，為方形，查看xml也就是160dp(240px)  getWidth得到是某個view的實際尺寸
        mBorderRect.set(0, 0, getWidth(), getHeight());
        //計算 圓形帶邊界部分（外圓）的最小半徑，取mBorderRect的寬高減去一個邊緣大小的一半的較小值（這個地方我比較納悶為什麼求外圓半徑需要先減去一個邊緣大小）
        mBorderRadius = Math.min((mBorderRect.height() - mBorderWidth) / 2, (mBorderRect.width() - mBorderWidth) / 2);
        // 初始圖片顯示區域為mBorderRect（CircleImageView的佈局實際大小）
        mDrawableRect.set(mBorderRect);
        if (!mBorderOverlay) {
            //demo里始終執行
            //通過inset方法  使得圖片顯示的區域從mBorderRect大小上下左右內移邊界的寬度形成區域，查看xml邊界寬度為2dp（3px）,所以方形邊長為就是160-4=156dp(234px)
            mDrawableRect.inset(mBorderWidth, mBorderWidth);
        }
        //這里計算的是內圓的最小半徑，也即去除邊界寬度的半徑
        mDrawableRadius = Math.min(mDrawableRect.height() / 2, mDrawableRect.width() / 2);
        //設置渲染器的變換矩陣也即是mBitmap用何種縮放形式填充
        updateShaderMatrix();
        //手動觸發ondraw()函數 完成最終的繪制
        invalidate();
    }
    /**
     * 這個函數為設置BitmapShader的Matrix參數，設置最小縮放比例，平移參數。
     * 作用：保證圖片損失度最小和始終繪制圖片正中央的那部分
     */
    private void updateShaderMatrix() {
        float scale;
        float dx = 0;
        float dy = 0;

        mShaderMatrix.set(null);
        // 這里不好理解 這個不等式也就是(mBitmapWidth / mDrawableRect.width()) > (mBitmapHeight / mDrawableRect.height())
        //取最小的縮放比例
        if (mBitmapWidth * mDrawableRect.height() > mDrawableRect.width() * mBitmapHeight) {
            //y軸縮放 x軸平移 使得圖片的y軸方向的邊的尺寸縮放到圖片顯示區域（mDrawableRect）一樣）
            scale = mDrawableRect.height() / (float) mBitmapHeight;
            dx = (mDrawableRect.width() - mBitmapWidth * scale) * 0.5f;
        } else {
            //x軸縮放 y軸平移 使得圖片的x軸方向的邊的尺寸縮放到圖片顯示區域（mDrawableRect）一樣）
            scale = mDrawableRect.width() / (float) mBitmapWidth;
            dy = (mDrawableRect.height() - mBitmapHeight * scale) * 0.5f;
        }
        // shaeder的變換矩陣，我們這里主要用於放大或者縮小。
        mShaderMatrix.setScale(scale, scale);
        // 平移
        mShaderMatrix.postTranslate((int) (dx + 0.5f) + mDrawableRect.left, (int) (dy + 0.5f) + mDrawableRect.top);
        // 設置變換矩陣
        mBitmapShader.setLocalMatrix(mShaderMatrix);
    }
}
