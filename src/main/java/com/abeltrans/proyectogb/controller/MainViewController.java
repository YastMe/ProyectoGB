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
import javafx.stage.FileChooser;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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

    private void getFiles() {
        listFilesForFolder(new File("clientes"), files);
        listFilesForFolder(new File("proveedores"), files);
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
        lblTotal.setText(olEntidades.size() + "");
        tvEntidades.getItems().setAll(olEntidades);
    }

    private void fillTable() {
        olEntidades = FXCollections.observableArrayList();
        tcFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        tcFecha.setSortable(false);
        tcArticulo.setCellValueFactory(new PropertyValueFactory<>("articulo"));
        tcCtn.setCellValueFactory(new PropertyValueFactory<>("numSerie"));
        tcCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        getFiles();
        getEntidades();
        removeDuplicates();
        sortEntidades();
        olEntidades.addAll(entidades);
        lblTotal.setText(olEntidades.size() + "");
        tvEntidades.getItems().setAll(olEntidades);
    }

    public void initialize() {
        files = new ArrayList<>();
        entidades = new ArrayList<>();
        entidadesUnicas = new ArrayList<>();
        botellas = new ArrayList<>();
        total = 0;
        getBotellas();
        fillTable();
    }

    private void sortEntidades() {
        if (entidadesUnicas != null && entidadesUnicas.size() > 0){
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
        for (File file : files) {
            if (file.getName().contains("cliente")) {
                try {
                    is = new FileInputStream(file);
                    XSSFWorkbook workbook = new XSSFWorkbook(is);
                    XSSFSheet sheet = workbook.getSheetAt(0);

                    Iterator<Row> rowIterator = sheet.rowIterator();

                    for (int i = 0; i < sheet.getLastRowNum() + 1; i++) {
                        Row row = rowIterator.next();
                        Iterator<Cell> cellIterator = row.cellIterator();
                        Entidad cliente = new Entidad();
                        if (row.getRowNum() > 0) {
                            while (cellIterator.hasNext()) {
                                Cell cell = cellIterator.next();
                                switch (cell.getColumnIndex()) {
                                    case (2):
                                        cliente.setFechaRaw(cell.getDateCellValue());
                                        break;
                                    case (6):
                                        cliente.setArticulo((int) Float.parseFloat(cell.toString()));
                                        break;
                                    case (8):
                                        cliente.setNumSerie(cell.toString());
                                        break;
                                    case (16):
                                        cliente.setCodigo(cell.toString());
                                        break;
                                    default:
                                        break;
                                }
                            }
                        }
                        if (cliente.getCodigo() != null && cliente.getArticulo() != 0 && cliente.getNumSerie() != null) {
                            clientes.add(cliente);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (file.getName().contains("proveedor")) {
                try {
                    is = new FileInputStream(file);
                    XSSFWorkbook workbook = new XSSFWorkbook(is);
                    XSSFSheet sheet = workbook.getSheetAt(0);

                    Iterator<Row> rowIterator = sheet.rowIterator();
                    for (int i = 0; i < sheet.getLastRowNum() + 1; i++) {
                        Row row = rowIterator.next();
                        Iterator<Cell> cellIterator = row.cellIterator();
                        Entidad proveedor = new Entidad();
                        if (row.getRowNum() > 0) {
                            while (cellIterator.hasNext()) {
                                Cell cell = cellIterator.next();
                                switch (cell.getColumnIndex()) {
                                    case (2):
                                        proveedor.setFechaRaw(cell.getDateCellValue());
                                        break;
                                    case (6):
                                        proveedor.setArticulo((int) Float.parseFloat(cell.toString()));
                                        break;
                                    case (8):
                                        proveedor.setNumSerie(cell.toString());
                                        break;
                                    case (17):
                                        proveedor.setCodigo(cell.toString());
                                        break;
                                    default:
                                        break;
                                }
                            }
                        }
                        if (proveedor.getCodigo() != null && proveedor.getArticulo() != 0 && proveedor.getNumSerie() != null) {
                            proveedores.add(proveedor);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        entidades.addAll(clientes);
        entidades.addAll(proveedores);
    }

    @javafx.fxml.FXML
    public void exportFile(ActionEvent actionEvent) {
        XSSFWorkbook workbook = new XSSFWorkbook();
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
        }

        try {
            FileChooser fileChooser = new FileChooser();
            File export = fileChooser.showSaveDialog(((Node) actionEvent.getTarget()).getScene().getWindow());
            if (export != null) {
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
        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(((Node) actionEvent.getTarget()).getScene().getWindow());
        if (selectedFiles != null && selectedFiles.size() > 0) {
            for (File file : selectedFiles) {
                if (!files.contains(file)) {
                    files.add(file);
                }
            }
        }
        entidades = new ArrayList<>();
        fillTable();
    }

    private void removeDuplicates() {
        if (entidades.size() > 1){
            entidadesUnicas.add(entidades.get(0));
            Entidad replace = null;
            for (Entidad entidad : entidades){
                boolean igual = false;
                for (Entidad entidad2: entidadesUnicas) {
                    if (entidad.equals(entidad2)){
                        replace = entidad2;
                        igual = true;
                        for (Botella botella : botellas) {
                            if (botella.isLleno(entidad2.getArticulo())){
                                total++;
                            }
                            else {
                                total--;
                            }
                        }
                        break;
                    }
                }
                if (igual) {
                    entidadesUnicas.set(entidadesUnicas.indexOf(replace), entidad);
                }
                else{
                    entidadesUnicas.add(entidad);
                }
            }
        }
        lblTotal.setText(String.valueOf(total));
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
                if (botella.getLleno() != 0 && botella.getVacio() != 0){
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
}
