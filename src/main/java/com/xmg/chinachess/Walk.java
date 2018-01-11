package com.xmg.chinachess;

public class Walk {

    public int x1, x2, y1, y2;

    public Walk(int x1, int y1, int x2, int y2) {
        super();
        this.x1 = x1;
        this.x2 = x2;
        this.y1 = y1;
        this.y2 = y2;
    }

    public Walk() {
        super();
    }

    public static Walk fromString(String data) {
        String tag[] = data.split(",");
        return new Walk(Integer.parseInt(tag[0]), Integer.parseInt(tag[1]), Integer.parseInt(tag[2]),
                Integer.parseInt(tag[3]));

    }

    @Override
    public String toString() {
        return x1 + "," + y1 + "," + x2 + "," + y2;
    }
}
