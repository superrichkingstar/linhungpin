package lin.hung.pin;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.SQLException;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.tts.auth.AuthInfo;
import com.baidu.tts.client.SpeechError;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;
import lin.hung.pin.base.BaseActivity;
import lin.hung.pin.db.BookList;
import lin.hung.pin.db.BookMarks;
import lin.hung.pin.dialog.PageModeDialog;
import lin.hung.pin.dialog.SettingDialog;
import lin.hung.pin.util.BrightnessUtil;
import lin.hung.pin.util.PageFactory;
import lin.hung.pin.view.PageWidget;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by Administrator on 2017/11/6 0015.
 */
public class ReadActivity extends BaseActivity implements SpeechSynthesizerListener {
    private static final String TAG = "ReadActivity";
    private final static String EXTRA_BOOK = "bookList";
    private final static int MESSAGE_CHANGEPROGRESS = 1;

    @Bind(R.id.bookpage)
    PageWidget bookpage;
    //    @Bind(R.id.btn_return)
//    ImageButton btn_return;
//    @Bind(R.id.ll_top)
//    LinearLayout ll_top;
    @Bind(R.id.tv_progress)
    TextView tv_progress;
    @Bind(R.id.rl_progress)
    RelativeLayout rl_progress;
    @Bind(R.id.tv_pre)
    TextView tv_pre;
    @Bind(R.id.sb_progress)
    SeekBar sb_progress;
    @Bind(R.id.tv_next)
    TextView tv_next;
    @Bind(R.id.tv_directory)
    TextView tv_directory;
    @Bind(R.id.tv_dayornight)
    TextView tv_dayornight;
    @Bind(R.id.tv_pagemode)
    TextView tv_pagemode;
    @Bind(R.id.tv_setting)
    TextView tv_setting;
    @Bind(R.id.bookpop_bottom)
    LinearLayout bookpop_bottom;
    @Bind(R.id.rl_bottom)
    RelativeLayout rl_bottom;
    @Bind(R.id.tv_stop_read)
    TextView tv_stop_read;
    @Bind(R.id.rl_read_bottom)
    RelativeLayout rl_read_bottom;
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.appbar)
    AppBarLayout appbar;

    private Config config;
    private WindowManager.LayoutParams lp;
    private BookList bookList;
    private PageFactory pageFactory;
    private int screenWidth, screenHeight;
    // popwindow是否顯示
    private Boolean isShow = false;
    private SettingDialog mSettingDialog;
    private PageModeDialog mPageModeDialog;
    private Boolean mDayOrNight;
    // 語音合成客戶端
    private SpeechSynthesizer mSpeechSynthesizer;
    private boolean isSpeaking = false;

    // 接收電池信息更新的廣播
    private BroadcastReceiver myReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
                Log.e(TAG,Intent.ACTION_BATTERY_CHANGED);
                int level = intent.getIntExtra("level", 0);
                pageFactory.updateBattery(level);
            }else if (intent.getAction().equals(Intent.ACTION_TIME_TICK)){
                Log.e(TAG,Intent.ACTION_TIME_TICK);
                pageFactory.updateTime();
            }
        }
    };

    @Override
    public int getLayoutRes() {
        return R.layout.activity_read;
    }

    @Override
    protected void initData() {
        if(Build.VERSION.SDK_INT >= 14 && Build.VERSION.SDK_INT < 19){
            bookpage.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.mipmap.return_button);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        config = Config.getInstance();
        pageFactory = PageFactory.getInstance();

        IntentFilter mfilter = new IntentFilter();
        mfilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        mfilter.addAction(Intent.ACTION_TIME_TICK);
        registerReceiver(myReceiver, mfilter);

        mSettingDialog = new SettingDialog(this);
        mPageModeDialog = new PageModeDialog(this);
        //獲取屏幕寬高
        WindowManager manage = getWindowManager();
        Display display = manage.getDefaultDisplay();
        Point displaysize = new Point();
        display.getSize(displaysize);
        screenWidth = displaysize.x;
        screenHeight = displaysize.y;
        //保持屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //隱藏
        hideSystemUI();
        //改變屏幕亮度
        if (!config.isSystemLight()) {
            BrightnessUtil.setBrightness(this, config.getLight());
        }
        //獲取intent中的攜帶的信息
        Intent intent = getIntent();
        bookList = (BookList) intent.getSerializableExtra(EXTRA_BOOK);

        bookpage.setPageMode(config.getPageMode());
        pageFactory.setPageWidget(bookpage);

        try {
            pageFactory.openBook(bookList);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "打開電子書失敗", Toast.LENGTH_SHORT).show();
        }

        initDayOrNight();

