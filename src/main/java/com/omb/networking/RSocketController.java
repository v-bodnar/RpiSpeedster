package com.omb.networking;

import com.omb.board.Speedster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

@Controller
public class RSocketController {
    private static final Logger LOGGER = LoggerFactory.getLogger(RSocketController.class);

    private Speedster speedster;

    @Autowired
    public RSocketController(Speedster speedster) {
        this.speedster = speedster;
    }

    @MessageMapping("forward")
    Mono<Void> forward(float speed) {
        LOGGER.debug("Moving forward {}", speed);
        speedster.forward(speed);
        return Mono.empty();
    }

    @MessageMapping("back")
    Mono<Void> back(float speed) {
        LOGGER.debug("Moving backwards {}", speed);
        speedster.back(speed);
        return Mono.empty();
    }

    @MessageMapping("right")
    Mono<Void> right() {
        LOGGER.debug("Wheel right");
        speedster.right();
        return Mono.empty();
    }

    @MessageMapping("left")
    Mono<Void> left() {
        LOGGER.debug("Wheel left");
        speedster.left();
        return Mono.empty();
    }

    @MessageMapping("hand-break")
    Mono<Void> handBreak() {
        LOGGER.debug("Hand Break");
        speedster.handBreak();
        return Mono.empty();
    }

    @MessageMapping("release-accelerator")
    Mono<Void> releaseAccelerator() {
        LOGGER.debug("Release Accelerator");
        speedster.releaseAccelerator();
        return Mono.empty();
    }

    @MessageMapping("release-wheel")
    Mono<Void> releaseWheel() {
        LOGGER.debug("Release Wheel");
        speedster.releaseWheel();
        return Mono.empty();
    }

    @MessageMapping("view-left")
    Mono<Void> viewLeft(float speed) {
        LOGGER.debug("Turn camera left {}", speed);
        speedster.viewLeft(speed);
        return Mono.empty();
    }

    @MessageMapping("view-right")
    Mono<Void> viewRight(float speed) {
        LOGGER.debug("Turn camera right {}", speed);
        speedster.viewRight(speed);
        return Mono.empty();
    }

    @MessageMapping("view-up")
    Mono<Void> viewUp(float speed) {
        LOGGER.debug("Turn camera up {}", speed);
        speedster.viewUp(speed);
        return Mono.empty();
    }

    @MessageMapping("view-down")
    Mono<Void> viewDown(float speed) {
        LOGGER.debug("Turn camera up {}", speed);
        speedster.viewDown(speed);
        return Mono.empty();
    }

    @MessageMapping("view-center")
    Mono<Void> viewCenter() {
        LOGGER.debug("Center camera");
        speedster.viewCenter();
        return Mono.empty();
    }

    @MessageMapping("hold-horizontal-view")
    Mono<Void> holdHorizontalView() {
        LOGGER.debug("Hold horizontal view");
        speedster.holdHorizontalView();
        return Mono.empty();
    }

    @MessageMapping("hold-vertical-view")
    Mono<Void> holdVerticalView() {
        LOGGER.debug("Hold vertical view");
        speedster.holdVerticalView();
        return Mono.empty();
    }
}