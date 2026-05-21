package dk.easv.weblagerexam.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class BarcodeAlertController {
    @FXML
    public Label barcodeValueLabel;
    @FXML
    public Button btnContinue;
    @FXML
    public Button btnStop;

    private boolean continueScanning = false;

    //Called from ScanningController to pass the barcode value
    public void setBarcodeValue(String barcodeValue) {
        barcodeValueLabel.setText("Barcode: " + barcodeValue);
    }


    @FXML
    public void onStopClicked(ActionEvent actionEvent) {
        continueScanning = false;

    }

    @FXML
    public void onContinueClicked(ActionEvent actionEvent) {
        continueScanning = true;
    }


    private void closeWindow(){
        Stage stage = (Stage) btnContinue.getScene().getWindow();
        stage.close();
    }

    // ScanningController checks thisafter popup closes
    public boolean isContinueScanning() {
        return continueScanning;
    }
}
