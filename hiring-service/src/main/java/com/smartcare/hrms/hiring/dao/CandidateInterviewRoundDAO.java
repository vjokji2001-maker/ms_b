package com.smartcare.hrms.hiring.dao;

import com.smartcare.hrms.hiring.model.entity.CandidateInterviewRound;

import jakarta.persistence.*;
import org.springframework.stereotype.Component;


import java.util.List;

@Component
public class CandidateInterviewRoundDAO {

    @PersistenceContext
    private EntityManager entityManager;

    public CandidateInterviewRound save(
            CandidateInterviewRound interviewRound) {

        entityManager.persist(interviewRound);

        return interviewRound;
    }

    public CandidateInterviewRound update(
            CandidateInterviewRound interviewRound) {

        return entityManager.merge(interviewRound);
    }

    public CandidateInterviewRound getById(Long id) {

        return entityManager.find(
                CandidateInterviewRound.class,
                id
        );
    }

    public CandidateInterviewRound getByCandidateAndRound(
            Long candidateApplicationId,
            Integer roundNumber) {

        String jpql =
                "SELECT i " +
                        "FROM CandidateInterviewRound i " +
                        "WHERE i.candidateApplicationId = :candidateApplicationId " +
                        "AND i.roundNumber = :roundNumber";

        List<CandidateInterviewRound> result =
                entityManager
                        .createQuery(
                                jpql,
                                CandidateInterviewRound.class
                        )
                        .setParameter(
                                "candidateApplicationId",
                                candidateApplicationId
                        )
                        .setParameter(
                                "roundNumber",
                                roundNumber
                        )
                        .getResultList();

        return result.isEmpty()
                ? null
                : result.get(0);
    }

    public CandidateInterviewRound getLatestRound(
            Long candidateApplicationId) {

        String jpql =
                "SELECT i " +
                        "FROM CandidateInterviewRound i " +
                        "WHERE i.candidateApplicationId = :candidateApplicationId " +
                        "ORDER BY i.roundNumber DESC";

        List<CandidateInterviewRound> result =
                entityManager
                        .createQuery(
                                jpql,
                                CandidateInterviewRound.class
                        )
                        .setParameter(
                                "candidateApplicationId",
                                candidateApplicationId
                        )
                        .setMaxResults(1)
                        .getResultList();

        return result.isEmpty()
                ? null
                : result.get(0);
    }

    public List<CandidateInterviewRound> getAllByCandidate(
            Long candidateApplicationId) {

        String jpql =
                "SELECT i " +
                        "FROM CandidateInterviewRound i " +
                        "WHERE i.candidateApplicationId = :candidateApplicationId " +
                        "ORDER BY i.roundNumber ASC";

        return entityManager
                .createQuery(
                        jpql,
                        CandidateInterviewRound.class
                )
                .setParameter(
                        "candidateApplicationId",
                        candidateApplicationId
                )
                .getResultList();
    }

    public void delete(
            CandidateInterviewRound interviewRound) {

        entityManager.remove(
                entityManager.contains(interviewRound)
                        ? interviewRound
                        : entityManager.merge(interviewRound)
        );
    }
}