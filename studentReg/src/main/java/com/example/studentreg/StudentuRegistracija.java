package com.example.studentreg;

import javafx.application.Application;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import javafx.scene.control.cell.CheckBoxTableCell;
import org.apache.poi.ss.usermodel.Cell;
import java.io.*;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;

public class StudentuRegistracija extends Application implements DuomenuApsikeitimas {


    public static class Asmuo {
        protected int id;
        protected SimpleStringProperty vardas;
        protected SimpleStringProperty pavarde;

        public Asmuo(int id, String vardas, String pavarde) {
            this.id = id;
            this.vardas = new SimpleStringProperty(vardas);
            this.pavarde = new SimpleStringProperty(pavarde);
        }

        public int getId() { return id; }
        public String getVardas() { return vardas.get(); }
        public void setVardas(String vardas) { this.vardas.set(vardas); }
        public String getPavarde() { return pavarde.get(); }
        public void setPavarde(String pavarde) { this.pavarde.set(pavarde); }
    }

    public static class Studentas extends Asmuo {
        private final SimpleStringProperty elPastas;
        private int grupesId;

        public Studentas(int id, String vardas, String pavarde, String elPastas, int grupesId) {
            super(id, vardas, pavarde);
            this.elPastas = new SimpleStringProperty(elPastas);
            this.grupesId = grupesId;
        }

        public String getElPastas() { return elPastas.get(); }
        public void setElPastas(String elPastas) { this.elPastas.set(elPastas); }
        public int getGrupesId() { return grupesId; }
        public void setGrupesId(int grupesId) { this.grupesId = grupesId; }
    }

    public static class Grupe {
        private final int id;
        private final SimpleStringProperty pavadinimas;
        private final SimpleStringProperty aprasymas;

        public Grupe(int id, String pavadinimas, String aprasymas) {
            this.id = id;
            this.pavadinimas = new SimpleStringProperty(pavadinimas);
            this.aprasymas = new SimpleStringProperty(aprasymas);
        }

        public int getId() { return id; }
        public String getPavadinimas() { return pavadinimas.get(); }
        public void setPavadinimas(String pavadinimas) { this.pavadinimas.set(pavadinimas); }
        public String getAprasymas() { return aprasymas.get(); }
        public void setAprasymas(String aprasymas) { this.aprasymas.set(aprasymas); }

        @Override
        public String toString() {
            return pavadinimas.get();
        }
    }

    public static class Lankomumas {
        private final int id;
        private final int studentoId;
        private final LocalDate data;
        private final SimpleBooleanProperty buvo;

        public Lankomumas(int id, int studentoId, LocalDate data, boolean buvo) {
            this.id = id;
            this.studentoId = studentoId;
            this.data = data;
            this.buvo = new SimpleBooleanProperty(buvo);
        }

        public int getId() { return id; }
        public int getStudentoId() { return studentoId; }
        public LocalDate getData() { return data; }
        public boolean isBuvo() { return buvo.get(); }
        public void setBuvo(boolean buvo) { this.buvo.set(buvo); }
    }

    public static class LankomumoIrasas {
        private final SimpleStringProperty studentas;
        private final SimpleBooleanProperty buvo;
        private final int studentoId;

        public LankomumoIrasas(String studentas, boolean buvo, int studentoId) {
            this.studentas = new SimpleStringProperty(studentas);
            this.buvo = new SimpleBooleanProperty(buvo);
            this.studentoId = studentoId;
        }

        public String getStudentas() { return studentas.get(); }
        public boolean isBuvo() { return buvo.get(); }
        public void setBuvo(boolean buvo) { this.buvo.set(buvo); }
        public int getStudentoId() { return studentoId; }

        public SimpleStringProperty studentasProperty() { return studentas; }
        public SimpleBooleanProperty buvoProperty() { return buvo; }
    }

    public static class LankomumoAtaskaita {
        private final String studentas;
        private final String grupe;
        private final long pasirode;
        private final long nepasirade;
        private final double procentai;

