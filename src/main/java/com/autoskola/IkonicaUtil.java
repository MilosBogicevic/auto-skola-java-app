package com.autoskola;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class IkonicaUtil {
    public static ImageView napravi(String naziv) {
        ImageView ikonica = new ImageView(new Image(IkonicaUtil.class.getResourceAsStream("/icons/" + naziv)));
        ikonica.setFitWidth(24);
        ikonica.setFitHeight(24);
        return ikonica;
    }
}
