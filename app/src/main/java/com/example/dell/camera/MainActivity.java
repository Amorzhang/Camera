package com.example.dell.camera;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dell.camera.util.IPManager;
import com.example.dell.camera.util.MyDataBaseHelper;
import com.example.dell.camera.util.PictureManage;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ZXH on 2017/9/10.
 */
public class MainActivity extends AppCompatActivity {


    //查看图片弹窗
    Dialog dia;
    //定义图片容器
    private ImageView imageView,imageView2;
    //拍照按钮
    private Button btn;
    //最新照片预览
    private ImageButton imageButton;
    //相册
    private TextView txv;
    //每隔2s变换主图片（Socket获取）
    private Bitmap bm;

    private IPManager ipManager = new IPManager();

    //定义设置IP，Port编辑窗,弹出窗
    private TextView ip,port;
    private View viewDialog;

    //定义屏幕管理器
    public WindowManager wm;

    //定义数据库工具
    private MyDataBaseHelper myDataBaseHelper;
    private SQLiteDatabase database;

    //socket初始化
    public Socket socket;
    private Bitmap bit;
    public InputStream inputStream;
    private String prentPath;
    //图片处理
    private PictureManage pictureManage;
    public final static String VIEW = "com.example.zxj.preview_example.VIEW";
    private static final int IMAGE = 1;
    private Context context;
    private String Tagip;
    private String Tagport;
    private Cursor c;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        context = this;

        //初始化数据库服务
        /*myDataBaseHelper = new MyDataBaseHelper(this,"carmea.db",null,1);
        database = myDataBaseHelper.getWritableDatabase();
        c = database.rawQuery("select * from Address",null);
        if (c.getCount() == 0){
            viewDialog = getLayoutInflater().inflate(R.layout.setting,null);
            new AlertDialog.Builder(this)
                    .setTitle("请设置IP地址")
                    .setIcon(android.R.drawable.ic_menu_add)
                    .setView(viewDialog)
                    .setNegativeButton("取消",null)
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ip = (TextView) viewDialog.findViewById(R.id.ip);
                            port = (TextView) viewDialog.findViewById(R.id.port);
                            int ID = 0;
                            String IP = ip.getText().toString();
                            String PORT = port.getText().toString();
                            ContentValues cv = new ContentValues();
                            cv.put("id",ID);
                            cv.put("ip",IP);
                            cv.put("port",PORT);
                            database.insert("Address",null,cv);
                            Toast.makeText(context, "数据添加成功", Toast.LENGTH_SHORT).show();
                            database.close();
                            while (c.moveToNext()) {
                                Tagip = c.getString(c.getColumnIndex("ip"));
                                Tagport = c.getString(c.getColumnIndex("port"));
                            }
                        }
                    }).show();
        }else {
            while (c.moveToNext()) {
                Tagip = c.getString(c.getColumnIndex("ip"));
                Tagport = c.getString(c.getColumnIndex("port"));
            }
        }
*/
        wm = (WindowManager) getApplicationContext()
                .getSystemService(Context.WINDOW_SERVICE);
        //主窗口获取
        this.imageView = (ImageView) findViewById(R.id.imageView);
        this.imageView.setImageResource(R.drawable.load);
        //最近图片获取
        this.imageButton = (ImageButton) findViewById(R.id.imageButton);


        //“相册”
        this.txv = (TextView) findViewById(R.id.txv);
        this.btn = (Button) findViewById(R.id.button);
        dia = new Dialog(this, R.style.edit_AlertDialog_style);
        dia.setContentView(R.layout.allview);
        imageView2 = (ImageView) dia.findViewById(R.id.allview);

