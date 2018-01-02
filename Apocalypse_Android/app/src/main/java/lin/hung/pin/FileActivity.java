package lin.hung.pin;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Toast;

import lin.hung.pin.adapter.FileAdapter;
import lin.hung.pin.base.BaseActivity;
import lin.hung.pin.db.BookList;
import lin.hung.pin.util.FileUtils;
import lin.hung.pin.util.Fileutil;

import org.litepal.crud.DataSupport;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.Bind;

/**
 * Created by Administrator on 2017/11/6 0011.
 */
public class FileActivity extends BaseActivity {

    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.btn_choose_all)
    Button btnChooseAll;
    @Bind(R.id.btn_delete)
    Button btnDelete;
    @Bind(R.id.btn_add_file)
    Button btnAddFile;
    @Bind(R.id.lv_file_drawer)
    ListView lvFileDrawer;

    public static final int EXTERNAL_STORAGE_REQ_CODE = 10 ;

    //文件根目錄
    private File root;
    private List<File> listFile = new ArrayList<>();
    private static FileAdapter adapter;
    private SearchTextFileTask mSearchTextFileTask;
    private SaveBookToSqlLiteTask mSaveBookToSqlLiteTask;
    @Override
    public int getLayoutRes() {
        return R.layout.activity_file;
    }

    @Override
    protected void initData() {
        getWindow().setBackgroundDrawable(null);
        setSupportActionBar(toolbar);
        //設置導航圖標
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        if(getSupportActionBar() != null) {
            getSupportActionBar().setTitle("導入圖書");
        }


        adapter = new FileAdapter(this, listFile);
        lvFileDrawer.setAdapter(adapter);

//        askPermissions();

        root = Environment.getExternalStorageDirectory();
        searchFile();


//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            checkPermission(FileActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, EXTERNAL_STORAGE_REQ_CODE,"添加圖書需要此權限，請允許");
//        }else{
//            root = Environment.getExternalStorageDirectory();
//            searchFile();
//        }

    }

    @Override
    protected void initListener() {
        lvFileDrawer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                adapter.setSelectedPosition(position);
            }
        });

        lvFileDrawer.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                return true;
            }
        });

        adapter.setCheckedChangeListener(new FileAdapter.CheckedChangeListener() {

            @Override
            public void onCheckedChanged(int position, CompoundButton buttonView, boolean isChecked) {
                setAddFileText(adapter.getCheckNum());
            }
        });
        //全選
        btnChooseAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.checkAll();
            }
        });
        //取消選擇
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.cancel();
            }
        });
        //把已經選擇的書加入書架
        btnAddFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveBookList();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSearchTextFileTask != null) {
            mSearchTextFileTask.cancel(true);
        }
        if (mSaveBookToSqlLiteTask != null){
            mSaveBookToSqlLiteTask.cancel(true);
        }
    }

    //保存選擇的txt文件
    private void saveBookList(){
        List<File> files = adapter.getCheckFiles();
        if (files.size() > 0) {
            List<BookList> bookLists = new ArrayList<BookList>();
            for (File file : files) {
                BookList bookList = new BookList();
                String bookName = Fileutil.getFileNameNoEx(file.getName());
                bookList.setBookname(bookName);
                bookList.setBookpath(file.getAbsolutePath());
                bookList.setImageArray(assetsToByteArray("cover_default_new.png"));
                bookLists.add(bookList);
            }
            mSaveBookToSqlLiteTask = new SaveBookToSqlLiteTask();
            mSaveBookToSqlLiteTask.execute(bookLists);
        }
    }

    private class SaveBookToSqlLiteTask extends AsyncTask<List<BookList>,Void,Integer>{
        private static final int FAIL = 0;
        private static final int SUCCESS = 1;
        private static final int REPEAT = 2;
        private BookList repeatBookList;

        @Override
        protected Integer doInBackground(List<BookList>... params) {
            List<BookList> bookLists = params[0];
            for (BookList bookList : bookLists){
                List<BookList> books = DataSupport.where("bookpath = ?", bookList.getBookpath()).find(BookList.class);
                if (books.size() > 0){
                    repeatBookList = bookList;
                    return REPEAT;
                }
            }

            try {
                DataSupport.saveAll(bookLists);
            } catch (Exception e){
                e.printStackTrace();
                return FAIL;
            }
            return SUCCESS;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            String msg = "";
            switch (result){
                case FAIL:
                    msg = "由於一些原因添加書本失敗";
                    break;
                case SUCCESS:
                    msg = "添加書本成功";
                    setAddFileText(0);
                    adapter.cancel();
                    break;
                case REPEAT:
                    msg = "書本" + repeatBookList.getBookname() + "重複了";
                    break;
            }

            Toast.makeText(FileActivity.this, msg, Toast.LENGTH_SHORT).show();
        }
    }

    //設置添加按鈕text
    protected void setAddFileText(final int num){
        btnAddFile.post(new Runnable() {
            @Override
            public void run() {
                btnAddFile.setText("加入書架(" + num + ")項");
            }
        });
    }
    protected void searchFile(){
//        startTime = System.currentTimeMillis();
        mSearchTextFileTask = new SearchTextFileTask();
        mSearchTextFileTask.execute();
    }

    private class SearchTextFileTask extends AsyncTask<Void,Void,Boolean>{
        @Override
        protected void onPreExecute() {
            showProgress(true,"正在掃描txt文件");
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            listFile = FileUtils.getSuffixFile(root.getAbsolutePath(),".txt");
            if (listFile == null || listFile.isEmpty()){
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            hideProgress();
            if (result) {
//                for(File file:listFile) {
//                    Toast.makeText(FileActivity.this, file.getAbsolutePath(), Toast.LENGTH_LONG).show();
//                }


                adapter.setFiles(listFile);  //list值傳到adapter
                setAddFileText(0);
//                endTime = System.currentTimeMillis();
//                Log.e("time",endTime - startTime + "");
            } else {
                Toast.makeText(FileActivity.this, "本機查不到txt文件", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        switch (requestCode) {
//            case EXTERNAL_STORAGE_REQ_CODE: {
//                // 如果請求被拒絕，那麼通常grantResults數組為空
//                if (grantResults.length > 0
//                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    //申請成功，進行相應操作
//                    root = Environment.getExternalStorageDirectory();
//                    searchFile();
//                } else {
//                    //申請失敗，可以繼續向用戶解釋。
//                }
//                return;
//            }
//        }
//    }


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


    private static final int REQ_PERMISSIONS = 0;

    private void askPermissions() {
        String[] permissions = {
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION
        };

        Set<String> permissionsRequest = new HashSet<>();
        for (String permission : permissions) {
            int result = ContextCompat.checkSelfPermission(this, permission);
            if (result != PackageManager.PERMISSION_GRANTED) {
                permissionsRequest.add(permission);
            }
        }

        if (!permissionsRequest.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    permissionsRequest.toArray(new String[permissionsRequest.size()]),
                    REQ_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQ_PERMISSIONS:
                String text = "";
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        text += permissions[i] + "\n";
                    }
                }
                if (!text.isEmpty()) {
                    text += getString(R.string.text_NotGranted);
                    Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