//        new Thread(){
//            @Override
//            public void run() {
//                super.run();
//                initialTts();
//            }
//        }.start();
//        initialTts();
    }

    @Override
    protected void initListener() {
        sb_progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            float pro;
            // 觸發操作，拖動
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                pro = (float) (progress / 10000.0);
                showProgress(pro);
            }

            // 表示進度條剛開始拖動，開始拖動時候觸發的操作
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            // 停止拖動時候
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                pageFactory.changeProgress(pro);
            }
        });

        mPageModeDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                hideSystemUI();
            }
        });

        mPageModeDialog.setPageModeListener(new PageModeDialog.PageModeListener() {
            @Override
            public void changePageMode(int pageMode) {
                bookpage.setPageMode(pageMode);
            }
        });

        mSettingDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                hideSystemUI();
            }
        });

        mSettingDialog.setSettingListener(new SettingDialog.SettingListener() {
            @Override
            public void changeSystemBright(Boolean isSystem, float brightness) {
                if (!isSystem) {
                    BrightnessUtil.setBrightness(ReadActivity.this, brightness);
                } else {
                    int bh = BrightnessUtil.getScreenBrightness(ReadActivity.this);
                    BrightnessUtil.setBrightness(ReadActivity.this, bh);
                }
            }

            @Override
            public void changeFontSize(int fontSize) {
                pageFactory.changeFontSize(fontSize);
            }

            @Override
            public void changeTypeFace(Typeface typeface) {
                pageFactory.changeTypeface(typeface);
            }

            @Override
            public void changeBookBg(int type) {
                pageFactory.changeBookBg(type);
            }
        });

        pageFactory.setPageEvent(new PageFactory.PageEvent() {
            @Override
            public void changeProgress(float progress) {
                Message message = new Message();
                message.what = MESSAGE_CHANGEPROGRESS;
                message.obj = progress;
                mHandler.sendMessage(message);
            }
        });

        bookpage.setTouchListener(new PageWidget.TouchListener() {
            @Override
            public void center() {
                if (isShow) {
                    hideReadSetting();
                } else {
                    showReadSetting();
                }
            }

            @Override
            public Boolean prePage() {
                if (isShow || isSpeaking){
                    return false;
                }

                pageFactory.prePage();
                if (pageFactory.isfirstPage()) {
                    return false;
                }

                return true;
            }

            @Override
            public Boolean nextPage() {
                Log.e("setTouchListener", "nextPage");
                if (isShow || isSpeaking){
                    return false;
                }

                pageFactory.nextPage();
                if (pageFactory.islastPage()) {
                    return false;
                }
                return true;
            }

            @Override
            public void cancel() {
                pageFactory.cancelPage();
            }
        });

    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MESSAGE_CHANGEPROGRESS:
                    float progress = (float) msg.obj;
                    setSeekBarProgress(progress);
                    break;
            }
        }
    };


    @Override
    protected void onResume(){
        super.onResume();
        if (!isShow){
            hideSystemUI();
        }
        if (mSpeechSynthesizer != null){
            mSpeechSynthesizer.resume();
        }
    }

    @Override
    protected void onStop(){
        super.onStop();
        if (mSpeechSynthesizer != null){
            mSpeechSynthesizer.stop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pageFactory.clear();
        bookpage = null;
        unregisterReceiver(myReceiver);
        isSpeaking = false;
        if (mSpeechSynthesizer != null){
            mSpeechSynthesizer.release();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (isShow){
                hideReadSetting();
                return true;
            }
            if (mSettingDialog.isShowing()){
                mSettingDialog.hide();
                return true;
            }
            if (mPageModeDialog.isShowing()){
                mPageModeDialog.hide();
                return true;
            }
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.read, menu);
        menu.getItem(1).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_add_bookmark){
            if (pageFactory.getCurrentPage() != null) {
                List<BookMarks> bookMarksList = DataSupport.where("bookpath = ? and begin = ?", pageFactory.getBookPath(),pageFactory.getCurrentPage().getBegin() + "").find(BookMarks.class);

                if (!bookMarksList.isEmpty()){
                    Toast.makeText(ReadActivity.this, "該書籤已存在", Toast.LENGTH_SHORT).show();
                }else {
                    BookMarks bookMarks = new BookMarks();
                    String word = "";
                    for (String line : pageFactory.getCurrentPage().getLines()) {
                        word += line;
                    }
                    try {
                        SimpleDateFormat sf = new SimpleDateFormat(
                                "yyyy-MM-dd HH:mm ss");
                        String time = sf.format(new Date());
                        bookMarks.setTime(time);
                        bookMarks.setBegin(pageFactory.getCurrentPage().getBegin());
                        bookMarks.setText(word);
                        bookMarks.setBookpath(pageFactory.getBookPath());
                        bookMarks.save();

                        Toast.makeText(ReadActivity.this, "書籤添加成功", Toast.LENGTH_SHORT).show();
                    } catch (SQLException e) {
                        Toast.makeText(ReadActivity.this, "該書籤已存在", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(ReadActivity.this, "添加書籤失敗", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }else if (id == R.id.action_read_book){
            initialTts();
            if (mSpeechSynthesizer != null){
                mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_VOLUME, "5");
                mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEED, "5");
                mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_PITCH, "5");
                mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEAKER, "0");
//                mSpeechSynthesizer.setParam(SpeechSynthesizer. MIX_MODE_DEFAULT);
//                mSpeechSynthesizer.setParam(SpeechSynthesizer. AUDIO_ENCODE_AMR);
//                mSpeechSynthesizer.setParam(SpeechSynthesizer. AUDIO_BITRA TE_AMR_15K85);
                mSpeechSynthesizer.setParam(SpeechSynthesizer. PARAM_VOCODER_OPTIM_LEVEL, "0");
                int result = mSpeechSynthesizer.speak(pageFactory.getCurrentPage().getLineToString());
                if (result < 0) {
                    Log.e(TAG,"error,please look up error code in doc or URL:http://yuyin.baidu.com/docs/tts/122 ");
                }else{
                    hideReadSetting();
                    isSpeaking = true;
                }
            }
        }

        return super.onOptionsItemSelected(item);
    }


    public static boolean openBook(final BookList bookList, Activity context) {
        if (bookList == null){
            throw new NullPointerException("BookList can not be null");
        }

        Intent intent = new Intent(context, ReadActivity.class);
        intent.putExtra(EXTRA_BOOK, bookList);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
        context.startActivity(intent);
        return true;
    }

//    public BookPageWidget getPageWidget() {
//        return bookpage;
//    }

    /**
     * 隱藏菜單。沉浸式閱讀
     */
    private void hideSystemUI() {
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        //  | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
    }

    private void showSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
    }

    //顯示書本進度
    public void showProgress(float progress){
        if (rl_progress.getVisibility() != View.VISIBLE) {
            rl_progress.setVisibility(View.VISIBLE);
        }
        setProgress(progress);
    }

    //隱藏書本進度
    public void hideProgress(){
        rl_progress.setVisibility(View.GONE);
    }

    public void initDayOrNight(){
        mDayOrNight = config.getDayOrNight();
        if (mDayOrNight){
            tv_dayornight.setText(getResources().getString(R.string.read_setting_day));
        }else{
            tv_dayornight.setText(getResources().getString(R.string.read_setting_night));
        }
    }

    //改變顯示模式
    public void changeDayOrNight(){
        if (mDayOrNight){
            mDayOrNight = false;
            tv_dayornight.setText(getResources().getString(R.string.read_setting_night));
        }else{
            mDayOrNight = true;
            tv_dayornight.setText(getResources().getString(R.string.read_setting_day));
        }
        config.setDayOrNight(mDayOrNight);
        pageFactory.setDayOrNight(mDayOrNight);
    }

    private void setProgress(float progress){
        DecimalFormat decimalFormat=new DecimalFormat("00.00");//構造方法的字元格式這里如果小數不足2位,會以0補足.
        String p=decimalFormat.format(progress * 100.0);//format 返回的是字元串
        tv_progress.setText(p + "%");
    }

    public void setSeekBarProgress(float progress){
        sb_progress.setProgress((int) (progress * 10000));
    }

    private void showReadSetting(){
        isShow = true;
        rl_progress.setVisibility(View.GONE);

        if (isSpeaking){
            Animation topAnim = AnimationUtils.loadAnimation(this, R.anim.dialog_top_enter);
            rl_read_bottom.startAnimation(topAnim);
            rl_read_bottom.setVisibility(View.VISIBLE);
        }else {
            showSystemUI();

            Animation bottomAnim = AnimationUtils.loadAnimation(this, R.anim.dialog_enter);
            Animation topAnim = AnimationUtils.loadAnimation(this, R.anim.dialog_top_enter);
            rl_bottom.startAnimation(topAnim);
            appbar.startAnimation(topAnim);
//        ll_top.startAnimation(topAnim);
            rl_bottom.setVisibility(View.VISIBLE);
//        ll_top.setVisibility(View.VISIBLE);
            appbar.setVisibility(View.VISIBLE);
        }
    }

    private void hideReadSetting() {
        isShow = false;
        Animation bottomAnim = AnimationUtils.loadAnimation(this, R.anim.dialog_exit);
        Animation topAnim = AnimationUtils.loadAnimation(this, R.anim.dialog_top_exit);
        if (rl_bottom.getVisibility() == View.VISIBLE) {
            rl_bottom.startAnimation(topAnim);
        }
        if (appbar.getVisibility() == View.VISIBLE) {
            appbar.startAnimation(topAnim);
        }
        if (rl_read_bottom.getVisibility() == View.VISIBLE) {
            rl_read_bottom.startAnimation(topAnim);
        }
//        ll_top.startAnimation(topAnim);
        rl_bottom.setVisibility(View.GONE);
        rl_read_bottom.setVisibility(View.GONE);
//        ll_top.setVisibility(View.GONE);
        appbar.setVisibility(View.GONE);
        hideSystemUI();
    }

    private void initialTts() {
        this.mSpeechSynthesizer = SpeechSynthesizer.getInstance();
        this.mSpeechSynthesizer.setContext(this);
        this.mSpeechSynthesizer.setSpeechSynthesizerListener(this);
        // 文本模型文件路徑 (離線引擎使用)
        this.mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE, ((AppContext)getApplication()).getTTPath() + "/"
                + AppContext.TEXT_MODEL_NAME);
        // 聲學模型文件路徑 (離線引擎使用)
        this.mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE, ((AppContext)getApplication()).getTTPath() + "/"
                + AppContext.SPEECH_FEMALE_MODEL_NAME);
        // 本地授權文件路徑,如未設置將使用默認路徑.設置臨時授權文件路徑，LICENCE_FILE_NAME請替換成臨時授權文件的實際路徑，僅在使用臨時license文件時需要進行設置，如果在[應用管理]中開通了正式離線授權，不需要設置該參數，建議將該行代碼刪除（離線引擎）
        // 如果合成結果出現臨時授權文件將要到期的提示，說明使用了臨時授權文件，請刪除臨時授權即可。
