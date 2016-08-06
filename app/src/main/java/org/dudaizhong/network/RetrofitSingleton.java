package org.jokar.dudaizhong.network;


import org.jokar.dudaizhong.network.download.DownloadProgressInterceptor;
import org.jokar.dudaizhong.network.download.DownloadProgressListener;
import org.jokar.dudaizhong.network.exception.CustomizeException;
import org.jokar.dudaizhong.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by Dudaizhong on 2016/8/4.
 * 推荐将网络请求的方法写在这里面,让activity/fragment更关注于界面
 */
public class RetrofitSingleton {

    private static ApiInterface apiService = null;
    private static Retrofit retrofit = null;
    private static OkHttpClient okHttpClient = null;
    private static DownloadProgressListener listener = null;

    /**
     * 初始化
     */
    private static void init() {

    }

    public RetrofitSingleton() {
        initOkHttp();
        initRetrofit();
        apiService = retrofit.create(ApiInterface.class);
    }

    public static RetrofitSingleton getInstance() {
        return SingletonHolder.INSTANCE;
    }


    private static class SingletonHolder {
        private static final RetrofitSingleton INSTANCE = new RetrofitSingleton();
    }

    public static ApiInterface getApiService() {
        if (apiService == null) {
            throw new NullPointerException("get apiService must be called after init");
        }
        return apiService;
    }

    private static void initOkHttp() {
        // https://drakeet.me/retrofit-2-0-okhttp-3-0-config
        // HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        // interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        // 下载文件进度的拦截器
        DownloadProgressInterceptor interceptor = new DownloadProgressInterceptor(listener);

        okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .retryOnConnectionFailure(true)
                .connectTimeout(15, TimeUnit.SECONDS)
                .build();
    }

    private static void initRetrofit() {
        retrofit = new Retrofit.Builder()
                .baseUrl(ApiInterface.baseUrl)
                .client(okHttpClient)
//                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
    }

//    public static void disposeFailureInfo(Throwable t, Context context) {
//        if (context != null) {
//            if (t.toString().contains("GaiException")
//                    || t.toString().contains("SocketTimeoutException")
//                    || t.toString().contains("UnknownHostException")) {
//                ToastUtil.showLong("没有网络");
//            } else if (t.toString().contains("ConnectException")) {
//                ToastUtil.showLong("网络连接失败");
//            } else {
//                ToastUtil.showLong(t.getMessage());
//            }
//        }
//    }


//------------------------------------------------------------------------------------------------
//------------------------------------------------------------------------------------------------

    //下载文件的接口
    public void downloadMusic(final File file, Subscriber subscriber) {
        apiService.download(null)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .map(new Func1<ResponseBody, InputStream>() {
                    @Override
                    public InputStream call(ResponseBody responseBody) {
                        return responseBody.byteStream();
                    }
                })
                .observeOn(Schedulers.computation())
                .doOnNext(new Action1<InputStream>() {
                    @Override
                    public void call(InputStream inputStream) {
                        try {
                            FileUtils.writeFile(inputStream, file);
                        } catch (IOException e) {
                            e.printStackTrace();
                            throw new CustomizeException(e.getMessage(), e);
                        }
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }
}

