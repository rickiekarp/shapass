package net.rickiekarp.shapass;

import javafx.stage.Stage;
import net.rickiekarp.shapass.core.AppStarter;
import net.rickiekarp.shapass.view.MainLayout;
import org.jetbrains.annotations.NotNull;

public class MainApp extends AppStarter {

    /**
     * Main Method
     * @param args Program arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(@NotNull Stage stage) {
        setMainClazz(MainApp.class);

        setWinType((byte) 1);
        setMinWidth(440);
        setMinHeight(145);
        setWidth(450);
        setHeight(205);
        setLayout(new MainLayout());

        super.start(stage);
    }
}
