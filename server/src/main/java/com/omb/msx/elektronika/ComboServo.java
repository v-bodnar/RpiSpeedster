package com.omb.msx.elektronika;

import com.pi4j.component.servo.ServoDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class ComboServo implements com.pi4j.component.servo.Servo {
    private static final Logger LOGGER = LoggerFactory.getLogger(ComboServo.class);
    //PCA9685 to stop the servo
    private final byte[] PWM_STOP = new byte[]{0X00, 0X00, 0X00, 0X00};
    //Adafruit Servo Hat used to command servo functions
    private final AdafruitHat servoHat;
    //Motor name "S01" through "S16"
    private final ServoName servoName;

    private final float period; //period or duty-cycle in milliseconds

    //I'm going to drain power from DC motor Output
    private final DcMotor dcMotor;
    /*
     * operating limits of servo are based on pulse width (milliseconds).
     * These initial values are fairly conservative and should work for
     * many servos. The operating limits can be updated by the application
     * with the setOperatingLimits() method. Consult the servo's data sheet
     * to obtain the manufacture's recommended values.
     */
    private float minimumPulseWidth = 0;
    private float neutralPulseWidth = 0;
    private float maximumPulseWidth = 0;

    /*
     * The relative minimum and maximum values to command the servo
     * through the setPosition() method. The minimum value corresponds to
     * minimumPulseWidth position of the servo. The maximum value corresponds
     * to the maximum pulse width position.
     */
    private float minimumX = 0.0f;
    private float maximumX = 1.0f;

    //PCA9685 Register addresses for PWM that control motor speed
    private final int[] pwmAddr;

    //Corresponding speed values
    private byte[] pwmValues;

    //current servo position
    private float servoPosition;

    public ComboServo(AdafruitHat servoHat, DcMotor dcMotor, int[] pwmAddr, ServoName servoName) {
        this.servoHat = servoHat;
        this.servoName = servoName;
        this.pwmAddr = pwmAddr;
        this.dcMotor = dcMotor;
        this.period = (1.0f / (float) servoHat.getFrequency()) * 1000.0f;
    }

    /**
     * Set the pulse width (milliseconds) to drive servo position.
     *
     * @param pulseWidth in milliseconds
     */
    public void setPulseWidth(float pulseWidth) {
        if (pulseWidth < minimumPulseWidth || pulseWidth > maximumPulseWidth || pulseWidth > period) {
            LOGGER.debug("*** Error *** pulseWidth value invalid");
            LOGGER.debug("Must be in range: {} to {}", minimumPulseWidth, maximumPulseWidth);
            LOGGER.debug("and must be less than period: {}", period);
            servoHat.stopAll();
            throw new IllegalArgumentException(Float.toString(pulseWidth));
        }

        //raw servo value for setting the pulseWidth
        int rawServo = (int) (Math.round((pulseWidth / period) * 4095.0));
        if (rawServo < 0) rawServo = 0;

        pwmValues = new byte[]{(byte) 0, (byte) 0, (byte) (rawServo & 0XFF), (byte) (rawServo >> 8)};
        sendCommands();

    }

    /**
     * Set the operating pulse width range for the servo. Consult the servo's data sheet
     * for the manufacturer's recommended pulse width designation.
     *
     * @param minimumPulseWidth minimum-position pulse width in milliseconds
     * @param neutralPulseWidth neutral-position pulse width in milliseconds
     * @param maximumPulseWidth maximum-position pulse width in milliseconds
     */
    public void setOperatingLimits(float minimumPulseWidth, float neutralPulseWidth, float maximumPulseWidth) {
        boolean hit = false;
        if (minimumPulseWidth <= 0.0 || neutralPulseWidth <= 0.0 || maximumPulseWidth <= 0.0) {
            hit = true;
            LOGGER.debug("*** Error *** pulse width values can not be 0.0");
        }
        if (minimumPulseWidth > period || neutralPulseWidth > period || maximumPulseWidth > period) {

        }
        if (minimumPulseWidth >= neutralPulseWidth) {
            hit = true;
            LOGGER.debug("*** Error *** minimumPulseWidth > neutralPulseWidth");
        }
        if (minimumPulseWidth >= maximumPulseWidth) {
            hit = true;
            LOGGER.debug("*** Error *** minimumPulseWidth >= maximumPulseWidth");
        }
        if (neutralPulseWidth >= maximumPulseWidth) {
            hit = true;
            LOGGER.debug("*** Error *** neutralPulseWidth >= maximumPulseWidth");
        }
        if (hit) {
            servoHat.stopAll();
            throw new IllegalArgumentException();
        }

        this.minimumPulseWidth = minimumPulseWidth;
        this.neutralPulseWidth = neutralPulseWidth;
        this.maximumPulseWidth = maximumPulseWidth;

        //we will need full power, which corresponds to 5V
        dcMotor.setPowerRange(100.0f);
        dcMotor.forward();
        dcMotor.speed(100f);
        setPulseWidth(neutralPulseWidth);
    }


    /**
     * Specify the relative minimum and maximum value range of the servo motor.
     * The value passed to the setPostion() method must be in this range.
     * The minimumX value corresponds to the servo position of minimum pulse width and
     * maximumX to the servo position of the maximum pulse width.
     *
     * @param minimumX Defaults to 0.0
     * @param maximumX Defaults to 1.0
     */
    public void setPositionRange(float minimumX, float maximumX) {
        if (minimumX >= maximumX) {
            LOGGER.debug("*** Error *** xMax must be greater than xMin");
            servoHat.stopAll();
            throw new IllegalArgumentException();
        }
        this.minimumX = minimumX;
        this.maximumX = maximumX;
    }

    /**
     * Move servo to relative position.
     *
     * @param servoPosition The range of the servoPosition corresponds to the minimum
     *                      and maximum values set in the setPositionRange() method. The default range
     *                      is 0.0 to 1.0 if the setPositionRange() method is not called.
     */
    @Override
    public void setPosition(float servoPosition) {
        if (servoPosition < minimumX || servoPosition > maximumX) {
            LOGGER.debug("*** Error *** servo value must be in range 0.0 to 1.0");
            servoHat.stopAll();
            throw new IllegalArgumentException(Float.toString(servoPosition));
        }
        float slope = (maximumPulseWidth - minimumPulseWidth) / (maximumX - minimumX);
        float b = maximumPulseWidth - slope * maximumX;
        float pulseWidth = slope * servoPosition + b;
        if (pulseWidth > maximumPulseWidth) pulseWidth = maximumPulseWidth;
        if (pulseWidth < minimumPulseWidth) pulseWidth = minimumPulseWidth;
        setPulseWidth(pulseWidth);
        this.servoPosition = servoPosition;
    }

    /**
     * Return the current servo position
     */
    @Override
    public float getPosition() {
        return (float) servoPosition;
    }

    public float getMinimumPosition() {
        return minimumX;
    }

    public float getMaximumPosition() {
        return maximumX;
    }

    @Override
    public void off() {
        servoHat.stopAll();
    }

    /**
     * Return the operating PWM frequency in cycles per second.
     * The operating frequency can be changed with the
     * AdafruitServoHat.setPwmFreq() method.
     *
     * @return PWM frequency in cycles/second
     */
    public float getPwmFreq() {
        return (float) servoHat.getFrequency();
    }

    /**
     * Return minimum operating pulse width of servo.
     *
     * @return minimumPulseWidth in milliseconds
     */
    public float getMinimumPulseWidth() {
        return minimumPulseWidth;
    }

    /**
     * Return neutral operating pulse width of servo.
     *
     * @return neutralPulseWidth in milliseconds
     */
    public float getNeutralPulseWidth() {
        return neutralPulseWidth;
    }

    /**
     * Return maximum operating pulse width of servo.
     *
     * @return maximumPulseWidth in milliseconds
     */
    public float getMaximumPulseWidth() {
        return maximumPulseWidth;
    }

    /**
     * Stop servo
     */
    public void stop() {
        pwmValues = PWM_STOP;
        sendCommands();
    }

    /**
     * Send commands to the I2C device.
     */
    private void sendCommands() {
        for (int i = 0; i < 4; i++) servoHat.write(pwmAddr[i], pwmValues[i]);
    }


    /**
     * Does nothing.
     */
    @Override
    public void setName(String name) {

    }

    /**
     * return name of servo, name constructed in method()
     */
    @Override
    public String getName() {
        //Here's our generated device
        return servoName.name();
    }

    /*
     * Not applicable methods to this implementation of the servo class
     */
    @Override
    public void setTag(Object tag) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getTag() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setProperty(String key, String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasProperty(String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getProperty(String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, String> getProperties() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeProperty(String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clearProperties() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ServoDriver getServoDriver() {
        throw new UnsupportedOperationException();
    }

    public enum ServoName {
        S_1, S_2, S_14, S_15;
    }

}
