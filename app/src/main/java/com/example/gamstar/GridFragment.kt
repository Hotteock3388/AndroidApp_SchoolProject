package com.example.gamstar

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.gamstar.dataclass.ContentDTO
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_grid.*
import kotlinx.android.synthetic.main.fragment_grid.view.*
import kotlinx.android.synthetic.main.item_detail.view.*

class GridFragment : Fragment() {


    var mainView: View? = null
    var imagesSnapshot  : ListenerRegistration? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mainView = inflater.inflate(R.layout.fragment_grid, container, false)


//        Glide.with(mainView)
//            .load(Uri.parse("gs://gamstar-d1100.appspot.com/images/JPEG_20200712_153728_.png"))
//            .apply(RequestOptions().centerCrop())
//            .into(mainView!!.gridImageView)
        return mainView
    }

    override fun onResume() {
        super.onResume()
        mainView?.gridFragment_recycleView?.adapter = GridFragmentRecyclerViewAdatper()
        mainView?.gridFragment_recycleView?.layoutManager = GridLayoutManager(activity, 3)
    }

    override fun onStop() {
        super.onStop()
        imagesSnapshot?.remove()
    }


    inner class GridFragmentRecyclerViewAdatper : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        var contentDTOs: ArrayList<ContentDTO>

        init {
            contentDTOs = ArrayList()
            imagesSnapshot = FirebaseFirestore
                .getInstance().collection("images").orderBy("timestamp")?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    contentDTOs.clear()
                    //if (querySnapshot == null) return@addSnapshotListener
                    for (snapshot in querySnapshot!!.documents) {
                        contentDTOs.add(snapshot.toObject(ContentDTO::class.java)!!)
                    }
                    notifyDataSetChanged()
                }
            readData()

        }

        fun readData() {
            FirebaseFirestore.getInstance().collection("images").document("YDX5FUoMbURvZJn5RMR7").get().addOnCompleteListener {
                task ->
                if(task.isSuccessful) {
                    Log.d("read", "ReadSuccess")
                    var userDTO = task.result?.toObject(ContentDTO::class.java)
                    //Log.d("read", userDTO?.explain)
                }

            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

            //현재 사이즈 뷰 화면 크기의 가로 크기의 1/3값을 가지고 오기
            val width = resources.displayMetrics.widthPixels / 3

            //val imageView = ImageView(parent.context)
            var imageView : ImageView = ImageView(parent.context)
            imageView.layoutParams = LinearLayoutCompat.LayoutParams(width, width)

            return CustomViewHolder(imageView)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {


            //var ref = FirebaseStorage.getInstance().getReference(contentDTOs[position].imageUrl.toString());


            //var ref = FirebaseStorage.getInstance().getReference(contentDTOs[position].imageUrl!!);

            var imageView = (holder as GridFragment.GridFragmentRecyclerViewAdatper.CustomViewHolder).imageView
            var ref = FirebaseStorage.getInstance().reference.child("images").child(contentDTOs[position].imageUrl.toString());
            ref.downloadUrl.addOnCompleteListener {
                    task ->
                Log.d("task", task.result.toString())
                if(task.isSuccessful){
                    Glide.with(holder.itemView.context)
//              .applyDefaultRequestOptions(RequestOptions().centerCrop())
                        //.load(contentDTOs[position].imageUrl)
                        .load(task.result)
                        .apply(RequestOptions().centerCrop())
                        .into(imageView)
                }
            }



           // imageView.setImageURI(Uri.parse(contentDTOs[position].imageUrl))
//            Picasso.get()
//                .load(contentDTOs[position].imageUrl)
//                .into(imageView)

            imageView.setOnClickListener {
                val fragment = UserFragment()
                val bundle = Bundle()

                bundle.putString("destinationUid", contentDTOs[position].uid)
                bundle.putString("userId", contentDTOs[position].userId)

                fragment.arguments = bundle
                activity!!.supportFragmentManager.beginTransaction()
                    .replace(R.id.main_content, fragment)
                    .commit()
            }
        }

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

        inner class CustomViewHolder(var imageView: ImageView) : RecyclerView.ViewHolder(imageView)
    }

}