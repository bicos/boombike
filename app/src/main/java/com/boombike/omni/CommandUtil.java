package com.boombike.omni;


import java.util.Random;

/**
 * <br />
 * created by CxiaoX at 2017/4/22 18:02.
 */

public class CommandUtil {

    private static  final String TAG="CommandUtil";

    private  static  final  byte ORDER_KEY=0x11;
    private  static  final  byte ORDER_UN_LOCK=0x21;
    private  static  final  byte ORDER_LOCK=0x22;
    private  static  final  byte ORDER_LOCK_STATUS=0x31;
    private  static  final  byte ORDER_OLE_DATA=0x51;
    private  static  final  byte ORDER_CLEAR_OLE_DATA=0x52;


    public static byte[]   getOpenCommand(int uid,byte bleKey){
        return getCommand(uid,bleKey,ORDER_UN_LOCK,(byte)0x00);
    }


    public static byte[] getCRCOpenCommand(int uid,byte bleKey){
        byte[] command = getOpenCommand( uid, bleKey);
        byte[] xorCommand=decode(command);
        byte[] crcOrder= CRCByte(xorCommand);
        return  crcOrder;
    }



    public static byte[] getCRCKeyCommand2(int uid,byte bleKey){
        byte[] command = getKeyCommand2( uid, bleKey);
        byte[] xorCommand=decode(command);
        byte[] crcOrder= CRCByte(xorCommand);
        return  crcOrder;
    }

    public static byte[]   getLockCommand(int uid,byte bleKey){
        return getCommand(uid,bleKey,ORDER_LOCK,(byte)0x00);
    }
    public static byte[] getCRCLockCommand(int uid,byte bleKey){
        byte[] command = getLockCommand( uid, bleKey);
        byte[] xorCommand=decode(command);
        byte[] crcOrder= CRCByte(xorCommand);
        return  crcOrder;
    }

    public static byte[]   getLockStatusCommand(int uid,byte bleKey){
        return getCommand(uid,bleKey,ORDER_LOCK_STATUS,(byte)0x00);
    }
    public static byte[]   getCRCLockStatusCommand(int uid,byte bleKey){
        byte[] command = getLockStatusCommand( uid, bleKey);
        byte[] xorCommand=decode(command);
        byte[] crcOrder= CRCByte(xorCommand);
        return  crcOrder;
    }
    public static byte[]   geOldDataCommand(int uid,byte bleKey){
        return getCommand(uid,bleKey,ORDER_OLE_DATA,(byte)0x00);
    }
    public static byte[]   getCRCOldDataCommand(int uid,byte bleKey){
        byte[] command = geOldDataCommand( uid, bleKey);
        byte[] xorCommand=decode(command);
        byte[] crcOrder= CRCByte(xorCommand);
        return  crcOrder;
    }
    public static byte[]   geClearDataCommand(int uid,byte bleKey){
        return getCommand(uid,bleKey,ORDER_CLEAR_OLE_DATA,(byte)0x00);
    }
    public static byte[]   getCRCClearDataCommand(int uid,byte bleKey){
        byte[] command = geClearDataCommand( uid, bleKey);
        byte[] xorCommand=decode(command);
        byte[] crcOrder= CRCByte(xorCommand);
        return  crcOrder;
    }






    private static byte[]   getCommand(int uid,byte bleKey,byte order,byte len){
        byte randKey = (byte) (new Random().nextInt(255) & 0xff);
        byte uidB1=(byte) (( uid>>24)&0xFF);
        byte uidB2=(byte) (( uid>>16)&0xFF);
        byte uidB3=(byte) ( ( uid>>8)&0xFF);
        byte uidB4=(byte) ( uid &0xFF);
        byte[] command=new byte[9];// 不包含CRC校验的长度
        command[0]= (byte) 0xFE;
        command[1]=(byte) (randKey); //随机数 x1
        command[2]= uidB1; // 用户id
        command[3]= uidB2;
        command[4]= uidB3;
        command[5]= uidB4;
        command[6]= bleKey;  // key
        command[7]= order;  // 命令代码
        command[8]= len;  // 长度
        return command;
    }


    private static byte[]   getKeyCommand2(int uid,byte bleKey){
        byte randKey = (byte) (new Random().nextInt(255) & 0xff);
        byte uidB1=(byte) (( uid>>24)&0xFF);
        byte uidB2=(byte) (( uid>>16)&0xFF);
        byte uidB3=(byte) ( ( uid>>8)&0xFF);
        byte uidB4=(byte) ( uid &0xFF);
        byte[] command=new byte[17];
        command[0]= (byte) 0xFE;
        command[1]=(byte) (randKey);
        command[2]= uidB1;
        command[3]= uidB2;
        command[4]= uidB3;
        command[5]= uidB4;
        command[6]= bleKey;  // key
        command[7]= ORDER_KEY;
        command[8]= 0x08;  // len
        // yOTmK50z   the key for get Key
        // 0x79 0x4F 0x54 0x6D 0x4B 0x35 0x30 0x7A

        // new HxeEL2g2

        command[9]=  'H';  //
        command[10]= 'x';  //
        command[11]= 'e';  //
        command[12]= 'E';  //
        command[13]= 'L';  //
        command[14]= '2';  //
        command[15]= 'g';  //
        command[16]= '2';  //
        return  command;
    }




    private static byte[] decode(byte[] command){
        byte[] xorComm = new byte[command.length];
        xorComm[0]=command[0];
        xorComm[1] =(byte) (command[1] +0x32);
        for(int i=2;i<command.length;i++){
            xorComm[i]= (byte) (command[i] ^ command[1]);
        }
        return xorComm;
    }

    private static byte[] CRCByte(byte[] ori){
        byte[] ret = new byte[ori.length+2];
        int crc = CRCUtil.calcCRC(ori);
        for(int i=0;i<ori.length;i++) ret[i]=ori[i];
        ret[ori.length]= (byte) ((crc>>8)&0xFF);
        ret[ori.length+1]=(byte)( crc &0xFF);
        return ret;
    }


}
