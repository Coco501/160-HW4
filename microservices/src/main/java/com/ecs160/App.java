package com.ecs160;

public class App 
{
    public static void main( String[] args )
    {
        System.out.println( "Hello from the microservices project!" );
        Launcher launcher = new Launcher();
        launcher.launch(8080);
    }
}
