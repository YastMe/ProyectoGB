module com.example.proyectogb {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;


    opens com.abeltrans.proyectogb to javafx.fxml;
    exports com.abeltrans.proyectogb;
    exports com.abeltrans.proyectogb.entities;
    opens com.abeltrans.proyectogb.entities to javafx.fxml;
    opens com.abeltrans.proyectogb.controller to javafx.fxml;
    exports com.abeltrans.proyectogb.controller;
}