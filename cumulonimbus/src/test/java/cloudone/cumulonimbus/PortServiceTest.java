package cloudone.cumulonimbus;

import cloudone.ServiceFullName;
import cloudone.cumulonimbus.model.RegisteredRuntime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Martin Mares (martin.mares at oracle.com)
 */
public class PortServiceTest {

    private File dir;

    @Before
    public void setUp() throws Exception {
        dir = Files.createTempDirectory("unit_cumul").toFile();
    }

    @After
    public void tearDown() throws Exception {
        if (dir != null) {
            deleteDirectory(dir);
        }
    }

    public static boolean deleteDirectory(File directory) {
        if(directory.exists()){
            File[] files = directory.listFiles();
            if(null!=files){
                for(int i=0; i<files.length; i++) {
                    if(files[i].isDirectory()) {
                        deleteDirectory(files[i]);
                    }
                    else {
                        files[i].delete();
                    }
                }
            }
        }
        return(directory.delete());
    }

    @Test
    public void basicReservations() throws Exception {
        Properties props = new Properties();
        props.setProperty(PortService.KEY_PORT_RANGE, "100-200");
        props.setProperty(PortService.KEY_PORT_RANGE_ADMIN, "300-400");
        PortService ps = PortService.init(props, dir);
        assertNotNull(ps);
        assertEquals(300, ps.reserveAdminPort());
        assertEquals(301, ps.reserveAdminPort());
        assertEquals(302, ps.reserveAdminPort());
        assertEquals(303, ps.reserveAdminPort());
        assertEquals(304, ps.reserveAdminPort());
        assertEquals(100, ps.reserveApplicationPort());
        assertEquals(101, ps.reserveApplicationPort());
        assertEquals(102, ps.reserveApplicationPort());
        assertEquals(103, ps.reserveApplicationPort());
        assertEquals(104, ps.reserveApplicationPort());
    }

    @Test
    public void basicRegistration() throws Exception {
        Properties props = new Properties();
        props.setProperty(PortService.KEY_PORT_RANGE, "100-200");
        props.setProperty(PortService.KEY_PORT_RANGE_ADMIN, "300-400");
        PortService ps = PortService.init(props, dir);
        assertNotNull(ps);
        RegisteredRuntime reg = new RegisteredRuntime(new ServiceFullName("a", "b", "1"), ps.reserveAdminPort());
        reg.registerApplication("one", ps.reserveApplicationPort());
        reg.registerApplication("two", ps.reserveApplicationPort());
        assertEquals(100, reg.getApplicationPort("one"));
        assertEquals(101, reg.getApplicationPort("two"));
        String instId = ps.registerRuntime(reg);
        assertEquals(reg.getInstanceId(), instId);
        RegisteredRuntime reg2 = new RegisteredRuntime(new ServiceFullName("a", "b", "2"), ps.reserveAdminPort());
        reg2.registerApplication("one", ps.reserveApplicationPort());
        reg2.registerApplication("two", ps.reserveApplicationPort());
        assertEquals(102, reg2.getApplicationPort("one"));
        assertEquals(103, reg2.getApplicationPort("two"));
        instId = ps.registerRuntime(reg2);
        //Multi registration
        try {
            RegisteredRuntime reg3 = new RegisteredRuntime(reg.getServiceName(), reg.getAdminPort());
            reg3.registerApplication("three", 5000);
            ps.registerRuntime(reg3); //Can not register twice
            assertTrue("Can not register twice same admin port", false);
        } catch (Exception e) {}
        try {
            RegisteredRuntime reg3 = new RegisteredRuntime(reg.getServiceName(), 5500);
            reg3.registerApplication("one", reg.getApplicationPort("one"));
            ps.registerRuntime(reg3); //Can not register twice
            assertTrue("Can not register twice same appliation port", false);
        } catch (Exception e) {}
        //Load from store
        PortService ps2 = PortService.init(props, dir);
        RegisteredRuntime nreg = ps2.getRegisteredRuntime(reg.getInstanceId());
        assertFalse(reg == nreg);
        assertEquals(reg, nreg);
        RegisteredRuntime nreg2 = ps2.getRegisteredRuntime(reg2.getInstanceId());
        assertFalse(reg2 == nreg2);
        assertEquals(reg2, nreg2);
        assertEquals(104, ps2.reserveApplicationPort());
        assertEquals(2, ps2.getRegisteredRuntimes().size());
    }

    @Test
    public void owerReservation() throws Exception {
        Properties props = new Properties();
        props.setProperty(PortService.KEY_PORT_RANGE, "100-105");
        props.setProperty(PortService.KEY_PORT_RANGE_ADMIN, "300-305");
        PortService ps = PortService.init(props, dir);
        assertNotNull(ps);
        assertEquals(100, ps.reserveApplicationPort());
        assertEquals(101, ps.reserveApplicationPort());
        assertEquals(102, ps.reserveApplicationPort());
        assertEquals(103, ps.reserveApplicationPort());
        assertEquals(104, ps.reserveApplicationPort());
        assertEquals(105, ps.reserveApplicationPort());
        try {
            ps.reserveApplicationPort();
            assertTrue("No more ports", false);
        } catch (Exception exc) {}
    }
}