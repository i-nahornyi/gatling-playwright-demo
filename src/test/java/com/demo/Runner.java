package com.demo;

import io.gatling.app.Gatling;
import io.gatling.shared.cli.GatlingCliOptions;

public class Runner {

    public static void main(String[] args) {
        String [] runParams = new String[]{
                GatlingCliOptions.Simulation.shortOption(),"com.demo.simulation.DemoSimulation",
                GatlingCliOptions.ResultsFolder.shortOption(), "target/gatling"
        };

        Gatling.main(runParams);
    }

}
