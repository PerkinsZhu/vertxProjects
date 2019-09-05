package com.perkins.apollo;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigService;
import org.junit.Test;

public class App {

    @Test
    public void baseTest() {
        System.setProperty("DEV.META","http://");
        System.setProperty("env","DEV");
        System.setProperty("app.id","");
        Config config = ConfigService.getAppConfig();
        Integer defaultRequestTimeout = 200;
        Integer age = config.getIntProperty("test.age", defaultRequestTimeout);
        System.out.printf(age + "");
    }

}
