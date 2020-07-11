package com.example.gamstar

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.gamstar.dataclass.ContentDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_add_photo.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class AddPhotoActivity : AppCompatActivity() {

    val PICK_IMAGE_FROM_ALBUM = 0

    var photoUri: Uri? = null

    var storage: FirebaseStorage? = null
    var firestore: FirebaseFirestore? = null
    private var auth: FirebaseAuth? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_photo)

        //storage = FirebaseStorage.getInstance()
        storage = FirebaseStorage.getInstance()

        firestore = FirebaseFirestore.getInstance()

        auth = FirebaseAuth.getInstance()

        val photoPickerIntent = Intent(Intent.ACTION_PICK)
        photoPickerIntent.type = "image/*"
        startActivityForResult(photoPickerIntent, PICK_IMAGE_FROM_ALBUM)

        addphoto_image.setOnClickListener {
            val photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            startActivityForResult(photoPickerIntent, PICK_IMAGE_FROM_ALBUM)
        }

        addphoto_btn_upload.setOnClickListener {
            contentUpload()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


        if (requestCode == PICK_IMAGE_FROM_ALBUM) {
            //이미지 선택시
            if (resultCode == Activity.RESULT_OK) {
                //이미지뷰에 이미지 세팅
                //println(data?.data)
                photoUri = data?.data
                addphoto_image.setImageURI(data?.data)
            } else {
                finish()
            }

        }
    }


    fun contentUpload() {
        progress_bar.visibility = View.VISIBLE
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_.png"
        var storageRef = storage?.reference?.child("images")?.child(imageFileName)
        //var file : Uri = photoUri!!

        var uploadTask = storageRef?.putFile(photoUri!!)
        uploadTask?.addOnFailureListener {
            progress_bar.visibility = View.GONE

            Toast.makeText(
                this, getString(R.string.upload_success),
                Toast.LENGTH_SHORT
            ).show()

        }?.addOnSuccessListener {
            progress_bar.visibility = View.GONE

            Toast.makeText(
                this, getString(R.string.upload_success),
                Toast.LENGTH_SHORT
            ).show()

            var uri = storageRef?.downloadUrl.toString()
            //디비에 바인딩 할 위치 생성 및 컬렉션(테이블)에 데이터 집합 생성


            //시간 생성
            val contentDTO = ContentDTO()

            //이미지 주소
            contentDTO.imageUrl = uri
            //유저의 UID
            contentDTO.uid = auth?.currentUser?.uid
            //게시물의 설명
            contentDTO.explain = addphoto_edit_explain.text.toString()
            //유저의 아이디
            contentDTO.userId = auth?.currentUser?.email
            //게시물 업로드 시간
            contentDTO.timestamp = System.currentTimeMillis()

            //게시물을 데이터를 생성 및 엑티비티 종료
            firestore?.collection("images")?.document()?.set(contentDTO)

            setResult(Activity.RESULT_OK)
            finish()
        }

//        var storageRef = storage?.reference?.child("images")?.child(imageFileName)


    }
}
