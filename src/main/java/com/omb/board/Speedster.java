package com.omb.board;

public interface Speedster {
    void forward(float speed);
    void back(float speed);
    void left();
    void right();
    void handBreak();
    void releaseAccelerator();
    void releaseWheel();


    void viewLeft(float speed);
    void viewRight(float speed);
    void viewUp(float speed);
    void viewDown(float speed);
    void viewCenter();
    void holdHorizontalView();
    void holdVerticalView();
}
