/*****************************************************************************
*
* RUS program for protected application
*
* Copyright (C) 2021 Thales Group. All rights reserved.
*
*
*****************************************************************************/

package com.safenet.patch;

public class Product 
{

    private static native byte[] getinfo(String scope, String format, int status[]);
    private static native String update(String update_data, int status[]);

    /**
     * Status of the last function call
     */
    private int status;

    static
    {
        try
        {
            System.loadLibrary("rus");
        }
        catch (UnsatisfiedLinkError e)
        {
            if ( e.getMessage().indexOf("already loaded in another classloader") == -1 )
            {
                throw e;
            }
        }
    }

    /**
     * Get Info - method to get fingerprint or C2V
     */
    public String GetInfo(String scope, String format)
    {
        byte[] info = { 0 };
        int[] status1 = { 0 };
        String s = null;
        status = 0;

        info = getinfo(scope, format, status1);

        status = status1[0];
        if( status == 0)
            s = new String(info);

        return s;
    }

    /**
     * Update - method to apply V2C
     */
    public String Update(String update_data)
    {
        int[] dll_status = {0};
        String s = null;
        status = 0;

        s = update(update_data, dll_status);
        status = dll_status[0];

        return s;
    }

    /**
     * Returns the error that occurred in the last function call
     */
    public int getLastError()
    {
        return status;
    }
}
