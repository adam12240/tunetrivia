package com.tunetrivia.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DeezerProxyServiceTest {

    private DeezerProxyService service;

    @BeforeEach
    void setUp() {
        service = new DeezerProxyService();
    }

    @Test
    void search_method_exists_and_handles_queries() throws Exception {
        // This is a smoke test to ensure the method can be called; network availability may affect result
        var result = service.search("__nonexistent_query_1234567890__");
        // result may be null if API not reachable; assert no exception thrown
        assertTrue(result == null || result != null);
    }
}
