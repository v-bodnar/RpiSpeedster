package com.omb.msx.elektronika;

import com.pi4j.component.motor.Motor;
import com.pi4j.component.motor.MotorState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Map;


public class DcMotor implements Motor {
    private static final Logger LOGGER = LoggerFactory.getLogger(DcMotor.class);

    //PWM values for setting a motor for power, stop, forward, and reverse directions
    private final byte[] PWM_STOP = new byte[]{0X00, 0X00, 0X00, 0X00};
    private final byte[] IN_FORWARD = new byte[]{0X00, 0X10, 0X00, 0X00};
    private final byte[] IN_REVERSE = new byte[]{0X00, 0X00, 0X00, 0X10};

    //msx-elektronika Motor Hat used to command motor functions
    private final AdafruitHat motorHat;
    private final DcMotorName motorName;
    private final DcMotorAddress motorAddress;

    //Corresponding speed values
    private byte[] pwmValues;
    //Corresponding motor direction values
    private byte[] in1Values;
    //Corresponding motor direction values
    private byte[] in2Values;

    //Relative maximum power value to command motor. This is a somewhat arbitrary value
    //that can be set by the setPowerRange() method. Some applications may want to use
    //a different range other than 0.0 (no power) to 1.0 (full throttle).
    private float maximumPower = 1.0f;

    //Speed setting for motor (-maximumPower to maximumPower, - for reverse, + for forward)
    private float speed = 0.0f;
    //Power setting (0.0 to maximumPower) used in conjunction with forward() and reverse() methods.
    private float power = 0.0f;

    //Current motor state: stop, forward, reverse
    private MotorState motorState;

    /*
     * The brakeMode is used in the stop method to quickly stop motor movement.
     * If true, the motor is temporary reversed for a set number of
     * milliseconds. This mode eliminates the coasting time for a motor.
     * If false then the motor coasts to a stop.
     */
    private boolean brakeMode = false;

    /*
     * The brakeModeValue specifies the number of milliseconds to reverse
     * the motor direction to minimize motor coasting. This value can
     * be set by the user with the method setBreakModeValue(milliseconds)
     * This value is dependent on the properties of the motor in use
     * and ideally should be set based on the motor used.
     */
    private long brakeModeValue = 35;

    public DcMotor(MsxElektronikaMotorHat motorHat, DcMotorAddress motorAddress, DcMotorName motorName) {
        this.motorHat = motorHat;
        this.motorName = motorName;
        this.motorAddress = motorAddress;
        this.motorState = MotorState.STOP;
        this.pwmValues = PWM_STOP;
        this.in2Values = PWM_STOP;
        this.in1Values = PWM_STOP;
    }


    /**
     * Command the LED PWMs to set the motor speed and motor direction.
     * PWM_VALUES = speed control
     * IN1_VALUES = 1st PWM for controlling direction
     * IN2_VALUES = 2nd PWM for controlling direction
     */
    private void sendCommands() {
        for (int i = 0; i < 4; i++) motorHat.write(motorAddress.getPwmAddress()[i], pwmValues[i]);
        for (int i = 0; i < 4; i++) motorHat.write(motorAddress.getIn2Address()[i], in2Values[i]);
        for (int i = 0; i < 4; i++) motorHat.write(motorAddress.getIn1Address()[i], in1Values[i]);
    }

    /**
     * Command the motor speed.
     * Positive speed moves in the forward direction.
     * Negative speed moves in the reverse direction.
     *
     * @param speed -maximumPower to maximumPower
     */
    public void speed(float speed) {
        if (speed < -maximumPower || speed > maximumPower) {
            LOGGER.debug("*** Error *** Speed value must be in range {} to {}", -maximumPower, maximumPower);
            motorHat.stopAll();
            throw new IllegalArgumentException(Float.toString(speed));
        }
        this.speed = speed;
        this.power = Math.abs(this.speed);
        pwmValues = this.setPwm(this.speed);

        //sets up the commanding values for the LED PWMs
        if (this.speed == 0.0) {
            //turn off PWMs
            this.stop();
            motorState = MotorState.STOP;
        } else if (this.speed > 0.0) {
            //set PWMs for forward direction
            in2Values = IN_FORWARD;
            in1Values = IN_REVERSE;
            motorState = MotorState.FORWARD;
        } else {
            //set PWMs for reverse direction
            in2Values = IN_REVERSE;
            in1Values = IN_FORWARD;
            motorState = MotorState.REVERSE;
        }
        //Command the PCA9685 for setting speed and direction of the DC motor
        this.sendCommands();
    }


