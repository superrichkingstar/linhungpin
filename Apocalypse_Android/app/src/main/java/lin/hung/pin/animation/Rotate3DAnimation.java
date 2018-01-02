package lin.hung.pin.animation;

import android.graphics.Camera;
import android.graphics.Matrix;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * Created by Administrator on 2017/11/6.
 */
public class Rotate3DAnimation extends Animation {
    private Camera mCamera;
    private final float mFromDegrees;
    private final float mToDegrees;
    private float mPivotXValue;// 控制項左上角X
    private float mPivotYValue;
    //private final float mDepthZ;   //不需要用到此參數
    private final float scaleTimes;
    private boolean mReverse;

    private float mPivotX;      //縮放點X
    private float mPivotY;      //縮放點Y


    /**
     * cover 動畫構造方法，一邊放大，一邊翻轉
     * @param mFromDegrees
     * @param mToDegrees
     * @param mPivotXValue  控制項左上角X
     * @param mPivotYValue  控制項左上角Y
     * @param scaleTimes    縮放比例
     * @param mReverse   動畫是否逆向進行
     */
    public Rotate3DAnimation(float mFromDegrees, float mToDegrees, float mPivotXValue, float mPivotYValue, float scaleTimes, boolean mReverse) {
        this.mFromDegrees = mFromDegrees;
        this.mToDegrees = mToDegrees;
        this.mPivotXValue = mPivotXValue;
        this.mPivotYValue = mPivotYValue;
        this.scaleTimes = scaleTimes;
        this.mReverse = mReverse;
    }

    @Override
    public void initialize(int width, int height, int parentWidth, int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
        mCamera = new Camera();
        mPivotX = resolvePivotX(mPivotXValue, parentWidth, width);  //計算縮放點X
        mPivotY = resolvePivoY(mPivotYValue, parentHeight, height);   //計算縮放點Y
    }

    /**
     * 執行順序 matrix.preTranslate() -->  camera.rotateY(degrees) -->   matrix.postTranslate() -->   matrix.postScale()
     * @param interpolatedTime
     * @param t
     */
    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        float degrees = mReverse ? mToDegrees + (mFromDegrees - mToDegrees) * interpolatedTime : mFromDegrees + (mToDegrees - mFromDegrees) * interpolatedTime;
        final Matrix matrix = t.getMatrix();

        final Camera camera = mCamera;

        camera.save();

        camera.rotateY(degrees);

        camera.getMatrix(matrix);
        camera.restore();

        //  matrix.preTranslate(-mPivotXValue, 0);      //在進行rotateY之前需要移動物體，讓物體左邊與Y軸對齊
        //  matrix.postTranslate(mPivotXValue, 0);      //還原物體位置

        if (mReverse) {
            matrix.postScale(1 + (scaleTimes - 1) * (1.0f - interpolatedTime), 1 + (scaleTimes - 1) * (1.0f - interpolatedTime), mPivotX - mPivotXValue , mPivotY - mPivotYValue);
        } else {
            // matrix.postScale(1 + (scaleTimes - 1) * interpolatedTime, 1 + (scaleTimes - 1) * interpolatedTime, mPivotX, mPivotY);
            matrix.postScale(1 + (scaleTimes - 1) * interpolatedTime, 1 + (scaleTimes - 1) * interpolatedTime, mPivotX - mPivotXValue , mPivotY - mPivotYValue );
        }
    }

    private float resolvePivotX(float margingLeft, int parentWidth, int width) {
        return (margingLeft * parentWidth) / (parentWidth - width);
    }

    private float resolvePivoY(float marginTop, int parentHeight, int height) {
        return (marginTop * parentHeight) / (parentHeight - height);
    }

    public void reverse() {
        mReverse = !mReverse;
    }

    public boolean getMReverse() {
        return mReverse;
    }

    public void setmPivotXValue (float mPivotXValue1) {
        this.mPivotXValue = mPivotXValue1;
    }

    public void setmPivotYValue (float mPivotYValue1) {
        this.mPivotYValue = mPivotYValue1;
    }
}
