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
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.safenet.patch.Product;

public class ProductKey extends Activity
{
    public static final int HASP_STATUS_OK              = 0;

    public static final int HASP_INV_FORMAT             = 15;

    public static final int HASP_INV_SCOPE              = 36;

    public static final int MAX_WIDTH_PIXELS            = 500;

    public static final String scope = new String(
        "<haspscope>\n" +
        " <license_manager hostname=\"localhost\" />\n" +
        "</haspscope>\n");

    public static final String host_fingerprint = new String("<haspformat format=\"host_fingerprint\"/>");

    public static final String updateinfo = new String("<haspformat format=\"updateinfo\"/>");

    /**the jsessionid for initiating WS calls to the EMS server*/
    protected String jSessionId = null;

    /**flag indicating if registration is needed*/
    protected int registrationStat = 0;
    protected String redirectToUserReg = "";

    /**set the URL here or input the URL manual*/
    public String url = "";
    public String pk = ""; 

    ActivateThread activateThread = null;

    TextView resultTV;
    public static String resText = "";
    public static boolean fromParent = true;
    public static boolean showMsg = false;

    public static ProductKey instance = null;
    private RelativeLayout layout = null;
    private ProgressBar pb = null;

    private EditText ipET           = null;
    private EditText portET         = null;
    private CheckBox sslCB          = null;

    public static final String SETTINGS         = "settings";
    public static final String IPHOST           = "ip";
    public static final String PORT             = "port";
    public static final String SSLSTATUS        = "SSL";

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        layout = new RelativeLayout(this);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.setContentView(layout);
        instance = this;

        DisplayMetrics dm = getResources().getDisplayMetrics();
        int w = dm.widthPixels;
        int h = dm.heightPixels;

        int topMargin = 0;

        if (w < h && w < MAX_WIDTH_PIXELS)
        {
            topMargin = 20;
        }
        else if (h < w && h < MAX_WIDTH_PIXELS)
        {
            topMargin = 20;
        }
        else
        {
            topMargin = 40;
        }

        final TextView title = new TextView(this);
        title.setText(Html.fromHtml("<b>Sentinel Protection System</b><br>"));
        title.setId(1);
        RelativeLayout.LayoutParams titleLP = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
        titleLP.leftMargin = 10;
        titleLP.topMargin = 5;

        final TextView ipTV = new TextView(this);
        ipTV.setText("IP address/Host name:");
        ipTV.setId(2);
        RelativeLayout.LayoutParams ipTVLP = new RelativeLayout.LayoutParams(160, LayoutParams.WRAP_CONTENT);
        ipTVLP.leftMargin=20;
        ipTVLP.addRule(RelativeLayout.BELOW, 1);

        ipET = new EditText(this);
        ipET.setHint("For example: 192.168.1.123");
        ipET.setId(3);
        ipET.setSingleLine();
        ipET.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        RelativeLayout.LayoutParams ipETLP = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        ipETLP.leftMargin=20;
        ipETLP.addRule(RelativeLayout.BELOW, 1);
        ipETLP.addRule(RelativeLayout.RIGHT_OF, 2);
        ipETLP.addRule(RelativeLayout.ALIGN_BASELINE, 2);

        final TextView portTV = new TextView(this);
        portTV.setText("Port:");
        portTV.setId(4);
        RelativeLayout.LayoutParams portTVLP = new RelativeLayout.LayoutParams(160, LayoutParams.WRAP_CONTENT);
        portTVLP.leftMargin=20;
        portTVLP.topMargin=topMargin;
        portTVLP.addRule(RelativeLayout.BELOW, 2);

        portET = new EditText(this);
        portET.setText("8080");
        portET.setId(5);
        portET.setSingleLine();
        portET.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        RelativeLayout.LayoutParams portETLP = new RelativeLayout.LayoutParams(160, LayoutParams.WRAP_CONTENT);
        portETLP.leftMargin=20;
        portETLP.topMargin=topMargin;
        portETLP.addRule(RelativeLayout.BELOW, 3);
        portETLP.addRule(RelativeLayout.RIGHT_OF, 4);
        portETLP.addRule(RelativeLayout.ALIGN_BASELINE, 4);

        final TextView sslTV = new TextView(this);
        sslTV.setText("SSL:");
        sslTV.setId(6);
        RelativeLayout.LayoutParams sslTVLP = new RelativeLayout.LayoutParams(160, LayoutParams.WRAP_CONTENT);
        sslTVLP.leftMargin=20;
        sslTVLP.topMargin=topMargin;
        sslTVLP.addRule(RelativeLayout.BELOW, 4);

