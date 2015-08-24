package cloudone.cumulonimbus.model;

import cloudone.ServiceFullName;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;


/**
 * @author Martin Mares (martin.mares at oracle.com)
 */
public class RegisteredRuntimeTest {

    @Test
    public void testCopyMap() {
        Map<String, Integer> apps = new HashMap<>();
        apps.put("one", 100);
        apps.put("two", 101);
        RegisteredRuntime rr = new RegisteredRuntime(new ServiceFullName("a", "b", "1"), "sec1", 1, 200, apps);
        assertEquals(100, rr.getApplicationPort("one"));
        assertEquals(101, rr.getApplicationPort("two"));
        apps.put("three", 102);
        apps.put("one", 500);
        assertEquals(100, rr.getApplicationPort("one"));
        assertEquals(101, rr.getApplicationPort("two"));
        assertEquals(-1, rr.getApplicationPort("three"));
    }

}
