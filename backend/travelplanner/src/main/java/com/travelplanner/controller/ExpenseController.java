package com.travelplanner.controller;

import com.travelplanner.entity.Expense;
import com.travelplanner.entity.TravelPlan;
import com.travelplanner.entity.User;
import com.travelplanner.repository.ExpenseRepository;
import com.travelplanner.service.TravelPlanService;
import com.travelplanner.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/expenses")
@CrossOrigin(origins = "*")
public class ExpenseController {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private TravelPlanService travelPlanService;

    @Autowired
    private UserService userService;

    @GetMapping("/plan/{planId}")
    public ResponseEntity<List<Expense>> getExpensesByPlan(@PathVariable Long planId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        TravelPlan plan = travelPlanService.getTravelPlan(planId);

        // 检查计划是否属于当前用户
        if (!plan.getUser().getId().equals(user.getId())) {
            return ResponseEntity.badRequest().build();
        }

        List<Expense> expenses = expenseRepository.findByTravelPlanId(planId);
        return ResponseEntity.ok(expenses);
    }

    @PostMapping
    public ResponseEntity<Expense> createExpense(@RequestBody Expense expense) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 验证旅行计划属于当前用户
        if (expense.getTravelPlan() != null && expense.getTravelPlan().getId() != null) {
            TravelPlan plan = travelPlanService.getTravelPlan(expense.getTravelPlan().getId());
            if (!plan.getUser().getId().equals(user.getId())) {
                return ResponseEntity.badRequest().build();
            }
        }

        Expense savedExpense = expenseRepository.save(expense);
        return ResponseEntity.ok(savedExpense);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Expense> updateExpense(@PathVariable Long id, @RequestBody Expense expenseDetails) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("费用记录不存在"));

        // 验证费用记录属于当前用户
        if (!expense.getTravelPlan().getUser().getId().equals(user.getId())) {
            return ResponseEntity.badRequest().build();
        }

        expense.setCategory(expenseDetails.getCategory());
        expense.setDescription(expenseDetails.getDescription());
        expense.setAmount(expenseDetails.getAmount());

        Expense updatedExpense = expenseRepository.save(expense);
        return ResponseEntity.ok(updatedExpense);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteExpense(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("费用记录不存在"));

        // 验证费用记录属于当前用户
        if (!expense.getTravelPlan().getUser().getId().equals(user.getId())) {
            return ResponseEntity.badRequest().build();
        }

        expenseRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/plan/{planId}/summary")
    public ResponseEntity<Map<String, Object>> getExpenseSummary(@PathVariable Long planId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        TravelPlan plan = travelPlanService.getTravelPlan(planId);

        // 检查计划是否属于当前用户
        if (!plan.getUser().getId().equals(user.getId())) {
            return ResponseEntity.badRequest().build();
        }

        List<Expense> expenses = expenseRepository.findByTravelPlanId(planId);

        double totalExpenses = expenses.stream().mapToDouble(Expense::getAmount).sum();
        double budgetRemaining = plan.getBudget() - totalExpenses;
        double budgetUsage = (totalExpenses / plan.getBudget()) * 100;

        Map<String, Object> summary = new HashMap<String, Object>(){{
            put("totalExpenses", totalExpenses);
            put("budgetRemaining", budgetRemaining);
            put("budgetUsage", budgetUsage);
            put("expenseCount", expenses.size());
        }};
//        Map.of(
//                "totalExpenses", totalExpenses,
//                "budgetRemaining", budgetRemaining,
//                "budgetUsage", budgetUsage,
//                "expenseCount", expenses.size()
//        );

        return ResponseEntity.ok(summary);
    }
}