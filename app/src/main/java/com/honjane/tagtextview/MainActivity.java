package com.honjane.tagtextview;

import android.app.Dialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.honjane.tagtextlib.TagTextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private boolean isClick;
    String [] citys = {"北京","上海","广州","深圳","杭州","南京","天津","重庆","长沙","郑州","成都","西安"};
    String [] scenics = {"洞天深处","缕月云开","菇古涵","高水长","上下天光","菊院荷风","坐石临流","武陵春色","柳浪闻莺","水木明瑟","西峰秀色","菱荷香","紫碧山房"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {

        final List<String> tags = new ArrayList<>();
        for (int i = 0; i < citys.length; i++) {
            tags.add(citys[i]);
        }


        List<String> tags1 = new ArrayList<>();
        for (int i = 0; i < scenics.length; i++) {
            tags1.add(scenics[i]);
        }


        TagTextView tagView = (TagTextView) findViewById(R.id.tag_view);
        TagTextView tagView1 = (TagTextView) findViewById(R.id.tag_view1);
        final TagTextView tagView2 = (TagTextView) findViewById(R.id.tag_view2);
        tagView.setTags(tags);
        tagView1.setTags(tags1);
        tagView2.setTags(tags);
        tagView2.setTagClickListener(new TagTextView.ITagClickListener() {
            @Override
            public void onTagClick(int position) {
                Toast.makeText(MainActivity.this,"点击了tag item "+tags.get(position),Toast.LENGTH_LONG).show();
            }

            @Override
            public void onMoreClick() {
                Toast.makeText(MainActivity.this,"点击了more ",Toast.LENGTH_LONG).show();
                tagView2.setSingleLine(false);
            }

        });
    }
}
