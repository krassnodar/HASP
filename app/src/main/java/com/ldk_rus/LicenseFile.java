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

import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ActionBar.LayoutParams;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.safenet.patch.Product;

public class LicenseFile extends Activity 
{
    public static final int HASP_STATUS_OK              = 0;

    /**
     * Unrecognized info format
     */
    public static final int HASP_INV_FORMAT             = 15;

    /**
     * Invalid XML scope
     */
    public static final int HASP_INV_SCOPE              = 36;

    /**
     * Specified v2c update already installed in the LLM
     */
    public static final int HASP_UPDATE_ALREADY_ADDED   = 65;

    /**
     * Trying to install a V2C file with an update counter that is out of
     * sequence with update counter in Sentinel HASP protection key. The first
     * value in the V2C file is greater than the value in Sentinel HASP
     * protection key.
     */
    public static final int HASP_UPDATE_TOO_NEW         = 55;

    /**
     * Secure storage ID mismatch
     */
    public static final int HASP_SECURE_STORE_ID_MISMATCH = 78;	

    public static final String scope = new String(
        "<haspscope>\n" +
        " <license_manager hostname=\"localhost\" />\n" +
        "</haspscope>\n");
    public static final String host_fingerprint = new String("<haspformat format=\"host_fingerprint\"/>");
    public static final String updateinfo = new String("<haspformat format=\"updateinfo\"/>");

    TextView tv;
    String filePath = null;

    private static final int READ_REQUEST_CODE = 1;
    private static final int WRITE_REQUEST_CODE = 2;

    public static String text = "";
    public static boolean fromParent = true;
    public static boolean showMsg = false;
    private static native byte[] getinfo(String scope, String format, int status[]);
    private static native String update(String update_data, int status[]);
    private String c2vInfo = "";

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        RelativeLayout layout = new RelativeLayout(this);
        super.setContentView(layout);

        final TextView title = new TextView(this);
        title.setId(1);
        title.setText(Html.fromHtml("<b>Sentinel Protection System</b><br>"));
        title.append(Html.fromHtml("Click <b>Collect Information</b> to collect system information and send it to the vendor. The vendor will send you a license update file (update.v2c).<br>"));
        title.append(Html.fromHtml("Click <b>Apply Update</b> to install your license when you receive the update.v2c file.<br>"));
        RelativeLayout.LayoutParams titleLP = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
        titleLP.leftMargin=10;
        titleLP.topMargin=5;

        Button c2v = new Button(this);
        c2v.setId(2);
        c2v.setText("Collect Information");
        RelativeLayout.LayoutParams c2vLP = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        c2vLP.topMargin = 16;
        c2vLP.leftMargin = 3;
        c2vLP.addRule(RelativeLayout.BELOW, 1);


        Button update  = new Button(this);
        update.setText("Apply Update");
        update.setId(3);
        RelativeLayout.LayoutParams updateLP = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        updateLP.leftMargin = 3;
        updateLP.addRule(RelativeLayout.BELOW, 2);
        //updateLP.addRule(RelativeLayout.RIGHT_OF, 2);
        //updateLP.addRule(RelativeLayout.ALIGN_BASELINE, 2);

        tv = new TextView(this);
        tv.setId(4);
        tv.setMovementMethod(new ScrollingMovementMethod());
        RelativeLayout.LayoutParams tvLP = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        tvLP.topMargin = 20;
        tvLP.leftMargin = 10;
        tvLP.addRule(RelativeLayout.BELOW, 3);

        layout.addView(title, titleLP);
        layout.addView(c2v, c2vLP);
        layout.addView(update, updateLP);
        layout.addView(tv, tvLP);		

        // come from parent activity, not set the text, otherwise set it
        if (fromParent)
        {
            fromParent = false;
        }
        else
        {
            tv.setText(text);
            if (showMsg)
            {
                comeback();
            }
        }

