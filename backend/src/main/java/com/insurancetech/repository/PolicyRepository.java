package com.insurancetech.repository;

import com.insurancetech.model.Policy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PolicyRepository extends JpaRepository<Policy, Long> {
    
    Optional<Policy> findByPolicyNumber(String policyNumber);
    
    List<Policy> findByUserId(Long userId);
    
    List<Policy> findByUserIdAndIsActiveTrue(Long userId);
}