        public LankomumoAtaskaita(String studentas, String grupe, long pasirode, long nepasirade, double procentai) {
            this.studentas = studentas;
            this.grupe = grupe;
            this.pasirode = pasirode;
            this.nepasirade = nepasirade;
            this.procentai = procentai;
        }

        public String getStudentas() { return studentas; }
        public String getGrupe() { return grupe; }
        public long getPasirode() { return pasirode; }
        public long getNepasirode() { return nepasirade; }
        public double getProcentai() { return procentai; }
    }

    // Data collections
    private final ObservableList<Studentas> studentai = FXCollections.observableArrayList();
    private final ObservableList<Grupe> grupes = FXCollections.observableArrayList();
    private final ObservableList<Lankomumas> lankomumoIrasai = FXCollections.observableArrayList();

    private int sekantisStudentoId = 1;
    private int sekantisGrupesId = 1;
    private int sekantisLankomumoId = 1;

    @Override
    public void start(Stage primaryStage) {
        TabPane tabPane = new TabPane();
        tabPane.getTabs().addAll(
                new StudentuTab().createTab("Studentai"),
                new GrupiuTab().createTab("Grupės"),
                new LankomumoTab().createTab("Lankomumas")
        );

        Scene scene = new Scene(tabPane, 900, 600);
        primaryStage.setTitle("Studentų Registracijos Sistema");
        primaryStage.setScene(scene);
        primaryStage.show();

        inicializuotiPradiniusDuomenis();
    }

    private void inicializuotiPradiniusDuomenis() {
        grupes.addAll(
                new Grupe(sekantisGrupesId++, "IF-1/1", "Informacinių sistemų 1 kursas 1 grupė"),
                new Grupe(sekantisGrupesId++, "IF-1/2", "Informacinių sistemų 1 kursas 2 grupė"),
                new Grupe(sekantisGrupesId++, "PI-2/1", "Programų inžinerija 2 kursas 1 grupė")
        );

        studentai.addAll(
                new Studentas(sekantisStudentoId++, "Jonas", "Jonaitis", "jonas@example.com", 1),
                new Studentas(sekantisStudentoId++, "Petras", "Petraitis", "petras@example.com", 1),
                new Studentas(sekantisStudentoId++, "Ona", "Onaitė", "ona@example.com", 2),
                new Studentas(sekantisStudentoId++, "Marytė", "Marijauskaitė", "maryte@example.com", 2),
                new Studentas(sekantisStudentoId++, "Antanas", "Antanaitis", "antanas@example.com", 3)
        );

        LocalDate siandien = LocalDate.now();
        lankomumoIrasai.addAll(
                new Lankomumas(sekantisLankomumoId++, 1, siandien, true),
                new Lankomumas(sekantisLankomumoId++, 2, siandien, false),
                new Lankomumas(sekantisLankomumoId++, 3, siandien.minusDays(1), true),
                new Lankomumas(sekantisLankomumoId++, 4, siandien.minusDays(1), true),
                new Lankomumas(sekantisLankomumoId++, 5, siandien.minusDays(2), false)
        );
    }

    // StudentuTab class
    private class StudentuTab extends AbstractWindowView<Studentas> {
        private TextField vardasField, pavardeField, elPastasField;
        private ComboBox<Grupe> grupeCombo;
        private Button pridetiButton, redaguotiButton, istrintiButton, importuotiButton, eksportuotiButton, ataskaitosButton;

        @Override
        protected void createInputFields() {
            HBox ivedimoLaukai = new HBox(10);
            vardasField = new TextField();
            vardasField.setPromptText("Vardas");
            pavardeField = new TextField();
            pavardeField.setPromptText("Pavardė");
            elPastasField = new TextField();
            elPastasField.setPromptText("El. paštas");
            grupeCombo = new ComboBox<>(grupes);
            grupeCombo.setPromptText("Grupė");

            ivedimoLaukai.getChildren().addAll(
                    new VBox(5, new Label("Vardas:"), vardasField),
                    new VBox(5, new Label("Pavardė:"), pavardeField),
                    new VBox(5, new Label("El. paštas:"), elPastasField),
                    new VBox(5, new Label("Grupė:"), grupeCombo)
            );

            mainPane.getChildren().add(ivedimoLaukai);
        }

