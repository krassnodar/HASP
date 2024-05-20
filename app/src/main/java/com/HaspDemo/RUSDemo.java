/*****************************************************************************
*
* Demo program for Sentinel LDK licensing services
*
*
* Copyright (C) 2021 Thales Group. All rights reserved.
*
*****************************************************************************/

package com.HaspDemo;

import android.Manifest;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class RUSDemo extends Activity
{
    RelativeLayout layout;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        layout = new RelativeLayout(this);
        super.setContentView(layout);

        String strTitle = "This program demonstrates the use of Sentinel LDK licensing functions and the license update process.<br>Copyright (C) Thales Group. All rights reserved.<br><br>";
        strTitle += "To activate your initial SL license, click <b>New SL Key</b>.<br>";
        strTitle += "To apply subsequent licenses, click <b>Update License</b>.";

        final TextView title = new TextView(this);
        title.setText(Html.fromHtml(strTitle));
        title.setId(1);
        RelativeLayout.LayoutParams titleLP = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        titleLP.leftMargin = 10;
        titleLP.topMargin  = 2;

        final Button newSLBt = new Button(this);
        newSLBt.setText("New SL Key");
        newSLBt.setId(2);
        RelativeLayout.LayoutParams newSLBtLP = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        newSLBtLP.topMargin = 40;
        newSLBtLP.leftMargin = 30;
        newSLBtLP.addRule(RelativeLayout.BELOW, 1);

        final Button updateBt = new Button(this);
        updateBt.setText("Update License");
        updateBt.setId(3);
        RelativeLayout.LayoutParams updateBtLP = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        updateBtLP.topMargin = 40;
        updateBtLP.leftMargin = 60;
        updateBtLP.addRule(RelativeLayout.RIGHT_OF, 2);
        updateBtLP.addRule(RelativeLayout.ALIGN_BASELINE, 2);

        layout.addView(title, titleLP);
        layout.addView(newSLBt, newSLBtLP);
        layout.addView(updateBt, updateBtLP);

        newSLBt.setOnClickListener(new OnClickListener()
        {
            @Override
                public void onClick(View v) 
            {
                invoke_ldkrus(true);
            }
        });

        updateBt.setOnClickListener(new OnClickListener()
        {
            @Override
                public void onClick(View v) 
            {
                invoke_ldkrus(false);
            }
        });

    }

    // Set proper value to newSL and invoke RUS activity
    public void invoke_ldkrus(boolean bPara)
    {
        Intent intent = new Intent();
        intent.setClassName(getApplication(), "com.ldk_rus.ldk_rus");

        Bundle bundle = new Bundle();
        bundle.putBoolean("newSL", bPara);
        intent.putExtras(bundle);
        startActivity(intent);
    }

}
