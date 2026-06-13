package com.sample.mosquito.services;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

public class ServiceController {

    public interface OnServiceAvailableListener {
        void onAvailable(MainService mainService);
    }

    private static ServiceController serviceController;
    private MainService mainService;

    public static ServiceController getInstance(Context context, OnServiceAvailableListener onServiceAvailableListener) {
        if (serviceController == null) {
            synchronized (ServiceController.class) {
                serviceController = new ServiceController(context, onServiceAvailableListener);
            }
        }
        if (serviceController.getService() != null) {
            onServiceAvailableListener.onAvailable(serviceController.mainService);
        }
        return serviceController;
    }

    private ServiceController(Context context, OnServiceAvailableListener onServiceAvailableListener) {

        Intent service = new Intent(context, MainService.class);
        context.startService(service);
        context.bindService(service, new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName className, IBinder service) {
                MainService.LocalBinder binder = (MainService.LocalBinder) service;
                mainService = (MainService) binder.getServiceInstance();
                onServiceAvailableListener.onAvailable(mainService);
                context.unbindService(this);
            }

            @Override
            public void onServiceDisconnected(ComponentName arg0) {
            }
        }, Context.BIND_AUTO_CREATE);
    }

    public MainService getService() {
        return mainService;
    }
}
