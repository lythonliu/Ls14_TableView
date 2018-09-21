package com.dongnao.alvin.ls14_tableview;

import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Administrator on 2017/6/5 0005.
 * 适配器模式
 */

public interface BaseTableAdapter {
    public  int getRowCount();


    public  int getColmunCount();

    public View getView(int row,int comun, View convertView, ViewGroup parent);

    public int getWidth(int column);

    public int getHeight(int row);

    public int getItemViewType(int row, int column);

    public int getViewTypeCount();
}