        //开启子线程
        new Thread(networkTask).start();
        //获取Imagebutton图片
        List<String> list = new ArrayList<String>();
        File  scanner5Directory = new File(Environment.getExternalStorageDirectory()+"/Boohee");
        if (scanner5Directory.isDirectory()) {
            for (File file : scanner5Directory.listFiles()) {
                String path = file.getAbsolutePath();
                if (path.endsWith(".jpg") || path.endsWith(".jpeg") || path.endsWith(".png")) {
                    list.add(path);
                }
            }
        }
        int i = list.size()-1;
        prentPath = list.get(i);
        bm = BitmapFactory.decodeFile(prentPath);
        imageButton.setImageBitmap(pictureManage.fitBitmap(bm,pictureManage.dptopx(this,50)));
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String val = data.getString("value");
            Log.i("mylog", "请求结果为-->" + val);
            if(socket == null) {

                Toast toast = Toast.makeText(MainActivity.this, Tagip+":"+Tagport+"\n"+"服务器未连接...", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.TOP,0,0);
                toast.show();
            }

        }
    };

    public Runnable networkTask = new Runnable() {
        @Override
        public void run() {
            // TODO
                try {
                    Tagip = ipManager.getConnectIp().get("b8:27:eb:82:dc:fd");
                    Tagport = "6666";
                    if(Tagip == null){
                        Toast.makeText(context,"请检测热点状态",Toast.LENGTH_SHORT);
                    }else {
                        socket = new Socket(Tagip, Integer.valueOf(Tagport));
                        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                        out.write("test.BMP".getBytes());
                        inputStream = socket.getInputStream();
                        bit = BitmapFactory.decodeStream(inputStream);
                        bit = pictureManage.fitBitmap(bit, wm.getDefaultDisplay().getWidth());
                        handler.post(runnableUi);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            // 在这里进行 http request.网络请求相关操作
                Message msg = new Message();
                Bundle data = new Bundle();
                data.putString("value", "请求结果");
                msg.setData(data);
                handler.sendMessage(msg);
            }
    };
    Runnable runnableUi=new  Runnable(){
        @Override
        public void run() {
            //更新界面
           imageView.setImageBitmap(bit);
        }

    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.menu_setting:
                viewDialog = getLayoutInflater().inflate(R.layout.setting,null);
                database = myDataBaseHelper.getWritableDatabase();
                Cursor c = database.rawQuery("select * from Address",null);
                ip = (TextView) viewDialog.findViewById(R.id.ip);
                port = (TextView) viewDialog.findViewById(R.id.port);
                while (c.moveToNext())
                {
                    String IP = c.getString(c.getColumnIndex("ip"));
                    String PORT = c.getString(c.getColumnIndex("port"));
                    ip.setText(IP);
                    port.setText(PORT);
                }
                c.close();
                database.close();
                new AlertDialog.Builder(this)
                        .setTitle("编辑信息")
                        .setIcon(android.R.drawable.ic_menu_add)
                        .setView(viewDialog)
                        .setNegativeButton("取消",null)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {;
                                String IP = ip.getText().toString();
                                String PORT = port.getText().toString();
                                ContentValues cv = new ContentValues();
                                cv.put("id",0);
                                cv.put("ip",IP);
                                cv.put("port",PORT);
                                database = myDataBaseHelper.getWritableDatabase();
                                database.update("Address",cv,null,null);
                                Toast.makeText(context, "数据修改成功", Toast.LENGTH_SHORT).show();
                                database.close();
                            }
                        }).show();
                break;
            case R.id.menu_about:
                break;
            case R.id.menu_quit:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onClick(View v){
        switch (v.getId()){
            case R.id.button:
                Log.d("onClick","点击拍照");
                if(bit == null){
                    Toast toast = Toast.makeText(this, "未获取到图片，请稍后再试", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }else{
                    String path = pictureManage.saveImageToGallery(this.getApplicationContext(),bit);
                    imageButton.setImageBitmap(pictureManage.fitBitmap(BitmapFactory.decodeFile(path),pictureManage.dptopx(this,50)));
                    prentPath = path;
                }

               // saveImageToGallery(this.getApplicationContext(),bit);
                break;
            case R.id.imageButton:
                Log.d("onClick","查看照片");
                int width = wm.getDefaultDisplay().getWidth();
                Bitmap bm = BitmapFactory.decodeFile(prentPath);
                imageView2.setImageBitmap(pictureManage.fitBitmap(bm,width));

                //imageView2.setImageResource(R.drawable.desert);
                //imageView2.setImageBitmap(BitmapFactory.decodeFile(imaePath));
                dia.show();
                dia.setCanceledOnTouchOutside(true);//点击可以退出
                Window window = dia.getWindow();
                WindowManager.LayoutParams attributes = window.getAttributes();
                attributes.x = 0;
                attributes.y = 40;
                dia.onWindowAttributesChanged(attributes);
                /*Intent intent1 = new Intent();
                intent1.setAction(android.content.Intent.ACTION_VIEW);
                intent1.setDataAndType(Uri.fromFile(new File("sdcard/Pictures/1.jpg")), "image*//*");
                startActivity(intent1);*/
                break;
            case R.id.txv:
                Log.d("onClick","查看相册");

                /*Intent albumIntent = new Intent(Intent.ACTION_PICK, null);
                albumIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image*//*");
                startActivityForResult(albumIntent, 1);*/
                Intent intent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, IMAGE);
                break;
        }
    }

    public void onBackPressed() {
        new AlertDialog.Builder(this).setTitle("确认退出吗？")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 点击“确认”后的操作
                        MainActivity.this.finish();

                    }
                })
                .setNegativeButton("返回", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 点击“返回”后的操作,这里不设置没有任何操作
                    }
                }).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //获取图片路径
        if (requestCode == IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            String[] filePathColumns = {MediaStore.Images.Media.DATA};
            Cursor c = getContentResolver().query(selectedImage, filePathColumns, null, null, null);
            c.moveToFirst();
            int columnIndex = c.getColumnIndex(filePathColumns[0]);
            String imagePath = c.getString(columnIndex);
            showImage(imagePath);
            c.close();
        }
    }
    private void showImage(String imaePath){
        Bitmap bm = BitmapFactory.decodeFile(imaePath);
        imageView2.setImageBitmap(pictureManage.fitBitmap(bm,wm.getDefaultDisplay().getWidth()));
       //imageView2.setImageBitmap(BitmapFactory.decodeFile(imaePath));
        dia.show();


        dia.setCanceledOnTouchOutside(true);//点击可以退出

        Window window = dia.getWindow();
        WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.x = 0;
        attributes.y = 40;
        dia.onWindowAttributesChanged(attributes);
       /* Intent intent = new Intent(this,DisplayViewActivity.class);
                String ID = String.valueOf(R.drawable.hydrangeas);
                intent.putExtra(VIEW,imaePath);
                startActivity(intent);*/
    }


 /*   //按宽度缩放图片显示
    public static Bitmap fitBitmap(Bitmap target, int newLong)
    {
        int width = target.getWidth();
        int height = target.getHeight();
        Matrix matrix = new Matrix();
        if(width>height){
            int newHeight = newLong;
            float scaleHeight = ((float)newHeight) / height;
            int newWidth = (int) (scaleHeight * width);
            matrix.postScale(scaleHeight, scaleHeight);
        }else{
            float scaleWidth = ((float) newLong) / width;
            // float scaleHeight = ((float)newHeight) / height;
            int newHeight = (int) (scaleWidth * height);
            matrix.postScale(scaleWidth, scaleWidth);
        }
        // Bitmap result = Bitmap.createBitmap(target,0,0,width,height,
        // matrix,true);
        Bitmap bmp = Bitmap.createBitmap(target, 0, 0, width, height, matrix,
                true);
        if (target != null && !target.equals(bmp) && !target.isRecycled())
        {
            target.recycle();
            target = null;
        }
        return bmp;// Bitmap.createBitmap(target, 0, 0, width, height, matrix,
        // true);
    }

    //dp转px
    public static int dptopx(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    //文件中读取Bitmap效率问题
    public static Bitmap readBitmap(String path){

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(path,options);
        int imageHeight = options.outHeight;
        int imageWidth = options.outWidth;
        String imageType = options.outMimeType;

        options.inJustDecodeBounds=false;
        //整个图像，下采样
        Bitmap patch=Bitmap.createBitmap(bitmap, 10, 10, 100, 100);
        return patch;
    }

    //保存图片
  *//*  public static void saveImage(Bitmap bmp) {
        File appDir = new File(Environment.getExternalStorageDirectory(), "Boohee");
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
*//*
    public static String saveImageToGallery(Context context, Bitmap bmp) {
        // 首先保存图片

        File appDir = new File(Environment.getExternalStorageDirectory(),"Boohee");
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 其次把文件插入到系统图库
        try {
            MediaStore.Images.Media.insertImage(context.getContentResolver(),
                    file.getAbsolutePath(), fileName, null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // 最后通知图库更新
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + appDir+"/"+fileName)));
    return appDir+"/"+fileName;
    }

*/

}
