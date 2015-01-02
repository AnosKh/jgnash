/*
 * jGnash, a personal finance application
 * Copyright (C) 2001-2015 Craig Cavanaugh
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package jgnash.uifx;

import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import jgnash.MainFX;
import jgnash.uifx.control.Alert;
import jgnash.uifx.control.ExceptionDialog;
import jgnash.uifx.util.FXMLUtils;
import jgnash.uifx.util.StageUtils;
import jgnash.util.Nullable;
import jgnash.util.ResourceUtils;

/**
 * Various static UI support methods
 *
 * @author Craig Cavanaugh
 */
public class StaticUIMethods {

    private StaticUIMethods() {
        // Utility class
    }

    public static void showOpenDialog() {
        final Stage dialog = FXMLUtils.loadFXML(MainFX.class.getResource("fxml/OpenDatabaseForm.fxml"), ResourceUtils.getBundle());
        dialog.setTitle(ResourceUtils.getBundle().getString("Title.Open"));
        dialog.setResizable(false);

        StageUtils.addBoundsListener(dialog, StaticUIMethods.class);

        dialog.showAndWait();
    }

    public static void displayError(final String message) {
        final Alert alert = new Alert(Alert.AlertType.ERROR, message);

        alert.setTitle(ResourceUtils.getBundle().getString("Title.Error"));
        alert.initOwner(MainApplication.getPrimaryStage());

        Platform.runLater(alert::showAndWait);
    }

    public static void displayMessage(final String message) {
        final Alert alert = new Alert(Alert.AlertType.INFORMATION, message);

        alert.setTitle(ResourceUtils.getBundle().getString("Title.Information"));
        alert.initOwner(MainApplication.getPrimaryStage());

        Platform.runLater(alert::showAndWait);
    }

    public static void displayWarning(final String message) {
        final Alert alert = new Alert(Alert.AlertType.WARNING, message);

        alert.setTitle(ResourceUtils.getBundle().getString("Title.Warning"));
        alert.initOwner(MainApplication.getPrimaryStage());

        Platform.runLater(alert::showAndWait);
    }

    /**
     * Displays a Yes and No dialog requesting confirmation
     *
     * @param title   Dialog title
     * @param message Dialog message
     * @return {@code ButtonBar.ButtonData.YES} or {@code ButtonBar.ButtonData.NO}
     */
    public static ButtonType showConfirmationDialog(final String title, final String message) {
        final Alert alert = new Alert(Alert.AlertType.YES_NO, message);

        alert.setTitle(title);
        alert.initOwner(MainApplication.getPrimaryStage());

        return alert.showAndWait().get();
    }

    public static void displayException(final Throwable exception) {
        Platform.runLater(() -> {
            ExceptionDialog exceptionDialog = new ExceptionDialog(exception);
            exceptionDialog.showAndWait();
        });
    }

    public static void displayTaskProgress(final Task<?> task) {
        MainApplication.setBusy(task);
    }

    /**
     * Returns a JavaFx Image from the class path
     *
     * @param image resource path
     * @return {@code} Image or {@code null} if not found
     */
    @Nullable
    public static Image getImage(final String image) {
        Image resourceImage;

        try {
            resourceImage = new Image(StaticUIMethods.class.getResourceAsStream(image));
        } catch (final Exception ex) {
            Logger.getLogger(StaticUIMethods.class.getName()).log(Level.WARNING, ex.getLocalizedMessage(), ex);
            resourceImage = null;
        }

        return resourceImage;
    }

    /**
     * Handler for displaying uncaught exceptions
     */
    public static class ExceptionHandler implements Thread.UncaughtExceptionHandler {

        @Override
        public void uncaughtException(final Thread t, final Throwable throwable) {
            Logger.getLogger(ExceptionHandler.class.getName()).log(Level.SEVERE, throwable.getLocalizedMessage(), throwable);

            displayException(throwable);
        }
    }
}
