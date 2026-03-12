package com.team1.mvp_test.batch.exception

class BatchJobAlreadyDoneException(jobName: String, parameter: String) :
    RuntimeException("Batch process already done with jobName : [$jobName], parameter: [$parameter]") {

}