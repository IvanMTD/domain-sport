package ru.fcpsr.domainsport.utils;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
public class InputStreamCollector {
    ByteArrayOutputStream targetStream = new ByteArrayOutputStream();
    @SneakyThrows
    public InputStreamCollector collectInputStream(InputStream input) {
        IOUtils.copy(input, targetStream);
        return this;
    }

    public InputStream getStream() {
        return new ByteArrayInputStream(targetStream.toByteArray());
    }

    public void closeStream(){
        try {
            targetStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