        @Override
        protected void createActionButtons() {
            HBox mygtukai = new HBox(10);
            pridetiButton = new Button("Pridėti");
            redaguotiButton = new Button("Redaguoti");
            istrintiButton = new Button("Ištrinti");
            importuotiButton = new Button("Importuoti iš CSV/Excel");
            eksportuotiButton = new Button("Eksportuoti į CSV/Excel");
            ataskaitosButton = new Button("Generuoti lankomumo ataskaitą");

            mygtukai.getChildren().addAll(pridetiButton, redaguotiButton, istrintiButton,
                    importuotiButton, eksportuotiButton, ataskaitosButton);
            mainPane.getChildren().add(mygtukai);
        }

        @Override
        protected void setupDataTable() {
            TableColumn<Studentas, String> vardasCol = new TableColumn<>("Vardas");
            vardasCol.setCellValueFactory(new PropertyValueFactory<>("vardas"));

            TableColumn<Studentas, String> pavardeCol = new TableColumn<>("Pavardė");
            pavardeCol.setCellValueFactory(new PropertyValueFactory<>("pavarde"));

            TableColumn<Studentas, String> elPastasCol = new TableColumn<>("El. paštas");
            elPastasCol.setCellValueFactory(new PropertyValueFactory<>("elPastas"));

            TableColumn<Studentas, String> grupeCol = new TableColumn<>("Grupė");
            grupeCol.setCellValueFactory(cellData -> {
                int grupesId = cellData.getValue().getGrupesId();
                Grupe grupe = grupes.stream().filter(g -> g.getId() == grupesId).findFirst().orElse(null);
                return new SimpleStringProperty(grupe != null ? grupe.getPavadinimas() : "");
            });

            dataTable.getColumns().addAll(vardasCol, pavardeCol, elPastasCol, grupeCol);
            dataTable.setItems(studentai);
            mainPane.getChildren().add(dataTable);
        }

