/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package siapefx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/**
 *
 * @author hiramaral
 */
public class SiapeFx extends Application { 
    @Override
    public void start(Stage primaryStage) throws Exception {
        Pane root = FXMLLoader.load(getClass().getResource("Interface.fxml"));
        
        Scene scene = new Scene(root, 850,400);
        primaryStage.setScene(scene);
      
        primaryStage.setTitle("INTERFACE GRÁFICA DA ORGEM DE PRODUÇÃO");
        primaryStage.setMinHeight(400);
        primaryStage.setMinWidth(400);
        primaryStage.show();
    }
    public static void siapefx(String[] args) {
        launch(args);
    }
    
}
