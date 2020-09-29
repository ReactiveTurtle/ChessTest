package ru.reactiveturtle.test.chessneuron;

public class Step {
    int startX, startY;
    int endX, endY;
    int figure;

    public Step(int startX, int startY, int endX, int endY, int figure) {
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        this.figure = figure;
    }
}
