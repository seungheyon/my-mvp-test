package com.team1.mvp_test.infra.s3.s3service

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.DeleteObjectRequest
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.util.IOUtils
import com.team1.mvp_test.common.error.S3ErrorMessage
import org.apache.tika.Tika
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayInputStream
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.*

@Service
class S3ServiceImpl (
    private val amazonS3: AmazonS3,
    @Value("\${cloud.aws.s3.bucket}") val bucket: String,
    @Value("\${cloud.aws.s3.baseurl}") val s3BaseUrl: String,
    //@Value("cloud.aws.s3.baseurl") val s3BaseUrl: String,
) : S3Service {

    private val allowedImageExtensions = arrayOf("png", "jpg", "jpeg")
    private val allowedDocumentExtensions = arrayOf("pdf")
    private val tika = Tika()

    override fun uploadMvpTestFile(file: MultipartFile): String {
        val dir = "mvpTestImage/"
        return upload(file, dir, allowedImageExtensions)
    }

    override fun uploadStepFile(file: MultipartFile): String {
        val dir = "stepDocument/"
        return upload(file, dir, allowedDocumentExtensions)
    }

    override fun uploadReportFile(file: MultipartFile): String {
        val dir = "reportDocument/"
        return upload(file, dir, allowedDocumentExtensions)
    }

    override fun deleteFile(fileUrl: String) {
        val baseUrl = s3BaseUrl
        val filePath = URLDecoder.decode(fileUrl.removePrefix(baseUrl), StandardCharsets.UTF_8.name())
        amazonS3.deleteObject(DeleteObjectRequest(bucket, filePath))
    }

    override fun upload(file: MultipartFile, dir: String, allowedExtensions: Array<String>): String {
        val extension = file.originalFilename?.let { validateFileExtension(it, allowedExtensions) }
            ?: throw IllegalArgumentException(S3ErrorMessage.FILE_TYPE_NOT_VALID.message)

        val bytes = IOUtils.toByteArray(file.inputStream)
        val byteArray = ByteArrayInputStream(bytes)
        validateFileContent(bytes, extension)


        val fileName = UUID.randomUUID().toString() + "-" + file.originalFilename!!
        val s3Key = "$dir$fileName"


        val objMeta = ObjectMetadata().apply {
            contentType = getContentType(extension)
            contentLength = bytes.size.toLong()
        }

        amazonS3.putObject(bucket, s3Key, byteArray, objMeta)

        return amazonS3.getUrl(bucket, s3Key).toString()
    }

    override fun validateFileExtension(fileName: String, allowedExtensions: Array<String>): String {
        val extensionIndex = fileName.lastIndexOf('.')
        if (extensionIndex == -1) throw IllegalArgumentException(S3ErrorMessage.FILE_TYPE_NOT_VALID.message)
        val extension = fileName.substring(extensionIndex + 1).lowercase(Locale.getDefault())
        if (!allowedExtensions.contains(extension)) throw IllegalArgumentException(S3ErrorMessage.FILE_TYPE_NOT_VALID.message)
        return extension
    }

    override fun getContentType(extension: String): String {
        return when (extension) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "pdf" -> "application/pdf"
            else -> "application/octet-stream"
        }
    }

    override fun validateFileContent(byteArray: ByteArray, extension: String) {
        val detectedType = tika.detect(byteArray)
        val expectedType = getContentType(extension)

        if (detectedType != expectedType) throw IllegalArgumentException(S3ErrorMessage.FILE_TYPE_NOT_VALID.message)
    }
}

