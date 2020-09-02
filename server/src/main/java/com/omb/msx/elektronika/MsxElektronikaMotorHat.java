package com.omb.msx.elektronika;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.omb.msx.elektronika.ComboServo.ServoName.*;
import static com.omb.msx.elektronika.DcMotor.DcMotorName.*;

public class MsxElektronikaMotorHat extends AdafruitHat {
    private static final int MOTOR_HAT_ADDRESS = 0X60;
    private static final double PWM_FREQUENCY = 400;
    private static final Map<DcMotor.DcMotorName, DcMotor.DcMotorAddress> ALLOCATED_DC_MOTORS = Collections.unmodifiableMap(new HashMap<DcMotor.DcMotorName, DcMotor.DcMotorAddress>() {{
        put(DC_A, new DcMotor.DcMotorAddress(
                new int[]{AdafruitHat.LED2_ON_L, AdafruitHat.LED2_ON_H, AdafruitHat.LED2_OFF_L, AdafruitHat.LED2_OFF_H},
                new int[]{AdafruitHat.LED3_ON_L, AdafruitHat.LED3_ON_H, AdafruitHat.LED3_OFF_L, AdafruitHat.LED3_OFF_H},
                new int[]{AdafruitHat.LED4_ON_L, AdafruitHat.LED4_ON_H, AdafruitHat.LED4_OFF_L, AdafruitHat.LED4_OFF_H}));

        put(DC_B, new DcMotor.DcMotorAddress(
                new int[]{AdafruitHat.LED7_ON_L, AdafruitHat.LED7_ON_H, AdafruitHat.LED7_OFF_L, AdafruitHat.LED7_OFF_H},
                new int[]{AdafruitHat.LED6_ON_L, AdafruitHat.LED6_ON_H, AdafruitHat.LED6_OFF_L, AdafruitHat.LED6_OFF_H},
                new int[]{AdafruitHat.LED5_ON_L, AdafruitHat.LED5_ON_H, AdafruitHat.LED5_OFF_L, AdafruitHat.LED5_OFF_H}));

        put(DC_C, new DcMotor.DcMotorAddress(
                new int[]{AdafruitHat.LED8_ON_L, AdafruitHat.LED8_ON_H, AdafruitHat.LED8_OFF_L, AdafruitHat.LED8_OFF_H},
                new int[]{AdafruitHat.LED9_ON_L, AdafruitHat.LED9_ON_H, AdafruitHat.LED9_OFF_L, AdafruitHat.LED9_OFF_H},
                new int[]{AdafruitHat.LED10_ON_L, AdafruitHat.LED10_ON_H, AdafruitHat.LED10_OFF_L, AdafruitHat.LED10_OFF_H}));

        put(DC_D, new DcMotor.DcMotorAddress(
                new int[]{AdafruitHat.LED13_ON_L, AdafruitHat.LED13_ON_H, AdafruitHat.LED13_OFF_L, AdafruitHat.LED13_OFF_H},
                new int[]{AdafruitHat.LED12_ON_L, AdafruitHat.LED12_ON_H, AdafruitHat.LED12_OFF_L, AdafruitHat.LED12_OFF_H},
                new int[]{AdafruitHat.LED11_ON_L, AdafruitHat.LED11_ON_H, AdafruitHat.LED11_OFF_L, AdafruitHat.LED11_OFF_H}));

    }});
    private static final Map<ComboServo.ServoName, int[]> ALLOCATED_SERVOS = Collections.unmodifiableMap(new HashMap<ComboServo.ServoName, int[]>() {
        {
            put(S_1, new int[]{AdafruitHat.LED0_ON_L, AdafruitHat.LED0_ON_H, AdafruitHat.LED0_OFF_L, AdafruitHat.LED0_OFF_H});
            put(S_2, new int[]{AdafruitHat.LED1_ON_L, AdafruitHat.LED1_ON_H, AdafruitHat.LED1_OFF_L, AdafruitHat.LED1_OFF_H});
            put(S_14, new int[]{AdafruitHat.LED14_ON_L, AdafruitHat.LED14_ON_H, AdafruitHat.LED14_OFF_L, AdafruitHat.LED14_OFF_H});
            put(S_15, new int[]{AdafruitHat.LED15_ON_L, AdafruitHat.LED15_ON_H, AdafruitHat.LED15_OFF_L, AdafruitHat.LED15_OFF_H});
        }
    });

    public MsxElektronikaMotorHat() {
        super(MOTOR_HAT_ADDRESS);
        stopAll();
        setPwmFreq(PWM_FREQUENCY);
    }

    public DcMotor getDcMotor(DcMotor.DcMotorName motor) {
        //Create an instance for this motor.
        return new DcMotor(this, ALLOCATED_DC_MOTORS.get(motor), motor);
    }

    public ComboServo getServoMotor(ComboServo.ServoName motor, DcMotor.DcMotorName dcMotorName) {

        //Create an instance for this motor.
        return new ComboServo(this, getDcMotor(dcMotorName), ALLOCATED_SERVOS.get(motor), motor);
    }
}
