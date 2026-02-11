package com.banking.account.config;

import jakarta.xml.bind.Marshaller;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.config.annotation.WsConfigurerAdapter;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;

import java.util.HashMap;
import java.util.Map;

@EnableWs
@Configuration
public class WebServiceConfig extends WsConfigurerAdapter {

    @Bean
    public Jaxb2Marshaller marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setPackagesToScan("com.banking.account.soap");
        Map<String, Object> props = new HashMap<>();
        props.put(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setMarshallerProperties(props);
        return marshaller;
    }

    @Bean(name = "accounts")
    public DefaultWsdl11Definition defaultWsdl11Definition(XsdSchema accountSchema) {
        DefaultWsdl11Definition definition = new DefaultWsdl11Definition();
        definition.setPortTypeName("AccountPort");
        definition.setLocationUri("/ws");
        definition.setTargetNamespace("http://banking.com/account/soap");
        definition.setSchema(accountSchema);
        return definition;
    }

    @Bean
    public XsdSchema accountSchema() {
        return new SimpleXsdSchema(new ClassPathResource("account.xsd"));
    }
}
