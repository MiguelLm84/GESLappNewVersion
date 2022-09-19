package com.example.geslapp.core.clases;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.List;

public class Update extends AsyncTask<String, Integer, String> {

    @SuppressLint("StaticFieldLeak")
    private static Context mContext;
    private ProgressDialog mPDialog;
    private static String PATH;
    private static boolean lastmod,updated;

    private final ConfigPreferences config = new ConfigPreferences();

    public void setContext(Activity context) {
        mContext = context;
        updated = false;

        context.runOnUiThread(() -> {
            mPDialog = new ProgressDialog(mContext);
            mPDialog.setMessage("Espere por favor...");
            mPDialog.setIndeterminate(true);
            mPDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mPDialog.setCancelable(false);
            mPDialog.show();
        });
    }

    @Override
    protected String doInBackground(String... arg0) {

        try {
            new CheckConnection().cancel(true);
            String sarg = arg0[0];
            URL url = new URL(arg0[0]);
            HttpURLConnection c = (HttpURLConnection) url.openConnection();
            c.setRequestMethod("GET");
            c.setDoOutput(true);
            c.connect();
            int lenghtOfFile = c.getContentLength();
            lastmod = LastModified(sarg);

            if(lastmod) {
                //V3
                PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
                File file = new File(PATH);
                
                File outputFile = new File(file, "geslapp.apk");
                if (outputFile.exists()) {
                    outputFile.delete();

                }
                if (!outputFile.exists()) {
                    outputFile.createNewFile();
                }

                //Toast.makeText(mContext, "Arhivo antiguo borrado" + outputFile.exists(), Toast.LENGTH_SHORT).show();
                FileOutputStream fos = new FileOutputStream(outputFile);
                InputStream is = c.getInputStream();

                byte[] buffer = new byte[1024];
                int len1;
                long total = 0;
                while ((len1 = is.read(buffer)) != -1) {
                    total += len1;
                    fos.write(buffer, 0, len1);
                    publishProgress((int) ((total * 100) / lenghtOfFile));
                }
                fos.close();
                is.close();
                if (mPDialog != null) mPDialog.dismiss();
                Handler handler = new Handler();

                handler.postDelayed(() -> {
                    installApk();
                    config.setLastAppUpdate(mContext);
                },500);

            } else {
                mPDialog.dismiss();
                Toast.makeText(mContext, "No hay actualizaciones back", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Log.e("UpdateAPP", "Update error! " + e.getMessage());
        }
        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (mPDialog != null) mPDialog.show();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        if (mPDialog != null) {
            mPDialog.setIndeterminate(false);
            mPDialog.setMax(100);
            mPDialog.setProgress(values[0]);
        }
    }

    @Override
    protected void onPostExecute(String result) {

        if (mPDialog != null) mPDialog.dismiss();
        PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        File file = new File(PATH);
        boolean isCreate = file.mkdirs();
        File outputFile = new File(file, "geslapp.apk");
        long filesize = outputFile.length();
       // Toast.makeText(mContext, ""+outputFile.lastModified(), Toast.LENGTH_SHORT).show();

        if(!lastmod||filesize == 0) {
            Toast.makeText(mContext, "No hay actualizaciones post"+filesize+"LASTMOD:"+lastmod, Toast.LENGTH_SHORT).show();
            Log.v("No last-modified information.",""+config.getLastUpdate(mContext));

        } else if (result != null ) {
            Toast.makeText(mContext, "Error de descarga" , Toast.LENGTH_LONG).show();

        } else {
            Toast.makeText(mContext, "Archivo añadido a descargas", Toast.LENGTH_SHORT).show();
        }
    }

    private void installApk() {

        try {
            String PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
            File file = new File(PATH + "/geslapp.apk");
            Intent intent = new Intent(Intent.ACTION_VIEW);
            if (Build.VERSION.SDK_INT >= 26) {
                Uri downloaded_apk = FileProvider.getUriForFile(mContext, mContext.getApplicationContext().getPackageName() + ".provider", file);
                intent.setDataAndType(downloaded_apk, "application/vnd.android.package-archive");
                List<ResolveInfo> resInfoList = mContext.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                for (ResolveInfo resolveInfo : resInfoList) {
                    mContext.grantUriPermission(mContext.getApplicationContext().getPackageName() + ".provider", downloaded_apk, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                mContext.startActivity(intent);
            } else {
                intent.setAction(Intent.ACTION_VIEW);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
                intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            mContext.startActivity(intent);
            updated = true;


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean LastModified(String url) {

        HttpURLConnection.setFollowRedirects(false);
        HttpURLConnection con = null;
        try {
            con = (HttpURLConnection) new URL(url).openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
         long date = con != null ? con.getLastModified() : 0;
         long lastupdate = config.getLastUpdate(mContext);

        if (date == config.getLastUpdate(mContext)) {
            System.out.println("No last-modified information."+new Date(date));
            //Toast.makeText(mContext, "lastmod:---"+config.getLastUpdate(mContext), Toast.LENGTH_SHORT).show();
            return false;

        }
        else {
            System.out.println("Last-Modified: " + new Date(date));
            config.setLastUpdate(mContext, date);
           // Toast.makeText(mContext, "lastmod:---"+config.getLastUpdate(mContext), Toast.LENGTH_SHORT).show();
            return true;
        }
    }
    public boolean getUpdated()
    {
        return updated;
    }
}
