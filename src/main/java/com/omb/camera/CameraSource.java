package com.omb.camera;

import java.io.IOException;

public interface CameraSource {
    byte[] getBytes() throws IOException;
}
