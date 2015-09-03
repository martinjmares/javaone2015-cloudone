package cloudone.cumulonimbus.util;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

/**
 * @author Martin Mares (martin.mares at oracle.com)
 */
public class PathUtilTest {

    @Test
    public void testNormalizePath() throws Exception {
        assertEquals("/", PathUtil.normalizePath("/"));
        assertEquals("/", PathUtil.normalizePath(""));
        assertEquals("/foo", PathUtil.normalizePath("foo"));
        assertEquals("/foo", PathUtil.normalizePath("foo/"));
        assertEquals("/foo/bar", PathUtil.normalizePath("foo/bar/"));
    }

    @Test
    public void testParsePath() throws Exception {
        assertEquals(Collections.<String>emptyList(), PathUtil.parsePath("", false));
        assertEquals(Collections.<String>emptyList(), PathUtil.parsePath("/", false));
        assertEquals(Arrays.asList("foo"), PathUtil.parsePath("foo", false));
        assertEquals(Arrays.asList("foo", "bar.xml"), PathUtil.parsePath("/foo/bar.xml", false));
        assertEquals(Arrays.asList("foo", "bar"), PathUtil.parsePath("/foo/bar/", false));
        assertEquals(Arrays.asList("foo", "", "bar"), PathUtil.parsePath("/foo//bar", false));
        assertEquals(Arrays.asList("foo", "{id}", "bar"), PathUtil.parsePath("/foo/{id}/bar", false));
        assertEquals(Arrays.asList("foo", "aaa{id}bbb", "bar"), PathUtil.parsePath("/foo/aaa{id}bbb/bar", false));
        assertEquals(Arrays.asList("foo", "aaa{id:[abc]+}bbb", "bar"), PathUtil.parsePath("/foo/aaa{id:[abc]+}bbb/bar", false));
        assertEquals(Arrays.asList("foo", "aaa{id}bbb", "bar{id2}"), PathUtil.parsePath("/foo/aaa{id}bbb/bar{id2}", false));
        //To regexp
        assertEquals(Arrays.asList("foo"), PathUtil.parsePath("foo", true));
        assertEquals(Arrays.asList("foo", "bar\\.xml"), PathUtil.parsePath("/foo/bar.xml", true));
        assertEquals(Arrays.asList("foo", "bar"), PathUtil.parsePath("/foo/bar/", true));
        assertEquals(Arrays.asList("foo", "(.*)", "bar"), PathUtil.parsePath("/foo/{id}/bar", true));
        assertEquals(Arrays.asList("foo", "aaa(.*)bbb\\.xml", "bar"), PathUtil.parsePath("/foo/aaa{id}bbb.xml/bar", true));
        assertEquals(Arrays.asList("foo", "aaa([abc]+)bbb", "bar"), PathUtil.parsePath("/foo/aaa{id:[abc]+}bbb/bar", true));
        assertEquals(Arrays.asList("foo", "aaa(.*)bbb", "bar(.*)"), PathUtil.parsePath("/foo/aaa{id}bbb/bar{id2}", true));
    }
}