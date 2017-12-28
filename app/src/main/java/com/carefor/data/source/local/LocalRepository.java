package com.carefor.data.source.local;

import android.content.Context;
import android.support.annotation.NonNull;

import com.carefor.data.source.DataSource;
import com.carefor.data.source.cache.CacheRepository;


import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by baige on 2017/12/21.
 */

public class LocalRepository implements DataSource {
    private final static String TAG = LocalRepository.class.getCanonicalName();

    private static LocalRepository INSTANCE = null;

    private LocalRepository(@NonNull Context context){
        checkNotNull(context);
        CacheRepository cacheRepository = CacheRepository.getInstance();
        cacheRepository.readConfig(context);
    }

    public static LocalRepository getInstance(@NonNull Context context) {
        if (INSTANCE == null) {
            synchronized (LocalRepository.class) { //对获取实例的方法进行同步
                if (INSTANCE == null) {
                    INSTANCE = new LocalRepository(context);
                }
            }
        }
        return INSTANCE;
    }
}
