package cloudone.cumulonimbus;

import cloudone.ServiceFullName;
import cloudone.cumulonimbus.model.RegisteredRuntime;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Martin Mares (martin.mares at oracle.com)
 */
public class PortServiceTest {

    @Test
    public void basicReservations() throws Exception {
        Properties props = new Properties();
        props.setProperty(PortService.KEY_PORT_RANGE, "100-200");
        props.setProperty(PortService.KEY_PORT_RANGE_ADMIN, "300-400");
        PortService ps = PortService.init(props);
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
        PortService ps = PortService.init(props);
        assertNotNull(ps);
        ServiceRegistryService.RegistrationListener listener = ps.getNewListener();
        assertNotNull(listener);
        // Register first
        Map<String, Integer> apps = new HashMap<>();
        apps.put("one", ps.reserveApplicationPort());
        apps.put("two", ps.reserveApplicationPort());
        assertEquals(new Integer(100), apps.get("one"));
        assertEquals(new Integer(101), apps.get("two"));
        RegisteredRuntime reg1 = new RegisteredRuntime(new ServiceFullName("a", "b", "1"), "sec1", 1, ps.reserveAdminPort(), apps);
        listener.register(reg1, null);
        // Register second
        apps.put("one", ps.reserveApplicationPort());
        apps.put("two", ps.reserveApplicationPort());
        assertEquals(new Integer(102), apps.get("one"));
        assertEquals(new Integer(103), apps.get("two"));
        RegisteredRuntime reg2 = new RegisteredRuntime(new ServiceFullName("a", "b", "2"), "sec2", 2, ps.reserveAdminPort(), apps);
        listener.register(reg2, null);
        //Multi registration
        try {
            RegisteredRuntime reg3 = new RegisteredRuntime(reg1.getServiceName(),  "secELSE", 1, reg1.getAdminPort(), reg1.getApplicationPorts());
            listener.register(reg3, null); //Can not register twice
            assertTrue("Can not register twice same admin port", false);
        } catch (Exception e) {}
    }

    @Test
    public void owerReservation() throws Exception {
        Properties props = new Properties();
        props.setProperty(PortService.KEY_PORT_RANGE, "100-105");
        props.setProperty(PortService.KEY_PORT_RANGE_ADMIN, "300-305");
        PortService ps = PortService.init(props);
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