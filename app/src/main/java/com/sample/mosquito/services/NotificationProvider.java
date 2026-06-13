package com.sample.mosquito.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Build;

import com.sample.mosquito.R;

public class NotificationProvider {

    public interface OnNotificationClick {
        void onButtonClick();
    }

    public static final int CODE = 1234;
    private final String BUTTON_CLICK = "button_click";

    private final Context context;
    private final OnNotificationClick callback;
    private final NotificationManager notificationManager;
    private Notification notification;
    private boolean isShowing;
    private Class<?> activityClass;

    public NotificationProvider(Context context, OnNotificationClick callback) {
        this.context = context;
        this.callback = callback;

        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createChannels();
    }

    private void createChannels() {
        NotificationChannel builderChannel = new NotificationChannel(context.getString(R.string.app_name), context.getString(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT);
        builderChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        builderChannel.enableLights(false);
        builderChannel.enableVibration(false);
        builderChannel.setSound(null, null);
        notificationManager.createNotificationChannel(builderChannel);
    }

    public void setActivityClass(Class<?> activityToOpen) {
        activityClass = activityToOpen;
    }

    public Notification getNotification() {
        return notification;
    }

    public int getNotificationCode() {
        return CODE;
    }

    public void show(int res, int res2, int res3) {
        show(context.getString(res), context.getString(res2), context.getString(res3));
    }

    public void show(int res, int res2) {
        show(context.getString(res), context.getString(res2), null);
    }

    public void show(int res) {
        show(context.getString(res), null, null);
    }

    public void show(String title, String detail) {
        show(title, detail, null);
    }

    public void show(String title, String detail, String action) {
        isShowing = true;
        PendingIntent pendingIntent = null;
        if (activityClass != null) {
            Intent resultIntent = new Intent(context, activityClass);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addNextIntentWithParentStack(resultIntent);
            pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        }

        Notification.Builder builder;
        builder = new Notification.Builder(context, context.getString(R.string.app_name));
        builder.setAutoCancel(false);
        builder.setOngoing(true);
        builder.setShowWhen(true);
        builder.setColorized(false);
        builder.setTicker(title);
        builder.setContentTitle(title);
        if (detail != null) builder.setContentText(detail);
        builder.setSmallIcon(R.drawable.icon_notification);
        builder.setCategory(Notification.CATEGORY_SERVICE);
        if (pendingIntent != null) builder.setContentIntent(pendingIntent);
        if (action != null) {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU) {
                pendingIntent = PendingIntent.getBroadcast(context, CODE, new Intent(BUTTON_CLICK), PendingIntent.FLAG_IMMUTABLE);
            } else {
                pendingIntent = PendingIntent.getBroadcast(context, CODE, new Intent(BUTTON_CLICK), PendingIntent.FLAG_UPDATE_CURRENT);
            }
            builder.addAction(new Notification.Action.Builder(Icon.createWithResource(context, R.drawable.icon_close), action, pendingIntent).build());
        }
        /*
        RemoteViews root = new RemoteViews(context.getPackageName(), R.layout.notification);
        root.setTextViewText(R.id.textTitle, title);
        root.setImageViewBitmap(R.id.appIcon, tintBitmap(R.drawable.icon_file, Color.valueOf(context.getColor(R.color.main_normal))));
        if (detail != null) root.setTextViewText(R.id.textDetail, detail);
        builder.setCustomContentView(root);
        */

        try {
            IntentFilter filter = new IntentFilter();
            filter.addAction(BUTTON_CLICK);
            context.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED);
        } catch (Exception ignore) {
        }

        notification = builder.build();
        notificationManager.notify(CODE, notification);
    }

    public boolean isShowing() {
        return isShowing;
    }

    public void hide() {
        isShowing = false;
        notificationManager.cancel(CODE);
        try {
            context.unregisterReceiver(receiver);
        } catch (Exception ignore) {
        }
    }

    public void cancelAll() {
        isShowing = false;
        hide();
        notificationManager.cancelAll();
    }

    public Bitmap tintBitmap(int id, Color target) {
        Drawable drawable = context.getDrawable(id);
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        Bitmap bitmap2 = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        for (int y = 0; y < bitmap.getHeight(); y++) {
            for (int x = 0; x < bitmap.getWidth(); x++) {
                int color = Color.argb(bitmap2.getColor(x, y).alpha(), target.red(), target.green(), target.blue());
                bitmap2.setPixel(x, y, color);
            }
        }
        bitmap.recycle();
        return bitmap2;
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent == null || intent.getAction() == null ? "" : intent.getAction();
            hide();
            if (action.equals(BUTTON_CLICK)) callback.onButtonClick();
        }
    };
}
