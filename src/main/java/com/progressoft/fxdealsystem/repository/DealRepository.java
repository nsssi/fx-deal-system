package com.progressoft.fxdealsystem.repository;

import com.progressoft.fxdealsystem.model.Deal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DealRepository extends JpaRepository<Deal, Long> {

    // Vérifier si un deal existe déjà par son ID unique
    boolean existsByDealUniqueId(String dealUniqueId);

    // Trouver un deal par son ID unique
    Optional<Deal> findByDealUniqueId(String dealUniqueId);
}