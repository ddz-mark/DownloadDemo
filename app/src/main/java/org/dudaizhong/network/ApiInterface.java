package org.jokar.dudaizhong.network;

import org.jokar.dudaizhong.utils.StringUtils;

import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Streaming;
import retrofit2.http.Url;
import rx.Observable;

/**
 * Created by Dudaizhong on 2016/8/4.
 */
public interface ApiInterface {

    String BASE_URL = "http://p12.dfs.kuaipan.cn/cdlnode/dl/?ud=b7iEjaqihMyuojr8NmA4G3TAJJMoDGxw5HaF8xTMDGG8MwjhRy4E7DjQP4UlK)wc~0-http:@@180.97.176.19@ufa_new@~PRfqHVzmo4HQjXLzS0)gg8N(IoDz0Wg-0-@0-HVzmo4HQjXLzS0)gg8N(IoDz0Wg-mVGsQdMJWVWIQP0G9ASVsQE0UjTrdvFgq1si7bpGsTU-761a9858502200-0-4003818~1&src=0-4003818--&tm=1470414607&cip=113.250.157.200&bea=YXR0YWNobWVudDtmaWxlbmFtZT0xNDM2MjgwNjE1Lm1wMzs=";
    String baseUrl = StringUtils.getHostName(BASE_URL);

    @Streaming
    @GET
    Observable<ResponseBody> download(@Url String url);
}
