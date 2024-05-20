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
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;


public class FileAdapter extends BaseAdapter
{
    private Bitmap mIcon1;
    private Bitmap mIcon2;
    private Bitmap mIcon3;
    private Bitmap mIcon4;
    private List<String> mItems;
    private List<String> mPaths;
    private Context mContext;

    public FileAdapter(Context context,List<String> it,List<String> pa) throws Exception
    {
        mContext = context;

        mItems = it;
        mPaths = pa;

        try 
        {
            byte[] data;
            ByteArrayInputStream bais;
            
            byte[] root_bytes = RootIcon.getRootIconBytes();
            bais = new ByteArrayInputStream(root_bytes);
            data = readStream(bais);
            mIcon1 = BitmapFactory.decodeByteArray(data, 0, data.length);
            mIcon1 = Bitmap.createScaledBitmap(mIcon1, 32, 32, false);
            bais.close();
            
            byte[] parent_bytes = ParentIcon.getParentIconBytes();
            bais = new ByteArrayInputStream(parent_bytes);
            data = readStream(bais);
            mIcon2 = BitmapFactory.decodeByteArray(data, 0, data.length);
            mIcon2 = Bitmap.createScaledBitmap(mIcon2, 32, 32, false);
            bais.close();
            
            byte[] folder_bytes = FolderIcon.getFolderIconBytes();
            bais = new ByteArrayInputStream(folder_bytes);
            data = readStream(bais);
            mIcon3 = BitmapFactory.decodeByteArray(data, 0, data.length);
            mIcon3 = Bitmap.createScaledBitmap(mIcon3, 32, 32, false);
            bais.close();
            
            byte[] file_bytes = FileIcon.getFileIconBytes();
            bais = new ByteArrayInputStream(file_bytes);
            data = readStream(bais);
            mIcon4 = BitmapFactory.decodeByteArray(data, 0, data.length);
            mIcon4 = Bitmap.createScaledBitmap(mIcon4, 32, 32, false);
            bais.close();
        }
        catch (IOException e1) 
        {
            e1.printStackTrace();
        }
    }

    public static byte[] readStream(InputStream inStream) throws Exception
    {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        
        byte[] buffer = new byte[1024];
        int len = 0;
        
        while ( (len = inStream.read(buffer)) != -1 )
        {
            outStream.write(buffer, 0, len);
        }

        outStream.close();
        inStream.close();
        
        return outStream.toByteArray();
    }

    @Override
    public int getCount()
    {
        return mItems.size();
    }

    @Override
    public Object getItem(int position)
    {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(int position, View v, ViewGroup parent)
    {
        ViewHolder holder = new ViewHolder();

        if( v == null )
        {
            RelativeLayout layout = new RelativeLayout(mContext);

            ImageView imageView = new ImageView(mContext);
            imageView.setId(1);
            RelativeLayout.LayoutParams imageViewLP = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
            imageViewLP.leftMargin = 10;
            imageViewLP.topMargin  = 10;

            TextView text = new TextView(mContext);
            text.setId(2);
            RelativeLayout.LayoutParams textLP = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            textLP.leftMargin = 10;
            textLP.topMargin  = 10;
            textLP.addRule(RelativeLayout.RIGHT_OF, 1);

            layout.addView(imageView, imageViewLP);
            layout.addView(text, textLP);
            v = layout;

            holder.text = text;
            holder.icon = imageView;

            v.setTag(holder);
        }
        else
        {
            holder = (ViewHolder) v.getTag();
        }

        File f = new File(mPaths.get(position).toString());
        
        if ( mItems.get(position).toString().equals("b1") )
        {
            holder.text.setText("Back to /");
            holder.icon.setImageBitmap(mIcon1);
        }
        else if ( mItems.get(position).toString().equals("b2") )
        {
            holder.text.setText("Back to ..");
            holder.icon.setImageBitmap(mIcon2);
        }
        else
        {
            holder.text.setText(f.getName());
            
            if ( f.isDirectory() )
            {
                holder.icon.setImageBitmap(mIcon3);
            }
            else
            {
                holder.icon.setImageBitmap(mIcon4);
            }
        }

        return v;
    }

    private class ViewHolder
    {
        TextView text;
        ImageView icon;
    }  
}
