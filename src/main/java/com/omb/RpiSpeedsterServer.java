package com.omb;

import com.omb.camera.CameraSource;
import com.omb.streaming.RtspServer;
import com.pi4j.system.NetworkInfo;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

import java.io.IOException;

public class RpiSpeedsterServer extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
//        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
//        System.out.println(Core.NATIVE_LIBRARY_NAME.toString());

        Scene scene = new Scene(createHBoxLayout(), 350, 200);
        primaryStage.setTitle("Peeper Server");
        primaryStage.setScene(scene);
        primaryStage.show();

        new RtspServer(new CameraSource() {
            @Override
            public byte[] getBytes() throws IOException {
                return new byte[0];
            }
        }).start();
    }

    public HBox createHBoxLayout() throws IOException, InterruptedException {
        HBox hbox = new HBox();

        hbox.setSpacing(10);
        hbox.setPadding(new Insets(5));
        hbox.setAlignment(Pos.CENTER_LEFT);

        Label ipLabel = new Label("Current IP: ");
        TextField ipField = new TextField();
        ipField.setText(String.valueOf(NetworkInfo.getIPAddresses()));
        ipField.setEditable(false);
        HBox.setHgrow(ipField, Priority.ALWAYS);

        hbox.getChildren().addAll(ipLabel, ipField);

        return hbox;
    }
}
