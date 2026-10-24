package com.smartcare.hrms.hiring.dao;

import com.smartcare.hrms.hiring.model.entity.CandidateApplication;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;

;
import java.util.List;
import java.util.UUID;

@Component
public class CandidateApplicationDAO {

    @PersistenceContext
    private EntityManager entityManager;

    public CandidateApplication save(
            CandidateApplication candidateApplication) {

        entityManager.persist(candidateApplication);

        return candidateApplication;
    }

    public CandidateApplication update(
            CandidateApplication candidateApplication) {

        return entityManager.merge(candidateApplication);
    }

    public CandidateApplication getById(Long id) {

        return entityManager.find(
                CandidateApplication.class,
                id
        );
    }

    public List<CandidateApplication> getAll() {

        String jpql =
                "SELECT c " +
                        "FROM CandidateApplication c " +
                        "ORDER BY c.createdAt DESC";

        return entityManager
                .createQuery(
                        jpql,
                        CandidateApplication.class
                )
                .getResultList();
    }

    public void delete(
            CandidateApplication candidateApplication) {

        entityManager.remove(
                entityManager.contains(candidateApplication)
                        ? candidateApplication
                        : entityManager.merge(candidateApplication)
        );
    }
}