package com.kenshoo.integrations.service;

import com.kenshoo.integrations.dao.IntegrationsDao;
import com.kenshoo.integrations.entity.Integration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class IntegrationsServiceImplTest {
    @Mock
    private KsNormalizerClient normalizerClient;

    @Mock
    private IntegrationsDao integrationsDao;

    @InjectMocks
    private IntegrationsServiceImpl integrationsService;


    @Before
    public void setUp() throws IOException {
        // Set up common mock behavior here
        when(normalizerClient.normalize("123")).thenReturn("normalized_123");

        when(integrationsDao.fetchAll()).thenReturn(Arrays.asList(
                new Integration(1, "123", "data1"),
                new Integration(2, "456", "data2"),
                new Integration(3, "789", "data3")
        ));

        when(integrationsDao.updateKsId(anyString(), anyString())).thenReturn(1);
    }

    @Test

    public void testInsertIntegration() {
        try {
            // Calling the method to test
            integrationsService.insertIntegration("123", "data");

            // assertions based on the expected behavior
            verify(normalizerClient, times(1)).normalize("123");
            verify(integrationsDao, times(1)).insert("normalized_123", "data");
        } catch (IOException e) {
            e.printStackTrace();
            fail("IOException was thrown unexpectedly");
        }
    }


    @Test
    public void testFetchIntegrationsByKsId() {
        // Set up
        when(integrationsDao.fetchByKsId("normalized_123")).thenReturn(Arrays.asList(
                new Integration(1, "normalized_123", "data1"),
                new Integration(2, "normalized_123", "data2")
        ));

        // Call the method to test
        List<Integration> result = integrationsService.fetchIntegrationsByKsId("123");

        // Aassertions based on the expected behavior
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("data1", result.get(0).getData());
        assertEquals("data2", result.get(1).getData());

    }

    @Test
    public void testMigrate() {
        try {
            when(normalizerClient.normalize("123")).thenReturn("normalized_123");
            when(normalizerClient.normalize("456")).thenReturn("normalized_456");
            when(normalizerClient.normalize("789")).thenReturn("normalized_789");
            when(integrationsDao.updateKsId("123", "normalized_123")).thenReturn(1);
            when(integrationsDao.updateKsId("456", "normalized_456")).thenReturn(1);
            when(integrationsDao.updateKsId("789", "normalized_789")).thenReturn(1);

            // Call the method to test
            int affectedRows = integrationsService.migrate();

            // assertions
            assertEquals(3, affectedRows);
        } catch (IOException e) {
            e.printStackTrace();
            fail("IOException was thrown unexpectedly");
        }
    }

    @Test
    public void testMigrate_FetchAllEmpty() {
        // Set up
        when(integrationsDao.fetchAll()).thenReturn(Collections.emptyList());

        // Call the method to test
        int affectedRows = integrationsService.migrate();

        // assertions
        assertEquals(0, affectedRows);
    }

    @Test
    public void testMigrate_UpdateKsIdZeroAffectedRows() {
        try {
            // set up
            when(integrationsDao.updateKsId(anyString(), anyString())).thenReturn(0);
            when(normalizerClient.normalize(anyString())).thenReturn("normalized_value");

            // Call the method to test
            int affectedRows = integrationsService.migrate();

            // assertions
            assertEquals(0, affectedRows);
        } catch (IOException e) {
            e.printStackTrace();
            fail("IOException was thrown unexpectedly");
        }
    }
}
