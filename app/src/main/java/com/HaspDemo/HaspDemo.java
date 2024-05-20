/*****************************************************************************
*
* Demo program for Sentinel LDK licensing services
*
*
* Copyright (C) 2021 Thales Group. All rights reserved.
*
*
* Sentinel DEMOMA key required with features 0
*
*****************************************************************************/

package com.HaspDemo;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.Aladdin.Hasp;
import com.Aladdin.HaspApiVersion;
import com.Aladdin.HaspStatus;
import com.Aladdin.HaspTime;
import com.Aladdin.HaspUsbHandler;


public class HaspDemo extends Activity 
{
    public static final int DEMO_MEMBUFFER_SIZE = 128;

    public static final String vendorCode = new String(
        "AzIceaqfA1hX5wS+M8cGnYh5ceevUnOZIzJBbXFD6dgf3tBkb9cvUF/Tkd/iKu2fsg9wAysYKw7RMA" +
        "sVvIp4KcXle/v1RaXrLVnNBJ2H2DmrbUMOZbQUFXe698qmJsqNpLXRA367xpZ54i8kC5DTXwDhfxWT" +
        "OZrBrh5sRKHcoVLumztIQjgWh37AzmSd1bLOfUGI0xjAL9zJWO3fRaeB0NS2KlmoKaVT5Y04zZEc06" +
        "waU2r6AU2Dc4uipJqJmObqKM+tfNKAS0rZr5IudRiC7pUwnmtaHRe5fgSI8M7yvypvm+13Wm4Gwd4V" +
        "nYiZvSxf8ImN3ZOG9wEzfyMIlH2+rKPUVHI+igsqla0Wd9m7ZUR9vFotj1uYV0OzG7hX0+huN2E/Id" +
        "gLDjbiapj1e2fKHrMmGFaIvI6xzzJIQJF9GiRZ7+0jNFLKSyzX/K3JAyFrIPObfwM+y+zAgE1sWcZ1" +
        "YnuBhICyRHBhaJDKIZL8MywrEfB2yF+R3k9wFG1oN48gSLyfrfEKuB/qgNp+BeTruWUk0AwRE9XVMU" +
        "uRbjpxa4YA67SKunFEgFGgUfHBeHJTivvUl0u4Dki1UKAT973P+nXy2O0u239If/kRpNUVhMg8kpk7" +
        "s8i6Arp7l/705/bLCx4kN5hHHSXIqkiG9tHdeNV8VYo5+72hgaCx3/uVoVLmtvxbOIvo120uTJbuLV" +
        "TvT8KtsOlb3DxwUrwLzaEMoAQAFk6Q9bNipHxfkRQER4kR7IYTMzSoW5mxh3H9O8Ge5BqVeYMEW36q" +
        "9wnOYfxOLNw6yQMf8f9sJN4KhZty02xm707S7VEfJJ1KNq7b5pP/3RjE0IKtB2gE6vAPRvRLzEohu0" +
        "m7q1aUp8wAvSiqjZy7FLaTtLEApXYvLvz6PEJdj4TegCZugj7c8bIOEqLXmloZ6EgVnjQ7/ttys7VF" +
        "ITB3mazzFiyQuKf4J6+b/a/Y");

    public static final String scope = new String(
        "<haspscope>\n" +
        " <license_manager hostname=\"localhost\" />\n" +
        "</haspscope>\n");

    public static final String accessibleKeys = new String(
        "<haspformat root=\"hasp_info\">" + 
        "    <hasp>" + 
        "        <attribute name=\"id\" />" + 
        "        <attribute name=\"type\" />" + 
        "        <feature>" + 
        "            <attribute name=\"id\" />" + 
        "        </feature>" + 
        "    </hasp>" + 
        "</haspformat>");

    public static final String host_fingerprint = new String("<haspformat format=\"host_fingerprint\"/>");

    public static final String updateinfo = new String("<haspformat format=\"updateinfo\"/>");

    public static final String keyinfo = new String(
        "<haspformat root=\"hasp_info\">" + 
        "    <hasp>" + 
        "        <element name=\"id\" /> " + 
        "        <element name=\"type\" /> " + 
        "        <element name=\"configuration\" /> " + 
        "    </hasp>" + 
        "</haspformat>");

