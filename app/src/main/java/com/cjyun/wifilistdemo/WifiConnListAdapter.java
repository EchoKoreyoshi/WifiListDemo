package com.cjyun.wifilistdemo;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * 创建者: vee
 * 时 间: 2016/8/8 0008
 * 描 述： TODO
 */
public class WifiConnListAdapter extends BaseAdapter {

    private static final String TAG = "vee";
    private LayoutInflater inflater;
    private ArrayList<WifiElement> mArr;

    public WifiConnListAdapter(Context context, ArrayList<WifiElement> list) {
        this.inflater = LayoutInflater.from(context);
        this.mArr = list;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return mArr.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return mArr.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        View view = inflater.inflate(R.layout.widget_wifi_conn_lv, null);
        TextView ssid = (TextView) view.findViewById(R.id.wifi_conn_name);
        TextView wpe = (TextView) view.findViewById(R.id.wifi_conn_wpe);
        ImageView level = (ImageView) view.findViewById(R.id.wifi_conn_level);

        ssid.setText(mArr.get(position).getSsid());
        //截取一段字符串  只要第一个【】号里面的内容
        String[] mWPE = mArr.get(position).getCapabilities().split("\\[");
        String[] s = mWPE[1].split("\\]");
        String s1 = s[0];
        Log.i(TAG, s1);

        wpe.setText("通过" + s1 + "保护");

        int i = abs(mArr.get(position).getLevel());
        if (i <= 50) {
            level.setBackgroundResource(R.mipmap.wifi_strength_5);
        } else if (i > 50 && i <= 65) {
            level.setBackgroundResource(R.mipmap.wifi_strength_4);
        } else if (i > 65 && i <= 75) {
            level.setBackgroundResource(R.mipmap.wifi_strength_3);
        } else if (i > 75 && i <= 90) {
            level.setBackgroundResource(R.mipmap.wifi_strength_2);
        } else {
            level.setBackgroundResource(R.mipmap.wifi_strength_1);
        }
        // level.setText(String.valueOf(mArr.get(position).getLevel()));
        return view;
    }

    /**
     * 绝对值
     *
     * @param num
     * @return
     */
    private int abs(int num) {
        return num * (1 - ((num >>> 31) << 1));
    }

}
