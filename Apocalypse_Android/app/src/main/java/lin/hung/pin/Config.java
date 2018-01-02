package lin.hung.pin;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;

/**
 * Created by Administrator on 2017/11/6 0018.
 */
public class Config {
    private final static String SP_NAME = "config";
    private final static String BOOK_BG_KEY = "bookbg";
    private final static String FONT_TYPE_KEY = "fonttype";
    private final static String FONT_SIZE_KEY = "fontsize";
    private final static String NIGHT_KEY = "night";
    private final static String LIGHT_KEY = "light";
    private final static String SYSTEM_LIGHT_KEY = "systemlight";
    private final static String PAGE_MODE_KEY = "pagemode";

    public final static String FONTTYPE_DEFAULT = "";
    public final static String FONTTYPE_QIHEI = "font/msjhbd.ttf";  //FONTTYPE_QIHEI = "font/qihei.ttf"   FONTTYPE QIHEI羿創旗黑
    public final static String FONTTYPE_WAWA = "font/font1.ttf";

    public final static String FONTTYPE_FZXINGHEI = "font/fzxinghei.ttf";
    public final static String FONTTYPE_FZKATONG = "font/fzkatongT.ttf";  //FONTTYPE_FZKATONG = "font/fzkatong.ttf"   FONTTYPE FZKATONG方正卡通簡體
    public final static String FONTTYPE_BYSONG = "font/bkai00mp.ttf";  //FONTTYPE_BYSONG = "font/bysong.ttf"   FONTTYPE_BYSONG繁宋體

    public final static int BOOK_BG_DEFAULT = 0;
    public final static int BOOK_BG_1 = 1;
    public final static int BOOK_BG_2 = 2;
    public final static int BOOK_BG_3 = 3;
    public final static int BOOK_BG_4 = 4;

    public final static int PAGE_MODE_SIMULATION = 0;
    public final static int PAGE_MODE_COVER = 1;
    public final static int PAGE_MODE_SLIDE = 2;
    public final static int PAGE_MODE_NONE = 3;

    private Context mContext;
    private static Config config;
    private SharedPreferences sp;
    //字體
    private Typeface typeface;
    //字體大小
    private float mFontSize = 0;
    //亮度值
    private float light = 0;
    private int bookBG;

    private Config(Context mContext){
        this.mContext = mContext.getApplicationContext();
        sp = this.mContext.getSharedPreferences(SP_NAME,Context.MODE_PRIVATE);
    }

    public static synchronized Config getInstance(){
        return config;
    }

    public static synchronized Config createConfig(Context context){
        if (config == null){
            config = new Config(context);
        }

        return config;
    }

    public int getPageMode(){
        return sp.getInt(PAGE_MODE_KEY,PAGE_MODE_SIMULATION);
    }

    public void setPageMode(int pageMode){
        sp.edit().putInt(PAGE_MODE_KEY,pageMode).commit();
    }

    public int getBookBgType(){
        return sp.getInt(BOOK_BG_KEY,BOOK_BG_DEFAULT);
    }

    public void setBookBg(int type){
        sp.edit().putInt(BOOK_BG_KEY,type).commit();
    }

    public Typeface getTypeface(){
        if (typeface == null) {
            String typePath = sp.getString(FONT_TYPE_KEY,FONTTYPE_QIHEI);
            typeface = getTypeface(typePath);
        }
        return typeface;
    }

    public String getTypefacePath(){
        String path = sp.getString(FONT_TYPE_KEY,FONTTYPE_QIHEI);
        return path;
    }

    public Typeface getTypeface(String typeFacePath){
        Typeface mTypeface;
        if (typeFacePath.equals(FONTTYPE_DEFAULT)){
            mTypeface = Typeface.DEFAULT;
        }else{
            mTypeface = Typeface.createFromAsset(mContext.getAssets(),typeFacePath);
        }
        return mTypeface;
    }

    public void setTypeface(String typefacePath){
        typeface = getTypeface(typefacePath);
        sp.edit().putString(FONT_TYPE_KEY,typefacePath).commit();
    }

    public float getFontSize(){
        if (mFontSize == 0){
            mFontSize = sp.getFloat(FONT_SIZE_KEY, mContext.getResources().getDimension(R.dimen.reading_default_text_size));
        }
        return mFontSize;
    }

    public void setFontSize(float fontSize){
        mFontSize = fontSize;
        sp.edit().putFloat(FONT_SIZE_KEY,fontSize).commit();
    }

    /**
     * 獲取夜間還是白天閱讀模式,true為夜晚，false為白天
     */
    public boolean getDayOrNight() {
        return sp.getBoolean(NIGHT_KEY, false);
    }

    public void setDayOrNight(boolean isNight){
        sp.edit().putBoolean(NIGHT_KEY,isNight).commit();
    }

    public Boolean isSystemLight(){
        return sp.getBoolean(SYSTEM_LIGHT_KEY,true);
    }

    public void setSystemLight(Boolean isSystemLight){
        sp.edit().putBoolean(SYSTEM_LIGHT_KEY,isSystemLight).commit();
    }

    public float getLight(){
        if (light == 0){
            light = sp.getFloat(LIGHT_KEY,0.1f);
        }
        return light;
    }
    /**
     * 記錄配置文件中亮度值
     */
    public void setLight(float light) {
        this.light = light;
        sp.edit().putFloat(LIGHT_KEY,light).commit();
    }
}
