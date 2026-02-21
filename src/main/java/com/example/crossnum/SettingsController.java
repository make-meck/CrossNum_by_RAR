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
import javax.sound.sampled.FloatControl;


import java.net.URL;
import java.util.ResourceBundle;

public class SettingsController implements Initializable {

    @FXML private ToggleButton music_button;
    @FXML private ToggleButton sound_button;
    @FXML private Slider musicVolume;
    @FXML private Slider soundVolume;

    private static Clip audioClip;
    private static boolean isMusicOn = false;
    private static double musicSavedVolume = 0.0;
    private static double soundSavedVolume = 0.0;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        music_button.setSelected(isMusicOn);

        if (musicVolume != null) {
            musicVolume.setValue(musicSavedVolume);

            Platform.runLater(() -> {
                applySliderFill(musicVolume);

                musicVolume.valueProperty().addListener((obs, oldVal, newVal) -> {
                    applySliderFill(musicVolume);
                    musicSavedVolume = newVal.doubleValue();
                    adjustClipVolume(musicSavedVolume);

                    if (musicSavedVolume > 0 && !music_button.isSelected()) {
                        music_button.setSelected(true);
                        isMusicOn = true;

                        if (audioClip != null && !audioClip.isRunning()) {
                            audioClip.loop(Clip.LOOP_CONTINUOUSLY);
                        }
                    } else if (musicSavedVolume == 0 && music_button.isSelected()) {
                        music_button.setSelected(false);
                        isMusicOn = false;

                        if (audioClip != null && audioClip.isRunning()) {
                            audioClip.stop();
                        }
                    }
                });
            });
        }

        if (soundVolume != null) {
            sound_button.setSelected(true);
            soundVolume.setValue(soundSavedVolume);

            Platform.runLater(() -> {
                applySliderFill(soundVolume);

                soundVolume.valueProperty().addListener((obs, oldVal, newVal) -> {
                    applySliderFill(soundVolume);
                    soundSavedVolume = newVal.doubleValue();
                    adjustClipVolume(soundSavedVolume);
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

                adjustClipVolume(musicSavedVolume);
                adjustClipVolume(soundSavedVolume);
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
            musicSavedVolume = 100.0;

            musicVolume.setValue(musicSavedVolume);
            adjustClipVolume(musicSavedVolume);
        } else {
            audioClip.stop();
            musicSavedVolume = 0.0;

            musicVolume.setValue(musicSavedVolume);
            adjustClipVolume(musicSavedVolume);
        }
    }

    @FXML
    public void soundToggle() {
        // if (audioClip == null) return;
        // isMusicOn = music_button.isSelected();

        if (sound_button.isSelected()) {
            // audioClip.loop(Clip.LOOP_CONTINUOUSLY);
            musicSavedVolume = 100.0;

            soundVolume.setValue(musicSavedVolume);
            adjustClipVolume(musicSavedVolume);
        } else {
            //audioClip.stop();
            musicSavedVolume = 0.0;

            soundVolume.setValue(musicSavedVolume);
            adjustClipVolume(musicSavedVolume);
        }
    }

    public static void adjustClipVolume(double volumeValue) {
        if (audioClip == null) return;

        try {
            FloatControl gainControl = (FloatControl) audioClip.getControl(FloatControl.Type.MASTER_GAIN);

            if (volumeValue <= 0) {
                gainControl.setValue(gainControl.getMinimum());
            } else {
                float linearVolume = (float) (volumeValue / 100.0);
                float decibels = 20f * (float) Math.log10(linearVolume);

                decibels = Math.min(decibels, gainControl.getMaximum());
                gainControl.setValue(decibels);
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Volume control is not supported on this specific audio file/OS.");
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
