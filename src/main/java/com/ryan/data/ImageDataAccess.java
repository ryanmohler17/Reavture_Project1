package com.ryan.data;

import java.awt.image.BufferedImage;
import java.util.UUID;

public interface ImageDataAccess {

    String loadBase64Img(UUID uuid);

    BufferedImage loadBufferedImg(UUID uuid);

    UUID saveImg(BufferedImage image);

}
