package org.jokar.dudaizhong.service;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.jokar.dudaizhong.MainActivity;
import org.jokar.dudaizhong.R;
import org.jokar.dudaizhong.bean.Download;
import org.jokar.dudaizhong.network.DownloadAPI;
import org.jokar.dudaizhong.network.download.DownloadProgressListener;
import org.jokar.dudaizhong.utils.StringUtils;

import java.io.File;


import rx.Subscriber;

/**
 * Created by JokAr on 16/7/5.
 */
public class DownloadService extends IntentService {
    private static final String TAG = "DownloadService";

    private NotificationCompat.Builder notificationBuilder;
    private NotificationManager notificationManager;


    private String apkUrl = "http://download.fir.im/v2/app/install/572eec6fe75e2d7a05000008?download_token=572bcb03dad2eed7c758670fd23b5ac4";

//    private String apkUrl = "http://p12.dfs.kuaipan.cn/cdlnode/dl/?ud=b7iEjaqihMyuojr8NmA4G3TAJJMoDGxw5HaF8xTMDGG8MwjhRy4E7DjQP4UlK)wc~0-http:@@180.97.176.19@ufa_new@~PRfqHVzmo4HQjXLzS0)gg8N(IoDz0Wg-0-@0-HVzmo4HQjXLzS0)gg8N(IoDz0Wg-mVGsQdMJWVWIQP0G9ASVsQE0UjTrdvFgq1si7bpGsTU-761a9858502200-0-4003818~1&src=0-4003818--&tm=1470414607&cip=113.250.157.200&bea=YXR0YWNobWVudDtmaWxlbmFtZT0xNDM2MjgwNjE1Lm1wMzs=";
    public DownloadService() {
        super("DownloadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_download)
                .setContentTitle("Download")
                .setContentText("Downloading File")
                .setAutoCancel(true);

        notificationManager.notify(0, notificationBuilder.build());

        download();
    }

    private void download() {
        DownloadProgressListener listener = new DownloadProgressListener() {
            @Override
            public void update(long bytesRead, long contentLength, boolean done) {
                Download download = new Download();
                download.setTotalFileSize(contentLength);
                download.setCurrentFileSize(bytesRead);
                int progress = (int) ((bytesRead * 100) / contentLength);
                download.setProgress(progress);

                sendNotification(download);
            }
        };
        File outputFile = new File(Environment.getExternalStoragePublicDirectory
                (Environment.DIRECTORY_DOWNLOADS), "music.mp3");
        String baseUrl = StringUtils.getHostName(apkUrl);
//        RetrofitSingleton.getInstance().downloadMusic(outputFile, new Subscriber() {
//            @Override
//            public void onCompleted() {
//                downloadCompleted();
//            }
//
//            @Override
//            public void onError(Throwable e) {
//                e.printStackTrace();
//                downloadCompleted();
//                Log.e(TAG, "onError: " + e.getMessage());
//            }
//
//            @Override
//            public void onNext(Object o) {
//
//            }
//        });

        new DownloadAPI(baseUrl, listener).downloadAPK(apkUrl, outputFile, new Subscriber() {
            @Override
            public void onCompleted() {
                downloadCompleted();
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                downloadCompleted();
                Log.e(TAG, "onError: " + e.getMessage());
            }

            @Override
            public void onNext(Object o) {

            }
        });
    }

    private void downloadCompleted() {
        Download download = new Download();
        download.setProgress(100);
        sendIntent(download);

        notificationManager.cancel(0);
        notificationBuilder.setProgress(0, 0, false);
        notificationBuilder.setContentText("File Downloaded");
        notificationManager.notify(0, notificationBuilder.build());
    }

    private void sendNotification(Download download) {

        sendIntent(download);
        notificationBuilder.setProgress(100, download.getProgress(), false);
        notificationBuilder.setContentText(
                StringUtils.getDataSize(download.getCurrentFileSize()) + "/" +
                        StringUtils.getDataSize(download.getTotalFileSize()));
        notificationManager.notify(0, notificationBuilder.build());
    }

    private void sendIntent(Download download) {

        Intent intent = new Intent(MainActivity.MESSAGE_PROGRESS);
        intent.putExtra("download", download);
        LocalBroadcastManager.getInstance(DownloadService.this).sendBroadcast(intent);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        notificationManager.cancel(0);
    }
}
