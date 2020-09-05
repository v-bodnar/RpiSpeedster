import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { NgxJoystickModule } from 'ngx-joystick';
import { AppComponent } from './app.component';

@NgModule({
  declarations: [
    AppComponent
  ],
  imports: [
    BrowserModule,
    NgxJoystickModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