//        this.mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_LICENCE_FILE, ((AppContext)getApplication()).getTTPath() + "/"
//                + AppContext.LICENSE_FILE_NAME);
        // 請替換為語音開發者平臺上註冊應用得到的App ID (離線授權)
        this.mSpeechSynthesizer.setAppId("8921835"/*這里只是為了讓Demo運行使用的APPID,請替換成自己的id。*/);
        // 請替換為語音開發者平臺註冊應用得到的apikey和secretkey (在線授權)
        this.mSpeechSynthesizer.setApiKey("sjEFlROl4j090FtDTHlEpvFB",
                "a2d95dc24960e03ef2d41a5fb1a2c025"/*這里只是為了讓Demo正常運行使用APIKey,請替換成自己的APIKey*/);
        // 發音人（在線引擎），可用參數為0,1,2,3。。。（服務器端會動態增加，各值含義參考文檔，以文檔說明為準。0--普通女聲，1--普通男聲，2--特別男聲，3--情感男聲。。。）
        this.mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEAKER, "0");
        // 設置Mix模式的合成策略
        this.mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_MIX_MODE, SpeechSynthesizer.MIX_MODE_DEFAULT);
        // 授權檢測介面(只是通過AuthInfo進行檢驗授權是否成功。)
        // AuthInfo介面用於測試開發者是否成功申請了在線或者離線授權，如果測試授權成功了，可以刪除AuthInfo部分的代碼（該介面首次驗證時比較耗時），不會影響正常使用（合成使用時SDK內部會自動驗證授權）
        AuthInfo authInfo = this.mSpeechSynthesizer.auth(TtsMode.MIX);

        if (authInfo.isSuccess()) {
            Log.e(TAG,"auth success");
        } else {
            String errorMsg = authInfo.getTtsError().getDetailMessage();
            Log.e(TAG,"auth failed errorMsg=" + errorMsg);
        }

        // 初始化tts
        mSpeechSynthesizer.initTts(TtsMode.MIX);
        // 加載離線英文資源（提供離線英文合成功能）
        int result = mSpeechSynthesizer.loadEnglishModel(((AppContext)getApplication()).getTTPath() + "/" + AppContext.ENGLISH_TEXT_MODEL_NAME, ((AppContext)getApplication()).getTTPath()
                + "/" + AppContext.ENGLISH_SPEECH_FEMALE_MODEL_NAME);
