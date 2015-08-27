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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Providers;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * Jersey specific Gson JSON provider that can be configured via {@code ContextResolver<GsonBuilder>} instance.
 * <p>
 *     Configuration ({@code GsonBuilder}) can be also provided directly to the feature during registration.
 * </p>
 *
 * @author Martin Mares (martin.mares at oracle.com)
 */
@Singleton
@Produces("*/*")
@Consumes("*/*")
public class GsonProvider<T> implements MessageBodyReader<T>, MessageBodyWriter<T> {

    @Context
    private Providers providers;

    private Gson gson;

    public GsonProvider() {
        gson = new Gson();
    }

    private boolean supportsMediaType(MediaType mediaType) {
        if (mediaType == null) {
            return true;
        }
        if ("application".equals(mediaType.getType())) {
            String subtype = mediaType.getSubtype();
            if ("json".equals(subtype) || "x-javascript".equals(subtype)) {
                return true;
            }
        }
        return mediaType.getSubtype().endsWith("+json");
    }

    @PostConstruct
    public void postConstruct() {
        final ContextResolver<GsonBuilder> contextResolver = providers.getContextResolver(GsonBuilder.class, MediaType.APPLICATION_JSON_TYPE);
        if (contextResolver != null) {
            final GsonBuilder builder = contextResolver.getContext(GsonBuilder.class);
            if (builder != null) {
                gson = builder.create();
            }
        }
    }

    private Gson getGson() {
        return gson;
    }

    /**
     * Returns {@code true} is mediaType is one of JSON types and type is not stream.
     */
    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        if (!supportsMediaType(mediaType)) {
            return false;
        }
        return  !InputStream.class.isAssignableFrom(type)
                && !Reader.class.isAssignableFrom(type);
    }

    /**
     * Returns {@code true} is mediaType is one of JSON types and type is not stream.
     */
    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        if (!supportsMediaType(mediaType)) {
            return false;
        }
        return  !OutputStream.class.isAssignableFrom(type)
                && !Writer.class.isAssignableFrom(type);
    }

    @Override
    public long getSize(T o, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public T readFrom(Class<T> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {
        return getGson().fromJson(new InputStreamReader(entityStream), genericType);
    }

    @Override
    public void writeTo(T o, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
        OutputStreamWriter writer = new OutputStreamWriter(entityStream);
        getGson().toJson(o, writer);
        writer.flush();
    }
}
