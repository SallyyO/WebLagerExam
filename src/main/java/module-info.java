module dk.easv.weblagerexam {
    requires javafx.controls;
    requires javafx.fxml;


    opens dk.easv.weblagerexam to javafx.fxml;
    exports dk.easv.weblagerexam;
}