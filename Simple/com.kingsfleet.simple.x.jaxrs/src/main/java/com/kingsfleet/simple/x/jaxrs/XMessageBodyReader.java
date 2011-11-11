package com.kingsfleet.simple.x.jaxrs;

import com.kingsfleet.simple.x.X;

import java.io.InputStream;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;



@Provider
@Consumes({"text/xml", "application/xml", "application/*xml"})
public class XMessageBodyReader 
  implements MessageBodyReader<X> {

    @Override
    public boolean isReadable(Class<?> class1, Type type, Annotation[] annotations, MediaType mediaType) {
        return true;
    }

    @Override
    public X readFrom(Class<X> class1, Type type, Annotation[] annotations, MediaType mediaType,
                      MultivaluedMap<String, String> multivaluedMap, InputStream inputStream) {
        return X.in(inputStream);
    }
}
