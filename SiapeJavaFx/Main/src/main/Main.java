/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/**
 *
 * @author hiramaral
 */
public class Main extends Application { 
    @Override
    public void start(Stage primaryStage) throws Exception {
        Pane root = FXMLLoader.load(getClass().getResource("Layout.fxml"));
        
        Scene scene = new Scene(root, 850,400);
        primaryStage.setScene(scene);
      
        primaryStage.setTitle("INTERFACE GRÁFICA DA ORGEM DE PRODUÇÃO");
        primaryStage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }
    
}
