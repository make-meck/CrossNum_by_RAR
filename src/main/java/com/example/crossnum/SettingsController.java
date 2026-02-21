package com.example.crossnum;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;

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
    private static Clip sfxsoundClip;
    private static boolean isMusicOn = false;
    private static boolean isSoundOn = true;
    private static double musicSavedVolume = 0.0;
    private static double soundSavedVolume = 100.0;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        music_button.setSelected(isMusicOn);
        sound_button.setSelected(isSoundOn);

        if (musicVolume != null) {
            musicVolume.setValue(musicSavedVolume);

            Platform.runLater(() -> {
                applySliderFill(musicVolume);

                musicVolume.valueProperty().addListener((obs, oldVal, newVal) -> {
                    applySliderFill(musicVolume);
                    musicSavedVolume = newVal.doubleValue();
                    adjustClipVolume(musicSavedVolume, audioClip);

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
            soundVolume.setValue(soundSavedVolume);

            Platform.runLater(() -> {
                applySliderFill(soundVolume);

                soundVolume.valueProperty().addListener((obs, oldVal, newVal) -> {
                    applySliderFill(soundVolume);
                    soundSavedVolume = newVal.doubleValue();
                    adjustClipVolume(soundSavedVolume, sfxsoundClip);

                    if (soundSavedVolume > 0 && !sound_button.isSelected()) {
                        sound_button.setSelected(true);
                        isSoundOn = true;
                    } else if (soundSavedVolume == 0 && sound_button.isSelected()) {
                        sound_button.setSelected(false);
                        isSoundOn = false;
                    }
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

                adjustClipVolume(musicSavedVolume, audioClip);
                adjustClipVolume(soundSavedVolume, sfxsoundClip);
            } catch (Exception e) {
                System.out.println("Audio Error: " + e.getMessage());
            }
        }

        if (sfxsoundClip == null) {
            try {
                URL sfxUrl = SettingsController.class.getResource("/audio/button_click.wav");
                AudioInputStream sfxStream = AudioSystem.getAudioInputStream(sfxUrl);
                sfxsoundClip = AudioSystem.getClip();
                sfxsoundClip.open(sfxStream);
            } catch (Exception e) {
                System.out.println("SFX Error: " + e.getMessage());
            }
        }
    }

    @FXML
    public void musicToggle() {
        SettingsController.playClickSound();
        if (audioClip == null) return;
        isMusicOn = music_button.isSelected();

        if (music_button.isSelected()) {
            audioClip.loop(Clip.LOOP_CONTINUOUSLY);
            musicSavedVolume = 100.0;

            musicVolume.setValue(musicSavedVolume);
            adjustClipVolume(musicSavedVolume, audioClip);
        } else {
            audioClip.stop();
            musicSavedVolume = 0.0;

            musicVolume.setValue(musicSavedVolume);
            adjustClipVolume(musicSavedVolume, audioClip);
        }
    }

    @FXML
    public void soundToggle() {
        if (sfxsoundClip == null) return;
        isSoundOn = sound_button.isSelected();
        SettingsController.playClickSound();

        if (sound_button.isSelected()) {
            soundSavedVolume = 100.0;
            soundVolume.setValue(soundSavedVolume);
            adjustClipVolume(soundSavedVolume, sfxsoundClip);
        } else {
            soundSavedVolume = 0.0;
            soundVolume.setValue(soundSavedVolume);
            adjustClipVolume(soundSavedVolume, sfxsoundClip);
        }
    }

    public static void setupGlobalClickSounds(Scene scene) {
        scene.removeEventFilter(MouseEvent.MOUSE_PRESSED, SettingsController::handleGlobalClick);
        scene.addEventFilter(MouseEvent.MOUSE_PRESSED, SettingsController::handleGlobalClick);
    }

    private static void handleGlobalClick(MouseEvent event) {
        Node target = (Node) event.getTarget();
        while (target != null) {
            if (target instanceof ButtonBase) {
                playClickSound();
                break;
            }
            target = target.getParent();
        }
    }

    public static void playClickSound() {
        if (isSoundOn && sfxsoundClip != null) {
            sfxsoundClip.setFramePosition(0);
            sfxsoundClip.start();
        }
    }

    public static void adjustClipVolume(double volumeValue, Clip clipName) {
        if (clipName == null) return;

        try {
            FloatControl gainControl = (FloatControl) clipName.getControl(FloatControl.Type.MASTER_GAIN);

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
