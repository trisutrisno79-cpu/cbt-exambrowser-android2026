package id.sch.cbtsmp.exambrowser

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var etServerUrl: EditText

    private val qrLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val scannedUrl = result.data?.getStringExtra("QR_RESULT") ?: ""
            if (scannedUrl.startsWith("http://") || scannedUrl.startsWith("https://")) {
                simpanDanBukaUjian(scannedUrl)
            } else {
                Toast.makeText(this, "QR Code tidak valid!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val cameraPermLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            qrLauncher.launch(Intent(this, QrScannerActivity::class.java))
        } else {
            Toast.makeText(this, "Izin kamera diperlukan untuk scan QR", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etServerUrl = findViewById(R.id.etServerUrl)
        val btnScanQr = findViewById<Button>(R.id.btnScanQr)
        val btnMasukUjian = findViewById<Button>(R.id.btnMasukUjian)

        val prefs = getSharedPreferences("CBT_PREFS", MODE_PRIVATE)
        etServerUrl.setText(prefs.getString("LAST_URL", "http://192.168.1.10/cbt_smp"))

        btnScanQr.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                qrLauncher.launch(Intent(this, QrScannerActivity::class.java))
            } else {
                cameraPermLauncher.launch(Manifest.permission.CAMERA)
            }
        }

        btnMasukUjian.setOnClickListener {
            var url = etServerUrl.text.toString().trim()
            if (url.isEmpty()) {
                Toast.makeText(this, "Masukkan alamat server CBT!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "http://$url"
            }
            simpanDanBukaUjian(url)
        }
    }

    private fun simpanDanBukaUjian(url: String) {
        val prefs = getSharedPreferences("CBT_PREFS", MODE_PRIVATE)
        prefs.edit().putString("LAST_URL", url).apply()

        val intent = Intent(this, ExamWebActivity::class.java)
        intent.putExtra("EXAM_URL", url)
        startActivity(intent)
    }
}
