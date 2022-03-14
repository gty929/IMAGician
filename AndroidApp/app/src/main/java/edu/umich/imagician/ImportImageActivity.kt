package edu.umich.imagician

import android.Manifest
import android.app.Activity
import android.content.ComponentName
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import edu.umich.imagician.utils.mediaStoreAlloc

import edu.umich.imagician.utils.toast


/**
 * Created by Tianyao Gu on 2022/3/6.
 */
class ImportImageActivity: AppCompatActivity()  {

    private lateinit var forCropResult: ActivityResultLauncher<Intent>
    private lateinit var forCameraResult: ActivityResultLauncher<Uri?>
    private lateinit var forPickedResult: ActivityResultLauncher<String>
    private var imageUri: Uri? = null
    private var isCreate = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_import_image)
        isCreate = intent.extras?.getBoolean("isCreate") ?: false
        updateExamineAndCreateButtonColor()

        /* prompt user for access permissions. We will be using one of Android’s standard-activity
         ActivityResultContracts to request permissions to access the camera, mic, and external
         storage.Since activities can be and are destroyed and re-created, for example everytime the
         screen orientation changes, the registration of activity result contracts must be done in
         an Activity’s onCreate(). This way, every time the Activity is re-created, the contract is
         re-registered.
         We create a “contract” that informs Android that a certain Activity will be started and the
         Activity will be expecting input of a certain type and will be returning output of other
         certain type. This ensures the type safety of starting an Activity for results. In this
         case, we specified that the Activity we want to start is to request multiple permissions,
         which is a standard Activity for which Android already provides a canned contract with
         baked-in input/output types.
         */
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
            /*
            to register it with the Android OS by calling registerForActivityResult(). As part of
            the registration process, we provided a callback to handle results from starting the
            Activity, in the form of a trailing lambda expression. The callback handler will examine
            the result of each permission request.
            If any of the permission is denied (it.value == false), for the sake of expediency, we
            will simply inform the user which permission (it.key) has been denied with a toast(),
            end PostActivity, and return to MainActivity. In a real app, you may want to be less
            draconian and let user continue to post text messages.
            */
            results.forEach {
                if (!it.value) {
                    toast("${it.key} access denied")
                    finish()
                }
            }
        }.launch(arrayOf(
            /* launch the registered contract to ask access permission to the camera, mic, and
            external storage */
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE))

        val cropIntent = initCropIntent()
        /* To pick an image or video from the device’s photo album or from Google Drive, we use the
        GetContent ActivityResultContract. To pick from on-device album, user must manually choose
        Photos from the menu. In registering a callback handler for this activity, instead of a
        lambda expression, we have used an anonymous function that takes a single argument of type
        nullable-Uri. If the uri is not null and contains the word “video”, we store it in videoUri
        and change the videoButton icon (to red). If it’s not a video uri, then it’s an image uri.
        To allow user to crop and zoom the image, we need to make a copy first, then we call
        doCrop() before posting.
         */
        forPickedResult =
            registerForActivityResult(ActivityResultContracts.GetContent(), fun(uri: Uri?) {
                uri?.let {
                    if (it.toString().contains("video")) {
                        return
                    } else {
//                        if (isCreate) {
//                            val inStream = contentResolver.openInputStream(it) ?: return
//                            imageUri = mediaStoreAlloc(contentResolver, "image/png")
//                            imageUri?.let {
//                                val outStream = contentResolver.openOutputStream(it) ?: return
//                                val buffer = ByteArray(8192)
//                                var read: Int
//                                while (inStream.read(buffer).also{ read = it } != -1) {
//                                    outStream.write(buffer, 0, read)
//                                }
//                                outStream.flush()
//                                outStream.close()
//                                inStream.close()
//                            }
//                            Log.d("imageUri", imageUri.toString())
//
//                            doCrop(cropIntent)
//                        } else {
                            // no need to crop
                            val intent = if (isCreate) Intent(this, InputInfoActivity::class.java) else Intent(this, ExamineActivity::class.java)
                            intent.putExtra("IMAGE_URI", it)
                            startActivity(intent)
//                        }

                    }
                } ?: run { Log.d("Pick media", "failed") }
            })

        /*
        When we call doCrop(), the user will be redirected to a separate cropping activity.
        We rely on third-party cropping capability published on device to perform the cropping
        function. To subscribe to this external capability, we first create an external activity
        Intent using initCropIntent().
         */
        forCropResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    result.data?.data.let {
                        imageUri?.run {
                            if (!toString().contains("ORIGINAL")) {
                                // delete uncropped photo taken for posting
                                contentResolver.delete(this, null, null)
                            }
                        }
                        imageUri = it
                    }
                    val intent = if (isCreate)
                        Intent(this, InputInfoActivity::class.java)
                    else Intent(this, ExamineActivity::class.java)

                    intent.putExtra("IMAGE_URI", imageUri)
                    startActivity(intent)
                } else {
                    Log.d("Crop", result.resultCode.toString())
                }
            }

        /*
        check whether the device has a camera
         */
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            toast("Device has no camera!")
            return
        }

        forCameraResult = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                doCrop(cropIntent)
            } else {
                Log.d("TakePicture", "failed")
            }
        }


    }




    /*
        When the cameraButton is clicked, first initialize viewState.imageUri with temporary
        storage space in the MediaStore for mediaType = "image/jpeg" and then launch the contract
        with viewState.imageUri as the launch argument.
        yyzjason: changed to png
         */

    fun onClickCamera(view: View?) {
            imageUri = mediaStoreAlloc(contentResolver,"image/png")
            forCameraResult.launch(imageUri)
    }
    fun onClickAlbum(view: View?) {
        forPickedResult.launch("*/*")
    }

    fun onClickCross(view: View?) {
        startActivity(Intent(this, MainActivity::class.java))
        overridePendingTransition(0, 0)
    }

    fun onClickCreate(view: View?) {
        if (LoginManager.isLoggedIn) {
            isCreate = true
            updateExamineAndCreateButtonColor()
        }
    }

    fun onClickExamine(view: View?) {
        isCreate = false
        updateExamineAndCreateButtonColor()
    }

    private fun updateExamineAndCreateButtonColor() {
        findViewById<ImageButton>(R.id.scanningCodeButton).alpha =  if (isCreate) 0.2F else 1F
        findViewById<ImageButton>(R.id.newWatermarkButton).alpha = if (isCreate) 1F else 0.2F
        findViewById<Button>(R.id.from_camera).isVisible = isCreate
    }
    /*
    This function first searches for availability of external on-device Activity capable of
    cropping. If such an Activity exists, it creates an explicit intent to redirect the user to the
    image cropper, pre-setting the intent to include our desired cropping features.
     */
    private fun initCropIntent(): Intent? {
        // Is there any published Activity on device to do image cropping?
        val intent = Intent("com.android.camera.action.CROP")
        intent.type = "image/*"
        val listofCroppers = packageManager.queryIntentActivities(intent, 0)
        // No image cropping Activity published
        if (listofCroppers.size == 0) {
            toast("Device does not support image cropping")
            return null
        }

        intent.component = ComponentName(
            listofCroppers[0].activityInfo.packageName,
            listofCroppers[0].activityInfo.name)

        // create a square crop box:
        intent.putExtra("outputX", 500)
            .putExtra("outputY", 500)
            .putExtra("aspectX", 1)
            .putExtra("aspectY", 1)
            // enable zoom and crop
            .putExtra("scale", true)
            .putExtra("crop", true)
            .putExtra("return-data", true)

        return intent
    }

    /*
    The function doCrop() uses forCropResult to launch the external activity. The variable
    forCropResult contains the registered contract to start a generic Activity for result.
     */
    private fun doCrop(intent: Intent?) {
        intent ?: run {
            return
        }

        imageUri?.let {
            intent.data = it
            forCropResult.launch(intent)
        }
    }



}