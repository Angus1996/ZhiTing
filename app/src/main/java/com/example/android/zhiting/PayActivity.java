package com.example.android.zhiting;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class PayActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay);

        Button wechatPay = (Button) findViewById(R.id.wechat_pay_button);
        Button aliPay = (Button) findViewById(R.id.ali_pay_button);

        wechatPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(PayActivity.this, "You have choosen wechat to pay!", Toast.LENGTH_SHORT).show();
            }
        });

        aliPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(PayActivity.this, "You have choosen ali to pay!",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
