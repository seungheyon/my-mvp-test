package com.team1.mvp_test.infra.s3.s3service

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.DeleteObjectRequest
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.util.IOUtils
import com.team1.mvp_test.common.error.S3ErrorMessage
import org.apache.tika.Tika
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayInputStream
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.*

@Service
@Primary
class S3ServicefakeImpl (
    private val amazonS3: AmazonS3,
    @Value("\${cloud.aws.s3.bucket}") val bucket: String,
    //@Value("\${cloud.aws.s3.baseurl}") val s3BaseUrl: String,
    //@Value("cloud.aws.s3.baseurl") val s3BaseUrl: String,
) : S3Service {

    private val allowedImageExtensions = arrayOf("png", "jpg", "jpeg")
    private val allowedDocumentExtensions = arrayOf("pdf")
    private val tika = Tika()

    override fun uploadMvpTestFile(file: MultipartFile): String {
        return "ok"
    }

    override fun uploadStepFile(file: MultipartFile): String {
        return "ok"
    }

    override fun uploadReportFile(file: MultipartFile): String {
        return "ok"
    }

    override fun deleteFile(fileUrl: String) {
    }

    override fun upload(file: MultipartFile, dir: String, allowedExtensions: Array<String>): String {
        return "ok"
    }

    override fun validateFileExtension(fileName: String, allowedExtensions: Array<String>): String {
        return "ok"
    }

    override fun getContentType(extension: String): String {
        return "ok"
    }

    override fun validateFileContent(byteArray: ByteArray, extension: String) {
    }
}

