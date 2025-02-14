package com.example.test;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Create by lizhen on 2018/10/9
 */
public class JacocoInstrumentation extends Instrumentation implements FinishListener {
    public static String TAG = "JacocoInstrumentation:";
    private static String DEFAULT_COVERAGE_FILE_PATH = "/mnt/sdcard/coverage.ec";
    private final Bundle mResults = new Bundle();
    private Intent mIntent;
    private static final boolean LOGD = true;
    private boolean mCoverage = true;
    private String mCoverageFilePath;

    public JacocoInstrumentation() {
    }

    @Override
    public void onCreate(Bundle arguments) {
        Log.d(TAG, "onCreate(" + arguments + ")");
        super.onCreate(arguments);
        DEFAULT_COVERAGE_FILE_PATH = getContext().getFilesDir().getPath() + "/coverage.ec";
        Log.d(TAG,"新建文件："+DEFAULT_COVERAGE_FILE_PATH);

        File file = new File(DEFAULT_COVERAGE_FILE_PATH);
        if(!file.exists()){
            try{
                file.createNewFile();
            }catch (IOException e){
                Log.d(TAG,"新建文件异常："+e);
                e.printStackTrace();}
        }
        if(arguments != null) {
            mCoverageFilePath = arguments.getString("coverageFile");
        }
        mIntent = new Intent(getTargetContext(), InstrumentedActivity.class);
        mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        start();
    }

    @Override
    public void onStart() {
        super.onStart();
        Looper.prepare();
        InstrumentedActivity activity = (InstrumentedActivity) startActivitySync(mIntent);
        activity.setFinishListener(this);
    }

    private String getCoverageFilePath() {
        if (mCoverageFilePath == null) {
            return DEFAULT_COVERAGE_FILE_PATH;
        } else {
            return mCoverageFilePath;
        }
    }

    private void generateCoverageReport() {
        Log.d(TAG, "generateCoverageReport():" + getCoverageFilePath());
        OutputStream out = null;
        try {
            out = new FileOutputStream(getCoverageFilePath(), false);
            Object agent = Class.forName("org.jacoco.agent.rt.RT")
                    .getMethod("getAgent")
                    .invoke(null);
            out.write((byte[]) agent.getClass().getMethod("getExecutionData", boolean.class)
                    .invoke(agent, false));
        } catch (Exception e) {
            Log.d(TAG, e.toString(), e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onActivityFinished() {
        if (LOGD)      Log.d(TAG, "onActivityFinished()");
        if (mCoverage) {
            generateCoverageReport();
        }
        finish(Activity.RESULT_OK, mResults);
    }

    @Override
    public void dumpIntermediateCoverage(String filePath) {

    }
}
