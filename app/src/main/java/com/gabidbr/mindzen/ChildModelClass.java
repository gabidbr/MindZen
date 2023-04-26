package com.gabidbr.mindzen;

public class ChildModelClass {

    int image;
    String descriptionText;
    String videoName;

    public ChildModelClass(int image, String descriptionText, String videoName) {
        this.image = image;
        this.descriptionText = descriptionText;
        this.videoName = videoName;
    }

    public String getVideoName() {
        return videoName;
    }
}
