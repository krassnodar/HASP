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

import java.io.File;
import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FileSortByName 
{
    public List<File> getFile(List<File> files) 
    {
        if ( files != null ) 
        {
            ComparatorFile comparator = new ComparatorFile();
            Collections.sort(files, comparator);
        } 
        else
        {
        }

        return files;
    }

    class ComparatorFile implements Comparator<File> 
    {

        @SuppressWarnings("unused")
        private Collator collator = Collator.getInstance();

        public ComparatorFile() 
        {
        }

        public int compare(File f1, File f2) 
        {
            if ( f1 == null || f2 == null )
            {
                if ( f1 == null ) 
                {
                    return -1;
                } 
                else
                {
                    return 1;
                }
            }
            else
            {
                if ( f1.isDirectory() == true && f2.isDirectory() == true )
                { 
                    return f1.getName().compareToIgnoreCase(f2.getName());
                }
                else
                {
                    if ( (f1.isDirectory() && !f2.isDirectory()) == true ) 
                    {
                        return -1;
                    } 
                    else if ( (f2.isDirectory() && !f1.isDirectory()) == true ) 
                    {
                        return 1;
                    } 
                    else 
                    {
                        return f1.getName().compareToIgnoreCase(f2.getName());
                    }
                }
            }
        }

    }
}
