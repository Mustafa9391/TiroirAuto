package intelligentecadenas.afatsum.com.intelligentecadenas;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.util.Log;


import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

import static intelligentecadenas.afatsum.com.intelligentecadenas.HandlerRC.rcSerialThread;


/**
 * Created by Mustafa on 13/08/2018.
 */

public class UsbData {
    //************** USB CONNECTION ***********************
    private static final String ACTION_USB_PERMISSION = "com.google.android.DemoKit.action.USB_PERMISSION";

    private UsbManager mUsbManager;
    private PendingIntent mPermissionIntent;
    private boolean mPermissionRequestPending;

    UsbAccessory mAccessory;
    ParcelFileDescriptor mFileDescriptor;
    FileInputStream mInputStream;
    FileOutputStream mOutputStream;


    Context ctx;
    Handler handler;
    boolean connected = false;
    boolean connectedCR = true;

    public Semaphore mutex = new Semaphore(1);
    public Semaphore semaphore = new Semaphore(0);
    public LinkedBlockingQueue<int[]> queue;

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbAccessory accessory = (UsbAccessory) intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
                    if (intent.getBooleanExtra(
                            UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        openAccessory(accessory);
                    } else {
                        Log.d("TAG", "permission denied for accessory "
                                + accessory);
                    }
                    mPermissionRequestPending = false;
                }
            } else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
                UsbAccessory accessory = (UsbAccessory) intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
                if (accessory != null && accessory.equals(mAccessory)) {
                    closeAccessory();
                    //Toast.makeText(ctx, "close accessory", Toast.LENGTH_LONG).show();
                    connectedCR = false;
                    // TODO : en cas de détachement du câble USB, déclencher des sécurités
                    // (atterrissage, message, commande à 0, mode manuel, etc)
                }
            }
        }
    };

    public void openAccessory(UsbAccessory accessory) {
        mFileDescriptor = mUsbManager.openAccessory(accessory);
        if (mFileDescriptor != null) {
            mAccessory = accessory;
            FileDescriptor fd = mFileDescriptor.getFileDescriptor();
            mInputStream = new FileInputStream(fd);
            mOutputStream = new FileOutputStream(fd);
            //added
            rcSerialThread = new RCSerialThread(ctx, fd, handler);
            rcSerialThread.setPriority(Thread.MAX_PRIORITY);
            HandlerRC.setRcSerialThread(rcSerialThread);
            rcSerialThread.start();
            connected = true;
            Log.d("TAG", "accessory opened");
        } else {
            connected = false;
            Log.d("TAG", "accessory open fail");
            //Toast.makeText(ctx, "Device not connected", Toast.LENGTH_SHORT).show();
        }
    }




    public void closeAccessory() {
        //Toast.makeText(ctx, "close device", Toast.LENGTH_SHORT).show();
        try {
            if (mFileDescriptor != null) {
                mFileDescriptor.close();
            }
        } catch (IOException e) {
        } finally {
            mFileDescriptor = null;
            mAccessory = null;
        }
    }

    public UsbData(Context context, Handler handler) {
        ctx = context;
        this.handler = handler;
        mUsbManager = (UsbManager) ctx.getSystemService(Context.USB_SERVICE);
        mPermissionIntent = PendingIntent.getBroadcast(ctx, 0, new Intent(ACTION_USB_PERMISSION), 0);
        //permission ici surtout pour Android 6 et +, sinon pour Android 5 et - il suffit dans le Manifest
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
        ctx.registerReceiver(mUsbReceiver, filter);
    }

    public void onresumeUSB() {
        if (mInputStream != null && mOutputStream != null) {
            return;
        }

        UsbAccessory[] accessories = mUsbManager.getAccessoryList();
        UsbAccessory accessory = (accessories == null ? null : accessories[0]);
        if (accessory != null) {
            if (mUsbManager.hasPermission(accessory)) {
                openAccessory(accessory);
            } else {
                synchronized (mUsbReceiver) {
                    if (!mPermissionRequestPending) {
                        mUsbManager.requestPermission(accessory, mPermissionIntent);
                        mPermissionRequestPending = true;
                    }
                }
            }
        } else {
            //usbSerialThread.handler=this.handler;
            Log.d("TAG", "mAccessory is null");
        }
    }

    public boolean isConnected() {
        return mUsbManager != null;
    }

    public boolean isCRConnected() {
        return connectedCR;
    }

    public void unregistre() {
        ctx.unregisterReceiver(mUsbReceiver);
    }


}
