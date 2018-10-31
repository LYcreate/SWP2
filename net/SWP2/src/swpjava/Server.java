package swpjava;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {
    private ServerSocket ss=null;
    private Socket senderSocket=null;
    private Socket receiverSocket=null;
    private int port;
    private int senderIndex=0;
    private int receiverIndex=0;
    private ArrayList<byte[]> frameArray=new ArrayList<>();
    public Server(int port){
        try{
            this.port=port;
            ss=new ServerSocket(port);
            while(true) {
                Socket socket=ss.accept();
                BufferedInputStream bis=new BufferedInputStream(socket.getInputStream());
                byte[] modebuff=new byte[2];
                bis.read(modebuff);
                int mode=SWPUtils.byte2ToInt(modebuff);
                if (receiverSocket==null&&mode==1) {
                    receiverSocket=socket;
                    System.out.println("接收端已连接到服务器");
                    if(senderSocket!=null&&receiverSocket!=null){
                        break;
                    }
                }
                else if (senderSocket==null&&mode==0){
                    senderSocket=socket;
                    System.out.println("发送端已连接到服务器");
                    if(senderSocket!=null&&receiverSocket!=null){
                        break;
                    }
                }
            }
            System.out.println("开始工作");
            Thread dataThread=new Thread(new DataThread(senderSocket,receiverSocket));
            Thread backThread=new Thread(new BackThread(senderSocket,receiverSocket));
            dataThread.start();
            backThread.start();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    class DataThread implements Runnable{
        private Socket senderSocket=null;
        private Socket receiverSocket=null;
        public DataThread(Socket senderSocket,Socket receiverSocket){
            this.receiverSocket=receiverSocket;
            this.senderSocket=senderSocket;
        }
        @Override
        public void run() {
            try {
                BufferedInputStream bis = new BufferedInputStream(senderSocket.getInputStream());
                BufferedOutputStream bos=new BufferedOutputStream(receiverSocket.getOutputStream());
                int len=0;
                byte[] buff=new byte[86];
                while(-1!=(len=bis.read(buff))){
                    System.out.println("正在传送");
                    synchronized ("lock1") {
                        bos.write(buff, 0, len);
                        bos.flush();
                        for (int i = 0; i < 86; i++) {
                            System.out.println(buff[i]);
                        }

                        System.out.println("正在传送");
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    class BackThread implements Runnable{
        private Socket senderSocket=null;
        private Socket receiverSocket=null;
        public BackThread(Socket senderSocket,Socket receiverSocket){
            this.receiverSocket=receiverSocket;
            this.senderSocket=senderSocket;
        }
        @Override
        public void run() {
            try {
                BufferedInputStream bis = new BufferedInputStream(receiverSocket.getInputStream());
                BufferedOutputStream bos=new BufferedOutputStream(senderSocket.getOutputStream());
                int len=0;
                byte[] buff=new byte[13];
                while(-1!=(bis.read(buff))){
                    synchronized ("lock1") {
                        bos.write(buff, 0, len);
                        bos.flush();
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
//    class ServerSenderThread implements Runnable{
//        private Socket socket=null;
//        public ServerSenderThread(Socket socket){
//            this.socket=socket;
//        }
//        @Override
//        public void run() {
//            try{
//                System.out.println("服务器发送端线程已开启");
//                BufferedInputStream bis=new BufferedInputStream(socket.getInputStream());
//                int len=0;
//                byte[] buff=new byte[64];
//                while(-1!=(len=bis.read(buff))){
//                    synchronized ("lock") {
//                        frameArray.add(buff);
//                        senderIndex++;
//                    }
//                }
//                bis.close();
//                ss.close();
//            }catch (Exception e){
//                e.printStackTrace();
//            }
//        }
//    }
//
//    class ServerReceiverThread implements Runnable{
//        private Socket socket=null;
//        public ServerReceiverThread(Socket socket){
//            this.socket=socket;
//        }
//        @Override
//        public void run() {
//            try{
//                System.out.println("服务器接收端线程已开启");
//                BufferedOutputStream bos=new BufferedOutputStream(socket.getOutputStream());
//                while(true){
//                    synchronized ("lock") {
//                        if (receiverIndex < senderIndex) {
//                            bos.write(frameArray.get(receiverIndex));
//                            bos.flush();
//                            receiverIndex++;
//                        }
//                    }
//                }
//            }catch (Exception e){
//                e.printStackTrace();
//            }
//        }
//    }

    public static void main(String[] args) {
        new Server(8888);
    }
}
