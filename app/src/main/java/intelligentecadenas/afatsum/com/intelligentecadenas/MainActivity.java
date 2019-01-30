package intelligentecadenas.afatsum.com.intelligentecadenas;

import android.app.Activity;
import android.content.Intent;
import android.hardware.usb.UsbAccessory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import static intelligentecadenas.afatsum.com.intelligentecadenas.HandlerRC.rcSerialThread;

public class MainActivity extends Activity {

    private UsbData usbData;
    UsbAccessory mAccessory;
    Button BTOpen;
    EditText ETPassword;
    String DataToSend = "";

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            try {
                BTOpen.setText(Function.Opened ? "Ouvrir" : "Fermer");
                super.handleMessage(msg);

            } catch (NullPointerException e) {
            } catch (Exception e) {
            }

        }
    };
    private Timer swipeTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init_comp();
        getEtat();
    }

    public void getEtat() {
        final Handler handler = new Handler();
        final Runnable Update = new Runnable() {
            public void run() {
                try {
                    BTOpen.setText(Function.Opened ? "Ouvrir" : "Fermer");

                } catch (Exception e) {
                }
            }
        };
        swipeTimer = new Timer();
        swipeTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(Update);
            }
        }, 500, 500);

    }

    private void init_comp() {
        BTOpen = (Button) findViewById(R.id.BTOpen);
        BTOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Function.Password.equals(ETPassword.getText().toString())) {
                    if (Function.Opened) {
                        DataToSend = "cnd 00";
                    } else {
                        DataToSend = "cnd 01";
                    }
                    //Function.Opened = !Function.Opened;
                    if (rcSerialThread != null) {
                        if (rcSerialThread.getMmOutStream() != null) {
                            rcSerialThread.write(DataToSend.getBytes());
                        } else {
                            Toast.makeText(MainActivity.this, "Erreur", Toast.LENGTH_LONG).show();
                        }
                    } else
                        Toast.makeText(MainActivity.this, "Veuillez connecter le tiroir avec le téléphone par cable USB", Toast.LENGTH_LONG).show();

                }
                else
                    Toast.makeText(MainActivity.this, "Mot de passe incorrect", Toast.LENGTH_LONG).show();

            }
        });

        ETPassword = (EditText) findViewById(R.id.ETPassword);
    }


    @Override
    public Object onRetainNonConfigurationInstance() {  // librairie USB
        if (mAccessory != null) {
            return mAccessory;
        } else {
            return super.onRetainNonConfigurationInstance();
        }
    }

    @Override
    protected void onPause() {
        usbData.closeAccessory();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        usbData = new UsbData(getApplicationContext(), handler);
        usbData.onresumeUSB();

        if (getLastNonConfigurationInstance() != null) {
            mAccessory = (UsbAccessory) getLastNonConfigurationInstance();
            usbData.openAccessory(mAccessory);
        }

    }
}
