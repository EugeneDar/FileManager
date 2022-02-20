package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipArchiver {

    @FXML
    public Text message;
    @FXML
    public Text text1;
    @FXML
    public Text text2;
    @FXML
    public Text text3;
    @FXML
    public TextField fileAddress;
    @FXML
    public TextField directoryAddress;
    @FXML
    public TextField archiveName;

    public void browse (ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File file = directoryChooser.showDialog(null);
        if (file != null) {
            directoryAddress.setText(file.getAbsolutePath());
        }

        try (BufferedReader reader = new BufferedReader(new FileReader("C:\\Users\\Eugene\\IdeaProjects\\tempFile234")) ){
            String tempText = reader.readLine();
            fileAddress.setText(tempText);
        } catch (Exception e) {
            System.out.println("File read Error (browse function)");
        }


    }

    public void toZip (ActionEvent event) throws Exception {
        text1.setText("");
        text2.setText("");
        text3.setText("");
        message.setText("");
        try {
            if (!new File(fileAddress.getText()).exists()) {
                text1.setText("File not found");
                return;
            }
            if (!new File(directoryAddress.getText()).exists()) {
                text2.setText("Directory not found");
                return;
            }
            if (archiveName.getText().isEmpty()) {
                text3.setText("Invalid file name");
                return;
            }

            File file = new File(new File(directoryAddress.getText()), archiveName.getText());
        } catch (Exception ignore) {
            text3.setText("Invalid file name");
            return;
        }

        zip(fileAddress.getText(), directoryAddress.getText() + File.separator + archiveName.getText() + ".zip");

        message.setText("Success");
    }

    private static void zip(String sourceFile, String zip_file) throws Exception
    {
        // zip_file is directory
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zip_file));
        addDirToZipArchive(zos, new File(sourceFile), null);
        zos.flush();
        zos.close();

        System.out.println("Zip " + sourceFile + " Copmlete");
    }

    private static void addDirToZipArchive(ZipOutputStream zos, File fileToZip, String parrentDirectoryName) throws Exception {
        if (fileToZip == null || !fileToZip.exists()) {
            return;
        }

        String zipEntryName = fileToZip.getName();
        if (parrentDirectoryName!=null && !parrentDirectoryName.isEmpty()) {
            zipEntryName = parrentDirectoryName + "/" + fileToZip.getName();
        }

        if (fileToZip.isDirectory()) {
            System.out.println("+" + zipEntryName);
            for (File file : fileToZip.listFiles()) {
                addDirToZipArchive(zos, file, zipEntryName);
            }
        } else {
            System.out.println("   " + zipEntryName);
            byte[] buffer = new byte[1024];
            FileInputStream fis = new FileInputStream(fileToZip);
            zos.putNextEntry(new ZipEntry(zipEntryName));
            int length;
            while ((length = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, length);
            }
            zos.closeEntry();
            fis.close();
        }
    }
}
