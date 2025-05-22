/**
 * Sample Skeleton for 'connect.fxml' Controller Class
 */

package il.cshaifasweng.OCSFMediatorExample.client;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class ConnectController {

  @FXML // fx:id="ipTF"
  private TextField ipTF; // Value injected by FXMLLoader

  @FXML
  void connect(ActionEvent event) throws IOException {
    if (!ipTF.getText().equals("")) {
      SimpleClient.setHostIp(ipTF.getText());
    }
    App.connect();
  }

}
