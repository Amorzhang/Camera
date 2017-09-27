package com.example.dell.camera.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

/**
 * Created by DELL on 2017/9/20.
 */
public class MyDataBaseHelper extends SQLiteOpenHelper {

    public static final String CREATE_MESSAGE = "CREATE TABLE Address("
            +"id INT PRIMARY KEY NOT NULL,"
            + "ip text NOT NULL,"
            + "port text NOT NULL)";

    private Context myContent;

    public MyDataBaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        myContent = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_MESSAGE);
        //提示数据库创建成功
        Toast.makeText(myContent, "数据库创建成功", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