        c2v.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v) 
            {
                collectInformation();
            }
        });

        update.setOnClickListener(new OnClickListener()
        {
            @Override
            @SuppressWarnings("rawtypes")
            public void onClick(View v)
            {
                v2cFileSearch();
            }
        });
    }

    public void v2cFileSearch() {

        // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file browser.
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        // To search for all documents available via installed storage providers,
        // it would be "*/*".
        intent.setType("*/*");

        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    private String readv2cFromUri(Uri uri) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        try (
                InputStream inputStream = getContentResolver().openInputStream(uri);
                BufferedReader reader = new BufferedReader(
                     new InputStreamReader(Objects.requireNonNull(inputStream)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
        }
        return stringBuilder.toString();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) 
    {
        if ( resultCode == RESULT_OK )
        {
            if ( requestCode == READ_REQUEST_CODE )
            {
                if (intent != null && intent.getData() != null) {
                    try {
                        String v2cContent = readv2cFromUri(intent.getData());
                        updateLicense(v2cContent);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (requestCode == WRITE_REQUEST_CODE) {
                if (intent != null && intent.getData() != null) {
                    writeToFile(intent.getData(), c2vInfo);
                }
            }
        }
    }

    public void collectInformation()
    {
         String format;
        int status;

        text = "Collect information: ";

        Bundle bundle = this.getIntent().getExtras();
        if ( bundle == null )
        {
            format = host_fingerprint;
        }
        else
        {
            boolean b = bundle.getBoolean("newSL", false);
            if ( b )
            {
                // to get fingerprint for install a new SL
                format = host_fingerprint;
            }
            else
            {
                // to get C2V for update an existing protection key (SL or HL)
                format = updateinfo;
            }
        }

        Product product = new Product();
        // call GetInfo to get fingerprint or c2v
        c2vInfo = product.GetInfo(scope, format);
        status = product.getLastError();
        switch (status) 
        {
        case HASP_STATUS_OK:
            text += "OK\n";
            break;
        case HASP_INV_FORMAT:
            text += "Invalid format";
            break;
        case HASP_INV_SCOPE:
            text += "Invalid scope";
            break;
        default:
            text += "Failed with status:" + status;
            break;
        }

        if ( status == HASP_STATUS_OK ) 
        {
            createFile();
        } 

        tv.setText(text);
    }

    private void createFile() {
        // when you create document, you need to add Intent.ACTION_CREATE_DOCUMENT
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);

        // filter to only show openable items.
       // intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/*");

        // Create a file with the requested Mime type
        intent.putExtra(Intent.EXTRA_TITLE, "request.c2v");

        startActivityForResult(intent, WRITE_REQUEST_CODE);
    }

    private void writeToFile(Uri uri, String content) {
        OutputStream outputStream;
        try {
            outputStream = getContentResolver().openOutputStream(uri);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(outputStream));
            bw.write(content);
            bw.flush();
            bw.close();
            text += "Write request.c2v successfully!\n";
            tv.setText(text);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateLicense(String v2c)
    {
        text = "";
        int status = -1;
        if ( v2c == null )
        {
            text += "Can't load v2c file";
        }
        else
        {
            text += "Apply update: ";  

            Product product = new Product();
            // call Update to apply v2c
            product.Update(v2c);
            status = product.getLastError();
            switch (status)
            {
            case HASP_STATUS_OK:
                text += "OK";
                break;
            case HASP_UPDATE_ALREADY_ADDED:
                text += "Update already added";
                break;
            case HASP_UPDATE_TOO_NEW:
                text += "Update too new";
                break;
            case HASP_SECURE_STORE_ID_MISMATCH:
                text += "Secure storage ID mismatch occurred";
                break;
            default:
                text += "Failed with status:" + status;
                break;
            }
        }

        text += "\n";
        tv.setText(text);
        if(status == HASP_STATUS_OK)
        {
            comeback();
        }
    }

    public void comeback()
    {
        showMsg = true;

        new AlertDialog.Builder(this)  
            .setTitle("Update Succeeded")
            .setMessage("Your product license has been updated successfully.\n\nClick OK to start your application.")
            .setPositiveButton("OK", new DialogInterface.OnClickListener() 
        {
            public void onClick(DialogInterface dialog, int which) 
            {
                showMsg = false;
                finish();
                ldk_rus.instance.finish();
            }
        })
            .show();
    }
}
