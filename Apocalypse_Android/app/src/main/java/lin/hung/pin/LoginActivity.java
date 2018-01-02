package lin.hung.pin;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lin.hung.pin.web.Common;
import lin.hung.pin.web.MyTask;

import java.util.HashSet;
import java.util.Set;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "CheckAccount";
    public static final int EXTERNAL_STORAGE_REQ_CODE = 10 ;
    private String login;
    private TextView tvTitle;
    private EditText etAccount;
    private EditText etPassword;
    private RelativeLayout textLayout0;
    private Button btHelper;
    private MyTask checkAccount;
    private float amountToMoveRight = 0.0f;
    private float amountToMoveDown = -175.0f;
    private static final int GOTO_MAIN_ACTIVITY = 0;
    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {

            switch (msg.what) {
                case GOTO_MAIN_ACTIVITY:
                    textLayout0.setVisibility(View.VISIBLE);
                    final Animation am = new TranslateAnimation(0.0f, amountToMoveRight, 0.0f, amountToMoveDown);
                    //setDuration (long durationMillis) 設定動畫開始到結束的執行時間
                    am.setDuration(1000);
                    //如果設置為true，控制項動畫結束時，將保持動畫最後時的狀態
                    am.setFillAfter(true);
                    //setRepeatCount (int repeatCount) 設定重複次數 -1為無限次數
                    am.setRepeatCount(0);
                    am.setAnimationListener(new TranslateAnimation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }
                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                        @Override
                        public void onAnimationEnd(Animation animation) {
                            tvTitle.clearAnimation();
                            textLayout0.clearAnimation();
                            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(tvTitle.getWidth(), tvTitle.getHeight());
                            lp.setMargins(tvTitle.getLeft(), tvTitle.getTop()-175, 0, 0);
//                            lp.setMargins(tvTitle.getLeft(), tvTitle.getTop(), tvTitle.getRight(), tvTitle.getBottom());
//                            lp.setMargins((int) amountToMoveRight, (int) (amountToMoveDown + 470), 0, 0);
                            tvTitle.setLayoutParams(lp);
                        }
                    });
                    //開始執行動畫
                    tvTitle.startAnimation(am);
                    textLayout0.startAnimation(am);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermission(LoginActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, EXTERNAL_STORAGE_REQ_CODE, "添加圖書需要此權限，請允許");
        }


//        askPermissions();
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        etAccount = (EditText) findViewById(R.id.etAccount);
        etPassword = (EditText) findViewById(R.id.etPassword);
        textLayout0 = (RelativeLayout) findViewById(R.id.textLayout0);
        btHelper = (Button) findViewById(R.id.btHelper);
        mHandler.sendEmptyMessageDelayed(GOTO_MAIN_ACTIVITY, 2000); //2秒跳轉
    }

    public void onClick(View view) {
        if(view.getId()==R.id.btHelper) {
            etAccount.setText("superrichkingstar");
            etPassword.setText("9876543210");
        }
        else {
            String account = etAccount.getText().toString();
            String password = etPassword.getText().toString();
            if (Common.networkConnected(this)) {
                String url = Common.URL + "BookShelfServlet";
                try {
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("action", "checkAccount");
                    jsonObject.addProperty("account", account);
                    jsonObject.addProperty("password", password);
                    String jsonOut = jsonObject.toString();
                    checkAccount = new MyTask(url, jsonOut);
                    String jsonIn = checkAccount.execute().get();
                    Log.d(TAG, jsonIn);
                    Gson gson = new Gson();
                    login = gson.fromJson(jsonIn, String.class);
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }
            } else {
                Common.showToast(this, R.string.msg_NoNetwork);
            }

            if(login.equals("LoginOK")) {
                Intent intent = new Intent(LoginActivity.this,GuideActivity.class);
                intent.putExtra("Account", account);
                intent.putExtra("Password", password);
                LoginActivity.this.startActivity(intent);
                LoginActivity.this.finish();
            } else {
                Intent intent = new Intent(LoginActivity.this,LoginActivity.class);
                LoginActivity.this.startActivity(intent);
                LoginActivity.this.finish();
                Toast.makeText(this, "您輸入的帳號或密碼不正確，請重新再輸入", Toast.LENGTH_LONG).show();
            }
        }
    }




    /**
     * 檢查是否擁有權限
     * @param thisActivity
     * @param permission
     * @param requestCode
     * @param errorText
     */
    protected void checkPermission (Activity thisActivity, String permission, int requestCode, String errorText) {
        //判斷當前Activity是否已經獲得了該權限
        if(ContextCompat.checkSelfPermission(thisActivity,permission) != PackageManager.PERMISSION_GRANTED) {
            //如果App的權限申請曾經被用戶拒絕過，就需要在這裡跟用戶做出解釋
            if (ActivityCompat.shouldShowRequestPermissionRationale(thisActivity,
                    permission)) {
                Toast.makeText(this,errorText,Toast.LENGTH_SHORT).show();
                //進行權限請求
                ActivityCompat.requestPermissions(thisActivity,
                        new String[]{permission},
                        requestCode);
            } else {
                //進行權限請求
                ActivityCompat.requestPermissions(thisActivity,
                        new String[]{permission},
                        requestCode);
            }
        } else {

        }
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