        sslCB = new CheckBox(this);
        sslCB.setId(7);
        sslCB.setChecked(false);
        RelativeLayout.LayoutParams sslCBLP = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        sslCBLP.leftMargin=20;
        sslCBLP.topMargin=topMargin;
        sslCBLP.addRule(RelativeLayout.BELOW, 5);
        sslCBLP.addRule(RelativeLayout.RIGHT_OF, 6);
        sslCBLP.addRule(RelativeLayout.ALIGN_BASELINE, 6);

        final TextView pkTV = new TextView(this);
        pkTV.setText("Product Key:");
        pkTV.setId(8);
        RelativeLayout.LayoutParams pkTVLP = new RelativeLayout.LayoutParams(160, LayoutParams.WRAP_CONTENT);
        pkTVLP.leftMargin=20;
        pkTVLP.topMargin=topMargin;
        pkTVLP.addRule(RelativeLayout.BELOW, 6);

        final EditText pkET = new EditText(this);
        pkET.setHint("Enter your product key here");
        pkET.setId(9);
        pkET.setSingleLine();
        pkET.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        RelativeLayout.LayoutParams pkETLP = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        pkETLP.leftMargin=20;
        pkETLP.topMargin=topMargin;
        pkETLP.addRule(RelativeLayout.BELOW, 7);
        pkETLP.addRule(RelativeLayout.RIGHT_OF, 8);
        pkETLP.addRule(RelativeLayout.ALIGN_BASELINE, 8);

        final Button activate = new Button(this);
        activate.setText("Activate");
        activate.setId(10);
        RelativeLayout.LayoutParams activeLP = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        activeLP.topMargin=15;
        activeLP.addRule(RelativeLayout.BELOW, 8);
        activeLP.addRule(RelativeLayout.CENTER_HORIZONTAL);

        resultTV = new TextView(this);
        resultTV.setId(11);
        RelativeLayout.LayoutParams resultLP = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        resultLP.topMargin=2;
        resultLP.leftMargin=20;
        resultLP.addRule(RelativeLayout.BELOW, 10);
        resultLP.addRule(RelativeLayout.CENTER_HORIZONTAL);
        resultTV.setMovementMethod(new ScrollingMovementMethod());

        layout.addView(title, titleLP);
        layout.addView(ipTV, ipTVLP);
        layout.addView(ipET, ipETLP);
        layout.addView(portTV, portTVLP);
        layout.addView(portET, portETLP);
        layout.addView(sslTV, sslTVLP);
        layout.addView(sslCB, sslCBLP);
        layout.addView(pkTV, pkTVLP);
        layout.addView(pkET, pkETLP);
        layout.addView(activate, activeLP);
        layout.addView(resultTV, resultLP);

        initView();

        // come from parent activity, not set the text, otherwise set it
        if (fromParent)
        {
            fromParent = false;
        }
        else
        {
            resultTV.setText(resText);
            if (showMsg)
            {
                showDialog();
            }
        }

