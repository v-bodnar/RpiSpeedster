package com.omb;

import com.pi4j.component.adafruithat.AdafruitCombo;
import com.pi4j.component.adafruithat.AdafruitDcMotor;
import com.pi4j.component.adafruithat.AdafruitServo;

public class RpiSpeedsterServer {

    public static void main(String... args) {
//        AdafruitHat adafruitHat;
        System.out.println("Hi");
        final int motorHATAddress = 0X60;
        AdafruitCombo adafruitCombo = new AdafruitCombo(motorHATAddress);
        /*
         * Because the Adafruit motor HAT uses PWMs that pulse independently of
         * the Raspberry Pi the motors will keep running at its current direction
         * and power levels if the program abnormally terminates.
         * A shutdown hook like the one in this example is useful to stop the
         * motors when the program is abnormally interrupted.
         */
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                System.out.println("Turn off all motors");
                adafruitCombo.stopAll();
            }
        });

        AdafruitDcMotor motorLeft = adafruitCombo.getDcMotor("M3");
        motorLeft.setPowerRange(100.0f);
        AdafruitServo servo = adafruitCombo.getServo("S00");


        //Set pulse width operating limits of servo, consult servo data sheet
        //for manufaturer's recommended operating limits. Values
        //are in milliseconds;
        float minimumPulseWidth = 0.9f;
        float neutralPulseWidth = 1.6f;
        float maximumPulseWidth = 2.1f;
        servo.setOperatingLimits(minimumPulseWidth, neutralPulseWidth, maximumPulseWidth);

        //Set relative range of servo for setPosition() commanding
        //(The default is 0.0 to 1.0)
        servo.setPositionRange(0.0F, 100.0f);

        //Now command just the left motor.
        //For future events stop by coasting.
        motorLeft.setBrakeMode(true);

        //Set power but do not set or change the motor state (stop, forward, reverse)
        //The power value will be used with the next forward() or reverse() command and
        //does not otherwise change the current motor power level.
        motorLeft.setPower(50.0f);

        //move forward at power level specified above
        System.out.println("Move foward");
        motorLeft.forward();
        adafruitCombo.sleep(1000);

        //Option 1: Command servo position by using pulse width
        for (float pulseWidth : new float[]{minimumPulseWidth, neutralPulseWidth, maximumPulseWidth}) {
            servo.setPulseWidth(pulseWidth);
            adafruitCombo.sleep(1000);
        }

        //Move servo to neutral position.
        servo.setPulseWidth(neutralPulseWidth);
        adafruitCombo.sleep(1000);

        //Option 2: Command servo position by relative values
        for (float position : new float[]{0.0f, 10.0f, 20.0f, 30.0f, 040.0f, 50.0f, 60.0f, 70.0f, 80.0f, 90.0f, 100.00f, 0.0f, 100.0f, 0.0f, 100.0f}) {
            System.out.format("Move to position: %8.1f\n", position);
            servo.setPosition(position);
            adafruitCombo.sleep(1000);
        }

        motorLeft.stop();

        //stop all servos
        adafruitCombo.stopAll();
    }
}
