package com.team1.mvp_test.common.exception

class ModelNotFoundException : RuntimeException {
    constructor(modelName: String) : super("Model with name $modelName not found")
    constructor(modelName: String, id: Long) : super("Model with name $modelName not found with id $id")
    constructor(modelName: String, name: String) : super("Model with name $modelName not found with $name")
}