module com.example.studentreg {
    requires javafx.controls;
    requires javafx.fxml;
    requires itextpdf;

    requires java.sql;
    requires org.apache.poi.ooxml;  // For Excel support (XSSF)
    requires org.apache.poi.poi;


    opens com.example.studentreg to javafx.fxml;
    exports com.example.studentreg;
}