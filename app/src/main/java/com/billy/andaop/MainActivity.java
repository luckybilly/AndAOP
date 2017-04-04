package com.billy.andaop;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.billy.lib.service.MyUtil;
import com.billy.lib.service.processors.DemoProcessor;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //这里会添加一个start
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            return;//这里也会添加一个end
        }
        setContentView(R.layout.activity_main);
        View view = findViewById(R.id.activity_main);
        view.setOnClickListener(new MyOnClick());
        //在结束的地方会添加一个end
    }

    private void show(String name, String ok
            , int num, int a, float b, double c, boolean d
    ) {
        String myName = "billy_1";
        Toast.makeText(this, myName, Toast.LENGTH_SHORT).show();
        System.out.println("hello" + name + ok + num + a + b + c + d);
    }
    class MyOnClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            show("aaa", "bbb" , 1, 1, 1f, 1.0, false);
            Toast.makeText(MainActivity.this
                    ,MyUtil.getInfo()
                        + "\n" + MyUtil.getDetail()
                        + "\n" + new DemoProcessor("testProcessor").getName()
                    ,Toast.LENGTH_SHORT).show();
        }
    }
}
