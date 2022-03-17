package com.abeltrans.proyectogb.controller;

import com.abeltrans.proyectogb.entities.Botella;
import com.abeltrans.proyectogb.entities.Entidad;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class MainViewController {

    private static List<File> files;
    private static List<Entidad> entidades;
    private static List<Botella> botellas;
    private static List<Entidad> entidadesUnicas;
    private static ObservableList<Entidad> olEntidades;
    private static int total;
    private static File ruta;
    @javafx.fxml.FXML
    private TextField txtCodCliente;
    @javafx.fxml.FXML
    private TableColumn tcFecha;
    @javafx.fxml.FXML
    private TableView<Entidad> tvEntidades;
    @javafx.fxml.FXML
    private TableColumn tcCodigo;
    @javafx.fxml.FXML
    private TableColumn tcCtn;
    @javafx.fxml.FXML
    private Label lblTotal;
    @javafx.fxml.FXML
    private TextField txtCtn;
    @javafx.fxml.FXML
    private TableColumn tcArticulo;
    @javafx.fxml.FXML
    private Button btnExportar;
    @javafx.fxml.FXML
    private Button btnImportar;
    @javafx.fxml.FXML
    private Button vaciarTabla;
    @javafx.fxml.FXML
    private DatePicker txtDate;
    @javafx.fxml.FXML
    private ImageView logo;

    public static void listFilesForFolder(File folder, List<File> files) {
        if (folder.exists()) {
            for (final File fileEntry : folder.listFiles()) {
                if (fileEntry.isDirectory()) {
                    listFilesForFolder(fileEntry, files);
                } else if (!files.contains(fileEntry)) {
                    files.add(fileEntry);
                }
            }
        }
    }

    @javafx.fxml.FXML
    public void filtrarCodigo(Event event) {
        olEntidades = FXCollections.observableArrayList();
        for (Entidad entidad : entidadesUnicas) {
            if (txtCodCliente.getText().trim() != "") {
                if (txtCtn.getText().trim() != "") {
                    if (entidad.getCodigo().contains(txtCodCliente.getText().toUpperCase()) && entidad.getNumSerie().contains(txtCtn.getText().toUpperCase())) {
                        olEntidades.add(entidad);
                    }
                } else {
                    if (entidad.getCodigo().contains(txtCodCliente.getText().toUpperCase())) {
                        olEntidades.add(entidad);
                    }
                }
            } else if (txtCtn.getText().trim() != "") {
                if (entidad.getNumSerie().contains(txtCtn.getText().toUpperCase())) {
                    olEntidades.add(entidad);
                }
            }
        }
        if (txtCodCliente.getText().trim() == "" && txtCtn.getText().trim() == "") {
            olEntidades.setAll(entidadesUnicas);
        }
        tvEntidades.getItems().setAll(olEntidades);
    }

    private void fillTable() {
        olEntidades = FXCollections.observableArrayList();
        tvEntidades.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Entidad entidad, boolean b) {
                super.updateItem(entidad, b);
                if (entidad != null) {
                    LocalDate date = entidad.getFechaRaw();
                    LocalDate caducidad = txtDate.getValue();
                    if (date.isBefore(caducidad)) {
                        setStyle("-fx-font-weight: bold");
                    } else {
                        setStyle("");
                    }
                } else {
                    setStyle("");
                }
            }
        });
        tcFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        tcFecha.setSortable(false);
        tcArticulo.setCellValueFactory(new PropertyValueFactory<>("articulo"));
        tcCtn.setCellValueFactory(new PropertyValueFactory<>("numSerie"));
        tcCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        getEntidades();
        removeDuplicates();
        sortEntidades();
        removeVacio();
        olEntidades.addAll(entidadesUnicas);
        tvEntidades.getItems().setAll(olEntidades);
    }

    private void removeVacio() {
        List<Entidad> llenos = new ArrayList<>();
        llenos.addAll(entidadesUnicas);

        for (Entidad entidad : entidadesUnicas) {
            for (Botella botella : botellas) {
                if (botella.isVacio(entidad.getArticulo())) {
                    llenos.remove(entidad);
                    break;
                }
            }
        }

        entidadesUnicas = llenos;

        lblTotal.setText(String.valueOf(entidadesUnicas.size()));
    }

    public void initialize() {
        files = new ArrayList<>();
        entidades = new ArrayList<>();
        entidadesUnicas = new ArrayList<>();
        botellas = new ArrayList<>();
        logo.setImage(new Image("file:icon.png"));
        txtDate.setValue(LocalDate.now().minusYears(5));
        getBotellas();
        fillTable();
    }

    private void sortEntidades() {
        if (entidadesUnicas != null && entidadesUnicas.size() > 0) {
            Comparator<Entidad> dateComparator = new Comparator<Entidad>() {
                @Override
                public int compare(Entidad e1, Entidad e2) {
                    return e1.getFechaRaw().compareTo(e2.getFechaRaw());
                }
            };
            entidadesUnicas.sort(dateComparator);
        }
    }

    private void getEntidades() {
        FileInputStream is;
        List<Entidad> clientes = new ArrayList<>();
        List<Entidad> proveedores = new ArrayList<>();
        int fecha = 0, articulo = 0, codigoCliente = 0, codigoProveedor = 0, numSerie = 0;
        boolean isCliente = true;
        for (File file : files) {
            try {
                is = new FileInputStream(file);
                XSSFWorkbook workbook = new XSSFWorkbook(is);
                XSSFSheet sheet = workbook.getSheetAt(0);

                Iterator<Row> rowIterator = sheet.rowIterator();

                for (int i = 0; i < sheet.getLastRowNum() + 1; i++) {
                    Row row = rowIterator.next();
                    Iterator<Cell> cellIterator = row.cellIterator();
                    Entidad entidad = new Entidad();
                    if (row.getRowNum() > 0) {
                        while (cellIterator.hasNext()) {
                            Cell cell = cellIterator.next();
                            int cellIndex = cell.getColumnIndex();
                            if (cellIndex == fecha) {
                                entidad.setFechaRaw(cell.getDateCellValue());
                            } else if (cellIndex == articulo) {
                                entidad.setArticulo((int) Float.parseFloat(cell.toString()));
                            } else if (cellIndex == codigoCliente) {
                                if (cell.toString().equals("")) {
                                    isCliente = false;
                                }
                                    entidad.setCodigo(cell.toString());
                            } else if (cellIndex == codigoProveedor) {
                                if (cell.toString().equals("")) {
                                    isCliente = true;
                                }
                                    entidad.setCodigo(cell.toString());
                            } else if (cellIndex == numSerie) {
                                entidad.setNumSerie(cell.toString());
                            }
                        }
                    } else {
                        while (cellIterator.hasNext()) {
                            Cell cell = cellIterator.next();
                            switch (cell.toString()) {
                                case "Fecha":
                                    fecha = cell.getColumnIndex();
                                    break;
                                case "Cód. artículo":
                                    articulo = cell.getColumnIndex();
                                    break;
                                case "Cód. proveedor":
                                    codigoProveedor = cell.getColumnIndex();
                                    break;
                                case "Cód. cliente":
                                    codigoCliente = cell.getColumnIndex();
                                    break;
                                case "Núm. serie":
                                    numSerie = cell.getColumnIndex();
                                    break;
                            }
                        }
                    }
                    if (entidad.getCodigo() != null && entidad.getArticulo() != 0 && entidad.getNumSerie() != null) {
                        if (isCliente){
                            clientes.add(entidad);
                        }
                        else {
                            proveedores.add(entidad);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
//            if (file.getName().contains("cliente")) {
//                try {
//                    is = new FileInputStream(file);
//                    XSSFWorkbook workbook = new XSSFWorkbook(is);
//                    XSSFSheet sheet = workbook.getSheetAt(0);
//
//                    Iterator<Row> rowIterator = sheet.rowIterator();
//
//                    for (int i = 0; i < sheet.getLastRowNum() + 1; i++) {
//                        Row row = rowIterator.next();
//                        Iterator<Cell> cellIterator = row.cellIterator();
//                        Entidad cliente = new Entidad();
//                        if (row.getRowNum() > 0) {
//                            while (cellIterator.hasNext()) {
//                                Cell cell = cellIterator.next();
//                                int cellIndex = cell.getColumnIndex();
//                                if (cellIndex == fecha) {
//                                    cliente.setFechaRaw(cell.getDateCellValue());
//                                } else if (cellIndex == articulo) {
//                                    cliente.setArticulo((int) Float.parseFloat(cell.toString()));
//                                } else if (cellIndex == codigoCliente) {
//                                    cliente.setCodigo(cell.toString());
//                                } else if (cellIndex == numSerie) {
//                                    cliente.setNumSerie(cell.toString());
//                                }
//                            }
//                        } else {
//                            while (cellIterator.hasNext()) {
//                                Cell cell = cellIterator.next();
//                                switch (cell.toString()) {
//                                    case "Fecha":
//                                        fecha = cell.getColumnIndex();
//                                        break;
//                                    case "Cód. artículo":
//                                        articulo = cell.getColumnIndex();
//                                        break;
//                                    case "Cód. cliente":
//                                        codigoCliente = cell.getColumnIndex();
//                                        break;
//                                    case "Núm. serie":
//                                        numSerie = cell.getColumnIndex();
//                                        break;
//                                }
//                            }
//                        }
//                        if (cliente.getCodigo() != null && cliente.getArticulo() != 0 && cliente.getNumSerie() != null) {
//                            clientes.add(cliente);
//                        }
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            } else if (file.getName().contains("proveedor")) {
//                try {
//                    is = new FileInputStream(file);
//                    XSSFWorkbook workbook = new XSSFWorkbook(is);
//                    XSSFSheet sheet = workbook.getSheetAt(0);
//
//                    Iterator<Row> rowIterator = sheet.rowIterator();
//                    for (int i = 0; i < sheet.getLastRowNum() + 1; i++) {
//                        Row row = rowIterator.next();
//                        Iterator<Cell> cellIterator = row.cellIterator();
//                        Entidad proveedor = new Entidad();
//                        if (row.getRowNum() > 0) {
//                            while (cellIterator.hasNext()) {
//                                Cell cell = cellIterator.next();
//
//                                int cellIndex = cell.getColumnIndex();
//                                if (cellIndex == fecha) {
//                                    proveedor.setFechaRaw(cell.getDateCellValue());
//                                } else if (cellIndex == articulo) {
//                                    proveedor.setArticulo((int) Float.parseFloat(cell.toString()));
//                                } else if (cellIndex == codigoProveedor) {
//                                    proveedor.setCodigo(cell.toString());
//                                } else if (cellIndex == numSerie) {
//                                    proveedor.setNumSerie(cell.toString());
//                                }
//                            }
//                        } else {
//                            while (cellIterator.hasNext()) {
//                                Cell cell = cellIterator.next();
//                                switch (cell.toString()) {
//                                    case "Fecha":
//                                        fecha = cell.getColumnIndex();
//                                        break;
//                                    case "Cód. artículo":
//                                        articulo = cell.getColumnIndex();
//                                        break;
//                                    case "Cód. proveedor":
//                                        codigoProveedor = cell.getColumnIndex();
//                                        break;
//                                    case "Núm. serie":
//                                        numSerie = cell.getColumnIndex();
//                                        break;
//                                }
//                            }
//                        }
//                        if (proveedor.getCodigo() != null && proveedor.getArticulo() != 0 && proveedor.getNumSerie() != null) {
//                            proveedores.add(proveedor);
//                        }
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
        }
        entidades.addAll(clientes);
        entidades.addAll(proveedores);
    }

    @javafx.fxml.FXML
    public void exportFile(ActionEvent actionEvent) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        CellStyle cellStyle = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        cellStyle.setDataFormat(
                createHelper.createDataFormat().getFormat("m/d/yy"));
        XSSFSheet sheet = workbook.createSheet();
        sheet.setColumnWidth(0, 5000);
        sheet.setColumnWidth(1, 5000);
        sheet.setColumnWidth(2, 5000);
        sheet.setColumnWidth(3, 5000);
        int rowNum = 0;
        Row row = sheet.createRow(rowNum++);
        Cell cell = row.createCell(0);
        cell.setCellValue("Código de cliente");
        cell = row.createCell(1);
        cell.setCellValue("Código de artículo");
        cell = row.createCell(2);
        cell.setCellValue("Número de serie");
        cell = row.createCell(3);
        cell.setCellValue("Fecha");
        for (Entidad entidad : olEntidades) {
            row = sheet.createRow(rowNum++);
            cell = row.createCell(0);
            cell.setCellValue(entidad.getCodigo());
            cell = row.createCell(1);
            cell.setCellValue(entidad.getArticulo());
            cell = row.createCell(2);
            cell.setCellValue(entidad.getNumSerie());
            cell = row.createCell(3);
            cell.setCellValue(entidad.getFechaRaw());
            cell.setCellStyle(cellStyle);
        }

        try {
            FileChooser fileChooser = new FileChooser();
            if (ruta != null) {
                fileChooser.setInitialDirectory(ruta);
            }
            fileChooser.setInitialFileName("Export.xlsx");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel file (*.xlsx)", "*.xlsx"));
            File export = fileChooser.showSaveDialog(((Node) actionEvent.getTarget()).getScene().getWindow());
            if (export != null) {
                if (!export.getName().contains(".")) {
                    export = new File(export.getAbsolutePath().toString() + ".xlsx");
                }
                FileOutputStream out = new FileOutputStream(export);
                workbook.write(out);
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @javafx.fxml.FXML
    public void importFile(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        if (ruta != null) {
            fileChooser.setInitialDirectory(ruta);
        }
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel file (*.xlsx)", "*.xlsx"));
        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(((Node) actionEvent.getTarget()).getScene().getWindow());
        if (selectedFiles != null && selectedFiles.size() > 0) {
            for (File file : selectedFiles) {
                if (!files.contains(file)) {
                    files.add(file);
                }
            }
            ruta = files.get(0).getParentFile();
        }
        entidades = new ArrayList<>();
        fillTable();
    }

    private void removeDuplicates() {
        if (entidades.size() > 1) {
            Entidad replace = null;
            for (Entidad entidad : entidades) {
                boolean igual = false;
                for (Entidad entidad2 : entidadesUnicas) {
                    if (entidad.equals(entidad2)) {
                        replace = entidad2;
                        igual = true;
                        break;
                    }
                }
                if (igual) {
                    if (entidad.getFechaRaw().isAfter(replace.getFechaRaw())) {
                        entidadesUnicas.set(entidadesUnicas.indexOf(replace), entidad);
                    }
                } else {
                    entidadesUnicas.add(entidad);
                }
            }
        }
    }

    private void getBotellas() {
        try {
            FileInputStream is = new FileInputStream(new File("Codigos gases.xlsx"));
            XSSFWorkbook workbook = new XSSFWorkbook(is);
            XSSFSheet sheet = workbook.getSheetAt(0);

            Iterator<Row> rowIterator = sheet.rowIterator();
            for (int i = 0; i < sheet.getLastRowNum() + 1; i++) {
                Row row = rowIterator.next();
                Iterator<Cell> cellIterator = row.cellIterator();
                Botella botella = new Botella();
                if (row.getRowNum() > 0) {
                    while (cellIterator.hasNext()) {
                        Cell cell = cellIterator.next();
                        switch (cell.getColumnIndex()) {
                            case 0:
                                botella.setLleno(Float.parseFloat(cell.toString()));
                                break;
                            case 1:
                                botella.setVacio(Float.parseFloat(cell.toString()));
                                break;
                            default:
                                break;
                        }
                    }
                }
                if (botella.getLleno() != 0 && botella.getVacio() != 0) {
                    botellas.add(botella);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @javafx.fxml.FXML
    public void vaciar(ActionEvent actionEvent) {
        txtCtn.setText("");
        txtCodCliente.setText("");
        initialize();
    }

    @javafx.fxml.FXML
    public void fillTable(ActionEvent actionEvent) {
        fillTable();
    }
}
