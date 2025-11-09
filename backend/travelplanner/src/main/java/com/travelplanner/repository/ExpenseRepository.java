package com.travelplanner.repository;

import com.travelplanner.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByTravelPlanId(Long planId);
    List<Expense> findByTravelPlanIdAndCategory(Long planId, String category);
}