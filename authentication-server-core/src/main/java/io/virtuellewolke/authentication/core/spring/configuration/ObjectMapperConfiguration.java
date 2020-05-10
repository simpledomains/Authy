package io.virtuellewolke.authentication.core.spring.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlAnnotationIntrospector;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.stereotype.Component;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Slf4j
@Component
@RequiredArgsConstructor
public class ObjectMapperConfiguration implements InitializingBean {

    @Retention(RetentionPolicy.RUNTIME)
    public @interface JacksonXmlIgnore {
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface JacksonJsonIgnore {
    }

    public static class XmlIgnoreIntrospector extends JacksonXmlAnnotationIntrospector {
        @Override
        public boolean hasIgnoreMarker(AnnotatedMember m) {
            return m.hasAnnotation(JacksonXmlIgnore.class) || super.hasIgnoreMarker(m);
        }
    }

    public static class JsonIgnoreIntrospector extends JacksonAnnotationIntrospector {
        @Override
        public boolean hasIgnoreMarker(AnnotatedMember m) {
            return m.hasAnnotation(JacksonJsonIgnore.class) || super.hasIgnoreMarker(m);
        }
    }

    private final MappingJackson2XmlHttpMessageConverter xmlConverter;
    private final MappingJackson2HttpMessageConverter    converter;

    @Override
    public void afterPropertiesSet() {
        ObjectMapper xmlMapper = xmlConverter.getObjectMapper();
        if (xmlMapper instanceof XmlMapper) {
            ((XmlMapper) xmlMapper).configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
            xmlMapper.setAnnotationIntrospector(new XmlIgnoreIntrospector());
        }

        ObjectMapper jsonMapper = converter.getObjectMapper();
        jsonMapper.setAnnotationIntrospector(new JsonIgnoreIntrospector());
    }
}
