import {Component, HostListener, ViewChild} from '@angular/core';
import {RsocketService} from '../service/rsocket.service'
import {JoystickEvent, NgxJoystickComponent} from 'ngx-joystick';
import {JoystickManagerOptions, JoystickOutputData} from 'nipplejs';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  upPressed: boolean = false;
  downPressed: boolean = false;
  rightPressed: boolean = false;
  leftPressed: boolean = false;
  qPressed: boolean = false;
  ePressed: boolean = false;
  spacePressed: boolean = false;
  speed: number = 30


  url: string = 'ws://192.168.0.100:7000';
  rsocketService: RsocketService = new RsocketService(this.url);

  joystickOptions: JoystickManagerOptions = {
    mode: 'semi',
    catchDistance: 50,
    color: 'lightblue'
  };

  joystickData: JoystickOutputData;
  intervalJoystickData: IntervalData;
  interval;
  @ViewChild('semiJoystick', {static: false}) joystick: NgxJoystickComponent;


  @HostListener('window:keydown', ['$event'])
  keyupEvent(event: KeyboardEvent) {
    if (event.key === 'ArrowUp' || event.key === 'w' || event.key === 'W') {
      this.upArrowPressed()
    } else if (event.key === 'ArrowDown' || event.key === 's' || event.key === 'S') {
      this.downArrowPressed()
    } else if (event.key === 'ArrowLeft' || event.key === 'a' || event.key === 'A') {
      this.leftArrowPressed()
    } else if (event.key === 'ArrowRight' || event.key === 'd' || event.key === 'D') {
      this.rightArrowPressed()
    } else if (event.key === 'e' || event.key === 'E') {
      this.ePressed = true;
      this.increaseSpeed()
    } else if (event.key === 'q' || event.key === 'Q') {
      this.qPressed = true;
      this.decreaseSpeed()
    } else if (event.code === 'Space') {
      this.spacePressed = true;
      this.handBreak()
    }
  }

  @HostListener('window:keyup', ['$event'])
  keydownEvent(event: KeyboardEvent) {
    if (event.key === 'ArrowUp' || event.key === 'w' || event.key === 'W') {
      this.upArrowReleased()
    } else if (event.key === 'ArrowDown' || event.key === 's' || event.key === 'S') {
      this.downArrowReleased()
    } else if (event.key === 'ArrowLeft' || event.key === 'a' || event.key === 'A') {
      this.leftArrowReleased()
    } else if (event.key === 'ArrowRight' || event.key === 'd' || event.key === 'D') {
      this.rightArrowReleased()
    } else if (event.key === 'e' || event.key === 'E') {
      this.ePressed = false;
    } else if (event.key === 'q' || event.key === 'Q') {
      this.qPressed = false;
    } else if (event.code === 'Space') {
      this.spacePressed = false;
    }
  }

  increaseSpeed() {
    if ((this.speed + 5) <= 100) {
      this.speed += 5;
    }
  }

  decreaseSpeed() {
    if ((this.speed - 5) >= 0) {
      this.speed -= 5;
    }
  }


  upArrowPressed() {
    if (this.upPressed === false) {
      this.upPressed = true;
      this.rsocketService.sendMessage(this.speed.toString(), 'forward')
    }
  }

  downArrowPressed() {
    if (this.downPressed === false) {
      this.downPressed = true;
      this.rsocketService.sendMessage(this.speed.toString(), 'back')
    }
  }

  leftArrowPressed() {
    if (this.leftPressed === false) {
      this.leftPressed = true;
      this.rsocketService.sendMessage('', 'left')
    }
  }

  rightArrowPressed() {
    if (this.rightPressed === false) {
      this.rightPressed = true;
      this.rsocketService.sendMessage('', 'right')
    }
  }

  upArrowReleased() {
    if (this.upPressed === true) {
      this.upPressed = false;
      this.rsocketService.sendMessage('', 'release-accelerator')
    }
  }

  downArrowReleased() {
    if (this.downPressed === true) {
      this.downPressed = false;
      this.rsocketService.sendMessage('', 'release-accelerator')
    }
  }

  leftArrowReleased() {
    if (this.leftPressed === true) {
      this.leftPressed = false;
      this.rsocketService.sendMessage('', 'release-wheel')
    }
  }

  rightArrowReleased() {
    if (this.rightPressed === true) {
      this.rightPressed = false;
      this.rsocketService.sendMessage('', 'release-wheel')
    }
  }

  handBreak() {
    this.rsocketService.sendMessage('', 'hand-break')
  }

  moveCamera() {
    if (this.joystickData.distance !== this.intervalJoystickData.distance ||
      (this.joystickData.direction && this.joystickData.direction.angle &&
        this.joystickData.direction.angle !== this.intervalJoystickData.direction)) {
      this.intervalJoystickData = new IntervalData(this.joystickData.direction.angle, this.joystickData.distance)
      if (this.intervalJoystickData.direction && this.intervalJoystickData.direction === "up") {
        this.rsocketService.sendMessage(Math.round(this.intervalJoystickData.distance).toString(), 'view-up')
      } else if (this.intervalJoystickData.direction && this.intervalJoystickData.direction === "down") {
        this.rsocketService.sendMessage(Math.round(this.intervalJoystickData.distance).toString(), 'view-down')
      } else if (this.intervalJoystickData.direction && this.intervalJoystickData.direction === "right") {
        this.rsocketService.sendMessage(Math.round(this.intervalJoystickData.distance).toString(), 'view-right')
      } else if (this.intervalJoystickData.direction && this.intervalJoystickData.direction === "left") {
        this.rsocketService.sendMessage(Math.round(this.intervalJoystickData.distance).toString(), 'view-left')
      }
    }
  }

  holdCamera() {
    this.pauseTimer();
    this.intervalJoystickData = null;
    this.rsocketService.sendMessage('', 'hold-horizontal-view')
    this.rsocketService.sendMessage('', 'hold-vertical-view')
  }

  onJoystickMove(event: JoystickEvent) {
    this.joystickData = event.data;
  }

  startTimer() {
    this.intervalJoystickData = new IntervalData("up", 0);
    this.interval = setInterval(() => {
      this.moveCamera()
    }, 250)
  }

  pauseTimer() {
    clearInterval(this.interval);
  }

}

class IntervalData {
  direction: string;
  distance: number;


  constructor(direction: string, distance: number) {
    this.direction = direction;
    this.distance = distance;
  }
}
