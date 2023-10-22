package com.kenshoo.integrations.service;

import com.kenshoo.integrations.entity.Integration;
import com.kenshoo.integrations.dao.IntegrationsDao;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class IntegrationsServiceImpl implements IntegrationsService {

    private final KsNormalizerClient normalizerClient;
    private final IntegrationsDao integrationsDao;

    public IntegrationsServiceImpl(KsNormalizerClient normalizerClient, IntegrationsDao integrationsDao) {
        this.normalizerClient = normalizerClient;
        this.integrationsDao = integrationsDao;
    }

    @Override
    public void insertIntegration(String ksId, String data) {
        if (ksId == null || ksId.isEmpty()) {
            throw new IllegalArgumentException("ksId cannot be null");
        }

        try {
            String normalizedKsId = normalizerClient.normalize(ksId);
            integrationsDao.insert(normalizedKsId, data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Integration> fetchIntegrationsByKsId(String ksId) {
        if (ksId == null || ksId.isEmpty()) {
            throw new IllegalArgumentException("ksId cannot be null");
        }
        try {
            String normalizedKsId = normalizerClient.normalize(ksId);
            return integrationsDao.fetchByKsId(normalizedKsId);
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList(); // Return an empty list
        }
    }

    @Override
    public int migrate() {
        List<Integration> integrations = integrationsDao.fetchAll();
        int affectedRows = 0;

        // add all the id's to the dataset and work without duplicates and null values
        Set<String> uniqueIds = integrations.stream()
                .map(Integration::getKsId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        for ( String id : uniqueIds  ) {
            try {
                    String normalizedId = normalizerClient.normalize(id);
                    //check if the id is already normalized or not
                    if (!id.equals(normalizedId)) {
                        //if the id is not equal to normalizedID (the rows are affected), calculate
                        affectedRows += integrationsDao.updateKsId(id, normalizedId);
                    }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return affectedRows;
    }
}
