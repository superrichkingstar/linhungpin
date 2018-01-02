package lin.hung.pin;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import lin.hung.pin.db.BookList;
import lin.hung.pin.web.BookShelf;
import lin.hung.pin.web.Common;
import lin.hung.pin.web.EBookGetThingTask;
import lin.hung.pin.web.MyTask;

import org.litepal.crud.DataSupport;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by Administrator on 2017/11/6 0022.
 */
public class GuideActivity extends AppCompatActivity {

    private static final String TAG = "BookShelf";
    private String account;
    private String password;
    List<BookShelf> bookShelfList;
    private List<BookList> bookLists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(getLayoutRes());

        Intent intent = getIntent();
        account = intent.getStringExtra("Account");
        password = intent.getStringExtra("Password");




        initData();
        initListener();




//        try {
//            InputStream mAssets = getAssets().open("txt/A.txt");
//            Toast.makeText(this, mAssets.available()+"\n"+file.isDirectory()+"\n"+file.mkdirs(), Toast.LENGTH_LONG).show();
//            file.delete();
//            Toast.makeText(this, "/sdcard/txt/資料夾是否存在?"+file.exists()+"", Toast.LENGTH_LONG).show();
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//        }
        //bookLists = DataSupport.findAll(BookList.class);
        //Toast.makeText(this, bookLists.toString(), Toast.LENGTH_LONG).show();

//        CopyAssets(this, "txt", path);
//        BookList bookList1=new BookList("愛意", path+"A.txt", 0, "UTF-8", imagepath+"A.jpg");
//        bookList1.save();
//        bookLists.add(bookList1);
//        BookList bookList2=new BookList("情傷", path+"B.txt", 0, "UTF-8", imagepath+"B.jpg");
//        bookList2.save();
//        bookLists.add(bookList2);
//        BookList bookList3=new BookList("無情", path+"C.txt", 0, "UTF-8", imagepath+"C.jpg");
//        bookList3.save();
//        bookLists.add(bookList3);
//        BookList bookList4=new BookList("陽世", path+"D.txt", 0, "UTF-8", imagepath+"D.jpg");
//        bookList4.save();
//        //Toast.makeText(this, Environment.getExternalStorageDirectory().toString()+"\n"+"第一本小說id=  "+bookList1.getId()+"\n"+"第二本小說id=  "+bookList2.getId()+"\n"+"第三本小說id=  "+bookList3.getId()+"\n"+"第四本小說id=  "+bookList4.getId(), Toast.LENGTH_LONG).show();
//        bookLists.add(bookList4);
//        BookList bookList5=new BookList("庸俗", path+"E.txt", 0, "UTF-8", imagepath+"E.jpg");
//        bookList5.save();
//        bookLists.add(bookList5);
//        BookList bookList6=new BookList("安全", path+"F.txt", 0, "UTF-8", imagepath+"F.jpg");
//        bookList6.save();
//        bookLists.add(bookList6);
//        BookList bookList7=new BookList("無意", path+"G.txt", 0, "UTF-8", imagepath+"G.jpg");
//        bookList7.save();
//        bookLists.add(bookList7);
//        BookList bookList8=new BookList("若水", path+"H.txt", 0, "UTF-8", imagepath+"H.jpg");
//        bookList8.save();
//        bookLists.add(bookList8);
//        BookList bookList9=new BookList("善意", path+"I.txt", 0, "UTF-8", imagepath+"I.jpg");
//        bookList9.save();
//        bookLists.add(bookList9);
//        BookList bookList10=new BookList("如果", path+"J.txt", 0, "UTF-8", imagepath+"J.jpg");
//        bookList10.save();
//        bookLists.add(bookList10);
//        BookList bookList11=new BookList("情意", path+"K.txt", 0, "UTF-8", imagepath+"K.jpg");
//        bookList11.save();
//        bookLists.add(bookList11);
//        BookList bookList12=new BookList("幫忙", path+"L.txt", 0, "UTF-8", imagepath+"L.jpg");
//        bookList12.save();
//        bookLists.add(bookList12);
//        BookList bookList13=new BookList("疙瘩", path+"M.txt", 0, "UTF-8", imagepath+"M.jpg");
//        bookList13.save();
//        bookLists.add(bookList13);
//        BookList bookList14=new BookList("苦情", path+"N.txt", 0, "UTF-8", imagepath+"N.jpg");
//        bookList14.save();
//        bookLists.add(bookList14);
//        BookList bookList15=new BookList("勇氣", path+"O.txt", 0, "UTF-8", imagepath+"O.jpg");
//        bookList15.save();
//        bookLists.add(bookList15);
//        BookList bookList16=new BookList("成功", path+"P.txt", 0, "UTF-8", imagepath+"P.jpg");
//        bookList16.save();
//        bookLists.add(bookList16);
    }

    public int getLayoutRes() {
        return R.layout.activity_guide;
    }

    protected void initData() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        getWindow().getDecorView().postDelayed(new Runnable() {
            @Override
            public void run() {
//                String path="/sdcard/txt/";
//                String imagepath="/sdcard/image/";
                String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/txt/";
                String imagepath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/image/";
//                List<BookList> mBookList = DataSupport.where("bookpath LIKE '/sdcard/txt/%'").find(BookList.class);
                List<BookList> mBookList = DataSupport.where("bookpath LIKE '"+Environment.getExternalStorageDirectory().getAbsolutePath()+"/txt/%'").find(BookList.class);
                for(BookList element:mBookList) {
                    DataSupport.delete(BookList.class, element.getId());
                }
                File file = new File(path);
                deleteLocal(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Download/");
                deleteFolder(path);
                deleteFolder(imagepath);

                CopyAssets(GuideActivity.this, "local", Environment.getExternalStorageDirectory().getAbsolutePath()+"/Download/");
                getBookShelf();


                Intent intent = new Intent(GuideActivity.this,MainActivity.class);


                intent.putExtra("Account", account);
                intent.putExtra("Password", password);



                GuideActivity.this.startActivity(intent);
                GuideActivity.this.finish();
            }
        },1000);
    }

    protected void initListener() {

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
                String txtPath=Environment.getExternalStorageDirectory().getAbsolutePath()+"/txt/";
                String jpgPath=Environment.getExternalStorageDirectory().getAbsolutePath()+"/image/";
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


//    private void getBookShelf() {
//        if (Common.networkConnected(this)) {
//            String url = Common.URL + "BookShelfServlet";
//            try {
//                JsonObject jsonObject = new JsonObject();
//                jsonObject.addProperty("action", "getBookShelf");
//                jsonObject.addProperty("account", account);
//                jsonObject.addProperty("password", password);
//                String jsonOut = jsonObject.toString();
//                MyTask eBookGetBookShelfTask = new MyTask(url, jsonOut);
//                String jsonIn = eBookGetBookShelfTask.execute().get();
//                Log.d(TAG, jsonIn);
//                Gson gson = new Gson();
//                Type listType = new TypeToken<List<BookShelf>>(){ }.getType();
//                bookShelfList = gson.fromJson(jsonIn, listType);
//            } catch (Exception e) {
//                Log.e(TAG, e.toString());
//            }
//            if (bookShelfList == null || bookShelfList.isEmpty()) {
//                Common.showToast(this, R.string.msg_NoBookShelfListFound);
//            } else {
//                for(BookShelf bookShelf:bookShelfList) {
//                    bookLists = DataSupport.findAll(BookList.class);
//                    BookList bookList=new BookList(bookShelf.getBookName(), "/sdcard/txt/"+bookShelf.getMainFileName()+".txt", 0, "UTF-8", "/sdcard/image/"+bookShelf.getMainFileName()+".jpg");
//                    bookList.save();
//                    bookLists.add(bookList);
//                    requestAndCopyToSdCard(url,"getBook", bookShelf.getBookId(), "/sdcard/txt/"+bookShelf.getMainFileName()+".txt");
//                    requestAndCopyToSdCard(url,"getImage", bookShelf.getBookId(), "/sdcard/image/"+bookShelf.getMainFileName()+".jpg");
//
//
//                }
//                //jsonObject.addProperty("imageSize", imageSize);
//            }
//        } else {
//            Common.showToast(this, R.string.msg_NoNetwork);
//        }
//    }
//
//    public void requestAndCopyToSdCard(String url, String action, int bookId, String filePath) {
//        try {
//            JsonObject jsonObject = new JsonObject();
//            jsonObject.addProperty("action", action);
//            jsonObject.addProperty("account", account);
//            jsonObject.addProperty("password", password);
//            jsonObject.addProperty("bookId", bookId);
//            String jsonOut = jsonObject.toString();
//            EBookGetThingTask eBookGetImageTask = new EBookGetThingTask(url, filePath, jsonOut);
//            InputStream is = eBookGetImageTask.execute().get();
//            FileOutputStream fos = new FileOutputStream(new File(filePath));
//            byte[] buffer = new byte[1024];
//            int byteCount = 0;
//            while ((byteCount = is.read(buffer)) != -1) {// 循環從輸入流讀取
//                // buffer位元組
//                fos.write(buffer, 0, byteCount);// 將讀取的輸入流寫入到輸出流
//            }
//            fos.flush();// 刷新緩衝區
//            is.close();
//            fos.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}
