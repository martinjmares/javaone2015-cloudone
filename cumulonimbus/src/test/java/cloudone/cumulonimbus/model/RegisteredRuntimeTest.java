package cloudone.cumulonimbus.model;

import cloudone.ServiceFullName;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

/**
 * @author Martin Mares (martin.mares at oracle.com)
 */
public class RegisteredRuntimeTest {

    @Test
    public void jsonTest() {
        RegisteredRuntime r = new RegisteredRuntime(new ServiceFullName("a.a.a", "b", "1.0"), 1000);
        r.registerApplication("app1", 1001);
        r.registerApplication("app2", 1001);
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        String s = gson.toJson(r);
        RegisteredRuntime r2 = gson.fromJson(s, RegisteredRuntime.class);
        assertEquals(r, r2);
    }

}
