package com.perm.DoomPlay;

/*
 *    Copyright 2013 Vladislav Krot
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 *    You can contact me <DoomPlaye@gmail.com>
 */
import android.content.Context;
import android.os.Environment;

import java.io.*;
import java.util.ArrayList;

class Serializator<T extends Serializable>
{
    static enum FileNames
    {
        Audio,Group,User,Album
    }

    private final Context context;
    private final FileNames name;

    public Serializator(Context context,FileNames name)
    {
        this.context = context;
        this.name = name;
    }

    private File getPathByType()
    {

        String dir =  String.format("/Android/data/%s/files/", context.getPackageName());

        File sd = Environment.getExternalStorageDirectory();

        File stacktrace = new File(sd.getPath() + dir,
                name.toString() +".txt");


        File dumpdir = stacktrace.getParentFile();
        dumpdir.mkdirs();

        return stacktrace;
    }

    /*
    throw exception

    private Class returnedClass()
    {
        ParameterizedType parameterizedType = (ParameterizedType)getClass().getGenericSuperclass();
        return (Class) parameterizedType.getActualTypeArguments()[0];
    }
    */

    public void inSerialize(ArrayList<T> objects)
    {

        File file = getPathByType();

        OutputStream fileStream = null;
        ObjectOutputStream objectStream = null;


        if(file.exists())
            file.delete();

        try
        {

            fileStream = new FileOutputStream(file);
            objectStream = new ObjectOutputStream(fileStream);
            objectStream.writeObject(objects);


        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if(fileStream != null)
                try {
                    fileStream.flush();
                    fileStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            if(objectStream != null)
                try {
                    objectStream.flush();
                    objectStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }
    public ArrayList<T> getSerialization()
    {
        ArrayList<T> list = new ArrayList<T>();

        File distinctPath = getPathByType();

        if(!distinctPath.exists())
            return list;


        InputStream outFile = null;
        ObjectInputStream outObject = null;
        try
        {


            outFile = new FileInputStream(distinctPath);
            outObject = new ObjectInputStream(outFile);

            list  = (ArrayList <T>)outObject.readObject();

            return list;


        }
        catch(IOException e)
        {
            e.printStackTrace();
            return list;
        } catch (ClassNotFoundException e)
        {
            e.printStackTrace();
            return list;
        }

        finally
        {
            if(outFile != null)
                try {
                    outFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            if(outObject != null)
                try {
                    outObject.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }

    }

}
