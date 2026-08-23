package com.evoting.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ElectronicVotingSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(ElectronicVotingSystemApplication.class, args);
        System.out.println("\n=======================================================");
        System.out.println("  SECUREVOTE - ELECTRONIC VOTING SYSTEM IS NOW ONLINE  ");
        System.out.println("  Access Web Portal at: http://localhost:8080         ");
        System.out.println("  Access H2 Console at: http://localhost:8080/h2-console ");
        System.out.println("=======================================================\n");
    }
}
