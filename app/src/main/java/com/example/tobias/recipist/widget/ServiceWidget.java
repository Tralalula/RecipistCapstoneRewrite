package com.example.tobias.recipist.widget;

        import android.content.Intent;
        import android.widget.RemoteViewsService;

/**
 * Created by Tobias on 26-06-2016.
 */
public class ServiceWidget extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new DataProviderWidget(getApplicationContext(), intent);
    }
}
