package com.fyspring.stepcounter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class WindowUtils {

    private static Dialog maDialog;

    public static void showQuXiaoDialog(final Activity activity, final OnSuccesListener onSuccesListener)
    {
        if (maDialog != null)
        {
            maDialog.dismiss();
        }
        maDialog = new Dialog(activity, R.style.CustomDialog);
        View dialogView = View.inflate(activity, R.layout.quxiao_layout, null);
        EditText mbm_etxm = (EditText) dialogView.findViewById(R.id.mbm_etxm);
        EditText mbm_etdh = (EditText) dialogView.findViewById(R.id.mbm_etdh);
        TextView mbm_etxb = (TextView) dialogView.findViewById(R.id.mbm_etxb);
        EditText mbm_etnl = (EditText) dialogView.findViewById(R.id.mbm_etnl);
        RelativeLayout mbm_submit = (RelativeLayout)dialogView.findViewById(R.id.mbm_submit);
        maDialog.setContentView(dialogView);
        maDialog.setCancelable(false);
        mbm_etxb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WindowUtils.showSexChooseDialog(activity, new MeSexListener() {
                    @Override
                    public void MeSexLister(String sex) {
                        mbm_etxb.setText(sex);
                    }
                });

            }
        });
        mbm_submit.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (mbm_etxm.getText().toString().equals(""))
                {
                    ViewVibration.SetViewVibration(activity, mbm_etxm);
                    return;
                }
                if (mbm_etdh.getText().toString().equals(""))
                {
                    ViewVibration.SetViewVibration(activity, mbm_etdh);
                    return;
                }
                if (mbm_etxb.getText().toString().equals(""))
                {
                    Toast.makeText(activity,"请选择性别",Toast.LENGTH_LONG).show();
                    return;
                }
                if (mbm_etnl.getText().toString().equals(""))
                {
                    ViewVibration.SetViewVibration(activity, mbm_etnl);
                    return;
                }
                onSuccesListener.onSucces(mbm_etdh.getText().toString(),mbm_etxm.getText().toString(),mbm_etnl.getText().toString(),mbm_etxb.getText().toString());
                maDialog.dismiss();
            }
        });
        maDialog.setCanceledOnTouchOutside(false);// 设置点击屏幕Dialog不消失
        maDialog.show();
    }
    public static void showWindow(final Activity activity, final MeSexListener meSexListener)
    {
        // TODO Auto-generated method stub
        View contentView = View.inflate(activity, R.layout.change_sex_layout, null);
        Button xiangji = (Button) contentView.findViewById(R.id.duihuan_select_zhifubao_Btn);
        Button xiangce = (Button) contentView.findViewById(R.id.fabu_popuwindow_2_Lin);
        final Button quxiao = (Button) contentView.findViewById(R.id.fabu_popuwindow_quxiao_Lin);
        final LinearLayout close = (LinearLayout) contentView.findViewById(R.id.fabu_popuwindow_close_Lin);
        final PopupWindow popupWindow = new PopupWindow(contentView, ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        WindowUtils.setWindowBackgroundAlpha(activity, 0.5f);
        popupWindow.setAnimationStyle(R.style.pop_animation);
        popupWindow.showAtLocation(activity.findViewById(R.id.activity_main), Gravity.CENTER, 0, 0);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener()
        {
            @Override
            public void onDismiss()
            {
                WindowUtils.setWindowBackgroundAlpha(activity, 1f);
            }
        });
        xiangji.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (popupWindow != null && popupWindow.isShowing())
                {
                    popupWindow.dismiss();
                    WindowUtils.setWindowBackgroundAlpha(activity, 1f);

                    meSexListener.MeSexLister("男");
                }
            }
        });
        xiangce.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (popupWindow != null && popupWindow.isShowing())
                {
                    popupWindow.dismiss();
                    WindowUtils.setWindowBackgroundAlpha(activity, 1f);
                    /**
                     * 跳转到系统的相册方法
                     */
                    meSexListener.MeSexLister("女");


                }
            }
        });
        quxiao.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (popupWindow != null && popupWindow.isShowing())
                {
                    popupWindow.dismiss();
                    WindowUtils.setWindowBackgroundAlpha(activity, 1f);
                }
            }
        });
        close.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (popupWindow != null && popupWindow.isShowing())
                {
                    popupWindow.dismiss();
                    WindowUtils.setWindowBackgroundAlpha(activity, 1f);
                }
            }
        });

    }

    public static void showSexChooseDialog(final Activity activity, final MeSexListener meSexListener) {
         String[] sexArry = new String[]{"Female", "Male", "Others"};// 性别选择
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);// 自定义对话框
        builder.setSingleChoiceItems(sexArry, 0, new DialogInterface.OnClickListener() {// 2默认的选中

            @Override
            public void onClick(DialogInterface dialog, int which) {// which是被选中的位置
                meSexListener.MeSexLister(sexArry[which]);
                dialog.dismiss();// 随便点击一个item消失对话框，不用点击确认取消
            }
        });
        builder.show();
    }
    public static void setWindowBackgroundAlpha(Activity activity, float bgAlpha)
    {
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.alpha = bgAlpha; // 0.0-1.0
        activity.getWindow().setAttributes(lp);
    }
}
