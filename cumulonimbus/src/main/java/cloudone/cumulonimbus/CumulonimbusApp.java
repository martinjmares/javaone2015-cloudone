package cloudone.cumulonimbus;

import cloudone.C1Application;
import cloudone.C1Services;
import cloudone.cumulonimbus.resources.ConfigurationResource;
import cloudone.cumulonimbus.resources.LifecycleResource;
import cloudone.cumulonimbus.resources.ServiceResource;
import cloudone.internal.provider.DurationMessageBodyWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * Cumulonimbus represents central cloudOne service. Main goals are to provide configuration and navigation services.
 *
 * @author Martin Mares (martin.mares at oracle.com)
 */
public class CumulonimbusApp extends C1Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(CumulonimbusApp.class);
    public static final String BASE_DIR_NAME = "cumulonimbus";
    private static final String CONFIG_DIR_NAME = "config";
    private static final String FILE_NAME = "cumulonimbus.properties";

    public Set<Class<?>> getClasses() {
        return new HashSet<Class<?>>(Arrays.asList(new Class<?>[] {
                LifecycleResource.class,
                ConfigurationResource.class,
                ServiceResource.class,
                DurationMessageBodyWriter.class
        }));
    }

    @Override
    public void init() throws Exception {
        super.init();
        File cumulunDir = new File(C1Services.getInstance().getRuntimeInfo().getHomeDirectory(), CumulonimbusApp.BASE_DIR_NAME);
        if (!cumulunDir.exists()) {
            if (!cumulunDir.mkdirs()) {
                throw new IOException("Cumulonimbus home directory " + cumulunDir.getPath() + " does not exists and cannot be created!");
            }
        }
        Properties properties = loadConfiguration(cumulunDir);
        PortService portService = PortService.init(properties);
        ServiceRegistryService.init(cumulunDir, portService.getNewListener());
    }

    private Properties loadConfiguration(File cumulonimbusDir) throws Exception {
        File dir = new File(cumulonimbusDir, CONFIG_DIR_NAME);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new IOException("Configuration directory " + dir.getPath() + " does not exists and cannot be created!");
            }
        }
        File propsFile = new File(dir, FILE_NAME);
        if (!propsFile.exists()) {
            LOGGER.info("Writing default Cumulonimbus configuration to " + propsFile.getPath());
            try (InputStream is = getClass().getClassLoader().getResourceAsStream("cumulonimbus_default.properties");
                 OutputStream os = new FileOutputStream(propsFile)) {
                byte[] buff = new byte[64];
                int ind = 0;
                while ((ind = is.read(buff)) > 0) {
                    os.write(buff, 0, ind);
                }
            }
        }
        //Read props from the file
        Properties result = new Properties();
        result.load(new FileInputStream(propsFile));
        return result;
    }
}
