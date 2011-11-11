package com.kingsfleet.simple.x.jaxrs;

import com.kingsfleet.simple.x.X;

import java.io.OutputStream;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

@Provider
@Produces({"text/xml", "application/xml", "application/*xml"})
public class XMessageBodyWriter
  implements MessageBodyWriter<X> {


    @Override
    public boolean isWriteable(Class<?> class1, Type type, Annotation[] annotations, MediaType mediaType) {
        return true;
    }

    /**
     * @return We don't know at this point
     */
    @Override
    public long getSize(X x, Class<?> class1, Type type, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(X x, Class<?> class1, Type type, Annotation[] annotations, MediaType mediaType,
                        MultivaluedMap<String, Object> multivaluedMap, OutputStream outputStream) {
        
        x.out(outputStream);
        
    }
}
