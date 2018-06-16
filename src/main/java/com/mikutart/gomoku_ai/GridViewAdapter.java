package com.mikutart.gomoku_ai;

/**
 * Created by Hatsune Miku on 3/27/2018.
 */

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class GridViewAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private Context mContext;
    private Gomoku game;
    private LinearLayout.LayoutParams params;

    GridViewAdapter(Context context, Gomoku g) {
        game = g;
        mContext = context;
        mInflater = LayoutInflater.from(context);
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.gravity = Gravity.CENTER;
    }
    public int getCount() {
        return 15*15;
    }
    public Object getItem(int position) {
        return "";
    }
    public long getItemId(int position) {
        return position;
    }
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = mInflater.inflate(R.layout.grid_view_cell, null);
        ImageView iv = convertView.findViewById(R.id.img);
        int ico = R.drawable.plusdg;
        if (game != null) {
            int y = 0;
            position++;
            while(position > 15) {
                position -= 15;
                y++;
            }
            int x = position - 1;
            switch (game.board[x][y]) {
                case Gomoku.BLACK:
                    ico = R.drawable.black;
                    break;
                case Gomoku.WHITE:
                    ico = R.drawable.white;
                    break;
            }
        }
        iv.setImageResource(ico);
        convertView.setLayoutParams(params);
        return convertView;
    }
}