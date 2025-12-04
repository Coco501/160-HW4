package com.ecs160;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.SpringApplication;

@SpringBootApplication
public class App 
{
    public static void main(String[] args)
    {
        System.out.println("Hello from the microservices project!");
        SpringApplication.run(App.class, args);
    }
}
