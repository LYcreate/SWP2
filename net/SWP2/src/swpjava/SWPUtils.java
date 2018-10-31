package swpjava;

import java.util.ArrayList;
import java.util.Arrays;

public class SWPUtils {
    public static Long byte8ToLong(byte[] a){
        int c0=a[0]&0xFF;
        int c1=a[1]&0xFF;
        int c2=a[2]&0xFF;
        int c3=a[3]&0xFF;
        int c4=a[4]&0xFF;
        int c5=a[5]&0xFF;
        int c6=a[6]&0xFF;
        int c7=a[7]&0xFF;
        long x=(c0<<56)|(c1<<48)|(c2<<40)|(c3<<32)|(c4<<24)|(c5<<16)|(c6<<8)|c7;
        return x;
        //System.out.println(x);
    }
    public static int byte4ToInt(byte[] a){
        int c0=a[0]&0xFF;
        int c1=a[1]&0xFF;
        int c2=a[2]&0xFF;
        int c3=a[3]&0xFF;
        int x=(c0<<24)|(c1<<16)|(c2<<8)|c3;
        return x;
        //System.out.println(x);
    }
    public static int byte2ToInt(byte[] a){
        int c0=a[0]&0xFF;
        int c1=a[1]&0xFF;
        int x=(c0<<8)|(c1);
        return x;
        //System.out.println(x);
    }

    public static int byteToInt(byte a){
        int c0=a&0xFF;
        int x=c0;
        return x;
        //System.out.println(x1);
    }

    public static byte[] longToByte8(long i) {
        byte[] targets = new byte[8];
        targets[7] = (byte) (i & 0xFF);
        targets[6] = (byte) (i >> 8 & 0xFF);
        targets[5] = (byte) (i >> 16 & 0xFF);
        targets[4] = (byte) (i >> 24 & 0xFF);
        targets[3] = (byte) (i >> 32 & 0xFF);
        targets[2] = (byte) (i >> 40 & 0xFF);
        targets[1] = (byte) (i >> 48 & 0xFF);
        targets[0] = (byte) (i >> 56 & 0xFF);
        return targets;
    }

    public static byte[] intToByte4(int i) {
        byte[] targets = new byte[4];
        targets[3] = (byte) (i & 0xFF);
        targets[2] = (byte) (i >> 8 & 0xFF);
        targets[1] = (byte) (i >> 16 & 0xFF);
        targets[0] = (byte) (i >> 24 & 0xFF);
        return targets;
    }

    public static byte intToByte(int i) {
        byte targets1 = (byte) (i & 0xFF);
        return targets1;
    }

    public static byte[] intToByte2(int i) {
        byte[] targets1 = new byte[2];
        targets1[1] = (byte) (i & 0xFF);
        targets1[0] = (byte) (i >> 8 & 0xFF);
        return targets1;
    }

    public static byte[] slice(byte[] buff,int start,int end){
        byte[] newbyte=new byte[end-start+1];
        for (int i=0;i<(end-start+1);i++){
            newbyte[i]=buff[start+i];
        }
        return newbyte;
    }
    public static void slicecopy(byte[] srcbuff,int srcstart,int srcend,byte[] destbuff,int deststart,int destend){
        if ((srcend-srcstart)==(destend-deststart)){
            for (int i=0;i<=(destend-deststart);i++){
                destbuff[deststart+i]=srcbuff[srcstart+i];
            }
        }
        else{
            System.out.println("长度不匹配！");
        }
    }
    //数据帧86byte：模式1，目的主机4，目的端口2，数据大小1，窗口大小1，//帧数4，序列号4，误码标志1,//总数据量大小4,数据56,时间8,
    public static void make_dataFrame(ArrayList<byte[]> frameArray, byte[] buff, int mode, String host, int port, int datasize, int winsize,int frameSeq){

//        int frameAmount=(dataAmount/datasize)+1;
        int count=0;
        byte[] frame=new byte[86];
        frame[0] =intToByte(mode);
        byte[] target=new byte[]{0,1,2,3};
        slicecopy(target,0,3,frame,1,4);
        target = intToByte2(port);
        slicecopy(target,0,1,frame,5,6);
        frame[7] = intToByte(datasize);
        frame[8] = intToByte(winsize);
//               target=intToByte4(frameAmount);
//               slicecopy(target,0,3,frame,9,12);
        target = intToByte4(frameSeq);
        slicecopy(target,0,3,frame,13,16);
        frame[17]=intToByte(1);

//                slicecopy(intToByte4(dataAmount),0,3,frame,18,21);
        slicecopy(buff,0,55,frame,22,77);
        frameArray.add(frame);
    }
    public static byte[] make_Backframe(byte[] buff){
        byte[] backbuff=new byte[13];
//        System.out.println(Arrays.toString(buff));
        slicecopy(buff,13,17,backbuff,0,4);
        slicecopy(buff,78,85,backbuff,5,12);
        return backbuff;

    }
}
