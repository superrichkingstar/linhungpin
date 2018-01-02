package lin.hung.pin.web;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import lin.hung.pin.db.BookList;

import org.litepal.crud.DataSupport;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class EBookGetThingTask extends AsyncTask<String, Integer, byte[]> {
    private final static String TAG = "EBookGetThingTask";
    private String url, action, filePath, outStr;
    private int id;
    private Context context;

    public EBookGetThingTask(String url, String action,int id, String filePath, String outStr) {
        this.url = url;
        this.action = action;
        this.id = id;
        this.filePath = filePath;
        this.outStr = outStr;
    }

    @Override
    protected byte[] doInBackground(String... params) {

        return getRemoteThing();
    }

    @Override
    protected void onPostExecute(byte[] b) {
        if (isCancelled()) {
            return;
        }
        if(b.length>0) {
            if(action.equals("getImage")) {
                BookList bookList = DataSupport.find(BookList.class, id);
                bookList.setImageArray(b);
                bookList.save();
            } else {
                try {
                    FileOutputStream fos = new FileOutputStream(new File(filePath));
                    fos.write(b);
                    fos.flush();// 刷新緩衝區
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private byte[] getRemoteThing() {
        HttpURLConnection connection = null;
        InputStream is = null;
        byte[] b=null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setDoInput(true); // allow inputs
            connection.setDoOutput(true); // allow outputs
//            // 不知道請求內容大小時可以呼叫此方法將請求內容分段傳輸，設定0代表使用預設大小
//            // 參考HttpURLConnection API的Posting Content部分
//            connection.setChunkedStreamingMode(0);
            connection.setUseCaches(false); // do not use a cached copy
            connection.setRequestMethod("POST");
            connection.setRequestProperty("charset", "UTF-8");
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
            bw.write(outStr);
            Log.d(TAG, "output: " + outStr);
            bw.close();

            int responseCode = connection.getResponseCode();
            //InputStream gis;
            if (responseCode == 200) {  // && (gis=connection.getInputStream()) != null
                is = connection.getInputStream();   //gis   new BufferedInputStream()
                b = streamAsByteArray(is);
                //System.out.println(b);
            } else {
                Log.d(TAG, "response code: " + responseCode);
            }
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return b;
    }


    public byte[] streamAsByteArray(InputStream is) {
        ByteArrayOutputStream baos=null;
        try {
            baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int byteCount = 0;
            while ((byteCount = is.read(buffer)) != -1) {// 循環從輸入流讀取
                // buffer字節
                baos.write(buffer, 0, byteCount);// 將讀取的輸入流寫入到輸出流
            }
//            fos.flush();// 刷新緩衝區
//            is.close();
//            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return baos.toByteArray();
    }


    public void CopyToSdCard(InputStream is, String filePath) {
        try {
            FileOutputStream fos = new FileOutputStream(new File(filePath));
            byte[] buffer = new byte[1024];
            int byteCount = 0;
            while ((byteCount = is.read(buffer)) != -1) {// 循環從輸入流讀取
                // buffer字節
                fos.write(buffer, 0, byteCount);// 將讀取的輸入流寫入到輸出流
            }
            fos.flush();// 刷新緩衝區
            is.close();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}