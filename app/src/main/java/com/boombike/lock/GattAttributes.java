package com.boombike.lock;

import java.util.UUID;

/**
 * Description:ble uuid <br />
 */
public class GattAttributes {


    public final  static UUID UUID_SERVICE= UUID.fromString("0783b03e-8535-b5a0-7140-a304d2495cb7");
    public final  static UUID UUID_CHARACTERISTIC_WRITE= UUID.fromString("0783b03e-8535-b5a0-7140-a304d2495cba");
    public final  static UUID UUID_CHARACTERISTIC_READ= UUID.fromString("0783b03e-8535-b5a0-7140-a304d2495cb8");
    /** for notify */
    public final static UUID UUID_NOTIFICATION_DESCRIPTOR= UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");



}
