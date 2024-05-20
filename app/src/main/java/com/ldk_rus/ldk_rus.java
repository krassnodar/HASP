/*****************************************************************************
*
* RUS program for protected application
*
* Copyright (C) 2021 Thales Group. All rights reserved.
*
* Please export this class to JAR file
*
*****************************************************************************/

package com.ldk_rus;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.lang.reflect.Field;

public class ldk_rus extends Activity
{
    public static ldk_rus instance = null;
    public boolean bFP = true; 

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        RelativeLayout layout = new RelativeLayout(this);
        super.setContentView(layout);
        instance = this;

        final TextView title = new TextView(this);
        title.setText(Html.fromHtml("<b>Sentinel Protection System</b><br>"));
        title.append("The required license was not found. This wizard will guide you through the license installation process.\n");
        title.append(Html.fromHtml("If you have received a product key from the vendor, select the <b>Product Key</b> option below. Otherwise, select the <b>License File</b> option."));
        title.append("\n\nSelect the license update method:");
        title.setId(1);
        RelativeLayout.LayoutParams titleLP = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
        titleLP.leftMargin = 10;
        titleLP.topMargin = 5;

        final RadioGroup rg = new RadioGroup(this.getApplicationContext());
        rg.setId(2);
        RadioButton pk = new RadioButton(this);
        pk.setText("Product Key");
        pk.setId(3);
        pk.setChecked(true);
        RadioButton lf = new RadioButton(this);
        lf.setText("License File");
        lf.setId(4);
        rg.addView(pk);
        rg.addView(lf);

        RelativeLayout.LayoutParams rgLP = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
        rgLP.topMargin = 0;
        rgLP.leftMargin = 30;
        rgLP.addRule(RelativeLayout.BELOW, 1);

        Button next = new Button(this);
        next.setText("Next");
        RelativeLayout.LayoutParams nextLP = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        nextLP.topMargin = 2;
        nextLP.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        nextLP.addRule(RelativeLayout.BELOW, 2);       

        layout.addView(title, titleLP);
        layout.addView(rg, rgLP);
        layout.addView(next, nextLP);

        next.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v) 
            {
                int index = rg.getCheckedRadioButtonId();

                if ( index == 3 )
                {
                    nextPage("com.ldk_rus.ProductKey");
                }
                else
                {
                    nextPage("com.ldk_rus.LicenseFile");
                }
            }
        });

        Intent in = this.getIntent();
        if ( in != null )
        {
            Bundle bundle = this.getIntent().getExtras();        
            if ( bundle == null )
            {
                bFP = true;
            }
            else
            {
                boolean b = bundle.getBoolean("newSL", false);
                if ( b )
                {
                    // to get fingerprint for install a new SL
                    bFP = true;
                }
                else
                {
                    // to get C2V for update an existing protection key (SL or HL)
                    bFP = false;
                }
            }
        }

    }

    @SuppressWarnings("rawtypes")
    public void nextPage(String cn)
    {
        Intent intent = new Intent();
        intent.setClassName(getApplication(), cn);

        Bundle bundle = new Bundle();
        //pass newSL to the next activity
        bundle.putBoolean("newSL", bFP);
        intent.putExtras(bundle);

        // set the field fromParent to indicate whether the next page is start from here
        try
        {
            Class c = Class.forName(cn);
            Field fromParent = c.getField("fromParent");
            fromParent.setBoolean(null, true);
        }
        catch(Exception e)
        {
        }

        startActivity(intent);
    }

}
