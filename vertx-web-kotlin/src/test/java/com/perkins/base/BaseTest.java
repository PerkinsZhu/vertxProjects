package com.perkins.base;

import org.junit.Test;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
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
                int i = 0 ;
                while (( i = in.read(array)) != -1) {
                    System.out.print(new String(array));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        cachedThreadPool.awaitTermination(1, TimeUnit.DAYS);
    }
}
