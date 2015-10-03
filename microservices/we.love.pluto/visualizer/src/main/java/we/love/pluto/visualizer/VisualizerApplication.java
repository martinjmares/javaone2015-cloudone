package we.love.pluto.visualizer;

import java.util.Set;

import org.glassfish.jersey.gson.GsonFeature;
import org.glassfish.jersey.media.sse.SseFeature;

import com.google.common.collect.Sets;

import cloudone.C1Application;

/**
 * @author Michal Gajdos
 */
public class VisualizerApplication extends C1Application {

    @Override
    public Set<Class<?>> getClasses() {
        return Sets.newHashSet(VisualizerResource.class, SpaceObjectResource.class,
                SseFeature.class, GsonFeature.class);
    }
}
