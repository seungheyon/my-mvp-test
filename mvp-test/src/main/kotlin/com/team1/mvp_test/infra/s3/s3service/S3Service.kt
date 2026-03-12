package com.team1.mvp_test.infra.s3.s3service

import org.springframework.web.multipart.MultipartFile

interface S3Service {

    fun uploadMvpTestFile(file: MultipartFile): String
    fun uploadStepFile(file: MultipartFile): String
    fun uploadReportFile(file: MultipartFile): String
    fun deleteFile(fileUrl: String)
    fun upload(file: MultipartFile, dir: String, allowedExtensions: Array<String>): String
    fun validateFileExtension(fileName: String, allowedExtensions: Array<String>): String
    fun getContentType(extension: String): String
    fun validateFileContent(byteArray: ByteArray, extension: String)
}