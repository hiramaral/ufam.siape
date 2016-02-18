/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package siapefx;

import javafx.application.Platform;
import javafx.fxml.FXML;

/**
 *
 * @author hiramaral
 */
public class Controller {
    @FXML
    private void initialize() {
        System.out.println("Controller inicializado");
    }
 
     @FXML
    private void onExit() {
        Platform.exit();
    }
    
}
