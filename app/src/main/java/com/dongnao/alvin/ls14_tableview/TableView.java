package com.dongnao.alvin.ls14_tableview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/6/5 0005.
 */

public class TableView   extends ViewGroup{
    private BaseTableAdapter adapter;

    private int currentX;
    private int currentY;

    private int scrollX;
    private int scrollY;
    //第一行
    private int firstRow;
    //第一列
    private int firstColumn;
    private int[] widths;
    private int[] heights;

    @SuppressWarnings("unused")
    private View headView;
    private List<View> rowViewList;
    private List<View> columnViewList;
    private List<List<View>> bodyViewTable;
    private int rowCount;
    private int columnCount;
    private int width;
    private int height;

    private  int minimumVelocity;

    private  int maximumVelocity;
    //需要重绘标志位
    private boolean needRelayout;
    private VelocityTracker velocityTracker;
    //滑动最小距离
    private int touchSlop;

    private Recycler recycler;
    public TableView(Context context) {
        this(context,null);
    }

    public TableView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public TableView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        ViewConfiguration  configuration=ViewConfiguration.get(context);
        this.touchSlop=configuration.getScaledTouchSlop();
        needRelayout=true;
        this.headView = null;
        this.rowViewList = new ArrayList<View>();
        this.columnViewList = new ArrayList<View>();
        this.bodyViewTable = new ArrayList<List<View>>();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean intercept=false;
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                currentX= (int) ev.getRawX();
                currentY= (int) ev.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                //
                int x2 = Math.abs(currentX - (int)ev.getRawX());
                int y2 = Math.abs(currentY - (int)ev.getRawY());
                if(x2>touchSlop||y2>touchSlop)
                {
                  intercept=true;
                }

                break;
        }


        return intercept;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }
    //摆放子控件
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (needRelayout || changed) {
            needRelayout=false;
            if (adapter == null) {
                return;
            }
            width=r-l;
            height=b-t;
            int left, top, right, bottom;
            right=Math.min(width,sumArray(widths));
            bottom=Math.min(height,sumArray(heights));
            //已经绘制了第一个
            headView=makeAndStep(0,0,0,0,widths[0],heights[0]);
            //left不能从0开始
            left=widths[0]-scrollX;

            //填充第一行  橘红色的部分
            for (int i=firstColumn;i<columnCount&&left<width;i++) {
                right=left+widths[i];
                View view=makeAndStep(0,i,left,0,right,heights[0]);
                rowViewList.add(view);
                //循环  赋值
                left=right;
            }
            top=heights[0]-scrollY;
    //        填充绿色的部分
            for(int i=firstRow;i<rowCount&&top<height;i++) {
                bottom=top+heights[i];
                View view=makeAndStep(i,0,0,top,widths[0],bottom);
                columnViewList.add(view);
                top=bottom;
            }
            //top被改变
            top=heights[0]-scrollY;
            for (int i=firstColumn;i<rowCount&&top<height;i++) {
                bottom=top+heights[i];
                left=widths[0]-scrollX;
                List<View> list=new ArrayList<>();
                for (int j=firstRow;j<columnCount&&left<width;j++) {
                    right=left+widths[j];
                    View view=makeAndStep(i,j,left,top,right,bottom);
                    list.add(view);
                    left=right;
                }
                //里层循环完成 添加到body
                bodyViewTable.add(list);
                //上一个子控件的下边界  赋值给下一个 上边界
                top=bottom;
            }
        }
    }
    //获取一个View
    private View makeAndStep(int row, int colmun, int left, int top, int right, int bottom) {
        View view=obtainView(row,colmun,right,right-left,bottom-top);
        //给子控件  边界
        view.layout(left,top,right,bottom);
        return  view;
    }
    //真正获取View
    private View obtainView(int row, int colmun, int right, int width, int height) {
        //得到当前控件的类型
        int itemType=adapter.getItemViewType(row,colmun);
        //从回收池 拿到一个View
        View reclyView=recycler.getRecyclerdView(itemType);
        //reclyView 可能为空
        View view=adapter.getView(row,colmun,reclyView,this);
        if (view == null) {
            throw new RuntimeException("view  不能为空");
        }
        //View不可能为空
        view.setTag(R.id.tag_type_view,itemType);
        view.setTag(R.id.tag_column,colmun);
        view.setTag(R.id.tag_row,row);
        view.measure(MeasureSpec.makeMeasureSpec(width,MeasureSpec.EXACTLY),MeasureSpec.makeMeasureSpec(height,MeasureSpec.EXACTLY));
        addTableView(view,row,colmun);
        return view;
    }

    private void addTableView(View view, int row, int colmun) {
        if (row == 0 && colmun == 0) {
            addView(view,getChildCount()-1);
        }else if(colmun==0||row == 0)
        {
            addView(view,getChildCount()-2);
        }else {
            addView(view);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //   int   32   最高二位 widthMode    30  widthSize
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        final int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int w = 0;
        int h=0;
        if (adapter != null) {
            this.rowCount=adapter.getRowCount();
            this.columnCount=adapter.getColmunCount();
            widths=new int[columnCount];
            for (int i=0;i<columnCount;i++) {
                //数组每一个元素  存放着    控件宽度
                widths[i]=adapter.getWidth(i);
            }
            heights=new int[rowCount];
            for (int i=0;i<rowCount;i++) {
                heights[i]=adapter.getHeight(i);
            }
            if (widthMode == MeasureSpec.AT_MOST) {
                w=Math.min(widthSize,sumArray(widths));
            }else {
                w=widthSize;
            }

            if (heightMode == MeasureSpec.AT_MOST) {
                h=Math.min(heightSize,sumArray(heights));
            }else {
                h=heightSize;
            }
        }
        setMeasuredDimension(w,h);
    }


    //计算数组的  总和
    private int sumArray(int array[]) {
        return sumArray(array, 0, array.length);
    }
    private int sumArray(int array[], int start, int end) {
        int sum = 0;
        end += start;
        for (int i = start; i < end; i++) {
            sum += array[i];
        }
        return sum;
    }
    public void setAdapter(BaseTableAdapter baseTableAdapter) {
        this.adapter=baseTableAdapter;
        this.recycler=new Recycler(baseTableAdapter.getViewTypeCount());
        scrollX = 0;
        scrollY = 0;
        firstColumn = 0;
        firstRow = 0;
        needRelayout=true;
        requestLayout();

    }
}
