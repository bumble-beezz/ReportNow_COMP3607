// Student IDs: 816044172
package com.group22.reportnow.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

class StoreStudentIdTest {
    @Test
    void returnsAStudentId() {
        StoreStudentId store = new StoreStudentId();
        assertNotNull(store);
    }
}