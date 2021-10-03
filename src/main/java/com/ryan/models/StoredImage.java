package com.ryan.models;

import java.awt.image.BufferedImage;
import java.util.UUID;

public class StoredImage {

    private UUID id;
    private String image64;
    private BufferedImage bufferedImage;

    public StoredImage(UUID id, String image64) {
        this.id = id;
        this.image64 = image64;
    }

    public StoredImage(UUID id, BufferedImage bufferedImage) {
        this.id = id;
        this.bufferedImage = bufferedImage;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getImage64() {
        return image64;
    }

    public void setImage64(String image64) {
        this.image64 = image64;
    }

    public BufferedImage getBufferedImage() {
        return bufferedImage;
    }

    public void setBufferedImage(BufferedImage bufferedImage) {
        this.bufferedImage = bufferedImage;
    }
}
