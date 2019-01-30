package intelligentecadenas.afatsum.com.intelligentecadenas;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.content.ContentValues.TAG;

/**
 * Created by Mustafa on 13/08/2018.
 */

public class RCSerialThread extends Thread {
    private final FileInputStream mmInStream;
    private final FileOutputStream mmOutStream;
    Context context;
    Handler handler;

    public RCSerialThread(Context ctx, FileDescriptor fileDescriptor, Handler handl) {
        context = ctx;
        mmInStream = new FileInputStream(fileDescriptor);
        mmOutStream = new FileOutputStream(fileDescriptor);
        handler = handl;
    }

    public void run() {

        Log.w(TAG, "run");
        final byte[] buffer = new byte[20480];//512*38
        int value = 0;
        int i = 0;
        String trame_recue = new String();

        while (true) {

            try {

                value = mmInStream.read(buffer);
                i++;
                trame_recue = new String(buffer); // au format ASCII classique
                // "trame_recue" est de la forme : ch6a1500b1500c .. z1234

                if (trame_recue.substring(0, 3).equals("cdn")) {
                    // On bloque le drone à 0 horizontal si on reçoit un sfx
                    cdn_cmd(trame_recue); // traite la trame SFX
                }

                sleep(5);

            } catch (NullPointerException e) {

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public void write(byte[] bytes) {
        try {
            mmOutStream.write(bytes);
        } catch (IOException e) {
        }
    }

    public FileOutputStream getMmOutStream() {
        return mmOutStream;
    }


    public void cdn_cmd(String cmd) {
        String function = "", parametre = "", mode = "", value = "", x = "";
        function = cmd.substring(4, 6);
        value = cmd.substring(12, 16);
        if (function.equals("00"))
            Function.Opened = false;
        else
            Function.Opened = true;


    }


}