//        toPrint("loadEnglishModel result=" + result);
//
//        //列印引擎信息和model基本信息
//        printEngineInfo();
    }

    @OnClick({R.id.tv_progress, R.id.rl_progress, R.id.tv_pre, R.id.sb_progress, R.id.tv_next, R.id.tv_directory, R.id.tv_dayornight,R.id.tv_pagemode, R.id.tv_setting, R.id.bookpop_bottom, R.id.rl_bottom,R.id.tv_stop_read})
    public void onClick(View view) {
        switch (view.getId()) {
//            case R.id.btn_return:
//                finish();
//                break;
//            case R.id.ll_top:
//                break;
            case R.id.tv_progress:
                break;
            case R.id.rl_progress:
                break;
            case R.id.tv_pre:
                pageFactory.preChapter();
                break;
            case R.id.sb_progress:
                break;
            case R.id.tv_next:
                pageFactory.nextChapter();
                break;
            case R.id.tv_directory:
                Intent intent = new Intent(ReadActivity.this, MarkActivity.class);
                startActivity(intent);
                break;
            case R.id.tv_dayornight:
                changeDayOrNight();
                break;
            case R.id.tv_pagemode:
                hideReadSetting();
                mPageModeDialog.show();
                break;
            case R.id.tv_setting:
                hideReadSetting();
                mSettingDialog.show();
                break;
            case R.id.bookpop_bottom:
                break;
            case R.id.rl_bottom:
                break;
            case R.id.tv_stop_read:
                if (mSpeechSynthesizer!=null){
                    mSpeechSynthesizer.stop();
                    isSpeaking = false;
                    hideReadSetting();
                }
                break;
        }
    }

    /*
    * @param arg0
    */
    @Override
    public void onSynthesizeStart(String s) {

    }

    /**
     * 合成數據和進度的回調介面，分多次回調
     *
     * @param utteranceId
     * @param data 合成的音頻數據。該音頻數據是採樣率為16K，2位元組精度，單聲道的pcm數據。
     * @param progress 文本按字元劃分的進度，比如:你好啊 進度是0-3
     */
    @Override
    public void onSynthesizeDataArrived(String utteranceId, byte[] data, int progress) {

    }

    /**
     * 合成正常結束，每句合成正常結束都會回調，如果過程中出錯，則回調onError，不再回調此介面
     *
     * @param utteranceId
     */
    @Override
    public void onSynthesizeFinish(String utteranceId) {

    }

    /**
     * 播放開始，每句播放開始都會回調
     *
     * @param utteranceId
     */
    @Override
    public void onSpeechStart(String utteranceId) {

    }

    /**
     * 播放進度回調介面，分多次回調
     *
     * @param utteranceId
     * @param progress 文本按字元劃分的進度，比如:你好啊 進度是0-3
     */
    @Override
    public void onSpeechProgressChanged(String utteranceId, int progress) {

    }

    /**
     * 播放正常結束，每句播放正常結束都會回調，如果過程中出錯，則回調onError,不再回調此介面
     *
     * @param utteranceId
     */
    @Override
    public void onSpeechFinish(String utteranceId) {
        pageFactory.nextPage();
        if (pageFactory.islastPage()) {
            isSpeaking = false;
            Toast.makeText(ReadActivity.this,"小說已經讀完了",Toast.LENGTH_SHORT);
        }else {
            isSpeaking = true;
            mSpeechSynthesizer.speak(pageFactory.getCurrentPage().getLineToString());
        }
    }

    /**
     * 當合成或者播放過程中出錯時回調此介面
     *
     * @param utteranceId
     * @param error 包含錯誤碼和錯誤信息
     */
    @Override
    public void onError(String utteranceId, SpeechError error) {
        mSpeechSynthesizer.stop();
        isSpeaking = false;
        Log.e(TAG,error.description);
    }

}