        @Override
        protected void setupEventHandlers() {
            dataTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    vardasField.setText(newSelection.getVardas());
                    pavardeField.setText(newSelection.getPavarde());
                    elPastasField.setText(newSelection.getElPastas());
                    grupeCombo.setValue(grupes.stream()
                            .filter(g -> g.getId() == newSelection.getGrupesId())
                            .findFirst()
                            .orElse(null));
                }
            });

            pridetiButton.setOnAction(e -> pridetiStudenta());
            redaguotiButton.setOnAction(e -> redaguotiStudenta());
            istrintiButton.setOnAction(e -> istrintiStudenta());
            importuotiButton.setOnAction(e -> importuotiStudentus());
            eksportuotiButton.setOnAction(e -> eksportuotiStudentus());
            ataskaitosButton.setOnAction(e -> sukurtiLankomumoAtaskaita());
        }

        @Override
        protected void clearInputFields() {
            vardasField.clear();
            pavardeField.clear();
            elPastasField.clear();
            grupeCombo.setValue(null);
        }

        private void pridetiStudenta() {
            if (grupeCombo.getValue() == null) {
                rodytiKlaida("Pasirinkite grupę");
                return;
            }

            Studentas studentas = new Studentas(
                    sekantisStudentoId++,
                    vardasField.getText(),
                    pavardeField.getText(),
                    elPastasField.getText(),
                    grupeCombo.getValue().getId()
            );

            studentai.add(studentas);
            clearInputFields();
        }

        private void redaguotiStudenta() {
            Studentas pasirinktas = dataTable.getSelectionModel().getSelectedItem();
            if (pasirinktas == null) {
                rodytiKlaida("Pasirinkite studentą redagavimui");
                return;
            }

            if (grupeCombo.getValue() == null) {
                rodytiKlaida("Pasirinkite grupę");
                return;
            }

            pasirinktas.setVardas(vardasField.getText());
            pasirinktas.setPavarde(pavardeField.getText());
            pasirinktas.setElPastas(elPastasField.getText());
            pasirinktas.setGrupesId(grupeCombo.getValue().getId());

            dataTable.refresh();
            clearInputFields();
        }

        private void istrintiStudenta() {
            Studentas pasirinktas = dataTable.getSelectionModel().getSelectedItem();
            if (pasirinktas == null) {
                rodytiKlaida("Pasirinkite studentą ištrinimui");
                return;
            }

            if (patvirtintiVeiksma("Ar tikrai norite ištrinti pasirinktą studentą?")) {
                studentai.remove(pasirinktas);
                lankomumoIrasai.removeIf(irasas -> irasas.getStudentoId() == pasirinktas.getId());
            }
        }
    }

    // GrupiuTab class
    private class GrupiuTab extends AbstractWindowView<Grupe> {
        private TextField pavadinimasField, aprasymasField;
        private Button pridetiButton, istrintiButton;

        @Override
        protected void createInputFields() {
            HBox ivedimoLaukai = new HBox(10);
            pavadinimasField = new TextField();
            pavadinimasField.setPromptText("Pavadinimas");
            aprasymasField = new TextField();
            aprasymasField.setPromptText("Aprašymas");

            ivedimoLaukai.getChildren().addAll(
                    new VBox(5, new Label("Pavadinimas:"), pavadinimasField),
                    new VBox(5, new Label("Aprašymas:"), aprasymasField)
            );

            mainPane.getChildren().add(ivedimoLaukai);
        }

        @Override
        protected void createActionButtons() {
            HBox mygtukai = new HBox(10);
            pridetiButton = new Button("Pridėti");
            istrintiButton = new Button("Ištrinti");

            mygtukai.getChildren().addAll(pridetiButton, istrintiButton);
            mainPane.getChildren().add(mygtukai);
        }

        @Override
        protected void setupDataTable() {
            TableColumn<Grupe, String> pavadinimasCol = new TableColumn<>("Pavadinimas");
            pavadinimasCol.setCellValueFactory(new PropertyValueFactory<>("pavadinimas"));

            TableColumn<Grupe, String> aprasymasCol = new TableColumn<>("Aprašymas");
            aprasymasCol.setCellValueFactory(new PropertyValueFactory<>("aprasymas"));

            dataTable.getColumns().addAll(pavadinimasCol, aprasymasCol);
            dataTable.setItems(grupes);
            mainPane.getChildren().add(dataTable);
        }

        @Override
        protected void setupEventHandlers() {
            dataTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    pavadinimasField.setText(newSelection.getPavadinimas());
                    aprasymasField.setText(newSelection.getAprasymas());
                }
            });

            pridetiButton.setOnAction(e -> pridetiGrupe());
            istrintiButton.setOnAction(e -> istrintiGrupe());
        }

        @Override
        protected void clearInputFields() {
            pavadinimasField.clear();
            aprasymasField.clear();
        }

        private void pridetiGrupe() {
            Grupe grupe = new Grupe(
                    sekantisGrupesId++,
                    pavadinimasField.getText(),
                    aprasymasField.getText()
            );

            grupes.add(grupe);
            clearInputFields();
        }

        private void istrintiGrupe() {
            Grupe pasirinkta = dataTable.getSelectionModel().getSelectedItem();
            if (pasirinkta == null) {
                rodytiKlaida("Pasirinkite grupę ištrinimui");
                return;
            }

            boolean turiStudentu = studentai.stream().anyMatch(s -> s.getGrupesId() == pasirinkta.getId());
            if (turiStudentu) {
                rodytiKlaida("Negalima ištrinti grupės, kurioje yra studentų");
                return;
            }

            if (patvirtintiVeiksma("Ar tikrai norite ištrinti pasirinktą grupę?")) {
                grupes.remove(pasirinkta);
            }
        }
    }

    // LankomumoTab class
    private class LankomumoTab extends AbstractWindowView<LankomumoIrasas> {
        private DatePicker dataPicker;
        private ComboBox<Grupe> grupeCombo;
        private CheckBox rodytiUzpildytasDienasCheck;
        private Button ikeltiButton, issaugotiButton;

        @Override
        protected void createInputFields() {
            HBox filtravimoLaukai = new HBox(10);
            dataPicker = new DatePicker(LocalDate.now());
            grupeCombo = new ComboBox<>(grupes);
            grupeCombo.setPromptText("Pasirinkite grupę");
            rodytiUzpildytasDienasCheck = new CheckBox("Rodyti tik užpildytas dienas");

            ikeltiButton = new Button("Įkelti");
            issaugotiButton = new Button("Išsaugoti");

            filtravimoLaukai.getChildren().addAll(
                    new VBox(5, new Label("Grupė:"), grupeCombo),
                    new VBox(5, new Label("Data:"), dataPicker),
                    new VBox(5, new Label(" "), ikeltiButton),
                    new VBox(5, new Label(" "), issaugotiButton),
                    new VBox(5, new Label(" "), rodytiUzpildytasDienasCheck)
            );

            mainPane.getChildren().add(filtravimoLaukai);
        }

        @Override
        protected void createActionButtons() {
            // Buttons are already created in createInputFields
        }

        @Override
        protected void setupDataTable() {
            TableColumn<LankomumoIrasas, String> studentasCol = new TableColumn<>("Studentas");
            studentasCol.setCellValueFactory(new PropertyValueFactory<>("studentas"));

            TableColumn<LankomumoIrasas, Boolean> buvoCol = new TableColumn<>("Pasirodė");
            buvoCol.setCellValueFactory(cellData -> cellData.getValue().buvoProperty());
            buvoCol.setCellFactory(CheckBoxTableCell.forTableColumn(buvoCol));
            buvoCol.setEditable(true);

            dataTable.getColumns().addAll(studentasCol, buvoCol);
            dataTable.setEditable(true);
            mainPane.getChildren().add(dataTable);
        }

        @Override
        protected void setupEventHandlers() {
            ikeltiButton.setOnAction(e -> ikeltiLankomuma());
            issaugotiButton.setOnAction(e -> issaugotiLankomuma());
            rodytiUzpildytasDienasCheck.setOnAction(e -> {
                if (grupeCombo.getValue() != null && dataPicker.getValue() != null) {
                    ikeltiLankomuma();
                }
            });
        }

        @Override
        protected void clearInputFields() {
            // Not needed for this tab
        }

        private void ikeltiLankomuma() {
            Grupe pasirinktaGrupe = grupeCombo.getValue();
            LocalDate pasirinktaData = dataPicker.getValue();

            if (pasirinktaGrupe == null || pasirinktaData == null) {
                rodytiKlaida("Pasirinkite grupę ir datą");
                return;
            }

            List<Studentas> grupesStudentai = studentai.stream()
                    .filter(s -> s.getGrupesId() == pasirinktaGrupe.getId())
                    .collect(Collectors.toList());

            ObservableList<LankomumoIrasas> lankomumoDuomenys = FXCollections.observableArrayList();

            for (Studentas studentas : grupesStudentai) {
                Optional<Lankomumas> egzistuojantisIrasas = lankomumoIrasai.stream()
                        .filter(l -> l.getStudentoId() == studentas.getId() && l.getData().equals(pasirinktaData))
                        .findFirst();

                if (rodytiUzpildytasDienasCheck.isSelected() && !egzistuojantisIrasas.isPresent()) {
                    continue;
                }

                boolean buvo = egzistuojantisIrasas.map(Lankomumas::isBuvo).orElse(false);
                lankomumoDuomenys.add(new LankomumoIrasas(
                        studentas.getVardas() + " " + studentas.getPavarde(),
                        buvo,
                        studentas.getId()
                ));
            }

            dataTable.setItems(lankomumoDuomenys);
        }

        private void issaugotiLankomuma() {
            Grupe pasirinktaGrupe = grupeCombo.getValue();
            LocalDate pasirinktaData = dataPicker.getValue();

            if (pasirinktaGrupe == null || pasirinktaData == null) {
                rodytiKlaida("Pasirinkite grupę ir datą");
                return;
            }

            for (LankomumoIrasas irasas : dataTable.getItems()) {
                Optional<Lankomumas> egzistuojantisIrasas = lankomumoIrasai.stream()
                        .filter(l -> l.getStudentoId() == irasas.getStudentoId() && l.getData().equals(pasirinktaData))
                        .findFirst();

                if (egzistuojantisIrasas.isPresent()) {
                    egzistuojantisIrasas.get().setBuvo(irasas.isBuvo());
                } else {
                    lankomumoIrasai.add(new Lankomumas(
                            sekantisLankomumoId++,
                            irasas.getStudentoId(),
                            pasirinktaData,
                            irasas.isBuvo()
                    ));
                }
            }

            rodytiInformacija("Lankomumas sėkmingai išsaugotas");
        }
    }

    // Attendance report methods
    private void sukurtiLankomumoAtaskaita() {
        Dialog<Pair<LocalDate, LocalDate>> dialog = new Dialog<>();
        dialog.setTitle("Lankomumo ataskaita");
        dialog.setHeaderText("Pasirinkite laikotarpį");

        ButtonType generuotiButtonType = new ButtonType("Generuoti", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(generuotiButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        DatePicker nuoDataPicker = new DatePicker(LocalDate.now().minusDays(7));
        DatePicker ikiDataPicker = new DatePicker(LocalDate.now());

        grid.add(new Label("Nuo:"), 0, 0);
        grid.add(nuoDataPicker, 1, 0);
        grid.add(new Label("Iki:"), 0, 1);
        grid.add(ikiDataPicker, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == generuotiButtonType) {
                return new Pair<>(nuoDataPicker.getValue(), ikiDataPicker.getValue());
            }
            return null;
        });

        Optional<Pair<LocalDate, LocalDate>> result = dialog.showAndWait();

        result.ifPresent(datos -> {
            try {
                List<LankomumoAtaskaita> ataskaita = generuotiLankomumoAtaskaita(datos.getKey(), datos.getValue());

                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Išsaugoti ataskaitą kaip PDF");
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF failai", "*.pdf"));
                File file = fileChooser.showSaveDialog(null);

                if (file != null) {
                    sukurtiPDFAtaskaita(file, ataskaita, datos.getKey(), datos.getValue());
                    rodytiInformacija("Ataskaita sėkmingai išsaugota kaip PDF");
                }
            } catch (Exception e) {
                rodytiKlaida("Klaida generuojant ataskaitą: " + e.getMessage());
            }
        });
    }

    private List<LankomumoAtaskaita> generuotiLankomumoAtaskaita(LocalDate nuo, LocalDate iki) {
        List<LankomumoAtaskaita> ataskaita = new ArrayList<>();

        for (Studentas studentas : studentai) {
            long pasirode = lankomumoIrasai.stream()
                    .filter(l -> l.getStudentoId() == studentas.getId())
                    .filter(l -> !l.getData().isBefore(nuo))
                    .filter(l -> !l.getData().isAfter(iki))
                    .filter(Lankomumas::isBuvo)
                    .count();

            long nePasirode = lankomumoIrasai.stream()
                    .filter(l -> l.getStudentoId() == studentas.getId())
                    .filter(l -> !l.getData().isBefore(nuo))
                    .filter(l -> !l.getData().isAfter(iki))
                    .filter(l -> !l.isBuvo())
                    .count();

            double procentai = (pasirode + nePasirode) > 0 ?
                    (double) pasirode / (pasirode + nePasirode) * 100 : 0;

            Grupe grupe = grupes.stream()
                    .filter(g -> g.getId() == studentas.getGrupesId())
                    .findFirst()
                    .orElse(null);

            String grupesPavadinimas = grupe != null ? grupe.getPavadinimas() : "Nėra grupės";

            ataskaita.add(new LankomumoAtaskaita(
                    studentas.getVardas() + " " + studentas.getPavarde(),
                    grupesPavadinimas,
                    pasirode,
                    nePasirode,
                    procentai
            ));
        }

        return ataskaita;
    }

    private void sukurtiPDFAtaskaita(File file, List<LankomumoAtaskaita> ataskaita, LocalDate nuo, LocalDate iki)
            throws DocumentException, FileNotFoundException {
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();

        Paragraph title = new Paragraph("Studentų lankomumo ataskaita");
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        Paragraph period = new Paragraph(
                String.format("Laikotarpis: %s - %s", nuo.toString(), iki.toString()));
        period.setAlignment(Element.ALIGN_CENTER);
        period.setSpacingAfter(20);
        document.add(period);

        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);
        table.setSpacingAfter(10);

        addTableHeader(table, "Studentas");
        addTableHeader(table, "Grupe");
        addTableHeader(table, "Pasirode");
        addTableHeader(table, "Nepasirode");
        addTableHeader(table, "Lankomumas (%)");

        for (LankomumoAtaskaita irasas : ataskaita) {
            table.addCell(irasas.getStudentas());
            table.addCell(irasas.getGrupe());
            table.addCell(String.valueOf(irasas.getPasirode()));
            table.addCell(String.valueOf(irasas.getNepasirode()));
            table.addCell(String.format("%.1f", irasas.getProcentai()));
        }

        document.add(table);
        document.close();
    }

    private void addTableHeader(PdfPTable table, String header) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        cell.setBorderWidth(2);
        cell.setPhrase(new Phrase(header));
        table.addCell(cell);
    }

    // Data exchange methods
    @Override
    public void eksportuoti(File file) throws IOException {
        if (file.getName().toLowerCase().endsWith(".csv")) {
            eksportuotiICsv(file);
        } else {
            eksportuotiIExcel(file);
        }
    }

    @Override
    public void importuoti(File file) throws IOException {
        if (file.getName().toLowerCase().endsWith(".csv")) {
            importuotiIsCSV(file);
        } else {
            importuotiIsExcel(file);
        }
    }

    @Override
    public String getSupportedFileExtensions() {
        return "*.csv, *.xlsx";
    }

    private void importuotiStudentus() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Pasirinkite failą importui");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("CSV failai", "*.csv"),
                new FileChooser.ExtensionFilter("Excel failai", "*.xlsx")
        );

        File file = fileChooser.showOpenDialog(null);
        if (file == null) return;

        try {
            if (file.getName().toLowerCase().endsWith(".csv")) {
                importuotiIsCSV(file);
            } else {
                importuotiIsExcel(file);
            }
            rodytiInformacija("Studentai sėkmingai importuoti");
        } catch (Exception e) {
            rodytiKlaida("Klaida importuojant studentus: " + e.getMessage());
        }
    }

    private void importuotiIsCSV(File file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean pirmaLinija = true;

            while ((line = reader.readLine()) != null) {
                if (pirmaLinija) {
                    pirmaLinija = false;
                    continue;
                }

                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    String vardas = parts[0].trim();
                    String pavarde = parts[1].trim();
                    String elPastas = parts[2].trim();
                    String grupesPavadinimas = parts[3].trim();

                    Grupe grupe = grupes.stream()
                            .filter(g -> g.getPavadinimas().equalsIgnoreCase(grupesPavadinimas))
                            .findFirst()
                            .orElseGet(() -> {
                                Grupe naujaGrupe = new Grupe(sekantisGrupesId++, grupesPavadinimas, "Importuota grupė");
                                grupes.add(naujaGrupe);
                                return naujaGrupe;
                            });

                    studentai.add(new Studentas(
                            sekantisStudentoId++,
                            vardas,
                            pavarde,
                            elPastas,
                            grupe.getId()
                    ));
                }
            }
        }
    }

    private void importuotiIsExcel(File file) throws IOException {
        try (Workbook workbook = WorkbookFactory.create(file)) {
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;

                String vardas = getCellStringValue(row.getCell(0));
                String pavarde = getCellStringValue(row.getCell(1));
                String elPastas = getCellStringValue(row.getCell(2));
                String grupesPavadinimas = getCellStringValue(row.getCell(3));

                if (!vardas.isEmpty() && !pavarde.isEmpty()) {
                    Grupe grupe = grupes.stream()
                            .filter(g -> g.getPavadinimas().equalsIgnoreCase(grupesPavadinimas))
                            .findFirst()
                            .orElseGet(() -> {
                                Grupe naujaGrupe = new Grupe(sekantisGrupesId++, grupesPavadinimas, "Importuota grupė");
                                grupes.add(naujaGrupe);
                                return naujaGrupe;
                            });

                    studentai.add(new Studentas(
                            sekantisStudentoId++,
                            vardas,
                            pavarde,
                            elPastas,
                            grupe.getId()
                    ));
                }
            }
        }
    }

    private String getCellStringValue(Cell cell) {
        if (cell == null) return "";

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                return String.valueOf((int) cell.getNumericCellValue());
            default:
                return "";
        }
    }

    private void eksportuotiStudentus() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Pasirinkite failą eksportui");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("CSV failai", "*.csv"),
                new FileChooser.ExtensionFilter("Excel failai", "*.xlsx")
        );

        File file = fileChooser.showSaveDialog(null);
        if (file == null) return;

        try {
            if (file.getName().toLowerCase().endsWith(".csv")) {
                eksportuotiICsv(file);
            } else {
                eksportuotiIExcel(file);
            }
            rodytiInformacija("Studentai sėkmingai eksportuoti");
        } catch (Exception e) {
            rodytiKlaida("Klaida eksportuojant studentus: " + e.getMessage());
        }
    }

    private void eksportuotiICsv(File file) throws IOException {
        try (PrintWriter writer = new PrintWriter(file)) {
            writer.println("Vardas,Pavardė,El. paštas,Grupė");

            for (Studentas studentas : studentai) {
                Grupe grupe = grupes.stream()
                        .filter(g -> g.getId() == studentas.getGrupesId())
                        .findFirst()
                        .orElse(null);

                String grupesPavadinimas = grupe != null ? grupe.getPavadinimas() : "";

                writer.println(String.join(",",
                        studentas.getVardas(),
                        studentas.getPavarde(),
                        studentas.getElPastas(),
                        grupesPavadinimas
                ));
            }
        }
    }

    private void eksportuotiIExcel(File file) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Studentai");

            // Antraštės eilutė
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Vardas");
            headerRow.createCell(1).setCellValue("Pavardė");
            headerRow.createCell(2).setCellValue("El. paštas");
            headerRow.createCell(3).setCellValue("Grupė");

            // Duomenų eilutės
            int rowNum = 1;
            for (Studentas studentas : studentai) {
                Row row = sheet.createRow(rowNum++);

                Grupe grupe = grupes.stream()
                        .filter(g -> g.getId() == studentas.getGrupesId())
                        .findFirst()
                        .orElse(null);

                String grupesPavadinimas = grupe != null ? grupe.getPavadinimas() : "";

                row.createCell(0).setCellValue(studentas.getVardas());
                row.createCell(1).setCellValue(studentas.getPavarde());
                row.createCell(2).setCellValue(studentas.getElPastas());
                row.createCell(3).setCellValue(grupesPavadinimas);
            }

            // Išsaugome failą
            try (FileOutputStream outputStream = new FileOutputStream(file)) {
                workbook.write(outputStream);
            }
        }
    }

    // Pagalbiniai metodai
    private void rodytiInformacija(String pranesimas) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Informacija");
        alert.setHeaderText(null);
        alert.setContentText(pranesimas);
        alert.showAndWait();
    }

    private void rodytiKlaida(String pranesimas) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Klaida");
        alert.setHeaderText(null);
        alert.setContentText(pranesimas);
        alert.showAndWait();
    }

    private boolean patvirtintiVeiksma(String pranesimas) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Patvirtinimas");
        alert.setHeaderText(null);
        alert.setContentText(pranesimas);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    public static void main(String[] args) {
        launch(args);
    }
}