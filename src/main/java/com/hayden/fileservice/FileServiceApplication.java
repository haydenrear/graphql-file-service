package com.hayden.fileservice;

import com.hayden.graphql.models.visitor.model.DataSource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.http.codec.HttpMessageEncoder;
import org.springframework.http.converter.HttpMessageConverter;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class FileServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FileServiceApplication.class, args);
    }

}

