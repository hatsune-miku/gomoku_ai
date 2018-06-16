package com.mikutart.gomoku_ai;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceFragment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.GridView;

public class MainActivity extends AppCompatActivity {

    Gomoku game;
    Handler handler;
    GridViewAdapter adapter;
    int current = Gomoku.BLACK;
    int winner = 0;
    boolean finished = false;
    CheckBox cb1;
    CheckBox cb2;
    CheckBox cb3;

    void msgbox(String text) {
        AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this, R.style.NormalAlertDialogTheme);
        adb.setTitle(text)
                .setPositiveButton(getString(R.string.mb_ok), null)
                .setCancelable(false)
                .show();
    }
    void msgbox(String title, String text, DialogInterface.OnClickListener onClickListener) {
        AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this, R.style.NormalAlertDialogTheme);
        adb.setTitle(title)
                .setMessage(text)
                .setPositiveButton(getString(R.string.mb_ok), onClickListener)
                .show();
    }

    void somebodywins(boolean in_thread) {
        finished = false;
        switch (game.the_winner_is()) {
            case Gomoku.BLACK:
                finished = true;
                winner = Gomoku.BLACK;
                if(!in_thread) {
                    msgbox(getString(R.string.black_wins));
                } else {
                    Looper.prepare();
                    msgbox(getString(R.string.black_wins));
                    Looper.loop();
                }
                break;
            case Gomoku.WHITE:
                finished = true;
                winner = Gomoku.WHITE;
                if(!in_thread) {
                    msgbox(getString(R.string.white_wins));
                } else {
                    Looper.prepare();
                    msgbox(getString(R.string.white_wins));
                    Looper.loop();
                }
                break;
            case -3:
                finished = true;
                winner = -1;
                if(!in_thread) {
                    msgbox(getString(R.string.draw));
                } else {
                    Looper.prepare();
                    msgbox(getString(R.string.draw));
                    Looper.loop();
                }
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final View rootview = findViewById(R.id.rootview);
        rootview.setBackgroundColor(getResources().getColor(R.color.sgrey));

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final GridView board = findViewById(R.id.board);
        cb1 = findViewById(R.id.cb_1);
        cb2 = findViewById(R.id.cb_2);
        cb3 = findViewById(R.id.cb_3);

        game = new Gomoku(15, 15);
        adapter = new GridViewAdapter(MainActivity.this, game);
        board.setAdapter(adapter);

        handler = new Handler() {
            public void handleMessage(Message p) {
                switch (p.what) {
                    case -1:
                        adapter.notifyDataSetChanged();
                        break;
                    case 0:
                        adapter.notifyDataSetChanged();
                        somebodywins(false);
                        break;
                    case 1:
                        toolbar.setTitle(getString(R.string.processing));
                        break;
                    case 2:
                        toolbar.setTitle(getString(R.string.app_name));
                        break;
                }
                super.handleMessage(p);
            }
        };

        board.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> adapterView, View view, int i, long l) {

                if (finished) {
                    return;
                }

                i++;
                int y = 0;
                while (i > 15) {
                    i -= 15;
                    y++;
                }
                int x = i - 1;

                if(cb3.isChecked()) {
                    String scoreb = String.valueOf(game.score_for_point(x,y,Gomoku.BLACK));
                    String scorew = String.valueOf(game.score_for_point(x,y,Gomoku.WHITE));
                    msgbox("Debug","point " + String.valueOf(x) + "," + String.valueOf(y) + " score\nfor white: " + scorew + "\nfor black: " + scoreb, null);
                    return;
                }

                if (game.canplace(x, y)) {
                    game.place(x, y, current);
                    adapter.notifyDataSetChanged();
                    somebodywins(false);
                    if(finished) return;
                } else {
                    return;
                }
                if (!cb2.isChecked()) {
                    new Thread() {
                        @Override
                        public void run() {
                            if (cb1.isChecked()) {
                                handler.sendEmptyMessage(1);
                                try {
                                    sleep(800);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                handler.sendEmptyMessage(2);
                            }
                            game.computer_turn(Gomoku.WHITE);
                            handler.sendEmptyMessage(0);
                        }
                    }.start();
                } else {
                    if (current == Gomoku.WHITE) {
                        current = Gomoku.BLACK;
                    } else {
                        current = Gomoku.WHITE;
                    }
                }
            }
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this, R.style.NormalAlertDialogTheme);
                dialog.setTitle("选择一项")
                        .setItems(R.array.menu, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                switch (i) {
                                    case 0:
                                        game.undo(); adapter.notifyDataSetChanged(); break;
                                    case 1:
                                        game.reset(); adapter.notifyDataSetChanged(); finished = false; break;
                                    case 3:
                                        msgbox("EVE", "即对弈双方都由 AI 扮演。\n游戏将快速结束。", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                game.reset();
                                                adapter.notifyDataSetChanged();
                                                int p = (int)(15*Math.random());
                                                game.place(p,p,Gomoku.BLACK);
                                                finished = true;
                                                PVE();
                                            }
                                        });
                                        break;
                                    case 2:
                                        msgbox("ｸﾞｯ!(๑•̀ㅂ•́)و✧","Gomoku Artificial Idiot\n\nCoolapk: Eggtart\nWechat: A1knla", null);
                                }
                            }
                        })
                        .setNegativeButton("取消", null)
                        .show();
            }
        });

    }
    void PVE() {
        new Thread() {
            @Override
            public void run() {
                while(true) {
                    if(cb1.isChecked()) {
                        try {
                            sleep(800);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    game.computer_turn(Gomoku.WHITE);
                    handler.sendEmptyMessage(-1);
                    somebodywins(true);
                    if(finished) return;
                    if(cb1.isChecked()) {
                        try {
                            sleep(800);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    game.computer_turn(Gomoku.BLACK);
                    handler.sendEmptyMessage(-1);
                    somebodywins(true);
                    if(finished) return;
                }

            }
        }.start();
    }
}