        activate.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v) 
            {
                saveSettings();

                Data.initMsg();
                resText = "";
                resultTV.setText(resText);

                String ip  = ipET.getText().toString();
                if ( ip.isEmpty() )
                {
                    resText = "Please input the IP address or host name first.";
                    resultTV.setText(resText);
                    return;
                }
                else
                {
                    ip = ip.trim();
                }

                String port = portET.getText().toString();
                if ( port.isEmpty() )
                {
                    resText = "Please specify the port.";
                    resultTV.setText(resText);
                    return;
                }
                else
                {
                    port = port.trim();
                }

                boolean bSSL = sslCB.isChecked();
                if ( bSSL )
                {
                    url = "https://" + ip + ":" + port;
                }
                else
                {
                    url = "http://" + ip + ":" + port;
                }

                pk = pkET.getText().toString();
                if ( pk.isEmpty() )
                {
                    resText = "Please input the Product Key first.";
                    resultTV.setText(resText);
                    return;
                }
                else
                {
                    pk = pk.trim();
                }

                activate();
            }
        });
    }


    /**
     * This method does the following operation which are needed for activating:<br/>
     * 1. Get fingerprint or Reads the C2V of the key.<br/>
     * 2. Sends the fingerprint or C2V to the server and receives the license.</br>
     * 3. Install the license or updates the key with the license.
     * 
     */
    protected boolean doActivation()
    {
        boolean r = false;

        pk = pk.trim();

        String c2v = collectInformation();

        boolean ispassedServer = false;

        if ( c2v != null )
        {
            try
            {
                String v2c = Activation.getLicense(url, jSessionId, c2v, pk);

                ispassedServer = true;

                if ( v2c.contains("<hasp_info>") )
                {
                    r = Activation.updateKeyWithLicense(v2c);

                    Data.appendMsg("Key updated successfully.");
                }
            }
            catch(Exception e)
            {
                if ( ispassedServer )
                {
                    Data.appendMsg("Runtime Error: " + e.getMessage());
                }
                else
                {
                    Data.appendMsg("EMS Error: " + e.getMessage());
                }

                r = false;
            }
        }
        else
        {
            r = false;
        }

        return r;
    }


    /**
     * Logs in with product key and sets the jsessionid and the registration required flag
     * @return true if login succeeded
     * @throws Exception
     */
    protected boolean customerLogin()
    {
        //logging in
        String[] loginRes = null;

        try 
        {
            loginRes = Activation.customerLogin(url, pk);

            //validate login response
            if ( loginRes[0].isEmpty() )
            {
                Data.appendMsg("Error Login failed. No login data returned from server.");
                return false;
            }

            //validate and set jsessionid and registration flag
            jSessionId = loginRes[0];
            if ( jSessionId == null )
            {
                Data.appendMsg("Error Login failed. Session not created.");
                return false;
            }

            if ( loginRes[1] != null )
            {
                registrationStat = Integer.parseInt(loginRes[1]);
            }

            if( loginRes[2] != null )
            {
                redirectToUserReg = loginRes[2];
            }
        }
        catch (Exception e) 
        {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * Get fingerprint for install SL or get C2V for update protection key
     * @return fingerprint or C2V if successful
     */	
    public String collectInformation()
    {
        String info;
        String text;
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
                format = host_fingerprint;
            }
            else
            {
                format = updateinfo;
            }
        }

        Product product = new Product();
        info = product.GetInfo(scope, format);
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
            return info;
        } 
        else
        {
            Data.appendMsg(text);
            return null;
        }
    }

    public void showProgrssBar()
    {
        pb = new ProgressBar(this, null, android.R.attr.progressBarStyleLarge);
        pb.setIndeterminate(true);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, 150);
        params.addRule(RelativeLayout.CENTER_VERTICAL);
        layout.addView(pb, params);
    }

    public void closeProgressBar()
    {
        pb.setVisibility(View.GONE);
    }

    public void activate()
    {
        activateThread = new ActivateThread();
        activateThread.start();
        showProgrssBar();
    }

    class ActivateThread extends Thread
    {
        @Override
        public void run()
        {
            boolean r = false;
            EventHandler handler;

            handler = new EventHandler(Looper.getMainLooper());  

            try
            {
                r = customerLogin();

                if(  r )
                {
                    r = doActivation();

                    if ( r )
                    {
                        Data.appendMsg("Activation successfully.");
                    }
                    else
                    {
                    }
                }
            }
            catch(Exception e)
            {
                Data.appendMsg("Error activating: " + e.getMessage());
            }

            handler.removeMessages(0);
            Message msg;

            if ( r )
            {
                msg = handler.obtainMessage(1, 1, 1, Data.getMsg());
            }
            else
            {
                msg = handler.obtainMessage(2, 1, 1, "Activation failed, detailed information as follows:\n\n" + Data.getMsg());
            }

            handler.sendMessage(msg);  
        }
    }


    public class EventHandler extends Handler
    {
        public EventHandler(Looper looper)
        {
            super(looper);
        }

        @Override  
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);

            closeProgressBar();

            switch(msg.what)
            {
            case 1:
                showDialog();
                break;
            case 2:
                resText = (String)msg.obj;
                resultTV.setText(resText);
                break;
            default:
                break;
            }

        }

    };

    public void showDialog()
    {
        showMsg = true;
        new AlertDialog.Builder(ProductKey.instance)
            .setTitle("Update Succeeded")
            .setMessage("Your product license has been updated successfully.\n\nClick OK to start your application.")
            .setPositiveButton("OK", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                showMsg = false;
                ProductKey.instance.finish();
                ldk_rus.instance.finish();
            }
        })
            .show();
    }

    public void initView()
    {
        SharedPreferences preferences = getSharedPreferences(SETTINGS, MODE_PRIVATE);

        if ( preferences == null )
        {
            return;
        }

        String ip = preferences.getString(IPHOST, null);
        String port = preferences.getString(PORT, null);
        boolean bSSL = preferences.getBoolean(SSLSTATUS, false);

        ipET.setText(ip);

        if ( port != null && port.length() > 0 )
        {
            portET.setText(port);
        }

        sslCB.setChecked(bSSL);
    }

    public void saveSettings()
    {
        String ip       = ipET.getText().toString();

        String port     = portET.getText().toString();

        boolean bSSL    = sslCB.isChecked();

        SharedPreferences preferences = getSharedPreferences(SETTINGS, MODE_PRIVATE);

        Editor editor = preferences.edit();

        editor.putString(IPHOST, ip);

        editor.putString(PORT, port);

        editor.putBoolean(SSLSTATUS, bSSL);

        editor.commit();
    }
}
