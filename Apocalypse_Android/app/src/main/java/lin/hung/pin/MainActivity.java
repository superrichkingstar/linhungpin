package lin.hung.pin;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.widget.AbsoluteLayout;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.umeng.analytics.MobclickAgent;
import com.umeng.fb.FeedbackAgent;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;
import lin.hung.pin.adapter.ShelfAdapter;
import lin.hung.pin.animation.ContentScaleAnimation;
import lin.hung.pin.animation.Rotate3DAnimation;
import lin.hung.pin.base.BaseActivity;
import lin.hung.pin.db.BookList;
import lin.hung.pin.filechooser.FileChooserActivity;
import lin.hung.pin.util.CommonUtil;
import lin.hung.pin.util.DisplayUtils;
import lin.hung.pin.view.DragGridView;
import lin.hung.pin.web.BookShelf;
import lin.hung.pin.web.Common;
import lin.hung.pin.web.EBookGetThingTask;
import lin.hung.pin.web.MyTask;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.Bind;
import okhttp3.Call;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener, Animation.AnimationListener  {

    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.fab)
    FloatingActionButton fab;
    @Bind(R.id.nav_view)
    NavigationView navigationView;
    @Bind(R.id.drawer_layout)
    DrawerLayout drawer;
    @Bind(R.id.bookShelf)
    DragGridView bookShelf;


    private static final String TAG = "BookShelf";
    private String account;
    private String password;
    List<BookShelf> bookShelfList;

    private WindowManager mWindowManager;
    private AbsoluteLayout wmRootView;
    private View rootView;
    private Typeface typeface;

    private List<BookList> bookLists;
    private ShelfAdapter adapter;
    //點擊書本的位置
    private int itemPosition;
    private TextView itemTextView;
    //點擊書本在屏幕中的x，y坐標
    private int[] location = new int[2];

    private static TextView cover;
    private static ImageView content;
    //書本打開動畫縮放比例
    private float scaleTimes;
    //書本打開縮放動畫
    private static ContentScaleAnimation contentAnimation;
    private static Rotate3DAnimation coverAnimation;
    //書本打開縮放動畫持續時間
    public static final int ANIMATION_DURATION = 800;
    //打開書本的第一個動畫是否完成
    private boolean mIsOpen = false;
    //動畫加載計數器  0 默認  1一個動畫執行完畢   2二個動畫執行完畢
    private int animationCount=0;

    private static Boolean isExit = false;

    private Config config;
    @Override
    public int getLayoutRes() {
        return R.layout.activity_main;
    }

    @Override
    protected void initData() {
        setSupportActionBar(toolbar);
//        toolbar.setNavigationIcon(R.drawable.ic_menu_white_24dp);//設置導航圖標

        drawer.closeDrawers();

        //友盟統計
        MobclickAgent.setScenarioType(this, MobclickAgent.EScenarioType.E_UM_NORMAL);
        MobclickAgent.enableEncrypt(true);//6.0.0版本及以後
        //自動提醒反饋建議
        FeedbackAgent agent = new FeedbackAgent(this);
        agent.sync();

        config = Config.getInstance();
        // 刪除視窗背景
        getWindow().setBackgroundDrawable(null);
        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        wmRootView = new AbsoluteLayout(this);
        rootView = getWindow().getDecorView();
//        SQLiteDatabase db = Connector.getDatabase();  //初始化數據庫
        typeface = config.getTypeface();
        bookLists = DataSupport.findAll(BookList.class);


        Intent intent = getIntent();
        account = intent.getStringExtra("Account");
        password = intent.getStringExtra("Password");

//        String path="/sdcard/txt/";
//        String imagepath="/sdcard/image/";
//        List<BookList> mBookList = DataSupport.where("bookpath LIKE '/sdcard/txt/%'").find(BookList.class);
//        for(BookList element:mBookList) {
//            DataSupport.delete(BookList.class, element.getId());
//        }
//        File file = new File(path);
//        deleteLocal("/sdcard/Download/");
//        deleteFolder(path);
//        deleteFolder(imagepath);
//
//        CopyAssets(this, "local", "/sdcard/Download/");
//        getBookShelf();



        adapter = new ShelfAdapter(MainActivity.this,bookLists);
        bookShelf.setAdapter(adapter);
//        adapter.notifyDataSetChanged();   //更新內容
    }

    @Override
    protected void initListener() {
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                Intent intent = new Intent(MainActivity.this,FileChooserActivity.class);
                MainActivity.this.startActivity(intent);
            }
        });

