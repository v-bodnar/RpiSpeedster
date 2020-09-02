package com.omb;

import com.omb.msx.elektronika.ComboServo;
import com.omb.msx.elektronika.DcMotor;
import com.omb.msx.elektronika.MsxElektronikaMotorHat;
import com.pi4j.component.motor.MotorState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static com.omb.msx.elektronika.ComboServo.ServoName.S_14;
import static com.omb.msx.elektronika.ComboServo.ServoName.S_15;
import static com.omb.msx.elektronika.DcMotor.DcMotorName.*;
import static java.lang.Thread.sleep;

public class RpiSpeedster implements Speedster {
    private static final Logger LOGGER = LoggerFactory.getLogger(RpiSpeedster.class);
    private static final int TICKS_PER_SECOND = 10;
    private final MsxElektronikaMotorHat motorHat = new MsxElektronikaMotorHat();
    private final DcMotor driveMotor;
    private final DcMotor turnMotor;
    private final ComboServo cameraHorizontalMotor;
    private final ComboServo cameraVerticalMotor;
    private final ExecutorService steeringThread = Executors.newSingleThreadExecutor(r -> new Thread("Steering thread"));

    private AtomicReference<CameraViewState> cameraHorizontalState = new AtomicReference<>(new CameraViewState(0, Direction.NEUTRAL));
    private AtomicReference<CameraViewState> cameraVerticalState = new AtomicReference<>(new CameraViewState(0, Direction.NEUTRAL));

    public RpiSpeedster() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.warn("Turn off all motors");
            motorHat.stopAll();
        }));

        this.driveMotor = motorHat.getDcMotor(DC_A);
        this.turnMotor = motorHat.getDcMotor(DC_B);
        this.cameraHorizontalMotor = motorHat.getServoMotor(S_15, DC_C);
        this.cameraVerticalMotor = motorHat.getServoMotor(S_14, DC_D);
    }

    public void initialize() {
        this.driveMotor.setPowerRange(100.0f);
        this.turnMotor.setPowerRange(100.0f);
        this.cameraHorizontalMotor.setOperatingLimits(0.9f, 2.4f, 4.0f);
        this.cameraHorizontalMotor.setPositionRange(0.0f, 360f);
        this.cameraVerticalMotor.setOperatingLimits(0.9f, 2.4f, 4.0f);
        this.cameraVerticalMotor.setPositionRange(0.0f, 90f);
    }


    @Override
    public void forward(float speed) {
        driveMotor.forward();
        driveMotor.speed(speed);
    }

    @Override
    public void back(float speed) {
        driveMotor.reverse();
        driveMotor.speed(speed);
    }

    @Override
    public void left() {
        turnMotor.forward();
        turnMotor.speed(100);
    }

    @Override
    public void right() {
        turnMotor.reverse();
        turnMotor.speed(100);
    }

    @Override
    public void handBreak() {
        if (driveMotor.getState() == MotorState.FORWARD) {
            driveMotor.reverse();
            driveMotor.reverse(25);
            driveMotor.stop();
        } else if (driveMotor.getState() == MotorState.REVERSE) {
            driveMotor.forward();
            driveMotor.forward(25);
            driveMotor.stop();
        }
    }

    @Override
    public void releaseAccelerator() {
        driveMotor.stop();
    }

    @Override
    public void releaseWheel() {
        turnMotor.stop();
    }

    /**
     * @param speed degrees per second
     */
    @Override
    public void viewLeft(float speed) {
        view(Direction.NEGATIVE, cameraHorizontalMotor, speed);
    }

    /**
     * @param speed degrees per second
     */
    @Override
    public void viewRight(float speed) {
        view(Direction.POSITIVE, cameraHorizontalMotor, speed);
    }

    /**
     * @param speed degrees per second
     */
    @Override
    public void viewUp(float speed) {
        view(Direction.POSITIVE, cameraVerticalMotor, speed);
    }

    /**
     * @param speed degrees per second
     */
    @Override
    public void viewDown(float speed) {
        view(Direction.NEGATIVE, cameraVerticalMotor, speed);
    }

    @Override
    public void viewCenter() {
        cameraVerticalMotor.setPulseWidth(cameraVerticalMotor.getNeutralPulseWidth());
        cameraHorizontalMotor.setPulseWidth(cameraHorizontalMotor.getNeutralPulseWidth());
    }

    @Override
    public void releaseHorizontalView() {
        view(Direction.NEUTRAL, cameraHorizontalMotor, 0);
    }

    @Override
    public void releaseVerticalView() {
        view(Direction.NEUTRAL, cameraVerticalMotor, 0);
    }

    private void view(Direction direction, ComboServo servo, float speed) {
        CameraViewState newState = new CameraViewState(speed, direction);
        cameraHorizontalState.set(newState);
        float controlledSpeed = speed / TICKS_PER_SECOND;

        steeringThread.submit(() -> {
            float newPosition = servo.getPosition() - controlledSpeed;

            while (isSameSpeedAndDirection(newState, servo)) {
                if (direction == Direction.NEGATIVE && newPosition <= servo.getMinimumPosition()) {
                    servo.setPosition(servo.getMinimumPosition());
                    break;
                } else if (direction == Direction.POSITIVE && newPosition >= servo.getMinimumPosition()) {
                    servo.setPosition(servo.getMaximumPosition());
                    break;
                } else {
                    servo.setPosition(newPosition);
                    newPosition -= controlledSpeed;
                    waitOneTick();
                }
            }
        });
    }

    private synchronized boolean isSameSpeedAndDirection(CameraViewState newState, ComboServo comboServo) {
        if (comboServo.getName().equalsIgnoreCase(cameraHorizontalMotor.getName())) {
            return this.cameraHorizontalState.get().equals(newState);
        } else {
            return this.cameraVerticalState.get().equals(newState);
        }
    }

    private class CameraViewState {
        private final float speed;
        private final Direction direction;

        public CameraViewState(float speed, Direction direction) {
            this.speed = speed;
            this.direction = direction;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CameraViewState that = (CameraViewState) o;
            return Float.compare(that.speed, speed) == 0 &&
                    direction == that.direction;
        }

        @Override
        public int hashCode() {
            return Objects.hash(speed, direction);
        }
    }

    private enum Direction {
        NEGATIVE, NEUTRAL, POSITIVE;
    }

    private void waitOneTick() {
        try {
            sleep(TimeUnit.SECONDS.toMillis(1) / TICKS_PER_SECOND);
        } catch (InterruptedException e) {
            LOGGER.error("Steering thread interrupted", e);
            Thread.currentThread().interrupt();
        }
    }
}
