package cloudone.cumulonimbus;

import cloudone.ServiceFullName;
import cloudone.cumulonimbus.model.Cluster;
import cloudone.cumulonimbus.model.RegisteredRuntime;
import org.junit.Test;

import java.util.Collection;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Martin Mares (martin.mares at oracle.com)
 */
public class ServiceRegistryServiceTest {

    public static class CountingListener implements ServiceRegistryService.RegistrationListener {

        public int counter = 0;

        @Override
        public void register(RegisteredRuntime runtime) throws Exception {
            counter++;
        }

        @Override
        public void unregister(RegisteredRuntime runtime) {
            counter--;
        }
    }

    @Test
    public void testAddRegistrationListener() throws Exception {
        ServiceRegistryService service = new ServiceRegistryService();
        CountingListener listener = new CountingListener();
        service.addRegistrationListener(listener);
        RegisteredRuntime rr1 = service.register(new ServiceFullName("a", "b", "1"), 100, new HashMap<>());
        assertEquals(1, listener.counter);
        RegisteredRuntime rr2 = service.register(new ServiceFullName("a", "b", "2"), 101, new HashMap<>());
        assertEquals(2, listener.counter);
        assertTrue(service.unregister(rr1));
        assertEquals(1, listener.counter);
        // 2nd listener
        ServiceRegistryService.RegistrationListener listener2 = new ServiceRegistryService.RegistrationListener() {
                            @Override
                            public void register(RegisteredRuntime runtime) throws Exception {
                                throw new Exception();
                            }
                            @Override
                            public void unregister(RegisteredRuntime runtime) {
                            }
                        };
        service.addRegistrationListener(listener2);
        try {
            service.register(new ServiceFullName("a", "b", "3"), 103, new HashMap<>());
            assertTrue("Should not reach this point", false);
        } catch (Exception e) {}
        assertEquals(1, listener.counter);
    }

    @Test
    public void testRegisterUnregister() throws Exception {
        ServiceRegistryService service = new ServiceRegistryService();
        RegisteredRuntime rr1 = service.register(new ServiceFullName("a", "b", "1"), 100, new HashMap<>());
        RegisteredRuntime rr2 = service.register(new ServiceFullName("a", "b", "2"), 101, new HashMap<>());
        assertTrue(service.unregister(rr1));
        assertFalse(service.unregister(rr1));
    }

    @Test
    public void testGetClusters() throws Exception {
        ServiceRegistryService service = new ServiceRegistryService();
        RegisteredRuntime rr1 = service.register(new ServiceFullName("a", "b", "1"), 100, new HashMap<>());
        RegisteredRuntime rr2 = service.register(new ServiceFullName("a", "b", "2"), 101, new HashMap<>());
        RegisteredRuntime rr3 = service.register(new ServiceFullName("a", "b", "1"), 103, new HashMap<>());
        Collection<Cluster> clusters = service.getClusters();
        assertNotNull(clusters);
        assertEquals(2, clusters.size());
    }

    public static ServiceRegistryService getService() {
        return new ServiceRegistryService();
    }
}