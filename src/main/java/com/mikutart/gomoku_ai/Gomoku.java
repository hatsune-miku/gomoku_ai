package com.mikutart.gomoku_ai;

/**
 * Created by Hatsune Miku on 3/27/2018.
 */
class pointrank {
    int x;
    int y;
    int origin;
    int combo;
    int tolerate;
    int score;
    boolean secured;
    pointrank(int x, int y, int origin, int combo, int tolerate, int score, boolean secured) {
        this.x = x;
        this.y = y;
        this.origin = origin;
        this.combo = combo;
        this.tolerate = tolerate;
        this.score = score;
        this.secured = secured;
    }
    pointrank(){};
};

public class Gomoku {
    static final int BLACK = 2;
    static final int WHITE = 1;
    private int width;
    private int height;
    private int last_b_x;
    private int last_b_y;
    private int last_w_x;
    private int last_w_y;
    private int turns;
    private int max_turns;
    public int board[][];
    Gomoku(int width, int height) {
        this.width = width;
        this.height = height;
        this.last_b_x = 0;
        this.last_b_y = 0;
        this.last_w_x = 0;
        this.last_w_y = 0;
        this.max_turns = width * height;
        board = new int[width][height];
        reset();
    }
    public void reset() {
        turns = 0;
        for (int i = 0; i < height; i++) {
            board[i] = new int[width];
            for (int j = 0; j < width; j++)
                board[i][j] = 0;
        }
    }
    public boolean canplace(int x, int y) {
        if (!verify_point(x, y)) return false;
        return (board[x][y] == 0);
    }
    public boolean place(int x, int y, int origin) {
        if (!canplace(x, y)) return false;
        board[x][y] = origin;
        if (origin == BLACK) {
            last_b_x = x;
            last_b_y = y;
        }
        else {
            last_w_x = x;
            last_w_y = y;
        }
        turns++;
        return true;
    }

    public void undo() {
        board[last_b_x][last_b_y] = 0;
        board[last_w_x][last_w_y] = 0;
        turns -= 2;
    }

    private boolean verify_point(int x, int y) {
        return (0 <= x && x < width && 0 <= y && y < height);
    }
    private int opposite(int origin) {
        if (origin == BLACK) return WHITE;
        return BLACK;
    }
    private pointrank combo(int origin, int x, int y, int dir_x, int dir_y, int limit, int tolerance) {
        pointrank pr = new pointrank(x,y,origin,1,0,0,false);
        if (origin == 0)
            origin = board[x][y];
        pr.origin = origin;
        int step = 0;
        while (verify_point(x+=dir_x, y+=dir_y) && (step++ < limit)) {
            if (board[x][y] == pr.origin)
                pr.combo++;
            else if (board[x][y] == opposite(pr.origin)) {
                pr.secured = true;
                return pr;
            }
            else if (++pr.tolerate >= tolerance) {
                if (step <= 1) pr.combo = 0;
                return pr;
            } else {
                pr.combo++;
            }
        }
        return pr;
    }
    private int combo(int origin, int x, int y, int dir_x, int dir_y, int limit) {
        //pointrank pr = new pointrank(x,y,origin,1,0,0,false);
        if (origin == 0)
            origin = board[x][y];
        int count = 1;
        int step = 0;
        while (verify_point(x+=dir_x, y+=dir_y) && (step++ < limit)) {
            if (board[x][y] == origin)
                count++;
            else if (board[x][y] == opposite(origin)) {
                count--;
                //pr.secured = true;
                return count;
            } else {
                return count;
            }
        }
        return count;
    }
    public int the_winner_is() {
        if (turns >= max_turns) return -3;
        int origin = 0;
        int dir[][] = { {-1,0},{-1,1},{0,1},{1,1},{1,0},{1,-1},{0,-1},{-1,-1} };
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                for (int k = 0; k < 8; k++) {
                    if (board[i][j] != 0) {
                        pointrank pr = combo(0, i, j, dir[k][0], dir[k][1], 5, 0);
                        if (pr.combo >= 5)
                            return pr.origin;
                    }
                }
            }
        }
        return 0;
    }

    public int score_for_point(int x, int y, int origin) {
        int dir[][] = { {-1,0,1,0},{0,1,0,-1},{-1,-1,1,1},{-1,1,1,-1} };
        int score = 0;
        for (int k = 0; k < 4; k++) {
            int count = combo(origin, x, y, dir[k][0], dir[k][1], 6);
            count += combo(origin, x, y, dir[k][2], dir[k][3], 6);
            if(count >= 5)
                score += 10000;
            else if(count == 4)
                score += 1500;
            else if(count == 3)
                score += 1000;
            else if(count == 2)
                score += 700;
        }
        if(x < 4 || x > (width - 4) || y < 4 || y > (height - 4)) {
            score -= 100;
        }
        return score;
}

    private pointrank seek_point_for(int origin) {
        pointrank best = new pointrank();
        int score = -2147483647;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (canplace(i, j)) {
                    int s = score_for_point(i, j, origin);
                    if (s > score) {
                        best.x = i;
                        best.y = j;
                        best.score = s;
                        score = s;
                    }
                }
            }
        }
        return best;
    }

    public void computer_turn(int origin) {
        pointrank pr_b = seek_point_for(BLACK);
        pointrank pr_w = seek_point_for(WHITE);
        if(pr_b.score == 100000) {
            place(pr_b.x, pr_b.y, origin);
        } else if(pr_w.score == 100000) {
            place(pr_w.x, pr_w.y, origin);
        } else if (origin == BLACK) {
            if (pr_b.score > pr_w.score+100)
                place(pr_b.x, pr_b.y, origin);
            else
                place(pr_w.x, pr_w.y, origin);
        }
        else {
            if (pr_w.score > pr_b.score+100)
                place(pr_w.x, pr_w.y, origin);
            else
                place(pr_b.x, pr_b.y, origin);
        }
    }
}