    /**
     * Return current speed value for the motor
     *
     * @return speed
     */
    public float getSpeed() {
        return speed;
    }

    /**
     * Set the power value (speed) for the DC Motor. This method is used in combination
     * with the forward() and reverse() methods. The power value will be used to the
     * motor controller on the next forward() or reverse() command.
     *
     * @param power Valid value range: 0.0 (no power) to maximumPower (full throttle)
     */
    public void setPower(float power) {
        if (power < 0.0 || power > maximumPower) {
            LOGGER.debug("*** Error *** Power value must be in range 0.0 to {}", maximumPower);
            motorHat.stopAll();
            throw new IllegalArgumentException(Float.toString(power));
        }
        this.power = power;
        if (motorState == MotorState.REVERSE) this.speed = -power;
        else this.speed = power;

        pwmValues = this.setPwm(power);
    }

    /**
     * Return current power value for the motor
     *
     * @return Valid range: 0.0 (no power) to maximumPower (full throttle)
     */
    public float getPower() {
        return power;
    }

    /**
     * Optionally set the maximum power value that corresponds the
     * motor's full throttle.
     *
     * @param maximumPower full throttle value for DC motor. The default
     *                     is 1.0 unless set by the calling application.
     */
    public void setPowerRange(float maximumPower) {
        if (maximumPower <= 0.0) {
            LOGGER.debug("*** Error *** Power range value must be > 0.0");
            motorHat.stopAll();
            throw new IllegalArgumentException(Float.toString(maximumPower));
        }
        this.maximumPower = maximumPower;
    }

    /**
     * Return the maximum power value that corresponds to the maximum
     * throttle of the DC motor.
     *
     * @return maximumPower
     */
    public float getPowerRange() {
        return maximumPower;
    }

    /**
     * Convert the motor speed to the PWM values
     *
     * @param speed ranges from -maximumPower to maximumPower, positive numbers are forward direction, negative reverse
     */
    protected byte[] setPwm(float speed) {
        byte[] pwm = new byte[]{0, 0, 0, 0};
        int rawSpeed = Math.round(Math.abs(speed / maximumPower) * 4095); //PWM commanding is 12-bit (4095)
        pwm[2] = (byte) (rawSpeed & 0XFF);  //Extract low-order byte
        pwm[3] = (byte) (rawSpeed >> 8);    //Extract high-order byte
        return pwm;
    }

    /**
     * Set break mode for stop() method;
     * true  = brake the motor when stopping (quickly stop);
     * false = let the motor coast to a stop
     *
     * @param brakeMode true=brake, false= coast to a stop
     */
    public void setBrakeMode(boolean brakeMode) {
        this.brakeMode = brakeMode;
    }

    /**
     * Set Brake mode value.
     * Number of milliseconds to apply power in opposite
     * motor direction to brake (quickly stop) motor.
     * <p>
     * Dependent on motor characteristics. Set this
     * value when precise braking is required.
     *
     * @param brakeModeValue milliseconds
     */
    public void setBrakeModeValue(long brakeModeValue) {
        if (brakeModeValue < 1 || brakeModeValue > 100) {
            LOGGER.debug("*** Error *** brakeModeValue must be in range 1 - 100 milliseconds");
            motorHat.stopAll();
            throw new IllegalArgumentException(Long.toString(brakeModeValue));
        }
        this.brakeModeValue = brakeModeValue;
    }

    /**
     * Command the DC motor to go in the forward direction.
     */
    @Override
    public void forward() {
        in2Values = IN_FORWARD;
        in1Values = IN_REVERSE;
        //Command the PCA9685 for forward direction
        this.sendCommands();
        motorState = MotorState.FORWARD;
        this.speed = this.power;
    }

    /**
     * Command the DC motor to go in the forward direction for the time
     * specified then stop.
     */
    @Override
    public void forward(long milliseconds) {
        in2Values = IN_FORWARD;
        in1Values = IN_REVERSE;
        //Command the PCA9685 for forward direction
        this.sendCommands();
        motorState = MotorState.FORWARD;
        motorHat.sleep(milliseconds);
        this.stop();
        motorState = MotorState.STOP;
        this.speed = this.power;
    }

    /**
     * Command the DC motor to go in the reverse direction.
     */
    @Override
    public void reverse() {
        in2Values = IN_REVERSE;
        in1Values = IN_FORWARD;
        //Command the PCA9685 for reverse direction
        this.sendCommands();
        motorState = MotorState.REVERSE;
        this.speed = -this.power;
    }

