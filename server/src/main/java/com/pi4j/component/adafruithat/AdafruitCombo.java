package com.pi4j.component.adafruithat;

import java.util.HashMap;
import java.util.Map;

public class AdafruitCombo extends AdafruitMotorHat {

    Map<String, AdafruitServo> servos = new HashMap<>();

    public AdafruitCombo(int deviceAddr) {
        super(deviceAddr);
        servos.put("S00", new AdafruitServo(this, "S01"));
        servos.put("S01", new AdafruitServo(this, "S02"));
        servos.put("S14", new AdafruitServo(this, "S15"));
        servos.put("S15", new AdafruitServo(this, "S16"));
    }

    /**
     * Create an AdafruitServo instance for this servo. Each of the 16 servo pin sets
     * is assigned a unique name "S01" through "S16".  The pin sets are labeled on
     * the Adafruit Servo HAT with numbers 0 through 15. "S01" corresponds to pin set 0,
     * "S02" to pin set 1, etc.
     * @param servo valid values "S01" through "S16"
     * @return AdafruitServo instance for the specified servo
     */
    public AdafruitServo getServo(String servo) {
        if(servos.containsKey(servo)) {
            return servos.get(servo);
        }else {
            throw new IllegalArgumentException("Could not find servo, available servos: S00, S01, S14, S15");
        }
    }
}
