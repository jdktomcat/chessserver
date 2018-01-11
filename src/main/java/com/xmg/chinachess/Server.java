package com.xmg.chinachess;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server extends Thread {

    public static ExecutorService es;

    public static ServerSocket serverSocket;

    public static int PORT = 9898;

    public static int ONLINE_LIMIT = 1000;

    private boolean isRunning;

    @Override
    public void run() {
        super.run();
        System.out.println("服务器正在启动。");
        es = Executors.newCachedThreadPool();
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("服务器已经启动。");
            isRunning = true;
            while (isRunning) {
                Socket socket = serverSocket.accept();
                es.execute(new Client(socket));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("服务器结束。");
    }

    public static void main(String[] args) {
        new Server().start();
    }
}
