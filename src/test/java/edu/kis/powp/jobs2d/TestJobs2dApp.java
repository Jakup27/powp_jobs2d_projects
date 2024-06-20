package edu.kis.powp.jobs2d;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.kis.legacy.drawer.panel.DrawPanelController;
import edu.kis.legacy.drawer.shape.LineFactory;
import edu.kis.powp.appbase.Application;
import edu.kis.powp.jobs2d.command.ImporterFactory;
import edu.kis.powp.jobs2d.command.JsonCommandImporter;
import edu.kis.powp.jobs2d.command.gui.CommandManagerWindow;
import edu.kis.powp.jobs2d.command.gui.CommandManagerWindowCommandChangeObserver;
import edu.kis.powp.jobs2d.drivers.*;
import edu.kis.powp.jobs2d.drivers.LoggerDriver;
import edu.kis.powp.jobs2d.drivers.adapter.LineDriverAdapter;
import edu.kis.powp.jobs2d.drivers.transformators.TransformingJob2dDriverDecorator;
import edu.kis.powp.jobs2d.drivers.visitor.IVisitableDriver;
import edu.kis.powp.jobs2d.transformations.*;
import edu.kis.powp.jobs2d.events.*;
import edu.kis.powp.jobs2d.features.*;

public class TestJobs2dApp {
    private final static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    /**
     * Setup test concerning preset figures in context.
     *
     * @param application Application context.
     */
    private static void setupPresetTests(Application application) {
        SelectTestFigureOptionListener selectTestFigureOptionListener = new SelectTestFigureOptionListener(
                DriverFeature.getDriverManager());
        SelectTestFigure2OptionListener selectTestFigure2OptionListener = new SelectTestFigure2OptionListener(
                DriverFeature.getDriverManager());

        application.addTest("Figure Joe 1", selectTestFigureOptionListener);
        application.addTest("Figure Joe 2", selectTestFigure2OptionListener);
    }

    /**
     * Setup test using driver commands in context.
     *
     * @param application Application context.
     */
    private static void setupCommandTests(Application application) {
        application.addTest("Load Compound Rectangle command", new SelectLoadCompoundRectangleCommandOptionListener());

        application.addTest("Load secret command", new SelectLoadSecretCommandOptionListener());

        application.addTest("Load recorded command", new SelectLoadRecordedCommandOptionListener());

        application.addTest("Load deeply complex command", new SelectLoadDeeplyComplexCommandOptionListener());

        application.addTest("Run command", new SelectRunCurrentCommandOptionListener(DriverFeature.getDriverManager()));

    }

    private static void setupVisitorTests(Application application) {
        application.addTest("Show current command stats", new VisitorTest());
        application.addTest("Save deep copy of loaded command", new DeepCopyVisitorSaveTest());
        application.addTest("Load deep copy of saved command", new DeepCopyVisitorTest());
        application.addTest("Count loaded drivers", new DriverCountingVisitorTest());
    }

    private static void setupCommandTransformationVisitorTests(Application application) {
        application.addTest("Flip command ↔ horizontally", new CommandHorizontalFlipTest());
        application.addTest("Flip command ↕ vertically", new CommandVerticalFlipTest());
        application.addTest("Scale command (scale = 2)", new CommandScaleTest(2));
        application.addTest("Rotate command (degrees = 15)", new CommandRotateTest(15));
    }

