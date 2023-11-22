package com.distasilucas.cryptobalancetracker;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.Charset;

import static com.distasilucas.cryptobalancetracker.constants.Constants.PLATFORMS_ENDPOINT;

public class TestDataSource {

    public static MockHttpServletRequestBuilder retrieveAllPlatforms() {
        return MockMvcRequestBuilders.get(PLATFORMS_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON);
    }

    public static MockHttpServletRequestBuilder retrievePlatformById(String platformId) {
        var url = PLATFORMS_ENDPOINT.concat("/%s".formatted(platformId));

        return MockMvcRequestBuilders.get(url)
                .contentType(MediaType.APPLICATION_JSON);
    }

    public static MockHttpServletRequestBuilder savePlatform(String content) {
        return MockMvcRequestBuilders.post(PLATFORMS_ENDPOINT)
                .content(content)
                .contentType(MediaType.APPLICATION_JSON);
    }

    public static MockHttpServletRequestBuilder updatePlatform(String platformId, String content) {
        var url = PLATFORMS_ENDPOINT.concat("/%s".formatted(platformId));

        return MockMvcRequestBuilders.put(url)
                .content(content)
                .contentType(MediaType.APPLICATION_JSON);
    }

    public static MockHttpServletRequestBuilder deletePlatform(String platformId) {
        var url = PLATFORMS_ENDPOINT.concat("/%s".formatted(platformId));

        return MockMvcRequestBuilders.delete(url)
                .contentType(MediaType.APPLICATION_JSON);
    }

    public static String getFileContent(String path) throws IOException {
        var classPathResource = new ClassPathResource(path);
        return StreamUtils.copyToString(classPathResource.getInputStream(), Charset.defaultCharset());
    }
}
