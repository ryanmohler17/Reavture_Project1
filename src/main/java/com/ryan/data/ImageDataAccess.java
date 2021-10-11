package com.ryan.data;

import java.awt.image.BufferedImage;
import java.util.UUID;

public interface ImageDataAccess {

    /**
     * Loads a base 64 encoded image.
     *
     * @param uuid The id of the image
     * @return A base64 encoded string representing the image
     */
    String loadBase64Img(UUID uuid);

    /**
     * Loads an image as a buffered image
     *
     * @param uuid The id of the image
     * @return A BufferedImage
     */
    BufferedImage loadBufferedImg(UUID uuid);

    /**
     * Saves a buffered image
     *
     * @param image The image to save
     * @return The generated id of the image
     */
    UUID saveImg(BufferedImage image);

}
