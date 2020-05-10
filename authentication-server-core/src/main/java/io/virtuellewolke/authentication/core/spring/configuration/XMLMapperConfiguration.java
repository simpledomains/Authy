package io.virtuellewolke.authentication.core.spring.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class XMLMapperConfiguration implements InitializingBean {

    private final MappingJackson2XmlHttpMessageConverter xmlConverter;

    @Override
    public void afterPropertiesSet() throws Exception {
        ObjectMapper mapper = xmlConverter.getObjectMapper();

        if (mapper instanceof XmlMapper) {
            ((XmlMapper) mapper).configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
        }
    }
}
