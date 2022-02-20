package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import javax.security.auth.callback.Callback;
import java.io.*;
import java.nio.channels.FileChannel;
import java.util.*;
import java.awt.*;
import java.net.URL;

public class Controller implements Initializable {

    @FXML
    public ListView<MyFile> listView;
    @FXML
    public TextField addressField;
    @FXML
    public TextField addField;
    @FXML
    public TextField searchField;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        /*
        listView.setCellFactory(new Callback<ListView<MyFile>, ListCell<MyFile>>() {


                                @Override
                                public ListCell<Label> call(ListView<Label> list) {
                                    ListCell<Label> cell = new ListCell<Label>() {
                                        @Override
                                        public void updateItem(Label item, boolean empty) {
                                            super.updateItem(item, empty);
                                            if (item != null) {
                                                setItem(item);
                                            }
                                        }
                                    };

                                    return cell;
                                }
                            }
        );
        */
        loadComputerData();
    }



    public void doubleMouseClick (MouseEvent event) {
        if (event.getClickCount() == 2) {
            MyFile selectedFile = listView.getSelectionModel().getSelectedItem();

            if (selectedFile.isDirectory()) {
                loadData(selectedFile);
            } else {
                Desktop desktop = Desktop.getDesktop();
                try {
                    desktop.open(selectedFile);
                } catch (IOException ignored){}
            }
        }
    }



    private void loadData (MyFile myFile) {
        // get listFiles
        File[] files = myFile.listFiles();

        // Clear list
        clearList();

        // Add
        if (files.length == 0) {
            addressField.setText(myFile.getAbsolutePath());
            return;
        }
        for (File file : files) {
            if (!isValidFileOrDirectory(file)) {
                continue;
            }
            try {
                listView.getItems().add(new MyFile(file));
            } catch (Exception e) {
                System.out.println("Problem with Adding of " + file.getName());
                System.out.println(file);
            }
        }

        // Update addressField
        updateAddressField(myFile);
    }



    public void backToParent (ActionEvent event) {
        try {
            String address = addressField.getText();
            if (address.equals("Computer")) {
                return;
            }
            MyFile currentFile = new MyFile(address);
            if (currentFile.toString().equals("Local Disk (D:)") || currentFile.toString().equals("Local Disk (C:)")) {
                loadComputerData();
            } else {
                loadData(new MyFile(new File(address).getParentFile()));
            }
        } catch (Exception e) {
            System.out.println("Error backToParent ");
        }
    }



    private void updateAddressField (MyFile myFile) {
        addressField.setText(myFile.getAbsolutePath());
    }



    private void clearList () {
        ObservableList<MyFile> observableList = FXCollections.observableArrayList(listView.getItems());
        listView.getItems().removeAll(observableList);
    }



    public static boolean isValidFileOrDirectory (File file) {
        if (file.isHidden() || !file.exists()) {
            return false;
        }
        if (file.isDirectory()) {
            try {
                File[] files = file.listFiles();
                if (files.length == 0) {}
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }



    private void loadComputerData () {
        clearList();

        listView.getItems().add(new MyFile("C:\\"));
        listView.getItems().add(new MyFile("D:\\"));

        addressField.setText("Computer");
    }



    public void addFile (ActionEvent event) throws IOException {
        String newFileName = addField.getText();
        if (newFileName.isEmpty() || addressField.getText().equals("Computer")) {
            return;
        }
        String address = addressField.getText() + File.separator + newFileName;
        File newFile = new File(address);
        if (newFile.exists()) {
            return;
        }
        System.out.println("Add file named : " + addField.getText());
        if (newFile.isDirectory()) {
            System.out.println("I can not make directory");
        } else {
            newFile.createNewFile();
            listView.getItems().add(new MyFile(newFile));
        }
    }



    public void deleteFile (ActionEvent event) {
        MyFile selectedFile = listView.getSelectionModel().getSelectedItem();
        if (selectedFile.toString().equals("Local Disk (D:)") || selectedFile.toString().equals("Local Disk (C:)")) {
            System.out.println("Can't delete " + selectedFile);
            return;
        }
        listView.getItems().remove(selectedFile);
        try {
            if (selectedFile.delete()) {
                System.out.println("Delete file complete");
            } else {
                System.out.println("Delete file error");
            }
        } catch (Exception ignored) {
            System.out.println("Delete Error");
        }
    }



    public void searchFiles (ActionEvent event) {
        String text = searchField.getText();

        Queue<File> queue = new PriorityQueue<>();
        if (addressField.getText().equals("Computer")) {
            queue.add(new File("D:\\"));
            queue.add(new File("C:\\"));
        } else {
            queue.add(new File(addressField.getText()));
        }

        clearList();

        while (!queue.isEmpty()) {
            File currentDirectory = queue.remove();
            File[] files = currentDirectory.listFiles();
            for (File file : files) {
                if (file.isDirectory() && isValidFileOrDirectory(file)) {
                    queue.add(file);
                }
                if (file.getName().contains(text)) {
                    System.out.println("Found : " + file.getName());
                    listView.getItems().add(new MyFile(file));
                }
            }
        }

    }



    private MyFile copiedFile = null;

    public void copy (ActionEvent event) {
        copiedFile = listView.getSelectionModel().getSelectedItem();
    }

    public void paste (ActionEvent event) {
        if (copiedFile == null || addressField.getText().equals("Computer")) {
            return;
        }
        try {
            createNewFileOrDirectory(copiedFile, new File(addressField.getText()));
            listView.getItems().add(new MyFile(addressField.getText() + File.separator + copiedFile.getName()));
        } catch (Exception e) {
            System.out.println("Error in Paste");
        }
    }

    private void createNewFileOrDirectory (File source, File destinationFolder) {
        try {
            if (source.isDirectory()) {
                File directory = new File(destinationFolder, source.getName());

                if (!directory.exists()){
                    if (directory.mkdir()) {
                        System.out.println("dir was created : " + directory);
                    }
                }

                File[] files = source.listFiles();
                for (File file : files) {
                    File srcFile = new File(source, file.getName());

                    createNewFileOrDirectory(srcFile, directory);
                }
            } else {
                File newFile = new File(destinationFolder, source.getName());
                if (newFile.createNewFile()) {
                    try (FileChannel srcChannel = new FileInputStream(source).getChannel();
                         FileChannel sinkChanel = new FileOutputStream(newFile).getChannel()) {

                        srcChannel.transferTo(0, srcChannel.size(), sinkChanel);
                    }
                }
            }

            System.out.println("Success copy " + source + " -> " + destinationFolder);
        } catch (Exception e) {
            System.out.println("Error in copy/paste " + source + " -> " + destinationFolder);
        }
    }



    public void toZip (ActionEvent event) throws Exception{
        try {
            String tempFileData = listView.getSelectionModel().getSelectedItem().getAbsolutePath();
            File tempFile = new File("C:\\Users\\Eugene\\IdeaProjects\\tempFile234");
            if (tempFile.createNewFile()) {
                System.out.println("Create temp file SUCCESS");
            }
            BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
            writer.write(tempFileData);
            writer.close();
            tempFile.deleteOnExit();
        } catch (Exception e) {
            return;
        }

        Stage primaryStage = new Stage();
        Parent root = FXMLLoader.load(getClass().getResource("zip_archiver.fxml"));
        primaryStage.setTitle("Zip Archiver");
        primaryStage.setScene(new Scene(root, 477, 355));
        primaryStage.show();
    }

}
