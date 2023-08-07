package ru.barrier;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ru.barrier.services.AddDataTest;

@SpringBootApplication
public class BarrierSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(BarrierSystemApplication.class);

//        AddDataTest addDataTest = new AddDataTest();
//        addDataTest.newUser();
    }
}
