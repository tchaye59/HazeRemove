package net.almightshell.hazeremove;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

public class FXMLController implements Initializable {

    @FXML
    private ImageView imgOriginal;

    @FXML
    private ImageView imgHaze;

    @FXML
    private Label lblFileName;

    @FXML
    private Button btn_upload;

    private File file = null;

    private static final double krnlRatio = 0.01; // set kernel ratio
    private static final double minAtmosLight = 240.0; // set minimum atmospheric light
    private static final double eps = 0.000001;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }

    @FXML
    void processImageHandler(ActionEvent event) {
        try {

            file = getImageFile();
            if (file == null) {
                return;
            }
            lblFileName.setText(file.getName());

            imgOriginal.imageProperty().set(new Image(file.toURI().toURL().toString()));

            Mat image = Imgcodecs.imread(file.getAbsolutePath(), Imgcodecs.CV_LOAD_IMAGE_COLOR);

            Mat result = DarkChannelPriorDehaze.enhance(image, krnlRatio, minAtmosLight, eps);

            imgHaze.imageProperty().set(toFXImage(toBufferedImage(result)));
        } catch (IOException ex) {
            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public File getImageFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select an Image");
        if (file != null) {
            chooser.setInitialDirectory(file.getParentFile());
        }
        //Set extension filter
        FileChooser.ExtensionFilter extFilterJPG = new FileChooser.ExtensionFilter("JPG files (*.jpg)", "*.JPG");
        FileChooser.ExtensionFilter extFilterPNG = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.PNG");
        chooser.getExtensionFilters().addAll(extFilterJPG, extFilterPNG);
        return chooser.showOpenDialog(MainApp.stage);
    }

    public static WritableImage toFXImage(BufferedImage bi) {
        if (bi == null) {
            return null;
        }
        try {
            WritableImage wi = SwingFXUtils.toFXImage(bi, null);
            bi.flush();
            bi = null;
            return wi;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public BufferedImage toBufferedImage(Mat m) {
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (m.channels() > 1) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        int bufferSize = m.channels() * m.cols() * m.rows();
        byte[] b = new byte[bufferSize];
        m.get(0, 0, b); // get all the pixels
        BufferedImage image = new BufferedImage(m.cols(), m.rows(), type);
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(b, 0, targetPixels, 0, b.length);
        return image;
    }

}
