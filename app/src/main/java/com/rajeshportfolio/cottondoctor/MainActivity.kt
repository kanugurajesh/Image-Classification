package com.rajeshportfolio.cottondoctor
import android.content.Intent
import android.graphics.Bitmap
import android.media.Image
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.TextView
import android.widget.ImageView
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import com.rajeshportfolio.cottondoctor.ml.Classification
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.text.Normalizer

class MainActivity : AppCompatActivity() {

//    Declaring variables
    lateinit var TakePhoto: Button
    lateinit var ChoosePhoto: Button
    lateinit var Result: TextView
    lateinit var Image: ImageView
    lateinit var Predict: Button
    lateinit var bitmap: Bitmap

//    override on create method
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        TakePhoto = findViewById(R.id.button4)
        ChoosePhoto = findViewById(R.id.button5)
        Result = findViewById(R.id.textView2)
        Image = findViewById(R.id.imageView7)
        Predict = findViewById(R.id.button)

        var labels = application.assets.open("labels.txt").bufferedReader().readLines()

        // image processor
        var imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR))
            .build()

        TakePhoto.setOnClickListener {
            var intent: Intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, 2)
        }

        ChoosePhoto.setOnClickListener {
            var intent: Intent = Intent()
            intent.setAction(Intent.ACTION_GET_CONTENT)
            intent.setType("image/*")

//            handle null point
            startActivityForResult(Intent.createChooser(intent, "Select an Image"), 1)
        }

        Predict.setOnClickListener {
//            check whether image is selected or not
            if(!::bitmap.isInitialized){
                Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            var tensorImage = TensorImage(DataType.UINT8)
            tensorImage.load(bitmap)

            tensorImage = imageProcessor.process(tensorImage)

            val model = Classification.newInstance(this)

            val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.UINT8)
            inputFeature0.loadBuffer(tensorImage.buffer)

        // Runs model inference and gets result.
            val outputs = model.process(inputFeature0)
            val outputFeature0 = outputs.outputFeature0AsTensorBuffer.floatArray

            var maxIdx = 0
            outputFeature0.forEachIndexed { index, fl ->
                if (outputFeature0[maxIdx] < fl) {
                    maxIdx = index
                }
            }
            Result.setText(labels[maxIdx])
        // Releases model resources if no longer used.
            model.close()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 1){
            var uri = data?.data;
            bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
            Image.setImageBitmap(bitmap)
        }else if (requestCode == 2){
            bitmap = data?.extras?.get("data") as Bitmap
            Image.setImageBitmap(bitmap)
        }
    }
}