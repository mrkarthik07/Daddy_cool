package com.recordingapplication.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

import com.jaredrummler.android.processes.AndroidProcesses;
import com.jaredrummler.android.processes.models.AndroidAppProcess;
import com.jaredrummler.android.processes.models.Stat;
import com.recordingapplication.utill.ComonUtill;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;


/**
 *  App Monitor Service
 */
public class AppMonitorService extends IntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     */
    private List<RunningProcess> appList = null;
    final Handler mHandler = new Handler();
    boolean isModified = false;
    public AppMonitorService() {
        super("Test");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        final Runnable r = new Runnable(){
            @Override
            public void run() {
                getNewProcessList();
                mHandler.postDelayed(this, 10000);
            }
        };

        mHandler.postDelayed(r, 10000);
    }

    void getNewProcessList() {
        List<AndroidAppProcess> processes = AndroidProcesses.getRunningAppProcesses();
        Collections.sort(processes, new Comparator<AndroidAppProcess>() {
            @Override
            public int compare(AndroidAppProcess lhs, AndroidAppProcess rhs) {
                return ComonUtill.getName(getApplicationContext(), lhs).compareToIgnoreCase(ComonUtill.getName(getApplicationContext(), rhs));
            }
        });

        isModified = false;
        if(appList==null) {
            appList = new ArrayList<RunningProcess>();

            for(int i=0;i<processes.size();i++) {
                AndroidAppProcess process = processes.get(i);

                if(process.name.indexOf(":")>0) continue;
                if(process.name.endsWith(".so")) continue;

                RunningProcess newprocess = new RunningProcess();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                String realName = ComonUtill.getName(getApplicationContext(), process);
                long startTime = 0;
                try {
                    Stat stat = null;
                    stat = process.stat();
                    long bootTime = System.currentTimeMillis() - SystemClock.elapsedRealtime();
                    startTime = bootTime + (10 * stat.starttime());

                    newprocess.name = process.name;
                    newprocess.realName = realName;
                    newprocess.startTime = startTime;
                    newprocess.updateTime = System.currentTimeMillis();
                    newprocess.isActive = true;

                    appList.add(newprocess);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            isModified = true;
        }

        else {
            boolean isExist = false;

            long updateTime = System.currentTimeMillis();
            for (int j = 0; j < processes.size(); j++) {
                AndroidAppProcess process = processes.get(j);

                if (process.name.indexOf(":") > 0) continue;
                if (process.name.endsWith(".so")) continue;

                long startTime = 0;
                String realName = ComonUtill.getName(getApplicationContext(), process);
                try {
                    Stat stat = null;
                    stat = process.stat();
                    long bootTime = System.currentTimeMillis() - SystemClock.elapsedRealtime();
                    startTime = bootTime + (10 * stat.starttime());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if(startTime==0) continue;
                isExist = false;
                for(int i=0;i<appList.size();i++) {
                    RunningProcess oldProcess = appList.get(i);
                    if(process.name.equals(oldProcess.name)) {

                        oldProcess.startTime = startTime;
                        oldProcess.updateTime = updateTime;
                        oldProcess.isActive = true;

                        appList.set(i, oldProcess);
                        isExist = true;
                        break;
                    }
                }

                if(!isExist) {
                    RunningProcess newprocess = new RunningProcess();
                    newprocess.name = process.name;
                    newprocess.realName = realName;
                    newprocess.startTime = startTime;
                    newprocess.updateTime = updateTime;
                    newprocess.isActive = true;

                    appList.add(newprocess);

                    isModified = true;
                }
            }

            for(int i=0;i<appList.size();i++) {
                RunningProcess oldProcess = appList.get(i);
                if (updateTime != oldProcess.updateTime && oldProcess.isActive) {
                    oldProcess.isActive = false;
                    oldProcess.updateTime = updateTime;
                    appList.set(i, oldProcess);
                    isModified = true;
                }
            }
        }

        if(isModified) {
            saveToFile(appList);
            isModified = false;
        }
    }

    void saveToFile(List<RunningProcess> processes) {
        final String STRSAVEPATH = Environment.getExternalStorageDirectory()+ "/" + ComonUtill.DirName + "/";
        final String SAVEFILEPATH = ComonUtill.monitorfileName;

        File dir = new File(STRSAVEPATH);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File savefile = null;
        boolean isSuccess = false;
        if(dir.isDirectory()){
            savefile = new File((STRSAVEPATH+SAVEFILEPATH));
            if(savefile!=null && !savefile.exists()){
                try {
                    isSuccess = savefile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally{
                }
            }
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            assert savefile != null;
            BufferedWriter file = new BufferedWriter(new FileWriter(savefile.getAbsolutePath()));
            file.write("===== "+sdf.format(System.currentTimeMillis())+" =====");
            file.newLine();
            file.newLine();
            for (int i = 0; i < processes.size(); i++) {
                String closed_time = "";
                if(processes.get(i).isActive)
                    closed_time = "0";
                else
                    closed_time = sdf.format(processes.get(i).updateTime);

                String str, actionTime;
                int time = (int) (processes.get(i).updateTime - processes.get(i).startTime)/1000;
                actionTime = String.format("%d:%d:%d", time/3600, time/60%60, time%60);
                str = String.format("%s\t%s\t%s\t%s",
                        processes.get(i).realName,
                        sdf.format(processes.get(i).startTime),
                        closed_time,
                        actionTime);
                file.write(str);
                file.newLine();
            }
            file.write("=======================================");
            file.newLine();
            file.flush();
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.i("test", "===========================================================");
    }

    class RunningProcess {
        public RunningProcess() {
            startTime = 0;
            name = "";
            realName = "";
            updateTime = 0;
            isActive = false;
        }
        public long startTime;
        public String name ;
        public String realName ;
        public long updateTime ;
        public boolean isActive;
    }
}