    /**
     * Command the DC motor to go in the reverse direction for the time
     * specified then stop.
     */
    @Override
    public void reverse(long milliseconds) {
        in2Values = IN_REVERSE;
        in1Values = IN_FORWARD;
        //Command the PCA9685 for reverse direction
        this.sendCommands();
        motorState = MotorState.REVERSE;
        motorHat.sleep(milliseconds);
        this.stop();
        motorState = MotorState.STOP;
        this.speed = -this.power;
    }

    /**
     * Stop the motor.
     */
    @Override
    public void stop() {
        //if brakeMode then temporarily switch direction to quickly stop motor.
        if (brakeMode) {
            byte[] inSwitch = in2Values;
            in2Values = in1Values;
            in1Values = inSwitch;
            this.sendCommands();
            motorHat.sleep(brakeModeValue);
        }
        in2Values = PWM_STOP;
        in1Values = PWM_STOP;
        this.sendCommands();
        motorState = MotorState.STOP;
    }

    /**
     * Returns motor name
     */
    @Override
    public String getName() {
        //Here's our generated device
        return String.format("Adafuit DcMotor Device: 0X%04X Motor: %s", motorHat.DEVICE_ADDR, motorName);
    }

    /**
     * Is the motor state the value passed to this method?
     * returns true or false
     */
    @Override
    public boolean isState(MotorState state) {
        return (motorState == state);
    }

    /**
     * Is the motor stopped?
     * returns true of false
     */
    @Override
    public boolean isStopped() {
        return (motorState == MotorState.STOP);
    }

    /**
     * Return the motor state. Possible values returned:
     * MotorState.STOP
     * MotorState.FORWARD
     * MotorState.BACKWARD
     */
    @Override
    public MotorState getState() {
        //We're tracking the motor state with this variable
        return motorState;
    }


    /****************************************************
     * Methods below are place holders
     ***************************************************/

    /**
     * Place holder, does nothing
     */
    @Override
    public void setName(String name) {
        throw new UnsupportedOperationException();
    }

    /**
     * Place holder, does nothing
     */
    @Override
    public void setTag(Object tag) {
        throw new UnsupportedOperationException();
    }


    /**
     * Place holder, does nothing
     */
    @Override
    public Object getTag() {
        throw new UnsupportedOperationException();
    }


    /**
     * Place holder, does nothing
     */
    @Override
    public void setProperty(String key, String value) {
        throw new UnsupportedOperationException();
    }


    /**
     * Place holder, does nothing
     */
    @Override
    public boolean hasProperty(String key) {
        throw new UnsupportedOperationException();
    }


    /**
     * Place holder, does nothing
     */
    @Override
    public String getProperty(String key, String defaultValue) {
        throw new UnsupportedOperationException();
    }


    /**
     * Place holder, does nothing
     */
    @Override
    public String getProperty(String key) {
        throw new UnsupportedOperationException();
    }


    /**
     * Place holder, does nothing
     */
    @Override
    public Map<String, String> getProperties() {
        throw new UnsupportedOperationException();
    }


    /**
     * Place holder, does nothing
     */
    @Override
    public void removeProperty(String key) {
        throw new UnsupportedOperationException();
    }


    /**
     * Place holder, does nothing
     */
    @Override
    public void clearProperties() {
        throw new UnsupportedOperationException();
    }


    /**
     * This method disabled
     */
    @Override
    public void setState(MotorState state) {
        throw new UnsupportedOperationException();
    }

    static class DcMotorAddress {
        private final int[] pwmAddress;
        private final int[] in2Address;
        private final int[] in1Address;

        public DcMotorAddress(int[] pwmAddress, int[] in2Address, int[] in1Address) {
            this.pwmAddress = pwmAddress;
            this.in2Address = in2Address;
            this.in1Address = in1Address;
        }

        public int[] getPwmAddress() {
            return pwmAddress;
        }

        public int[] getIn2Address() {
            return in2Address;
        }

        public int[] getIn1Address() {
            return in1Address;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) return true;
            if (object == null || getClass() != object.getClass()) return false;
            DcMotorAddress that = (DcMotorAddress) object;
            return Arrays.equals(pwmAddress, that.pwmAddress) &&
                    Arrays.equals(in2Address, that.in2Address) &&
                    Arrays.equals(in1Address, that.in1Address);
        }

        @Override
        public int hashCode() {
            int result = Arrays.hashCode(pwmAddress);
            result = 31 * result + Arrays.hashCode(in2Address);
            result = 31 * result + Arrays.hashCode(in1Address);
            return result;
        }
    }

    public enum DcMotorName {
        DC_A, DC_B, DC_C, DC_D;
    }
}
