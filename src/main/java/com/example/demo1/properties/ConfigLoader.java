package com.example.demo1.properties;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {
    private static Properties properties = new Properties();

    // static initializer: 클래스 로딩 시 딱 한 번 실행
    static {
        try (InputStream input = ConfigLoader.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                System.out.println("Sorry, unable to find application.properties");
            } else {
                properties.load(input);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // static 메서드로 접근
    public static String getIp() {
        return properties.getProperty("ip");
    }

    public static int getPort() {
        String portStr = properties.getProperty("port");
        if (portStr == null) return -1;
        return Integer.parseInt(portStr);
    }
}
