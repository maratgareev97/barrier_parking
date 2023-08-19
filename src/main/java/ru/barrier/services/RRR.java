package ru.barrier.services;

public class RRR extends Thread {
    @Override
    public void run() {
        Integer i = 0;
        while (i < 60) {
            System.out.println(i + " 11111111111111111111111111111111111");
            i++;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println("Усе");
    }
}
