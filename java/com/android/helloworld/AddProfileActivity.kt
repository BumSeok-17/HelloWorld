package com.android.helloworld

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.OnProgressListener
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_add_profile.*


class AddProfileActivity : AppCompatActivity() {
    var profileset: Uri? = null
    var profile = Profile()
    var profilerecieved = Profile()
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    var usermail: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_profile)
        init()
    }

    fun init() {
        initPermission()
        if (intent.hasExtra("loginprofile")) {
            profilerecieved = intent.getSerializableExtra("loginprofile") as Profile
            usermail = profilerecieved.email.replace('.', '_')
            initUserInfo()
        }

        val storage = FirebaseStorage.getInstance()
        val storageReference = FirebaseStorage.getInstance().reference
        var bmpdf = BitmapFactory.decodeResource(resources, R.drawable.defaultpic)
        //profileimage.setImageBitmap(bmpdf)
        val islandRef = storage.reference.child(profilerecieved.email)
        Log.i("em",profilerecieved.email)
        val ONE_MEGABYTE: Long = 1024 * 1024
        islandRef.getBytes(ONE_MEGABYTE).addOnSuccessListener {
            var bmp = BitmapFactory.decodeByteArray(it, 0, it.size)
            profileimage.setImageBitmap(bmp)
            Log.i("em",profilerecieved.email)
        }.addOnFailureListener {
            Log.e("이미지불러오기","error")
        }
        profileimage.scaleType = ImageView.ScaleType.FIT_XY

        clickListener()
    }

    fun showDialog() {
        // Late initialize an alert dialog object
        lateinit var dialog: AlertDialog

        // Initialize a new instance of alert dialog builder object
        val builder = AlertDialog.Builder(this)

        // Set a title for alert dialog
        builder.setTitle("본인 정보 저장")

        // Set a message for alert dialog
        builder.setMessage("저장하시겠습니까?")


        // On click listener for dialog buttons
        val dialogClickListener = DialogInterface.OnClickListener { _, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    save()
                    dbSave()
                    val intent = Intent(this, HostsViewActivity::class.java)
                    intent.putExtra("loginprofile", profile)
                    startActivity(intent)
                }
                DialogInterface.BUTTON_NEGATIVE -> {
                    val intent = Intent(this, HostsViewActivity::class.java)
                    startActivity(intent)
                }
                DialogInterface.BUTTON_NEUTRAL -> {

                }
                //다시 돌아감
            }
        }


        // Set the alert dialog positive/yes button
        builder.setPositiveButton("YES", dialogClickListener)

        // Set the alert dialog negative/no button
        builder.setNegativeButton("NO", dialogClickListener)

        // Set the alert dialog neutral/cancel button
        builder.setNeutralButton("CANCEL", dialogClickListener)

        // Initialize the AlertDialog using builder object
        dialog = builder.create()

        // Finally, display the alert dialog
        dialog.show()
    }

    fun initPermission() {
        if (!checkAppPermission(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))) {
            val builder = AlertDialog.Builder(this)
            builder.setMessage("반드시 이미지 데이터에 대한 권한이 허용되어야 합니다.")
                .setTitle("권한 허용")
                .setIcon(R.drawable.abc_ic_star_black_48dp)
            builder.setPositiveButton("OK") { _, _ ->
                askPermission(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 100);
            }
            val dialog = builder.create()
            dialog.show()
        } else {
            Toast.makeText(
                getApplicationContext(),
                "권한이 승인되었습니다.", Toast.LENGTH_SHORT
            ).show();
        }
    }

    fun askPermission(requestPermission: Array<String>, REQ_PERMISSION: Int) {
        ActivityCompat.requestPermissions(
            this, requestPermission,
            REQ_PERMISSION
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            100 -> if (checkAppPermission(permissions)) { //퍼미션 동의했을 때 할 일
                Toast.makeText(this, "권한이 승인됨", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "권한이 거절됨", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100) {
            if (resultCode == Activity.RESULT_OK) {

                val storage = FirebaseStorage.getInstance()
                val storageReference = FirebaseStorage.getInstance().reference

                val filePath = data?.data
                if (filePath != null) {
                    val progressDialog = ProgressDialog(this)
                    progressDialog.setTitle("Uploading...")
                    progressDialog.show()

                    val ref = storageReference.child(profilerecieved.email)
                    ref.putFile(filePath)
                        .addOnSuccessListener(OnSuccessListener<UploadTask.TaskSnapshot> {
                            progressDialog.dismiss()
                            Log.i("ab", data.data.toString())
                        })
                        .addOnFailureListener(OnFailureListener { e ->
                            progressDialog.dismiss()
                        })
                        .addOnProgressListener(OnProgressListener<UploadTask.TaskSnapshot> { taskSnapshot ->
                            val progress = 100.0 * taskSnapshot.bytesTransferred / taskSnapshot
                                .totalByteCount
                        })
                }
                profileimage.scaleType = ImageView.ScaleType.FIT_XY
            }
        }
    }


    fun checkAppPermission(requestPermission: Array<String>): Boolean {
        val requestResult = BooleanArray(requestPermission.size)
        for (i in requestResult.indices) {
            requestResult[i] = ContextCompat.checkSelfPermission(
                this,
                requestPermission[i]
            ) == PackageManager.PERMISSION_GRANTED
            if (!requestResult[i]) {
                return false
            }
        }
        return true
    }

    fun clickListener() {
        saveicon.setOnClickListener {
            showDialog()
        }
        photolib.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = android.provider.MediaStore.Images.Media.CONTENT_TYPE
            intent.data = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            startActivityForResult(intent, 100)
        }
        nohost.setOnClickListener {
            nohost.setTextColor(Color.parseColor("#ff33b5e5"))
            maybehost.setTextColor(Color.parseColor("#ffaaaaaa"))
            host.setTextColor(Color.parseColor("#ffaaaaaa"))
            profile.hostPreference = 0
        }
        maybehost.setOnClickListener {
            nohost.setTextColor(Color.parseColor("#ffaaaaaa"))
            maybehost.setTextColor(Color.parseColor("#ff33b5e5"))
            host.setTextColor(Color.parseColor("#ffaaaaaa"))
            profile.hostPreference = 1
        }
        host.setOnClickListener {
            nohost.setTextColor(Color.parseColor("#ffaaaaaa"))
            maybehost.setTextColor(Color.parseColor("#ffaaaaaa"))
            host.setTextColor(Color.parseColor("#ff33b5e5"))
            profile.hostPreference = 2
        }
        prefermale.setOnClickListener {
            prefermale.setTextColor(Color.parseColor("#ff33b5e5"))
            preferany.setTextColor(Color.parseColor("#ffaaaaaa"))
            preferfemale.setTextColor(Color.parseColor("#ffaaaaaa"))
            profile.preferGender = 0
        }
        preferfemale.setOnClickListener {
            prefermale.setTextColor(Color.parseColor("#ffaaaaaa"))
            preferany.setTextColor(Color.parseColor("#ffaaaaaa"))
            preferfemale.setTextColor(Color.parseColor("#ff33b5e5"))
            profile.preferGender = 1
        }
        preferany.setOnClickListener {
            prefermale.setTextColor(Color.parseColor("#ffaaaaaa"))
            preferany.setTextColor(Color.parseColor("#ff33b5e5"))
            preferfemale.setTextColor(Color.parseColor("#ffaaaaaa"))
            profile.preferGender = 2
        }
        yes1.setOnClickListener {
            yes1.setTextColor(Color.parseColor("#ff33b5e5"))
            no1.setTextColor(Color.parseColor("#ffaaaaaa"))
            profile.kidPreference = true
        }
        yes2.setOnClickListener {
            yes2.setTextColor(Color.parseColor("#ff33b5e5"))
            no2.setTextColor(Color.parseColor("#ffaaaaaa"))
            profile.animalPreference = true
        }
        yes3.setOnClickListener {
            yes3.setTextColor(Color.parseColor("#ff33b5e5"))
            no3.setTextColor(Color.parseColor("#ffaaaaaa"))
            profile.wheelchairPreference = true
        }
        yes4.setOnClickListener {
            yes4.setTextColor(Color.parseColor("#ff33b5e5"))
            no4.setTextColor(Color.parseColor("#ffaaaaaa"))
            profile.smokingPreference = true
        }
        yes5.setOnClickListener {
            yes5.setTextColor(Color.parseColor("#ff33b5e5"))
            no5.setTextColor(Color.parseColor("#ffaaaaaa"))
            profile.kidExistence = true
        }
        yes6.setOnClickListener {
            yes6.setTextColor(Color.parseColor("#ff33b5e5"))
            no6.setTextColor(Color.parseColor("#ffaaaaaa"))
            profile.animalExistence = true
        }
        yes7.setOnClickListener {
            yes7.setTextColor(Color.parseColor("#ff33b5e5"))
            no7.setTextColor(Color.parseColor("#ffaaaaaa"))
            profile.smoking = true
        }
        no1.setOnClickListener {
            yes1.setTextColor(Color.parseColor("#ffaaaaaa"))
            no1.setTextColor(Color.parseColor("#ff33b5e5"))
            profile.kidPreference = false
        }
        no2.setOnClickListener {
            yes2.setTextColor(Color.parseColor("#ffaaaaaa"))
            no2.setTextColor(Color.parseColor("#ff33b5e5"))
            profile.animalPreference = false
        }
        no3.setOnClickListener {
            yes3.setTextColor(Color.parseColor("#ffaaaaaa"))
            no3.setTextColor(Color.parseColor("#ff33b5e5"))
            profile.wheelchairPreference = false
        }
        no4.setOnClickListener {
            yes4.setTextColor(Color.parseColor("#ffaaaaaa"))
            no4.setTextColor(Color.parseColor("#ff33b5e5"))
            profile.smokingPreference = false
        }
        no5.setOnClickListener {
            yes5.setTextColor(Color.parseColor("#ffaaaaaa"))
            no5.setTextColor(Color.parseColor("#ff33b5e5"))
            profile.kidExistence = false
        }
        no6.setOnClickListener {
            yes6.setTextColor(Color.parseColor("#ffaaaaaa"))
            no6.setTextColor(Color.parseColor("#ff33b5e5"))
            profile.animalExistence = false
        }
        no7.setOnClickListener {
            yes7.setTextColor(Color.parseColor("#ffaaaaaa"))
            no7.setTextColor(Color.parseColor("#ff33b5e5"))
            profile.smoking = false
        }
    }


    fun save() {
        profile.email = profilerecieved.email
        profile.phone = profilerecieved.phone
        profile.gender = profilerecieved.gender
        profile.reviewCount = profilerecieved.reviewCount
        profile.totalRating = profilerecieved.totalRating
        profile.reportCount = profilerecieved.reportCount
        if (profileset != null) {
            profile.profpic = profileset!!.toString()
        }
        profile.userName = ed0.text!!.toString()
        profile.aboutUser = ed1.text!!.toString()
        profile.interest = ed2.text!!.toString()
        profile.aboutInterest = ed3.text!!.toString()
        profile.purposeOfHW = ed4.text!!.toString()
        profile.musicBookMovie = ed5.text!!.toString()
        profile.impressiveExperience = ed6.text!!.toString()
        profile.offerabletoHost = ed7.text!!.toString()
        profile.countriesVisited = ed8.text!!.toString()
        profile.countriesLived = ed9.text!!.toString()
        profile.fluidLanguage = ed10.text!!.toString()
        profile.learningLanguage = ed11.text!!.toString()
        profile.occupation = ed12.text!!.toString()
        profile.education = ed13.text!!.toString()
        profile.countryBorn = ed14.text!!.toString()
        profile.resCity = aded1.text!!.toString()
        profile.resCountry = aded2.text!!.toString()
        profile.resPostcode = aded3.text!!.toString()
        profile.residence = aded4.text!!.toString()
        profile.bedOffer = ed15.text!!.toString()
        profile.cohabitMember = ed16.text!!.toString()
        profile.trafficStance = ed17.text!!.toString()
        profile.offerableToGuest = ed18.text!!.toString()
        profile.etc = ed19.text!!.toString()
        profile.acceptableGuest = spinner1.selectedItemPosition + 1
    }

    fun dbSave() {
        database = FirebaseDatabase.getInstance().reference
        database.child("profiles").child(usermail).child("profile").setValue(profile)
        Toast.makeText(this, "변경 사항 저장됨", Toast.LENGTH_SHORT).show()

    }

    fun initUserInfo() {
        profileimage.setImageURI(Uri.parse(profilerecieved.profpic))
        ed0.setText(profilerecieved.userName)
        ed1.setText(profilerecieved.aboutUser)
        ed2.setText(profilerecieved.interest)
        ed3.setText(profilerecieved.aboutInterest)
        ed4.setText(profilerecieved.purposeOfHW)
        ed5.setText(profilerecieved.musicBookMovie)
        ed6.setText(profilerecieved.impressiveExperience)
        ed7.setText(profilerecieved.offerabletoHost)
        ed8.setText(profilerecieved.countriesVisited)
        ed9.setText(profilerecieved.countriesLived)
        ed10.setText(profilerecieved.fluidLanguage)
        ed11.setText(profilerecieved.learningLanguage)
        ed12.setText(profilerecieved.occupation)
        ed13.setText(profilerecieved.education)
        ed14.setText(profilerecieved.countryBorn)
        aded1.setText(profilerecieved.resCity)
        aded2.setText(profilerecieved.resCountry)
        aded3.setText(profilerecieved.resPostcode)
        aded4.setText(profilerecieved.residence)
        ed15.setText(profilerecieved.bedOffer)
        ed16.setText(profilerecieved.cohabitMember)
        ed17.setText(profilerecieved.trafficStance)
        ed18.setText(profilerecieved.offerableToGuest)
        ed19.setText(profilerecieved.etc)
        ed0.setTextColor(Color.BLACK)
        ed1.setTextColor(Color.BLACK)
        ed2.setTextColor(Color.BLACK)
        ed3.setTextColor(Color.BLACK)
        ed4.setTextColor(Color.BLACK)
        ed5.setTextColor(Color.BLACK)
        ed6.setTextColor(Color.BLACK)
        ed7.setTextColor(Color.BLACK)
        ed8.setTextColor(Color.BLACK)
        ed9.setTextColor(Color.BLACK)
        ed10.setTextColor(Color.BLACK)
        ed11.setTextColor(Color.BLACK)
        ed12.setTextColor(Color.BLACK)
        ed13.setTextColor(Color.BLACK)
        ed14.setTextColor(Color.BLACK)
        ed15.setTextColor(Color.BLACK)
        ed16.setTextColor(Color.BLACK)
        ed17.setTextColor(Color.BLACK)
        ed18.setTextColor(Color.BLACK)
        ed19.setTextColor(Color.BLACK)
        aded1.setTextColor(Color.BLACK)
        aded2.setTextColor(Color.BLACK)
        aded3.setTextColor(Color.BLACK)
        aded4.setTextColor(Color.BLACK)
    }
}