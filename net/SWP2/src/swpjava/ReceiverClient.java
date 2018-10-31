package swpjava;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class ReceiverClient {
    private String host;
    private int port;
    private String receiverFilename="D:/copy2.txt";
    private Socket socket=null;
    private ArrayList<byte[]> frameArray=new ArrayList<>();
    private ArrayList<byte[]> backArray=new ArrayList<>();
    private int frameAmount=0;//收到有效帧的数量
    private int max=6;
    private int min=0;
    private int winsize=7;
    private int backAmount=0;//缓存的帧数
    public ReceiverClient(String host,int port){
        try{
            socket=new Socket(host,port);
            BufferedOutputStream bos=new BufferedOutputStream(socket.getOutputStream());
            byte[] request=SWPUtils.intToByte2(1);
            bos.write(request);
            bos.flush();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void receiverStart(){
        Thread datathread=new Thread(new DataThread());
        Thread backthread=new Thread(new BackThread());
        datathread.start();
        backthread.start();
    }
    class DataThread implements Runnable{
        @Override
        public void run() {
            try {
                System.out.println(socket==null);
                BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());
                BufferedOutputStream bosback=new BufferedOutputStream(socket.getOutputStream());
                byte[] buff = new byte[86];
                int len = 0;
                while (-1 != (len = bis.read(buff))) {
                    System.out.println("开始接受");
                    synchronized ("lock") {
                        int flag = SWPUtils.byteToInt(buff[17]);
                        int frameSeq = SWPUtils.byte4ToInt(SWPUtils.slice(buff, 13, 16));
                        int frameAmount2=SWPUtils.byte4ToInt(SWPUtils.slice(buff,9,12));
                        if(frameSeq<min){
                            //重帧
                            frameArray.subList(frameSeq,min-1).clear();
                            backArray.subList(backAmount+frameSeq-min,backAmount-1).clear();
                            backAmount=backAmount+frameSeq-min;
                            min=frameSeq;
                            frameAmount=frameAmount+frameSeq-min;
                            System.out.println("重帧");
                        }
                        if (frameSeq==min){
                            if (flag==1){
                                frameArray.add(buff);
                                byte[] backbuff=SWPUtils.make_Backframe(buff);
                                backArray.add(backbuff);
                                min++;
                                frameAmount++;
                                backAmount++;
                                System.out.println("接受时backAmount"+backAmount);
                                System.out.println("正在传送第"+frameAmount+"帧");
                            }
                            else {
                                byte[] backbuff=SWPUtils.make_Backframe(buff);
                                bosback.write(backbuff);
                            }
                        }
                        //丢帧不要
                        //传输完成
                        if (frameSeq==(frameAmount2-1)){
                            break;
                        }
                    }
                }
                writeFile();
                System.out.println("接受完成");
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    class BackThread implements Runnable{
        @Override
        public void run() {
            try {
                    BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());
                    while (true) {
                        while (backAmount > 0) {
                            synchronized ("lock") {
                                System.out.println("backAmount"+backAmount);
                                System.out.println("开始回传2");
                                byte[] backbuff = backArray.get(0);
                                bos.write(backbuff);
                                bos.flush();
                                backArray.remove(0);
                                backAmount--;
                                max++;
                            }
                        }
                    }
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    public void writeFile(){
        try {
            File file = new File(receiverFilename);
            if (!file.exists()) {
                System.out.println("不存在此文件！");
            }
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            for (int i = 0; i < frameAmount; i++) {
                byte[] framebuff=frameArray.get(i);
                byte[] databuff=SWPUtils.slice(framebuff,22,77);
                bos.write(databuff);
                bos.flush();
            }
            System.out.println("文件导出完成！");
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {

        ReceiverClient receiverClient=new ReceiverClient("127.0.0.1",8888);
        receiverClient.receiverStart();
    }
}