    public static final byte[] data = { 0x74, 0x65, 0x73, 0x74, 0x20, 0x73, 0x74, 0x72,
        0x69, 0x6e, 0x67, 0x20, 0x31, 0x32, 0x33, 0x00 };
    private static HaspTime datetime;

    TextView textView;

    String filePath = null;

    /**
     * Broadcast receiver for the USB permission requests sent
     * when calling HaspUSBHandler.getPermission().
     * 
     * It's implemented following the the Android documentation at:
     * http://developer.android.com/guide/topics/connectivity/usb/host.html
     */
    private BroadcastReceiver usbReceiver = new BroadcastReceiver()
    {        
        @Override
            public void onReceive(Context context, Intent intent) 
        {
            String action = intent.getAction();

            if ( HaspUsbHandler.ACTION_USB_PERMISSION.equals(action) ) 
            {
                synchronized (this) 
                {
                    UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if ( intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false) )
                    {
                        if ( device != null )
                        {
                            System.out.println("Permissions granted for USB device" + device);
                        }
                    }
                    else
                    {
                        System.out.println("Permission denied for USB device " + device);
                    }
                }
            }
        }
    }; 

    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);

        /* 
        * Create and register the broadcast receiver to get
        * the user answer at the permission request popup.
        */
        Context context = getApplicationContext();
        IntentFilter filter = new IntentFilter(HaspUsbHandler.ACTION_USB_PERMISSION);
        filter.addAction(HaspUsbHandler.ACTION_USB_PERMISSION);
        context.registerReceiver(usbReceiver, filter);

        setContentView(R.layout.activity_demo);
        textView = (TextView)findViewById(R.id.tv);
        textView.setMovementMethod(new ScrollingMovementMethod());

		/*
		 * Don't try to use the USB in the Android emulator.
         *
		 * In the Android 5 emulator, the UsbManager.getDeviceList() call fails internally 
         * with a NullPointerException, instead of returning an empty list.
         * This is likely an issue in the Emulator itself, and we workaround it here.
		 */
        if ( ! "goldfish".equals(Build.HARDWARE) )
        {
            /*
             * Requests permission at the user to access USB devices.
             */
            int messages = HaspUsbHandler.getPermission(getApplicationContext());
            if ( messages != 0 )
            {
                textView.setText("Sent " + messages + " request(s) for USB access permission\n");
            }
        }

        /*
         * Creates a basic GUI.
         */
        Button demo = (Button)findViewById(R.id.demo);

        demo.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                demo();
            }            
        });

        Button update = (Button)findViewById(R.id.update);

        update.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent();
                intent.setClassName(getApplication(), "com.HaspDemo.RUSDemo");
                startActivity(intent);
            }            
        });

    }

    public void demo()
    {
        String infos;
        String text;
        int status;
        int i;
        int fsize;

        Hasp hasp = new Hasp(Hasp.HASP_DEFAULT_FID);

        text = "GetVersion : ";       
        HaspApiVersion version = hasp.getVersion(vendorCode);
        status = version.getLastError();

        switch (status)
        {
            case HaspStatus.HASP_STATUS_OK:
                text += version.majorVersion() + "." 
                    + version.minorVersion()
                    + "." + version.buildNumber() + "\n";                
                break;
            default:
                text += "unexpected error\n";
        }

        /**********************************************************************
         * hasp_getinfo
         *  get keys currently connected 
         */

        text += "GetAccessibleKeys: ";        
        infos = hasp.getInfo(scope, accessibleKeys, vendorCode);
        status = hasp.getLastError();

        switch (status) 
        {
        case HaspStatus.HASP_STATUS_OK:
            text += "OK\n" + infos;
            break;
        case HaspStatus.HASP_FEATURE_NOT_FOUND:
            text += "feature not found\n";
            break;
        case HaspStatus.HASP_HASP_NOT_FOUND:
            text += "key not found\n";
            break;
        case HaspStatus.HASP_INV_VCODE:
            text += "invalid vendor code\n";
            break;
        case HaspStatus.HASP_SCOPE_RESULTS_EMPTY:
            text += "unable to locate any local keys\n";
            break; 
        default:
            text += "failed with status:" + status;
            break;
        }

        if ( status != HaspStatus.HASP_STATUS_OK )
        {
            textView.setText(text);            
            return;
        }

        /**********************************************************************
         * hasp_login
         *   establish a context for Sentinel services
         */

        /* login feature 0 */
        /* this default feature is available on any key */
        text += "Login feature 0: ";        
        hasp.login(vendorCode);
        status = hasp.getLastError();

        switch (status)
        {
            case HaspStatus.HASP_STATUS_OK:
                text += "OK";
                break;
            case HaspStatus.HASP_FEATURE_NOT_FOUND:
                text += "feature not found";
                break;
            case HaspStatus.HASP_HASP_NOT_FOUND:
                text += "key not found";
                break;
            case HaspStatus.HASP_INV_VCODE:
                text += "invalid vendor code";
                break;
            default:
                text += "failed with status:" + status;
        }

        if ( status != HaspStatus.HASP_STATUS_OK )
        {
            textView.setText(text);            
            return;
        }

        /********************************************************************
         * hasp_get_sessioninfo
         *   retrieve Sentinel key attributes
         */

        text += "\nGetSessionInfo : ";
        infos = hasp.getSessionInfo(keyinfo);
        status = hasp.getLastError();

        switch (status) 
        {
            case HaspStatus.HASP_STATUS_OK:
                text += "OK\n" + infos;
                break;
            case HaspStatus.HASP_INV_HND:
                text += "handle not active";
                break;
            case HaspStatus.HASP_INV_FORMAT:
                text += "unrecognized format";
                break;
            case HaspStatus.HASP_HASP_NOT_FOUND:
                text += "Sentinel key not found";
                break;
            default:
                text += "failed with status:" + status;
        }

        /********************************************************************
         * hasp_get_size
         *   retrieve the memory size of the Sentinel key
         */

        text += "\nGetRWMemorySize : ";
        fsize = hasp.getSize(Hasp.HASP_FILEID_RW);
        status = hasp.getLastError();

        switch (status) 
        {
            case HaspStatus.HASP_STATUS_OK:
                text += fsize + " bytes";
                break;
            case HaspStatus.HASP_INV_HND:
                text += "handle not active";
                break;
            case HaspStatus.HASP_INV_FILEID:
                text += "invalid file id";
                break;
            case HaspStatus.HASP_HASP_NOT_FOUND:
                text += "Sentinel key not found";
                break;
            default:
                text += "failed with status:" + status;
        }

        /* skip memory access if no memory available */
        if (fsize != 0) {

            /******************************************************************
             * hasp_read
             *   read from memory
             */

            /* limit memory size to be used in this demo program */
            if (fsize > DEMO_MEMBUFFER_SIZE) 
                fsize = DEMO_MEMBUFFER_SIZE;

            byte[] membuffer = new byte[DEMO_MEMBUFFER_SIZE];

            text += "\nReading : ";
            hasp.read(Hasp.HASP_FILEID_RW, 0, membuffer);
            status = hasp.getLastError();

            switch (status) 
            {
                case HaspStatus.HASP_STATUS_OK:
                    text += "OK";
                    break;
                case HaspStatus.HASP_INV_HND:
                    text += "handle not active";
                    break;
                case HaspStatus.HASP_INV_FILEID:
                    text += "invalid file id";
                    break;
                case HaspStatus.HASP_MEM_RANGE:
                    text += "beyond memory range of attached Sentinel key";
                    break;
                case HaspStatus.HASP_HASP_NOT_FOUND:
                    text += "hasp not found";
                    break;
                default:
                    text += "failed with status:" + status;
            }

            /******************************************************************
             * hasp_write
             *   write to memory
             */

            /* changes the bytes in memory */
            for ( i = 0; i < fsize; ++i )
            {
                ++membuffer[i];
            }

            text += "\nWriting : ";
            hasp.write(Hasp.HASP_FILEID_RW, 0, membuffer);
            status = hasp.getLastError();

            switch (status) 
            {
                case HaspStatus.HASP_STATUS_OK:
                    text += "OK";
                    break;
                case HaspStatus.HASP_INV_HND:
                    text += "handle not active";
                    break;
                case HaspStatus.HASP_INV_FILEID:
                    text += "invalid file id";
                    break;
                case HaspStatus.HASP_MEM_RANGE:
                    text += "beyond memory range of attached Sentinel key";
                    break;
                case HaspStatus.HASP_HASP_NOT_FOUND:
                    text += "Sentinel key not found";
                    break;
                default:
                    text += "failed with status:" + status;
            }

            /******************************************************************
             * hasp_read
             *   read from memory
             */

            text += "\nReading : ";
            hasp.read(Hasp.HASP_FILEID_RW, 0, membuffer);

            switch (status) 
            {
                case HaspStatus.HASP_STATUS_OK:
                    text += "OK";
                    break;
                case HaspStatus.HASP_INV_HND:
                    text += "handle not active\n";
                    break;
                case HaspStatus.HASP_INV_FILEID:
                    text += "invalid file id";
                    break;
                case HaspStatus.HASP_MEM_RANGE:
                    text += "beyond memory range of attached Sentinel key";
                    break;
                case HaspStatus.HASP_HASP_NOT_FOUND:
                    text += "Sentinel key not found";
                    break;
                default:
                    text += "failed with status:" + status;
            }
        }

        /**********************************************************************
         * hasp_encrypt
         *   encrypts a block of data using the Sentinel key
         *   (minimum buffer size is 16 bytes)
         */

        text += "\nEncrypting : ";
        hasp.encrypt(data);
        status = hasp.getLastError();

        switch (status) 
        {
            case HaspStatus.HASP_STATUS_OK:
                text += "OK";
                break;
            case HaspStatus.HASP_INV_HND:
                text += "handle not active";
                break;
            case HaspStatus.HASP_TOO_SHORT:
                text += "data length too short";
                break;
            case HaspStatus.HASP_ENC_NOT_SUPP:
                text += "attached key does not support AES encryption";
                break;
            case HaspStatus.HASP_FEATURE_NOT_FOUND:
                text += "Sentinel key not found";
                break;
            default:
                text += "failed with status:" + status;
        }

        /**********************************************************************
         * hasp_decrypt
         *   decrypts a block of data using the Sentinel key
         *   (minimum buffer size is 16 bytes)
         */

        text += "\nDecrypting : ";        
        hasp.decrypt(data);
        status = hasp.getLastError();

        switch (status) 
        {
            case HaspStatus.HASP_STATUS_OK:
                text += "OK";
                break;
            case HaspStatus.HASP_INV_HND:
                text += "handle not active";
                break;
            case HaspStatus.HASP_TOO_SHORT:
                text += "data length too short";
                break;
            case HaspStatus.HASP_ENC_NOT_SUPP:
                text += "attached key does not support AES encryption";
                break;
            case HaspStatus.HASP_FEATURE_NOT_FOUND:
                text += "key not found";
                break;
            default:
                text += "failed with status:" + status;
        }

        /**********************************************************************
         * hasp_get_rtc
         *   read current time from Sentinel Time key
         */

        text += "\nGetTime : ";
        datetime = hasp.getRealTimeClock();
        status = hasp.getLastError();

        switch (status) 
        {
            case HaspStatus.HASP_STATUS_OK:
                text += datetime.getHour() + ":" + datetime.getMinute() + ":" + datetime.getSecond();
                text += " " + datetime.getDay() + "/" + datetime.getMonth() + "/" + datetime.getYear();
                break;
            case HaspStatus.HASP_INV_TIME:
                text += "time value outside supported range\n";
                break;
            case HaspStatus.HASP_INV_HND:
                text += "handle not active";
                break;
            case HaspStatus.HASP_NO_TIME:
                text += "no Sentinel Time connected";
                break;
            default:
                text += "failed with status:" + status;
        }

        /**********************************************************************
         * hasp_logout
         *   closes established session and releases allocated memory
         */

        text += "\nLogout : ";
        hasp.logout();
        status = hasp.getLastError();

        switch (status) 
        {
            case HaspStatus.HASP_STATUS_OK:
                text += "OK";
                break;
            case HaspStatus.HASP_INV_HND:
                text += "handle not active";
                break;
            default:
                text += "failed with status:" + status;
        }
        textView.scrollTo(0, 0);
        textView.setText(text);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        getMenuInflater().inflate(R.menu.menu_demo, menu);
        return true;
    }

    static
    {
        /* load the HASP JNI library */
        System.loadLibrary("HASPJava");        
    }
}
