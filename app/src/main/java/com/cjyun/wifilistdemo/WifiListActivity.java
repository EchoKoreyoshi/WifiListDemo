package com.cjyun.wifilistdemo;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class WifiListActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, View.OnClickListener {

    private ListView wifiList;
    private Button wifi_switch_btn;
    private Button wifi_scan_btn;
    private Button wifi_cancle_btn;
    private List<android.net.wifi.ScanResult> list;
    private android.net.wifi.ScanResult mScanResult;
    private WifiAdmin mWifiAdmin;
    private WifiConnListAdapter mConnList;
    private TextView showConn;
    private ArrayList<WifiElement> wifiElement = new ArrayList<WifiElement>();
    private boolean isOpen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mWifiAdmin = new WifiAdmin(WifiListActivity.this);
        initView();

    }

    private void initView() {
        wifiList = (ListView) this.findViewById(R.id.wifi_conn_lv);
        wifi_switch_btn = (Button) this.findViewById(R.id.wifi_conn_switch_btn);
        wifi_scan_btn = (Button) this.findViewById(R.id.wifi_conn_scan_btn);
        wifi_cancle_btn = (Button) this.findViewById(R.id.wifi_conn_cancle_btn);
        showConn = (TextView) this.findViewById(R.id.wifi_show_conn);
        if (mWifiAdmin.getWifiState() == WifiManager.WIFI_STATE_DISABLED) {
            wifi_scan_btn.setText("打开wifi");
        } else {
            wifi_scan_btn.setText("关闭wifi");
            isOpen = true;
        }
        showConn.setText("已连接:" + initShowConn());
        wifi_cancle_btn.setOnClickListener(this);
        wifi_switch_btn.setOnClickListener(this);
        wifiList.setOnItemClickListener(this);
        wifi_scan_btn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.wifi_conn_cancle_btn:
                finish();
                break;
            case R.id.wifi_conn_switch_btn:
                mConnList = new WifiConnListAdapter(getApplicationContext(), getAllNetWorkList());
                wifiList.setAdapter(mConnList);
                break;
            case R.id.wifi_conn_scan_btn:
                if (isOpen) {
                    Toast.makeText(getApplicationContext(), "正在关闭wifi", Toast.LENGTH_SHORT).show();
                    if (mWifiAdmin.closeWifi()) {
                        Toast.makeText(getApplicationContext(), "wifi关闭成功", Toast.LENGTH_SHORT).show();
                        wifi_scan_btn.setText("打开wifi");
                        isOpen = false;
                    } else {
                        Toast.makeText(getApplicationContext(), "wifi关闭失败", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "正在打开wifi", Toast.LENGTH_SHORT).show();
                    if (mWifiAdmin.OpenWifi()) {
                        Toast.makeText(getApplicationContext(), "wifi打开成功", Toast.LENGTH_SHORT).show();
                        wifi_scan_btn.setText("关闭wifi");
                        isOpen = true;
                    } else {
                        Toast.makeText(getApplicationContext(), "wifi打开失败", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            default:
                break;
        }
    }

    private String initShowConn() {
        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String s = wifiInfo.getSSID() + "\n" + "IP地址:" + mWifiAdmin.ipIntToString(wifiInfo.getIpAddress()) + "\n" + "Mac地址:" + wifiInfo.getMacAddress();
        return s;
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
        // TODO Auto-generated method stub
        final String ssid = wifiElement.get(position).getSsid();
        AlertDialog.Builder dialog = new AlertDialog.Builder(WifiListActivity.this);
        final WifiConfiguration wifiConfiguration = mWifiAdmin.IsExsits(ssid);
        dialog.setTitle("是否连接");
        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (null == wifiConfiguration) {
                    setMessage(ssid);
                } else {
                    mWifiAdmin.Connect(wifiConfiguration);
                }
            }
        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub

            }
        }).setNeutralButton("移除", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                if (null != wifiConfiguration) {
                    int id = wifiConfiguration.networkId;
                    System.out.println("id>>>>>>>>>>" + id);
                    mWifiAdmin.removeNetworkLink(id);
                }
            }
        }).create();
        dialog.show();
    }

    private void setMessage(final String ssid) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(WifiListActivity.this);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout lay = (LinearLayout) inflater.inflate(R.layout.widget_wifi_pwd, null);
        dialog.setView(lay);
        final EditText pwd = (EditText) lay.findViewById(R.id.wifi_pwd_edit);
        dialog.setTitle(ssid);
        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub

                String pwdStr = pwd.getText().toString();
                boolean flag = mWifiAdmin.Connect(ssid, pwdStr, WifiAdmin.WifiCipherType.WIFICIPHER_WPA);
                if (flag) {
                    Toast.makeText(getApplicationContext(), "正在连接，请稍后", Toast.LENGTH_SHORT).show();
                } else {
                    showLog("链接错误");
                }
            }
        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub

            }
        }).create();
        dialog.show();
    }

    private ArrayList<WifiElement> getAllNetWorkList() {
        // 每次点击扫描之前清空上一次的扫描结果
        wifiElement.clear();
        // 开始扫描网络
        mWifiAdmin.startScan();
        list = mWifiAdmin.getWifiList();
        WifiElement element;
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                // 得到扫描结果
                mScanResult = list.get(i);
                element = new WifiElement();
                element.setSsid(mScanResult.SSID);
                element.setBssid(mScanResult.BSSID);
                element.setCapabilities(mScanResult.capabilities);
                element.setFrequency(mScanResult.frequency);
                element.setLevel(mScanResult.level);
                wifiElement.add(element);
            }
        }
        return wifiElement;
    }

    /**
     * 提示信息对话框
     *
     * @param msg
     */
    private void showLog(final String msg) {
        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                // TODO Auto-generated method stub
                super.onPostExecute(result);
                Dialog dialog = new AlertDialog.Builder(WifiListActivity.this).setTitle("提示").setMessage(msg).setNegativeButton("确定", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub

                    }
                }).create();// 创建
                // 显示对话框
                dialog.show();
            }

        }.execute();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        IntentFilter ins = new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        registerReceiver(netConnReceiver, ins);
    }

    private BroadcastReceiver netConnReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {

                if (checknet()) {
                    Log.d("111111>>>>>>>>>>", "成功");
                    showConn.setText("已连接：   " + initShowConn());
                } else {
                    Log.d("22222222>>>>>>>>>>", "失败");
                    showConn.setText("正在尝试连接：     " + initShowConn());

                }
            }
        }

    };

    /**
     * 获取网络
     */
    private NetworkInfo networkInfo;

    /**
     * 监测网络链接
     *
     * @return true 链接正常 false 链接断开
     */
    private boolean checknet() {
        ConnectivityManager connManager = (ConnectivityManager) this.getSystemService(CONNECTIVITY_SERVICE);
        // 获取代表联网状态的NetWorkInfo对象
        networkInfo = connManager.getActiveNetworkInfo();
        if (null != networkInfo) {
            return networkInfo.isAvailable();
        }
        return false;
    }

}
