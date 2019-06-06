package com.perkins.base;

import org.junit.Test;
import org.junit.internal.runners.statements.RunAfters;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class BaseTest {

    ExecutorService cachedThreadPool = Executors.newCachedThreadPool();

    @Test
    public void testPipStream() throws InterruptedException, IOException {
        PipedInputStream in = new PipedInputStream(1024 * 2);
        PipedOutputStream out = new PipedOutputStream();
        in.connect(out);
        cachedThreadPool.execute(() -> {
            try {
                for (int i = 0; i < 10; i++) {
                    out.write("你好\r\n".getBytes());
                    Thread.sleep(1000);
                }
                out.close();
                in.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        cachedThreadPool.execute(() -> {
            try {
                byte[] array = new byte[1024];
                int i = 0;
                while ((i = in.read(array)) != -1) {
                    System.out.print(new String(array));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        cachedThreadPool.awaitTermination(1, TimeUnit.DAYS);
    }

    @Test
    public void testInputStream() throws FileNotFoundException {
        InputStream inputStream = new FileInputStream(new File("D:\\zhupingjing\\testFile\\upload.txt"));
        //TODO 分析各种流的实现机制，源码
    }

    @Test
    public void testException() {
        Exception ex = new RuntimeException("错误消息");
        System.out.println(ex.getMessage());
    }

    @Test
    public void testSocket() throws InterruptedException, IOException {
        ServerSocket serrver = new ServerSocket(8888);
        while (true) {
            Socket socket = serrver.accept();
            System.out.println("-accepted---");
            cachedThreadPool.execute(() -> {
                try {
                    InputStream inputStream = socket.getInputStream();
                    byte[] bytes = new byte[50];
                    int len;
                    while ((len = inputStream.read(bytes)) != -1) {
                        System.out.println("len-->" + len);
                        printByteArray(bytes, len);
//                    System.out.println(bytesToHexString(bytes));
//                    sb.append(new String(bytes, 0, len, "UTF-8"));
                    }
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public static final String bytesToHexString(byte[] bArray) {
        StringBuffer sb = new StringBuffer(bArray.length);
        String sTemp;
        for (int i = 0; i < bArray.length; i++) {
            sTemp = Integer.toHexString(0xFF & bArray[i]);
            if (sTemp.length() < 2)
                sb.append(0);
            sb.append(sTemp.toUpperCase());
        }
        return sb.toString();
    }

    public static void printByteArray(byte[] array, int limit) {
        for (int i = 0; i < limit; i++) {
            System.out.print(array[i]);
        }
        System.out.println();
    }
}
