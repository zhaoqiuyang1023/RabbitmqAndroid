package com.example.rabbitmqandroid;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.QueueingConsumer;


public class MainActivity extends AppCompatActivity {


    private final static String EXCHANGE_NAME = "test_exchange_direct";

    private Handler handler = new Handler();
    TextView user1, user2;
    EditText editText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button button = findViewById(R.id.send);
        Button quanfa=findViewById(R.id.qunfa);
        quanfa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        fun("all");
                    }
                }).start();
            }
        });
        editText = findViewById(R.id.edit_user);
        user1 = findViewById(R.id.user1);
        user2 = findViewById(R.id.user2);
        //模拟两个用户监听
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.i("12", "user1,user2开始监听");
                react("user2");
                react("user1");

            }
        }).start();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        fun(editText.getText().toString());
                        //   react();
                    }
                }).start();


            }
        });
    }


    private void react(final String QUEUE_NAME) {
        try {
            Log.i("用户",QUEUE_NAME);
            // 获取到连接以及mq通道
            Connection connection = ConnectionUtil.getConnection();
            // 从连接中创建通道
            Channel channel = connection.createChannel();
            // 声明队列
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);

            // 绑定队列到交换机
            //QUEUE_NAME是用户id,routekey 是公司id
            channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "all");
            channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, QUEUE_NAME);

            // 定义队列的消费者
            final QueueingConsumer consumer = new QueueingConsumer(channel);

            // 监听队列
            channel.basicConsume(QUEUE_NAME, true, consumer);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    // 获取消息
                    while (true) {
                        QueueingConsumer.Delivery delivery = null;
                        try {
                            delivery = consumer.nextDelivery();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        final String message = new String(delivery.getBody());
                        handler.post(new Runnable() {

                            public void run() {
                                if (QUEUE_NAME.equals("user1")) {
                                    user1.setText(message);
                                } else {
                                    user2.setText(message);
                                }
                            }
                        });
                        System.out.println(" [x] Received '" + message + "'");
                        Log.i("12", "[x] Received '" + message + "'");
                    }
                }
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fun(String userId) {
        try {
            Log.i("用户",userId);
            // 获取到连接以及mq通道
            Connection connection = ConnectionUtil.getConnection();
            // 从连接中创建通道
            Channel channel = connection.createChannel();
            // 声明exchange
            channel.exchangeDeclare(EXCHANGE_NAME, "direct");


            // 消息内容
            String message;
            for (int i = 0; i < 100000000; i++) {
                message = "" + i;
                channel.basicPublish(EXCHANGE_NAME, userId, null, message.getBytes());
                System.out.println(" [x] Sent '" + message + "'");
               // Thread.sleep(1000);
            }

            //关闭通道和连接
            channel.close();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
