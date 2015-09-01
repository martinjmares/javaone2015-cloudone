/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package org.glassfish.jersey.gson.internal;

import org.junit.Test;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.annotation.Annotation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Martin Mares (martin.mares at oracle.com)
 */
public class GsonProviderTest {

    public static class A {
        private final String txt;
        private final int num;

        public A(String txt, int num) {
            this.txt = txt;
            this.num = num;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof A)) return false;

            A a = (A) o;

            if (num != a.num) return false;
            return !(txt != null ? !txt.equals(a.txt) : a.txt != null);

        }

        @Override
        public int hashCode() {
            int result = txt != null ? txt.hashCode() : 0;
            result = 31 * result + num;
            return result;
        }
    }

    @Test
    public void testIsReadable() throws Exception {
        GsonProvider provider = new GsonProvider();
        assertTrue(provider.isReadable(A.class, A.class, new Annotation[0], MediaType.APPLICATION_JSON_TYPE));
        assertTrue(provider.isReadable(A.class, A.class, new Annotation[0], new MediaType("application", "foo+json")));
        assertFalse(provider.isReadable(A.class, A.class, new Annotation[0], MediaType.TEXT_PLAIN_TYPE));
        assertFalse(provider.isReadable(FileInputStream.class,
                FileInputStream.class,
                new Annotation[0],
                MediaType.APPLICATION_JSON_TYPE));
    }

    @Test
    public void testIsWriteable() throws Exception {
        GsonProvider provider = new GsonProvider();
        assertTrue(provider.isWriteable(A.class, A.class, new Annotation[0], MediaType.APPLICATION_JSON_TYPE));
        assertTrue(provider.isWriteable(A.class, A.class, new Annotation[0], new MediaType("application", "foo+json")));
        assertFalse(provider.isWriteable(A.class, A.class, new Annotation[0], MediaType.TEXT_PLAIN_TYPE));
        assertFalse(provider.isWriteable(FileOutputStream.class,
                FileOutputStream.class,
                new Annotation[0],
                MediaType.APPLICATION_JSON_TYPE));
    }

    @Test
    public void testReadWrite() throws Exception {
        GsonProvider provider = new GsonProvider();
        A a = new A("foo", 100);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        provider.writeTo(a,
                a.getClass(),
                a.getClass(),
                new Annotation[0],
                MediaType.APPLICATION_JSON_TYPE,
                new MultivaluedHashMap<String, Object>(),
                os);
        ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
        Object o = provider.readFrom(A.class,
                        A.class,
                        new Annotation[0],
                        MediaType.APPLICATION_JSON_TYPE,
                        new MultivaluedHashMap<String, String>(),
                        is);
        assertTrue(o instanceof A);
        assertEquals(a, o);
    }
}