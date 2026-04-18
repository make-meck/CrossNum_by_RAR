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
import javafx.scene.media.AudioClip;

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
    private static Clip soundClip;
    private static Clip correctClip;
    private static Clip eraseClip;
    private static boolean isMusicOn = true;
    private static boolean isSoundOn = true;
    private static double musicSavedVolume = 100.0;
    private static double soundSavedVolume = 100.0;

    // Initializes the settings UI, button states, and slider values.
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
                    adjustClipVolume(soundSavedVolume, soundClip);

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

    // loads audio files
    public static void initMusic() {
        // initializes bgm
        if (audioClip == null) {
            try {
                URL audioUrl = SettingsController.class.getResource("/audio/crossnum_bgm.wav");
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioUrl);
                audioClip = AudioSystem.getClip();
                audioClip.open(audioStream);

                adjustClipVolume(musicSavedVolume, audioClip);

                if (isMusicOn) {
                    audioClip.loop(Clip.LOOP_CONTINUOUSLY);
                }
            } catch (Exception e) {
                System.out.println("Audio Error: " + e.getMessage());
            }
        }

        // initializes sound effects
        if (soundClip == null) {
            try {
                URL sfxUrl = SettingsController.class.getResource("/audio/button_click.wav");
                AudioInputStream sfxStream = AudioSystem.getAudioInputStream(sfxUrl);
                soundClip = AudioSystem.getClip();
                soundClip.open(sfxStream);

                adjustClipVolume(soundSavedVolume, soundClip);
            } catch (Exception e) {
                System.out.println("SFX Error: " + e.getMessage());
            }
        }

        // initializes correct sound effect
        if (correctClip == null) {
            try {
                URL correctUrl = SettingsController.class.getResource("/audio/yes.wav");
                AudioInputStream correctStream = AudioSystem.getAudioInputStream(correctUrl);
                correctClip = AudioSystem.getClip();
                correctClip.open(correctStream);

                adjustClipVolume(soundSavedVolume, correctClip);
            } catch (Exception e) {
                System.out.println("Correct SFX Error: " + e.getMessage());
            }
        }

        // initializes cell erase sound effect
        if (eraseClip == null) {
            try {
                URL eraseUrl = SettingsController.class.getResource("/audio/erase.wav");
                AudioInputStream eraseStream = AudioSystem.getAudioInputStream(eraseUrl);
                eraseClip = AudioSystem.getClip();
                eraseClip.open(eraseStream);

                adjustClipVolume(soundSavedVolume, eraseClip);
            } catch (Exception e) {
                System.out.println("Erase SFX Error: " + e.getMessage());
            }
        }
    }

    // plays correct sound effect
    public static void playCorrectSound() {
        if (isSoundOn && correctClip != null) {
            correctClip.setFramePosition(0);
            correctClip.start();
        }
    }

    // plays cell erase sound effect
    public static void playEraseSound() {
        if (isSoundOn && eraseClip != null) {
            eraseClip.setFramePosition(0);
            eraseClip.start();
        }
    }
    public static void lowerMusic(double targetVolume){
        if(audioClip == null || !isMusicOn) return;
        adjustClipVolume(targetVolume, audioClip);
    }

    // restores bgm after achievement
    public static void restoreAudio(){
        if(audioClip == null || !isMusicOn) return;
        adjustClipVolume(musicSavedVolume, audioClip);
    }

    // plays achievement bgm
    public static void playSuccessSound(){
        if(!isSoundOn) return;
        try{
            URL successUrl = SettingsController.class.getResource("/audio/game_success.mp3");
            if(successUrl == null) return;
            AudioClip clip = new AudioClip(successUrl.toExternalForm());
            clip.setVolume(soundSavedVolume/100.00);
            clip.play();
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    // plays level failed bgm
    public static void playFailSound(){
        if(!isSoundOn) return;
        try{
            URL failUrl = SettingsController.class.getResource("/audio/game_failed.wav");
            if(failUrl == null) return;
            AudioClip clip = new AudioClip(failUrl.toExternalForm());
            clip.setVolume(soundSavedVolume/100);
            clip.play();
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    // fxml of bgm toggle button when interacting
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

    // update ui sound effects toggle button when interacting
    @FXML
    public void soundToggle() {
        if (soundClip == null) return;
        isSoundOn = sound_button.isSelected();
        SettingsController.playClickSound();

        if (sound_button.isSelected()) {
            soundSavedVolume = 100.0;
            soundVolume.setValue(soundSavedVolume);
            adjustClipVolume(soundSavedVolume, soundClip);
        } else {
            soundSavedVolume = 0.0;
            soundVolume.setValue(soundSavedVolume);
            adjustClipVolume(soundSavedVolume, soundClip);
        }
    }

    // setting up button click sound effect
    public static void setupGlobalClickSounds(Scene scene) {
        scene.removeEventFilter(MouseEvent.MOUSE_PRESSED, SettingsController::handleGlobalClick);
        scene.addEventFilter(MouseEvent.MOUSE_PRESSED, SettingsController::handleGlobalClick);
    }

    // plays sound effect every button click
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
        if (isSoundOn && soundClip != null) {
            soundClip.setFramePosition(0);
            soundClip.start();
        }
    }

    // handles volume change when interacting with the toggle button
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

    // toggle button fxml changed
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
