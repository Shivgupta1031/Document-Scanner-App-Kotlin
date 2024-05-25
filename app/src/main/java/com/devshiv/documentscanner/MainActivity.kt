package com.devshiv.documentscanner

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.devshiv.documentscanner.databinding.ActivityMainBinding
import com.google.mlkit.vision.documentscanner.GmsDocumentScanner
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_JPEG
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_PDF
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.SCANNER_MODE_FULL
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private val TAG: String = "MyTag"
    var scanner: GmsDocumentScanner? = null
    var scannerLauncher: ActivityResultLauncher<IntentSenderRequest>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val options = GmsDocumentScannerOptions.Builder()
            .setGalleryImportAllowed(true)
            .setPageLimit(4)
            .setResultFormats(RESULT_FORMAT_JPEG, RESULT_FORMAT_PDF)
            .setScannerMode(SCANNER_MODE_FULL)
            .build()

        scanner = GmsDocumentScanning.getClient(options)
        scannerLauncher =
            registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val result = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
                    result?.getPages()?.let { pages ->
                        for (page in pages) {
                            val imageUri = pages.get(0).getImageUri()
                            Log.d(TAG, "scanDocument: $imageUri")
                        }
                    }
                    result?.getPdf()?.let { pdf ->
                        val pdfUri = pdf.getUri()
                        val pageCount = pdf.getPageCount()

                        binding.resultTxt.text =
                            "Page Uri = ${pdfUri.path} \nPageCount = $pageCount"
                    }
                } else {
                    Toast.makeText(this, "Failed To Fetch Result!", Toast.LENGTH_SHORT).show()
                }
            }

        binding.scanBtn.setOnClickListener {
            scanDocument()
        }
    }

    private fun scanDocument() {
        scanner?.getStartScanIntent(this@MainActivity)?.addOnSuccessListener { intentSender ->
            scannerLauncher?.launch(IntentSenderRequest.Builder(intentSender).build())
        }?.addOnFailureListener {
            Log.d(TAG, "scanDocument: ${it.message}")
            Toast.makeText(this, "Failed To Start Scanner!", Toast.LENGTH_SHORT).show()
        }
    }
}