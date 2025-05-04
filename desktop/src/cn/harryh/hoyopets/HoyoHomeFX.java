/** Copyright (c) 2022-2025, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.hoyopets;

import cn.harryh.hoyopets.assets.ModelsDataset;
import cn.harryh.hoyopets.concurrent.*;
import cn.harryh.hoyopets.controllers.BehaviorModule;
import cn.harryh.hoyopets.controllers.ModelsModule;
import cn.harryh.hoyopets.controllers.RootModule;
import cn.harryh.hoyopets.controllers.SettingsModule;
import cn.harryh.hoyopets.tray.HostTray;
import cn.harryh.hoyopets.utils.FXMLHelper;
import cn.harryh.hoyopets.utils.FXMLHelper.LoadFXMLResult;
import cn.harryh.arkpets.utils.GuiComponents;
import cn.harryh.arkpets.utils.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.Duration;

import java.util.Objects;
import java.util.UUID;

import static cn.harryh.hoyopets.Const.*;


/** HoyoPets Homepage the JavaFX app.
 */
public class HoyoHomeFX extends Application {
    public Stage stage;
    public HoyoConfig config;
    public ModelsDataset modelsDataset;
    public StackPane body;
    public GuiComponents.Toast toast;

    public RootModule rootModule;
    public ModelsModule modelsModule;
    public BehaviorModule behaviorModule;
    public SettingsModule settingsModule;

    static {
        FontsConfig.loadFontsToJavafx();
    }

    @Override
    public void start(Stage stage) throws Exception {
        Logger.info("Launcher", "Starting");
        this.stage = stage;
        Platform.setImplicitExit(false);

        // Load FXML for root node.
        LoadFXMLResult<HoyoHomeFX> fxml0 = FXMLHelper.loadFXML(getClass().getResource("/_UI/RootModule.fxml"));
        fxml0.initializeWith(this);
        rootModule = (RootModule) fxml0.controller();
        body = rootModule.body;

        // Setup scene and primary stage.
        Logger.info("Launcher", "Creating main scene");
        Scene scene = new Scene(rootModule.rootContainer);
        scene.getStylesheets().setAll(Objects.requireNonNull(getClass().getResource("/_UI/Main.css")).toExternalForm());
        scene.setFill(Color.TRANSPARENT);
        stage.getIcons().setAll(new Image(Objects.requireNonNull(getClass().getResource(iconFilePng)).toExternalForm()));
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setResizable(false);
        stage.setScene(scene);
        stage.setTitle(desktopTitle);
        rootModule.titleText.setText(desktopTitle);

        // After the stage is shown, do initialization.
        stage.show();
        rootModule.popSplashScreen(e -> {
            // Initialize socket server and HostTray.
            try {
                HostTray hostTray = HostTray.getInstance();
                hostTray.setOnCloseStage(() -> Platform.runLater(rootModule::exit));
                hostTray.setOnShowStage(() -> Platform.runLater(stage::show));
                SocketServer.getInstance().startServer(hostTray);
                hostTray.applyTrayIcon();
            } catch (PortUtils.NoPortAvailableException ex) {
                Logger.error("SocketServer", "No available port, thus server cannot be started");
                // No HostTray icon will be applied when this situation happens.
            } catch (PortUtils.ServerCollisionException ex) {
                Logger.error("SocketServer", "Server is already running");
                SocketClient socketClient = new SocketClient();
                socketClient.connect(() -> {
                            Logger.info("Launcher", "Request to start an existed Launcher");
                            socketClient.sendRequest(SocketData.ofOperation(UUID.randomUUID(), SocketData.Operation.ACTIVATE_LAUNCHER));
                            socketClient.disconnect();
                        },
                        new SocketClient.ClientSocketSession(socketClient, null));
                // Explicitly cancel the followed initialization in this start method.
                Platform.exit();
                return;
            } catch (Exception ex) {
                Logger.error("Launcher", "Failed to initialize socket server or HostTray, details see below.", ex);
            }

            // Initialize modules.
            Logger.info("Launcher", "Loading modules");
            try {
                LoadFXMLResult<HoyoHomeFX> fxml1 = FXMLHelper.loadFXML("/_UI/ModelsModule.fxml");
                LoadFXMLResult<HoyoHomeFX> fxml2 = FXMLHelper.loadFXML("/_UI/BehaviorModule.fxml");
                LoadFXMLResult<HoyoHomeFX> fxml3 = FXMLHelper.loadFXML("/_UI/SettingsModule.fxml");
                fxml1.addToNode(rootModule.wrapper1);
                fxml2.addToNode(rootModule.wrapper2);
                fxml3.addToNode(rootModule.wrapper3);
                modelsModule = (ModelsModule) fxml1.initializeWith(this);
                behaviorModule = (BehaviorModule) fxml2.initializeWith(this);
                settingsModule = (SettingsModule) fxml3.initializeWith(this);
            } catch (Exception ex) {
                Logger.error("Launcher", "Failed to initialize module, details see below.", ex);
            }

            // Post initialization.
            rootModule.syncRemoteMetaInfo();
            rootModule.moduleWrapperComposer.activate(0);

            Logger.info("Launcher", "Finished starting");
        }, Duration.ZERO, durationFast);
    }

    @Override
    public void stop() {
        if (config != null && config.launcher_solid_exit) {
            // Notify HoyoPets core instances that connected to this app to close.
            HostTray.getInstance().forEachMemberTray(memberTray -> memberTray.sendOperation(SocketData.Operation.LOGOUT));
        }
        SocketServer.getInstance().stopServer();
        ProcessPool.getInstance().shutdown();
        Logger.debug("Launcher", "Finished stopping");
    }

    public void popLoading(EventHandler<ActionEvent> handler) {
        rootModule.popLoading(handler);
    }

    public Window getWindow() {
        return rootModule.root.getScene().getWindow();
    }
}