//        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
//                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
//        drawer.setDrawerListener(toggle);
//        toggle.syncState();
//
//        navigationView.setNavigationItemSelectedListener(this);

        bookShelf.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (bookLists.size() > position) {
                    itemPosition = position;
                    String bookname = bookLists.get(itemPosition).getBookname();

                    adapter.setItemToFirst(itemPosition);
//                bookLists = DataSupport.findAll(BookList.class);
                    final BookList bookList = bookLists.get(itemPosition);
                    bookList.setId(bookLists.get(0).getId());
                    final String path = bookList.getBookpath();
                    File file = new File(path);
                    if (!file.exists()){
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle(MainActivity.this.getString(R.string.app_name))
                                .setMessage(path + "文件不存在,是否刪除該書本？")
                                .setPositiveButton("刪除", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        DataSupport.deleteAll(BookList.class, "bookpath = ?", path);
                                        bookLists = DataSupport.findAll(BookList.class);
                                        adapter.setBookList(bookLists);
                                    }
                                }).setCancelable(true).show();
                        return;
                    }

                    ReadActivity.openBook(bookList,MainActivity.this);

//                    if (!isOpen){
//                        bookLists = DataSupport.findAll(BookList.class);
//                        adapter.notifyDataSetChanged();
//                    }
//                    itemTextView = (TextView) view.findViewById(R.id.tv_name);
//                    //獲取item在屏幕中的x，y坐標
//                    itemTextView.getLocationInWindow(location);
//
//                    //初始化dialog
//                    mWindowManager.addView(wmRootView, getDefaultWindowParams());
//                    cover = new TextView(getApplicationContext());
//                    cover.setBackgroundDrawable(getResources().getDrawable(R.mipmap.cover_default_new));
//                    cover.setCompoundDrawablesWithIntrinsicBounds(null,null,null,getResources().getDrawable(R.mipmap.cover_type_txt));
//                    cover.setText(bookname);
//                    cover.setTextColor(getResources().getColor(R.color.read_textColor));
//                    cover.setTypeface(typeface);
//                    int coverPadding = (int) CommonUtil.convertDpToPixel(getApplicationContext(), 10);
//                    cover.setPadding(coverPadding, coverPadding, coverPadding, coverPadding);
//
//                    content = new ImageView(getApplicationContext());
//                    Bitmap contentBitmap = Bitmap.createBitmap(itemTextView.getMeasuredWidth(),itemTextView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
//                    contentBitmap.eraseColor(getResources().getColor(R.color.read_background_paperYellow));
//                    content.setImageBitmap(contentBitmap);
//
//                    AbsoluteLayout.LayoutParams params = new AbsoluteLayout.LayoutParams(
//                            itemTextView.getLayoutParams());
//                    params.x = location[0];
//                    params.y = location[1];
//                    wmRootView.addView(content, params);
//                    wmRootView.addView(cover, params);
//
//                    initAnimation();
//                    if (contentAnimation.getMReverse()) {
//                        contentAnimation.reverse();
//                    }
//                    if (coverAnimation.getMReverse()) {
//                        coverAnimation.reverse();
//                    }
//                    cover.clearAnimation();
//                    cover.startAnimation(coverAnimation);
//                    content.clearAnimation();
//                    content.startAnimation(contentAnimation);
                }
            }
        });
    }


    @Override
    protected void onRestart(){
        super.onRestart();
        DragGridView.setIsShowDeleteButton(false);
        bookLists = DataSupport.findAll(BookList.class);
        adapter.setBookList(bookLists);
        closeBookAnimation();
    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    @Override
    protected void onStop() {
        DragGridView.setIsShowDeleteButton(false);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        DragGridView.setIsShowDeleteButton(false);
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (drawer.isDrawerOpen(Gravity.LEFT)) {
                drawer.closeDrawers();
            } else {
                exitBy2Click();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 在2秒內按下返回鍵兩次才退出
     */
    private void exitBy2Click() {
        // press twice to exit
        Timer tExit;
        if (!isExit) {
            isExit = true; // ready to exit
            if(DragGridView.getShowDeleteButton()) {
                DragGridView.setIsShowDeleteButton(false);
                //要保證是同一個adapter對象,否則在Restart後無法notifyDataSetChanged
                adapter.notifyDataSetChanged();
            }else {
                Toast.makeText(this, this.getResources().getString(R.string.press_twice_to_exit), Toast.LENGTH_SHORT).show(); }
            tExit = new Timer();
            tExit.schedule(new TimerTask() {
                @Override
                public void run() {
                    isExit = false; // cancel exit
                }
            }, 2000); // 2 seconds cancel exit task

        } else {
            finish();
            // call fragments and end streams and services
            System.exit(0);
        }
    }

    //初始化dialog動畫
    private void initAnimation() {
        AccelerateInterpolator interpolator = new AccelerateInterpolator();

        float scale1 = DisplayUtils.getScreenWidthPixels(this) / (float) itemTextView.getMeasuredWidth();
        float scale2 = DisplayUtils.getScreenHeightPixels(this) / (float) itemTextView.getMeasuredHeight();
        scaleTimes = scale1 > scale2 ? scale1 : scale2;  //計算縮放比例

        contentAnimation = new ContentScaleAnimation( location[0], location[1],scaleTimes, false);
        contentAnimation.setInterpolator(interpolator);  //設置插值器
        contentAnimation.setDuration(ANIMATION_DURATION);
        contentAnimation.setFillAfter(true);  //動畫停留在最後一幀
        contentAnimation.setAnimationListener(this);

        coverAnimation = new Rotate3DAnimation(0, -180, location[0], location[1], scaleTimes, false);
        coverAnimation.setInterpolator(interpolator);
        coverAnimation.setDuration(ANIMATION_DURATION);
        coverAnimation.setFillAfter(true);
        coverAnimation.setAnimationListener(this);
    }

    public void closeBookAnimation() {

        if (mIsOpen && wmRootView!=null) {
            //因為書本打開後會移動到第一位置，所以要設置新的位置參數
            contentAnimation.setmPivotXValue(bookShelf.getFirstLocation()[0]);
            contentAnimation.setmPivotYValue(bookShelf.getFirstLocation()[1]);
            coverAnimation.setmPivotXValue(bookShelf.getFirstLocation()[0]);
            coverAnimation.setmPivotYValue(bookShelf.getFirstLocation()[1]);

            AbsoluteLayout.LayoutParams params = new AbsoluteLayout.LayoutParams(
                    itemTextView.getLayoutParams());
            params.x = bookShelf.getFirstLocation()[0];
            params.y = bookShelf.getFirstLocation()[1];//firstLocation[1]在滑動的時候回改變,所以要在dispatchDraw的時候獲取該位置值
            wmRootView.updateViewLayout(cover,params);
            wmRootView.updateViewLayout(content,params);
            //動畫逆向運行
            if (!contentAnimation.getMReverse()) {
                contentAnimation.reverse();
            }
            if (!coverAnimation.getMReverse()) {
                coverAnimation.reverse();
            }
            //清除動畫再開始動畫
            content.clearAnimation();
            content.startAnimation(contentAnimation);
            cover.clearAnimation();
            cover.startAnimation(coverAnimation);
        }
    }

    @Override
    public void onAnimationStart(Animation animation) {

    }

    @Override
    public void onAnimationEnd(Animation animation) {
        //有兩個動畫監聽會執行兩次，所以要判斷
        if (!mIsOpen) {
            animationCount++;
            if (animationCount >= 2) {
                mIsOpen = true;
                adapter.setItemToFirst(itemPosition);
//                bookLists = DataSupport.findAll(BookList.class);
                BookList bookList = bookLists.get(itemPosition);
                bookList.setId(bookLists.get(0).getId());
                ReadActivity.openBook(bookList,MainActivity.this);
            }

        } else {
            animationCount--;
            if (animationCount <= 0) {
                mIsOpen = false;
                wmRootView.removeView(cover);
                wmRootView.removeView(content);
                mWindowManager.removeView(wmRootView);
            }
        }
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }

    //獲取dialog屬性
    private WindowManager.LayoutParams getDefaultWindowParams() {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                0, 0,
                WindowManager.LayoutParams.TYPE_APPLICATION_PANEL,//windown類型,有層級的大的層級會覆蓋在小的層級
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                PixelFormat.RGBA_8888);

        return params;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        menu.getItem(2).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }else if (id == R.id.action_select_file){
//            Intent intent = new Intent(MainActivity.this, FileChooserActivity.class);
//            startActivity(intent);
//        }

        if (id == R.id.action_select_file){
            Intent intent = new Intent(MainActivity.this, FileChooserActivity.class);
            startActivity(intent);
        } else if(id == R.id.action_book_update) {
            Intent intent = new Intent(MainActivity.this,GuideActivity.class);
            intent.putExtra("Account", account);
            intent.putExtra("Password", password);
            MainActivity.this.startActivity(intent);
            MainActivity.this.finish();
        } else {
            Intent intent = new Intent(MainActivity.this,LoginActivity.class);
            MainActivity.this.startActivity(intent);
            MainActivity.this.finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_feedback) {
            FeedbackAgent agent = new FeedbackAgent(this);
            agent.startFeedbackActivity();

        } else if (id == R.id.nav_checkupdate) {
            checkUpdate(true);
        }else if (id == R.id.nav_about) {
            Intent intent = new Intent(MainActivity.this, AboutActivity.class);
            startActivity(intent);
        }

//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void checkUpdate(final boolean showMessage){
        String url = "http://api.fir.im/apps/latest/57be8d56959d6960d5000327";
        OkHttpUtils
                .get()
                .url(url)
                .addParams("api_token", "a48b9bbcef61f34c51160bfed26aa6b2")
                .build()
                .execute(new StringCallback()
                {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        if (showMessage) {
                            Toast.makeText(MainActivity.this, "檢查更新失敗！", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String version = jsonObject.getString("version");
                            String versionCode = CommonUtil.getVersionCode(MainActivity.this) + "";
                            if (versionCode.compareTo(version) < 0){
                                showUpdateDialog(jsonObject.getString("name"),jsonObject.getString("versionShort"),jsonObject.getString("changelog"),jsonObject.getString("update_url"),MainActivity.this);
                            }else{
                                if (showMessage) {
                                    Toast.makeText(MainActivity.this, "已經是最新版本！", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } catch (JSONException e) {
                            if (showMessage) {
                                Toast.makeText(MainActivity.this, "檢查更新失敗", Toast.LENGTH_SHORT).show();
                            }
                            e.printStackTrace();
                        }
                    }

                });

    }

    public static void showUpdateDialog(final String name, String version, String changelog, final String updateUrl, final Context context) {
        String title = "發現新版" + name + "，版本號：" + version;

        new android.support.v7.app.AlertDialog.Builder(context).setTitle(title)
                .setMessage(changelog)
                .setPositiveButton("下載", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Uri uri = Uri.parse(updateUrl);   //指定網址
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);           //指定Action
                        intent.setData(uri);                            //設置Uri
                        context.startActivity(intent);        //啟動Activity
                    }
                })
                .show();
    }



    public static void deleteLocal(String localPath) {
        try {
            File file = new File(localPath);
            String fileNames[] = file.list();
//            if (fileNames.length > 0) {// 如果是目錄
            for (String fileName : fileNames) {
                File mFile = new File(localPath + fileName);
                mFile.delete();
            }
//            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void deleteFolder(String FolderPath) {
        try {
            File file = new File(FolderPath);
            String fileNames[] = file.list();
//            if (fileNames.length > 0) {// 如果是目錄
            for (String fileName : fileNames) {
                File mFile = new File(FolderPath + fileName);
                mFile.delete();
            }
//            }
            file.delete();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** * 複製asset文件到指定目錄 * @param oldPath asset下的路徑 * @param newPath SD卡下保存路徑 */
    public static void CopyAssets(Context context, String oldPath, String newPath) {
        try {
            String fileNames[] = context.getAssets().list(oldPath);// 獲取assets目錄下的所有文件及目錄名
            if (fileNames.length > 0) {// 如果是目錄
                File file = new File(newPath);
                file.mkdirs();// 如果文件夾不存在，則遞歸
                for (String fileName : fileNames) {
                    CopyAssets(context, oldPath + "/" + fileName, newPath + fileName);
                }
            } else {// 如果是文件
                InputStream is = context.getAssets().open(oldPath);
                FileOutputStream fos = new FileOutputStream(new File(newPath));
                byte[] buffer = new byte[1024];
                int byteCount = 0;
                while ((byteCount = is.read(buffer)) != -1) {// 循環從輸入流讀取
                    // buffer位元組
                    fos.write(buffer, 0, byteCount);// 將讀取的輸入流寫入到輸出流
                }
                fos.flush();// 刷新緩衝區
                is.close();
                fos.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void getBookShelf() {
        if (Common.networkConnected(this)) {
            String url = Common.URL + "BookShelfServlet";
            try {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("action", "getBookShelf");
                jsonObject.addProperty("account", account);
                jsonObject.addProperty("password", password);
                String jsonOut = jsonObject.toString();
                MyTask eBookGetBookShelfTask = new MyTask(url, jsonOut);
                String jsonIn = eBookGetBookShelfTask.execute().get();
                Log.d(TAG, jsonIn);
                Gson gson = new Gson();
                Type listType = new TypeToken<List<BookShelf>>(){ }.getType();
                bookShelfList = gson.fromJson(jsonIn, listType);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
            if (bookShelfList == null || bookShelfList.isEmpty()) {
                Common.showToast(this, R.string.msg_NoBookShelfListFound);
            } else {
                String txtPath="/sdcard/txt/";
                String jpgPath="/sdcard/image/";
                File file1 = new File(txtPath);
                file1.mkdirs();// 如果txt文件夾不存在，則建立

//                File file2 = new File(jpgPath);
//                file2.mkdirs();// 如果image文件夾不存在，則建立
                bookLists = DataSupport.findAll(BookList.class);
                for(BookShelf bookShelf:bookShelfList) {
                    BookList bookList = new BookList(bookShelf.getBookName(), txtPath + bookShelf.getMainFileName() + ".txt", 0, "UTF-8", jpgPath + bookShelf.getMainFileName() + ".jpg");
                    bookList.save();
                    bookLists.add(bookList);
                    requestAndCopyToSdCard(url, "getBook", bookShelf.getBookId(), bookList.getId(), txtPath + bookShelf.getMainFileName() + ".txt");
                    requestAndCopyToSdCard(url, "getImage", bookShelf.getBookId(), bookList.getId(), jpgPath + bookShelf.getMainFileName() + ".jpg");
                    bookLists = DataSupport.findAll(BookList.class);
                }
                //jsonObject.addProperty("imageSize", imageSize);
            }
        } else {
            Common.showToast(this, R.string.msg_NoNetwork);
        }
    }

    public void requestAndCopyToSdCard(String url, String action, int bookId, int id, String filePath) {
        try {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", action);
            jsonObject.addProperty("account", account);
            jsonObject.addProperty("password", password);
            jsonObject.addProperty("bookId", bookId);
            String jsonOut = jsonObject.toString();
            EBookGetThingTask eBookGetThingTask = new EBookGetThingTask(url, action, id, filePath, jsonOut);
            byte[] b = eBookGetThingTask.execute().get();

            if(b.length>0) {
                if(action.equals("getImage")) {
                    BookList bookList = DataSupport.find(BookList.class, id);
                    bookList.setImageArray(b);
                    bookList.save();


//                    FileOutputStream fos = new FileOutputStream(new File(filePath));
//                    fos.write(b);
//                    fos.flush();// 刷新緩衝區
//                    fos.close();
                } else {
                    FileOutputStream fos = new FileOutputStream(new File(filePath));
                    fos.write(b);
                    fos.flush();// 刷新緩衝區
                    fos.close();
                }
            }
//            Toast.makeText(this, b.length+"", Toast.LENGTH_LONG).show();
//            byte[] buffer = new byte[1024];
//            int byteCount = 0;
//            while ((byteCount = is.read(buffer)) != -1) {// 循環從輸入流讀取
//                // buffer位元組
//                fos.write(buffer, 0, byteCount);// 將讀取的輸入流寫入到輸出流
//            }
//            fos.flush();// 刷新緩衝區
//            is.close();
//            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public byte[] assetsToByteArray(String fileName) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int byteCount = 0;
        try {
            InputStream is = getAssets().open(fileName);
            while ((byteCount = is.read(buffer)) != -1) {// 循環從輸入流讀取
                // buffer位元組
                baos.write(buffer, 0, byteCount);// 將讀取的輸入流寫入到輸出流
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return baos.toByteArray();
    }
}
