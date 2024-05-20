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

public class Data
{
    private static String msg = "";

    public static void initMsg()
    {
        Data.msg = "";
    }

    public static String getMsg()
    {
        return msg;
    }

    public static void appendMsg(String text)
    {
        Data.msg = Data.msg + text + "\n";
    }
}
