package com.team1.mvp_test.domain.category.controller

import com.team1.mvp_test.domain.category.service.CategoryService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/category")
class CategoryController(
    private val categoryService: CategoryService
) {

    @GetMapping
    fun getCategory(
    ): ResponseEntity<List<String>> {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(categoryService.getCategory())
    }
}