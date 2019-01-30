package intelligentecadenas.afatsum.com.intelligentecadenas;

/**
 * Created by Mustafa on 13/08/2018.
 */
public class HandlerRC {

    public static RCSerialThread rcSerialThread;

    public static RCSerialThread getRcSerialThread() {
        return rcSerialThread;
    }

    public static void setRcSerialThread(RCSerialThread rcSerialThread) {
        HandlerRC.rcSerialThread = rcSerialThread;
    }

}
