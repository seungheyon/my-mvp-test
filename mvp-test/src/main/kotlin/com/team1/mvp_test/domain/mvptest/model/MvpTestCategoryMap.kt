package com.team1.mvp_test.domain.mvptest.model

import com.team1.mvp_test.domain.category.model.Category
import jakarta.persistence.*

@Table(name = "test_category_map")
@Entity
class MvpTestCategoryMap(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne
    @JoinColumn(name = "test_id")
    val mvpTest: MvpTest,

    @ManyToOne
    @JoinColumn(name = "category_id")
    val category: Category,
) {

}