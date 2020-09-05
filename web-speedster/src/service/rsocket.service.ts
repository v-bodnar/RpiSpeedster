import {Injectable} from '@angular/core';
import {IdentitySerializer, JsonSerializer, RSocket, RSocketClient} from 'rsocket-core';
import RSocketWebSocketClient from 'rsocket-websocket-client';
import {Subject} from 'rxjs';

@Injectable()
export class RsocketService {

  client: RSocketClient;
  sub: Subject<string> = new Subject();
  endpoint
  url;

  constructor(url: string) {
    this.url = url;
  }

  ngOnInit(): void {
    // Create an instance of a client
    this.client = new RSocketClient({
      serializers: {
        data: JsonSerializer,
        metadata: IdentitySerializer
      },
      setup: {
        keepAlive: 60000,
        lifetime: 180000,
        // format of `data`
        dataMimeType: 'application/json',
        metadataMimeType: 'message/x.rsocket.routing.v0',
      },
      transport: new RSocketWebSocketClient({
        url: this.url
      }),
    });

    // Open the connection
    this.client.connect().subscribe({
      onComplete: (socket: RSocket) => {
        console.log("RSocket Connected to " + this.url);

        this.sub.subscribe({
          next: (data) => {
            socket.fireAndForget({
              data: data,
              metadata: String.fromCharCode(this.endpoint.length) + this.endpoint,
            });
          }
        })
      },

    });
  }

  sendMessage(message: string, endpoint: string) {
    console.log("sending message to:" + endpoint + ", data:" + message);
    this.endpoint = endpoint;
    this.sub.next(message);
  }
}
