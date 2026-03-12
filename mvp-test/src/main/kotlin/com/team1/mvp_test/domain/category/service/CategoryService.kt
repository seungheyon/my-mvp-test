package com.team1.mvp_test.domain.category.service

import com.team1.mvp_test.domain.category.repository.CategoryRepository
import org.springframework.stereotype.Service

@Service
class CategoryService(
    private val categoryRepository: CategoryRepository
) {
    fun getCategory(): List<String> {
        return categoryRepository.findAll().map { it.name }
    }
}