    /**
     * Setup driver manager, and set default IVisitableDriver for application.
     *
     * @param application Application context.
     */
    private static void setupDrivers(Application application) {
        IVisitableDriver loggerDriver = new LoggerDriver(false);
        DriverFeature.addDriver("Simple Logger driver", loggerDriver);

        IVisitableDriver loggerDriver2 = new LoggerDriver(true);
        DriverFeature.addDriver("Detailed Logger driver", loggerDriver2);

        DrawPanelController drawerController = DrawerFeature.getDrawerController();
        IVisitableDriver driver = new RecordingDriverDecorator(new LineDriverAdapter(drawerController, LineFactory.getBasicLine(), "basic"));
        DriverFeature.addDriver("Line Simulator with Recording Support", driver);
        DriverFeature.getDriverManager().setCurrentDriver(driver);

        driver = new RecordingDriverDecorator(new LineDriverAdapter(drawerController, LineFactory.getSpecialLine(), "special"));
        DriverFeature.addDriver("Special Line Simulator with Recording Support", driver);
        driver = new LineDriverAdapter(drawerController, LineFactory.getSpecialLine(), "special");
        DriverFeature.addDriver("Special line Simulator", driver);

        driver = new LoggerDriver(false);
        UsageMonitorDriverDecorator usageMonitorDriver = new UsageMonitorDriverDecorator(driver);
        DriverFeature.addDriver("Usage monitor with logger", usageMonitorDriver);

        driver = new LineDriverAdapter(drawerController, LineFactory.getSpecialLine(), "special");
        UsageMonitorDriverDecorator usageMonitorDriver2 = new UsageMonitorDriverDecorator(driver);
        DriverFeature.addDriver("Special line Simulator with usage monitor", usageMonitorDriver2);

        driver = new RealTimeDecoratorDriver(new LineDriverAdapter(drawerController, LineFactory.getBasicLine(), "basic"), application.getFreePanel());
        DriverFeature.addDriver("Basic line Simulator with real time drawing", driver);
        driver = new RealTimeDecoratorDriver(new LineDriverAdapter(drawerController, LineFactory.getSpecialLine(), "special"), application.getFreePanel());
        DriverFeature.addDriver("Special line Simulator with real time drawing", driver);

        DriverFeature.updateDriverInfo();

        DriversComposite driversComposite = new DriversComposite();
        driversComposite.addDriver(new LineDriverAdapter(drawerController, LineFactory.getBasicLine(), "basic"));
        driversComposite.addDriver(new LoggerDriver(true));
        DriverFeature.addDriver("BasicLine with Logger", driversComposite);

        IVisitableDriver lineFlippedDriver = new TransformingJob2dDriverDecorator(new LineDriverAdapter(drawerController, LineFactory.getBasicLine(), "basic"), new VerticalFlipTransformation());
        DriverFeature.addDriver("Line vertical Flip", lineFlippedDriver);

        IVisitableDriver lineShiftedDriver = new TransformingJob2dDriverDecorator(new LineDriverAdapter(drawerController, LineFactory.getBasicLine(), "basic"), new ShiftTransformation(50, -20));
        IVisitableDriver lineShiftedAndFlippedDriver = new TransformingJob2dDriverDecorator(lineShiftedDriver, new HorizontalFlipTransformation());
        DriverFeature.addDriver("Line Shift (50,-20) and horizontal Flip", lineShiftedAndFlippedDriver);

        IVisitableDriver lineScaledDriver = new TransformingJob2dDriverDecorator(new LineDriverAdapter(drawerController, LineFactory.getBasicLine(), "basic"), new ScaleTransformation(1.5));
        IVisitableDriver lineScaledAndRotatedDriver = new TransformingJob2dDriverDecorator(lineScaledDriver, new RotateTransformation(90));
        DriverFeature.addDriver("Line Scale 1.5 and Rotate 90deg", lineScaledAndRotatedDriver);

        DriverFeature.updateDriverInfo();
    }

    private static void setupWindows(Application application) {

        CommandManagerWindow commandManager = new CommandManagerWindow(CommandsFeature.getCommandManager(), DriverFeature.getDriverManager());
        application.addWindowComponent("Command Manager", commandManager);

        CommandManagerWindowCommandChangeObserver windowObserver = new CommandManagerWindowCommandChangeObserver(
                commandManager);
        CommandsFeature.getCommandManager().getChangePublisher().addSubscriber(windowObserver);
    }

    /**
     * Setup menu for adjusting logging settings.
     *
     * @param application Application context.
     */
    private static void setupLogger(Application application) {

        application.addComponentMenu(Logger.class, "Logger", 0);
        application.addComponentMenuElement(Logger.class, "Clear log",
                (ActionEvent e) -> application.flushLoggerOutput());
        application.addComponentMenuElement(Logger.class, "Fine level", (ActionEvent e) -> logger.setLevel(Level.FINE));
        application.addComponentMenuElement(Logger.class, "Info level", (ActionEvent e) -> logger.setLevel(Level.INFO));
        application.addComponentMenuElement(Logger.class, "Warning level",
                (ActionEvent e) -> logger.setLevel(Level.WARNING));
        application.addComponentMenuElement(Logger.class, "Severe level",
                (ActionEvent e) -> logger.setLevel(Level.SEVERE));
        application.addComponentMenuElement(Logger.class, "OFF logging", (ActionEvent e) -> logger.setLevel(Level.OFF));
    }


    private static void setupImporters() {
        ImporterFactory.addImporter("json", new JsonCommandImporter());
    }

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                Application app = new Application("Jobs 2D");
                DrawerFeature.setupDrawerPlugin(app);
                CommandsFeature.setupCommandManager();
                RecordFeature.setupRecorderPlugin(app);
                DriverFeature.setupDriverPlugin(app);
                MouseSettingsFeature.setupMouseSettingsFeature(app);
                setupDrivers(app);
                setupPresetTests(app);
                setupCommandTests(app);
                setupVisitorTests(app);
                setupCommandTransformationVisitorTests(app);
                setupLogger(app);
                setupWindows(app);
                setupImporters();
                app.setVisibility(true);
            }
        });
    }

}
