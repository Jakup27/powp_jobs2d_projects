package edu.kis.powp.jobs2d;

import edu.kis.powp.jobs2d.drivers.DriverManager;
import edu.kis.powp.jobs2d.drivers.visitor.DriverCountingVisitor;
import edu.kis.powp.jobs2d.drivers.visitor.IVisitableDriver;
import edu.kis.powp.jobs2d.features.DriverFeature;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;

public class DriverCountingVisitorTest implements ActionListener {
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    @Override
    public void actionPerformed(ActionEvent e) {

        DriverManager driverManager = DriverFeature.getDriverManager();
        IVisitableDriver driver = driverManager.getCurrentDriver();

        DriverCountingVisitor driverCountingVisitor = new DriverCountingVisitor();
        driver.accept(driverCountingVisitor);

        logger.info("Count of currently loaded drivers: " + driverCountingVisitor.getCount());

    }
}
