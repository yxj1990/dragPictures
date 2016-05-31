package com.example.homelink.dragpictures;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    DragPicturesView  mDragPictureView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDragPictureView = new DragPicturesView(this);
        ((RelativeLayout) findViewById(R.id.rl)).addView(mDragPictureView);
        setGridViewData();
    }


    /**
     * 刷新当前页面数据
     */
    private void setGridViewData() {
        mDragPictureView.RemoveAllViews();

        for (int i=0;i<8;i++){
            View convertView = LayoutInflater.from(this).inflate(R.layout.item_minsu_picture, null, false);
            ImageView img = (ImageView) convertView.findViewById(R.id.iv_picture);
            img.setImageResource(getResources().getIdentifier("i"+i,
                    "mipmap", this.getPackageName()));
//            img.setImageResource(R.drawable.icon_delete);
            mDragPictureView.AddView(convertView);
        }

        mDragPictureView.setEditType(1);
        mDragPictureView.redraw();
    }
}
