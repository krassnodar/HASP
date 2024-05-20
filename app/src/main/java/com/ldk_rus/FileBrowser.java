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

import android.annotation.SuppressLint;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileBrowser extends Activity implements OnItemClickListener
{
    public  static boolean   mFromParent = true;
    public  static String    mCurrentPath = "";
    private static String    mRootPath = "";
    private List<String>     mItems = null;
    private List<String>     mPaths = null;
    private TextView         mPath;
    private ListView         mListView;
    private FileAdapter      mAdapter;
    private List<File>       mList = null;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        RelativeLayout layout = new RelativeLayout(this);
        super.setContentView(layout);

        mPath = new TextView(this);
        mPath.setId(1);
        mPath.setTextSize(18);
        RelativeLayout.LayoutParams pathLP = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        pathLP.leftMargin = 10;
        pathLP.topMargin  = 10;

        mListView = new ListView(this);
        mListView.setId(2);
        RelativeLayout.LayoutParams listLP = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        listLP.leftMargin = 10;
        listLP.topMargin = 10;
        listLP.addRule(RelativeLayout.BELOW, 1);

        layout.addView(mPath, pathLP);
        layout.addView(mListView, listLP);
        
        // come from parent activity; show base directory, otherwise show saved current directory
        if (mFromParent)
        {
            mFromParent = false;
            
            if ( Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) )
            {
                File sdCardDir = Environment.getExternalStorageDirectory();

                try {
                    String path = sdCardDir.getCanonicalPath();
                    mRootPath = path;
                    getFileDir(path);
                }
                catch (IOException e) {
                    e.printStackTrace();
                } 
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        else
        {
            String path = mCurrentPath;
            try {
                getFileDir(path);
            }
            catch (IOException e) {
                e.printStackTrace();
            } 
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        mListView.setOnItemClickListener(this);        
    }

    private void getFileDir(String filePath) throws Exception
    {
        mCurrentPath = filePath;
        mPath.setText(filePath);
        mItems = new ArrayList<String>();
        mPaths = new ArrayList<String>();
        File f = new File(filePath);
        File[] files = f.listFiles();

        if( !filePath.equals(mRootPath) )
        {
            mItems.add("b1");
            mPaths.add(mRootPath);

            mItems.add("b2");
            mPaths.add(f.getParent());
        }

        mList = new ArrayList<File>();

        for ( File t: files )
        {
            mList.add(t);
        }

        FileSortByName fileSortByName = new FileSortByName();
        List<File> newList = fileSortByName.getFile(mList);

        for ( int i = 0; i < newList.size(); i++ )
        {
            File file = newList.get(i);


            if ( file.isDirectory() )
            {
                mItems.add(file.getName());
                mPaths.add(file.getPath());
            }
            else
            {
                String type = getMIMEType(file);

                if ( type.equals("v2c/*") )
                {
                    mItems.add(file.getName());
                    mPaths.add(file.getPath());                      
                }
            }
        }  

        mAdapter = new FileAdapter(this, mItems, mPaths);
        mListView.setAdapter(mAdapter);
    }

    private void openFile(File f) 
    {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(android.content.Intent.ACTION_VIEW);

        String type = getMIMEType(f);

        intent.setDataAndType(Uri.fromFile(f),type);
        startActivity(intent); 
    }

    @SuppressLint("DefaultLocale")
    private String getMIMEType(File f)
    {
        String type="";
        String fileName = f.getName();

        String end = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length()).toLowerCase(); 

        if ( end.equals("m4a") || end.equals("mp3") || end.equals("mid")
            || end.equals("xmf") || end.equals("ogg") || end.equals("wav") )
        {
            type = "audio"; 
        }
        else if( end.equals("3gp") || end.equals("mp4") || end.equals("avi") )
        {
            type = "video";
        }
        else if( end.equals("jpg") || end.equals("gif") || end.equals("png")
            || end.equals("jpeg") || end.equals("bmp") )
        {
            type = "image";
        }
        else if ( end.equals("v2c") || end.equals("v2cp") )
        {
            type = "v2c";
        }
        else
        {
            type="*";
        }

        type += "/*";

        return type; 
    }

    private void finishWithResult(File f)
    {
        String path = Uri.fromFile(f).toString();
        path = path.substring(7);

        Bundle conData = new Bundle();
        conData.putString("path", path);

        Intent intent = new Intent();
        intent.putExtras(conData);
        setResult(RESULT_OK, intent);

        finish();
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View v, int position, long id)
    {
        File file = new File(mPaths.get(position));
        if ( file.isDirectory() )
        {
            try {
                getFileDir(mPaths.get(position));
            } 
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        else
        {
            finishWithResult(file);            
        }
    }
}
