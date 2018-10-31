package swpjava;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.Socket;
import java.time.Clock;
import java.util.ArrayList;

public class SenderClient {
    private String host;
    private int port;
    private String sendFilename="D:/copy.txt";
    private Socket socket=null;
    private int min=0;
    private int max=6;
    private int backnum=0;
    private long backtime=0;
    private int winsize=7;
    private int frameAmount=0;
    private int dataAmount=0;
    private long timelimit=2000;
    private ArrayList<byte[]> frameArray=new ArrayList<>();
    private ArrayList<Long> timeList=new ArrayList<>();
    private ArrayList<Long> sendtimeList=new ArrayList<>();
    private ArrayList<Long> backtimeList=new ArrayList<>();
    public SenderClient(String host,int port){
        try{
            socket=new Socket(host,port);
            BufferedOutputStream bos=new BufferedOutputStream(socket.getOutputStream());
            byte[] request=SWPUtils.intToByte2(0);
            bos.write(request);
            bos.flush();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    class DataThread implements Runnable{
        @Override
        public void run() {
            try {
                File senderFile = new File(sendFilename);
                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(senderFile));
                BufferedOutputStream bos=new BufferedOutputStream(socket.getOutputStream());
                Clock clock=Clock.systemUTC();
                long time=0;
                timeList.add(time);
                int len=0;
                byte[] buff=new byte[56];
                while (-1!=(len=bis.read(buff))){
                    synchronized ("lock2") {
                        SWPUtils.make_dataFrame(frameArray, buff, 0, host, port, len, winsize,frameAmount);
                        frameAmount += 1;
                        dataAmount += len;
                    }
                }
                while(min<frameAmount) {
                    synchronized ("lock2") {
                        if ((max - min) < winsize && (max - min) >= 0) {
                            time = clock.millis();
                            backtime = timeList.get(backnum);
                            //超时处理
                            if (timeList.get(0) != 0 && (time - backtime > timelimit)) {
                                min = backnum;
                                if (max - min >= winsize) {
                                    max = min + winsize - 1;
                                }
                            }
                            byte[] framebuff = frameArray.get(min);
                            byte[] frameAmountArray = SWPUtils.intToByte4(frameAmount);
                            byte[] dataAmountArray = SWPUtils.intToByte4(dataAmount);
                            SWPUtils.slicecopy(frameAmountArray, 0, 3, framebuff, 9, 12);
                            SWPUtils.slicecopy(dataAmountArray, 0, 3, framebuff, 18, 21);
                            if (backtime == 0) {
                                timeList.remove(0);
                            }
                            backtime = clock.millis();
                            timeList.add(backtime);
                            byte[] timebuff = SWPUtils.longToByte8(backtime);
                            SWPUtils.slicecopy(timebuff, 0, 7, framebuff, 78, 85);
                            bos.write(framebuff);
                            bos.flush();
                            sendtimeList.add(clock.millis());
                            min++;
                            System.out.println("正在发送" + min);
                            System.out.println("min" + min + "max" + max + "winsize" + winsize);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    class BackThread implements Runnable{
        @Override
        public void run() {
            try{
                BufferedInputStream bis=new BufferedInputStream(socket.getInputStream());
                int len=0;
                byte[] backbuff=new byte[13];
                while(-1!=(len=bis.read(backbuff))){
                    synchronized ("lock2") {
                        int flag = SWPUtils.byteToInt(backbuff[4]);
                        byte[] frameSeqBuff = SWPUtils.slice(backbuff, 0, 3);
                        int frameSeq = SWPUtils.byte4ToInt(frameSeqBuff);
                        byte[] backtime2buff = SWPUtils.slice(backbuff, 5, 12);
                        long backtime2 = SWPUtils.byte8ToLong(backtime2buff);
                        if (frameSeq == backnum && flag == 1 && backtime2 == backtime) {
                            backnum++;
                            max++;
                            backtimeList.add(backtime2);
                        } else if (flag == 0 || frameSeq > backnum) {
                            min = backnum;
                            if ((max - min) >= winsize) {
                                max = min + winsize - 1;
                            }
                        }
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    public void senderStrat(){
        Thread sender=new Thread(new DataThread());
        Thread receiver=new Thread(new BackThread());
        sender.start();
        receiver.start();
    }
    public static void main(String[] args) {
        SenderClient senderClient=new SenderClient("127.0.0.1",8888);
        senderClient.senderStrat();
    }
}

