/*
Copyright (c) 2024 Tony Roy

This file is part of the Symbolic Logic APPlication (SLAPP).

SLAPP is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

SLAPP is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with SLAPP.  If not, see
<https://www.gnu.org/licenses/>.
 */

package slapp.editor.main_window.media_player;

import javafx.scene.layout.HBox;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

/**
 * Control bar across bottom of player
 */
public class MediaBar extends HBox {

    // introducing Sliders
    Slider time = new Slider(); // Slider for time
    Slider vol = new Slider(); // Slider for volume
    Button PlayButton;
    Label volume = new Label("Volume: ");
    MediaPlayer player;
    Label totalTime = new Label("");

    Label currentTime = new Label("");
    Player myPlayer;

    Font iconFont;

    /**
     * Create media bar
     *
     * @param myPlayer the media player to which the bar applies
     */

    public MediaBar(Player myPlayer)
//    public MediaBar(MediaPlayer play)
    { // Default constructor taking
        // the MediaPlayer object
        this.myPlayer = myPlayer;
        player = myPlayer.getPlayer();

        PlayButton = new Button();
        iconFont = Font.font("la-solid-900", FontWeight.BOLD, FontPosture.REGULAR, 12);
        PlayButton.setFont(iconFont);
        PlayButton.setText("\uf04c");






        setAlignment(Pos.CENTER); // setting the HBox to center
        setPadding(new Insets(5, 10, 5, 10));
        // Settih the preference for volume bar
        vol.setPrefWidth(70);
        vol.setMinWidth(30);
        vol.setValue(100);
        HBox.setHgrow(time, Priority.ALWAYS);
        PlayButton.setPrefWidth(30);
        PlayButton.setMaxHeight(30);



        // Adding the components to the bottom

        getChildren().add(PlayButton); // Playbutton
        getChildren().add(new Label(" "));
        getChildren().add(currentTime) ;
        getChildren().add(new Label("/"));
        getChildren().add(totalTime);
        getChildren().add(time); // time slider
        getChildren().add(volume); // volume slider
        getChildren().add(vol);

        setPrefHeight(60);
        setMinHeight(60);
        setMaxHeight(60);



        // Adding Functionality to play the media player

        PlayButton.setOnAction(e -> {
            playPause();
        });

        myPlayer.getMpane().setOnMousePressed(e -> {
            playPause();
        });

        // Providing functionality to time slider
        player.currentTimeProperty().addListener(new InvalidationListener() {
            public void invalidated(Observable ov)
            {
                updatesValues();
            }
        });

        // Inorder to jump to the certain part of video
        time.valueProperty().addListener(new InvalidationListener() {
            public void invalidated(Observable ov)
            {
                if (time.isPressed()) { // It would set the time
                    // as specified by user by pressing
                    player.seek(player.getMedia().getDuration().multiply(time.getValue() / 100));
                }
            }
        });

        // providing functionality to volume slider
        vol.valueProperty().addListener(new InvalidationListener() {
            public void invalidated(Observable ov)
            {
                if (vol.isPressed()) {
                    player.setVolume(vol.getValue() / 100); // It would set the volume
                    // as specified by user by pressing
                }
            }
        });
    }

    // Outside the constructor

    private void playPause() {
        Status status = player.getStatus(); // To get the status of Player
        if (status == status.PLAYING) {

            // If the status is Video playing
            if (player.getCurrentTime().greaterThanOrEqualTo(player.getTotalDuration())) {

                // If the player is at the end of video
                player.seek(player.getStartTime()); // Restart the video
                player.play();
            }
            else {
                // Pausing the player
                player.pause();
                PlayButton.setText("\uf04b");


         //       PlayButton.setText(">");
            }
        } // If the video is stopped, halted or paused
        if (status == Status.HALTED || status == Status.STOPPED || status == Status.PAUSED) {
            player.play(); // Start the video
       //     PlayButton.setText("||");
            PlayButton.setText("\uf04c");

        }
    }

    /**
     * Move slider as video plays
     */
    protected void updatesValues()
    {
        Platform.runLater(new Runnable() {
            public void run()
            {
                // Updating to the new time value
                // This will move the slider while running your video
                time.setValue(player.getCurrentTime().toMillis() / player.getTotalDuration().toMillis() * 100);

                double seconds = player.getCurrentTime().toSeconds();
                int hours = (int) (seconds/3600);
                int min = (int) ((seconds % 3600) / 60);
                int sec = (int) (seconds % 60);
                String timeString = String.format("%02d:%02d:%02d ", hours, min, sec);
                currentTime.setText(timeString);

            }
        });
    }

    /**
     * Label whose text will be set to the total duration of the video
     *
     * @return total time label
     */
    Label getTotalTime() {
        return totalTime;
    }

}
