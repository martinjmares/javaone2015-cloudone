package cloudone.cumulonimbus.persistence;

import cloudone.ServiceFullName;
import cloudone.cumulonimbus.ServiceRegistryService;
import cloudone.cumulonimbus.ServiceRegistryServiceTest;
import cloudone.cumulonimbus.model.Cluster;
import cloudone.cumulonimbus.model.RegisteredRuntime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Martin Mares (martin.mares at oracle.com)
 */
public class ServiceRegistryPersistenceTest {

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
    public void testStore() throws Exception {
        ServiceRegistryService service = ServiceRegistryServiceTest.getService();
        ServiceRegistryPersistence persistence = new ServiceRegistryPersistence(new File(dir, "strg.json"), service, true);
        service.addRegistrationListener(persistence);
        Map<String, Integer> apps = new HashMap<>();
        apps.put("one", 200);
        apps.put("two", 201);
        RegisteredRuntime rr1 = service.register(new ServiceFullName("a", "b", "1"), 100, apps);
        apps.put("one", 202);
        apps.put("two", 203);
        apps.put("three", 203);
        RegisteredRuntime rr2 = service.register(new ServiceFullName("a", "b", "2"), 101, apps);
        apps.clear();
        apps.put("foo", 204);
        apps.put("bar", 205);
        RegisteredRuntime rr3 = service.register(new ServiceFullName("a", "b", "1"), 102, apps);
        persistence.store();
        Collection<Cluster> clusters = persistence.loadClusters();
        assertNotNull(clusters);
        assertEquals(2, clusters.size());
        //Find cluster
        Cluster cl = null;
        for (Cluster cluster : clusters) {
            if ((new ServiceFullName("a", "b", "1")).equals(cluster.getFullName())) {
                cl = cluster;
                break;
            }
        }
        assertNotNull(cl);
        assertNotNull(cl.getRuntimes());
        assertEquals(2, cl.getRuntimes().size());
        //Find runtime
        RegisteredRuntime rr = null;
        for (RegisteredRuntime registeredRuntime : cl.getRuntimes()) {
            if (registeredRuntime.getInstanceId() == rr1.getInstanceId()) {
                rr = registeredRuntime;
                break;
            }
        }
        assertNotNull(rr);
        assertEquals(2, rr.getApplicationPorts().size());
        assertEquals(new Integer(201), rr.getApplicationPorts().get("two"));
    }
}