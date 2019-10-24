package com.example.rabbitmqandroid;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;

public class ConnectionUtil {
    private static Connection connection = null;

    public static Connection getConnection() throws Exception {
        if (connection == null) {
            //定义连接工厂
            ConnectionFactory factory = new ConnectionFactory();
            //设置服务地址
            factory.setHost("192.168.30.103");
            //端口
            factory.setPort(5672);
            //设置账号信息，用户名、密码、vhost
            factory.setVirtualHost("testhost");
            factory.setUsername("admin");
            factory.setPassword("123456");
            // 通过工程获取连接
            Connection connection = factory.newConnection();
            return connection;
        }
        return connection;

    }
}
