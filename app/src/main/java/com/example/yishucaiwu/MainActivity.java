package com.example.yishucaiwu;

import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText psw_text;
    private Button sendBtn;
    private TextView mainText;
    private SwipeRefreshLayout swipeRefresh;

    final String PSW = "dotadota";
    final String URL = "http://www.yishucaiwu.com/yishu_work.json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        psw_text = (EditText) findViewById(R.id.psw_text);
        sendBtn = (Button) findViewById(R.id.send);
        mainText = (TextView) findViewById(R.id.mainText);
        sendBtn.setOnClickListener(this);

        //判断本地数据库中是否存在密码
        if (isPswSaved()) {
            //隐藏EditText和Button控件
            psw_text.setVisibility(View.GONE);
            sendBtn.setVisibility(View.GONE);
            //显示正文内容
            sendRequestWithOkHttp();
        }

        swipeRefresh = (SwipeRefreshLayout)findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshMainText();
            }
        });
    }

    private void refreshMainText()
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    Thread.sleep(2000);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //显示正文内容
                        sendRequestWithOkHttp();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        }).start();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.send:
                //判断text中的密码是否正确
                String strPsw = psw_text.getText().toString().trim();
                if (strPsw.equals(PSW)) {
                    SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
                    editor.putString("psw", PSW);
                    editor.apply();
                    //隐藏EditText和Button控件
                    psw_text.setVisibility(View.GONE);
                    sendBtn.setVisibility(View.GONE);

                    //显示正文内容
                    sendRequestWithOkHttp();
                } else {
                    Toast.makeText(this, "sorry,wrong psw!", Toast.LENGTH_SHORT).show();
                }
        }
    }

    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.toolbar,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.settings:
            //    Toast.makeText(this,"You clicked Share",Toast.LENGTH_SHORT).show();
                AndroidShare androidShare = new AndroidShare(this);
                String strContent = mainText.getText().toString();
                androidShare.shareWeChatFriend("伊淑工作安排",strContent,0,null);
                break;
            default:
        }
        return true;
    }

    public boolean isPswSaved() {
        //从本地读取密码
        SharedPreferences pref = getSharedPreferences("data", MODE_PRIVATE);
        String strPsd = pref.getString("psw", "");
        if (strPsd.equals(PSW))
            return true;
        else
            return false;
    }

    private void sendRequestWithOkHttp() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url(URL)
                            .build();
                    Response response = client.newCall(request).execute();
                    String responseData = response.body().string();
                    showResponse(parseJSONWithGSON(responseData));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void showResponse(final String response) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //在此处分析接收的服务器数据，定制显示格式等
                mainText.setText(response);
            }
        });
    }

    private String parseJSONWithGSON(String jsonData)
    {
        Gson gson = new Gson();
        List<App> appList = gson.fromJson(jsonData,new TypeToken<List<App>>(){}.getType());
        String strText = "";
        for (App app:appList)
        {
            strText += app.getId() + app.getWork() + "\n";
        }
       return strText;
    }
}
