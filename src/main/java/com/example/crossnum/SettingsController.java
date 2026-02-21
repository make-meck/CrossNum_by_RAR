package com.example.crossnum;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;


import java.net.URL;
import java.util.ResourceBundle;

public class SettingsController implements Initializable {

    @FXML private ToggleButton music_button;
    @FXML private Slider musicVolume;
    @FXML private Slider soundVolume;

    private static Clip audioClip;
    private static boolean isMusicOn = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        music_button.setSelected(isMusicOn);

        if (musicVolume != null) {
            Platform.runLater(() -> {
                applySliderFill(musicVolume);

                musicVolume.valueProperty().addListener((obs, oldVal, newVal) -> {
                    applySliderFill(musicVolume);
                });
            });
        }

        if (soundVolume != null) {
            Platform.runLater(() -> {
                applySliderFill(soundVolume);

                soundVolume.valueProperty().addListener((obs, oldVal, newVal) -> {
                    applySliderFill(soundVolume);
                });
            });
        }
    }

    public static void initMusic() {
        if (audioClip == null) {
            try {
                URL audioUrl = SettingsController.class.getResource("/audio/crossnum_bgm.wav");
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioUrl);
                audioClip = AudioSystem.getClip();
                audioClip.open(audioStream);
            } catch (Exception e) {
                System.out.println("Audio Error: " + e.getMessage());
            }
        }
    }

    @FXML
    public void musicToggle() {
        if (audioClip == null) return;
        isMusicOn = music_button.isSelected();

        if (music_button.isSelected()) {
            audioClip.loop(Clip.LOOP_CONTINUOUSLY);
        } else {
            audioClip.stop();
        }
    }

    private void applySliderFill(Slider slider) {
        Node track = slider.lookup(".track");
        if (track != null) {
            double percentage = (slider.getValue() - slider.getMin()) /
                    (slider.getMax() - slider.getMin()) * 100;

            String style = String.format(
                    "-fx-background-color: linear-gradient(to right, #354b34 %f%%, #e5e5ea %f%%);",
                    percentage, percentage
            );

            track.setStyle(style);
        }
    }

}
