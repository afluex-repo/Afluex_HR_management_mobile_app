package afluex.hrm.afluexhr.Fragment;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.JsonObject;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

import afluex.hrm.afluexhr.Activity.DashboardActivity;
import afluex.hrm.afluexhr.Model.CommonResponse;
import afluex.hrm.afluexhr.Model.ResponseLeaveApllication;
import afluex.hrm.afluexhr.Model.ResponseProfile;
import afluex.hrm.afluexhr.R;
import afluex.hrm.afluexhr.Retrofit.ApiServices;
import afluex.hrm.afluexhr.Retrofit.ServiceGenerator;
import afluex.hrm.afluexhr.common.LoggerUtil;
import afluex.hrm.afluexhr.databinding.ContentProfileBinding;
import afluex.hrm.afluexhr.databinding.FragmentProfileBinding;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ProfileFragment extends Fragment {
    FragmentProfileBinding binding;
    ContentProfileBinding contentProfileBinding;

    ApiServices apiServices;


    Uri image_uri;
    ImageView iv_logo;
    Bitmap ProfilePhoto=null;


    private static final int Image_pick_gallery_code = 400;
    private static final int Image_pick_camera_code = 500;

    private static final int Camera_Request_code = 200;
    private static final int Storage_Request_code = 300;


    private String[] locationPermissions;
    private String[] cameraPermissions;
    private String[] storagePermissions;

    String Url="";



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding=FragmentProfileBinding.inflate(inflater,container,false);

        contentProfileBinding=binding.llContent;
        contentProfileBinding.txtLoginId.setText(getActivity().getSharedPreferences("LoginDetails", Context.MODE_PRIVATE).getString("LoginID",""));
        apiServices = ServiceGenerator.createService(ApiServices.class);

        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};


        getProfile();





        contentProfileBinding.etName.setEnabled(false);

        contentProfileBinding.etFatherName.setEnabled(false);


        contentProfileBinding.txtMobile.setEnabled(false);


        contentProfileBinding.etEmail.setEnabled(false);


        contentProfileBinding.etDob.setEnabled(false);


        contentProfileBinding.etAddress.setEnabled(false);

        contentProfileBinding.etCity.setEnabled(false);

        contentProfileBinding.etPinCode.setEnabled(false);

        contentProfileBinding.etState.setEnabled(false);

        contentProfileBinding.etPan.setEnabled(false);


        contentProfileBinding.etAcc.setEnabled(false);


        contentProfileBinding.etBank.setEnabled(false);


        contentProfileBinding.etBranch.setEnabled(false);


        contentProfileBinding.etIfsc.setEnabled(false);


        contentProfileBinding.icAdd.setVisibility(View.VISIBLE);
        contentProfileBinding.icUpdate.setVisibility(View.GONE);
        binding.imgAdd.setVisibility(View.GONE);
        
        contentProfileBinding.icAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contentProfileBinding.icAdd.setVisibility(View.GONE);
                contentProfileBinding.icUpdate.setVisibility(View.VISIBLE);


                contentProfileBinding.etName.setEnabled(true);

                contentProfileBinding.etFatherName.setEnabled(true);


                contentProfileBinding.txtMobile.setEnabled(true);


                contentProfileBinding.etEmail.setEnabled(true);

                contentProfileBinding.etDob.setEnabled(true);

                contentProfileBinding.etAddress.setEnabled(true);

                contentProfileBinding.etCity.setEnabled(true);


                contentProfileBinding.etPinCode.setEnabled(true);


                contentProfileBinding.etState.setEnabled(true);


                contentProfileBinding.etPan.setEnabled(true);


                contentProfileBinding.etAcc.setEnabled(true);
                contentProfileBinding.etBank.setEnabled(true);


                contentProfileBinding.etBranch.setEnabled(true);


                contentProfileBinding.etIfsc.setEnabled(true);
                binding.imgAdd.setVisibility(View.VISIBLE);
            }
        });

        contentProfileBinding.icUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfile();
            }
        });
        binding.imgAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                permissionCheck();
            }
        });



        // Inflate the layout for this fragment
        return binding.getRoot();
    }
    private void permissionCheck() {
        Dexter.withActivity(getActivity())
                .withPermissions(android.Manifest.permission.CAMERA,
                        Manifest.permission.READ_MEDIA_IMAGES )
                .withListener(new MultiplePermissionsListener() {


                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
//                        Log.e("Denied",""+report.getDeniedPermissionResponses().get(0).getPermissionName());
                        if (report.areAllPermissionsGranted()) {
                            Log.e("Camera123","Permission Granted");
                            showImagePickdialog();
                        }

                        if (report.isAnyPermissionPermanentlyDenied()) {
                            showSettingsDialog();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    private void showSettingsDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity());
        builder.setTitle("Grant Permission");
        builder.setMessage("This App Requires permission");
        builder.setPositiveButton("Goto settings", (dialog, which) -> {
            dialog.cancel();
            openSettings();
        });
        builder.setNegativeButton(getString(android.R.string.cancel), (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
        intent.setData(uri);
        getActivity().startActivityForResult(intent, 101);
    }


    private void showImagePickdialog() {
        String[] options={"Camera","Gallery"};
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        builder.setTitle("Pick Image")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which==0){

                                pick_from_camera();


                        }
                        else{
                            Log.e("CSTPER",""+check_storage_permission());

                                pick_from_gallery();



                        }
                    }
                }).show();
    }

    private void pick_from_gallery(){
        Intent intent=new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,Image_pick_gallery_code);
    }

    private void pick_from_camera(){
        /*ContentValues contentValues=new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE,"Temp_Image title");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION,"Temp_ Image Description");
        image_uri=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);
        Intent intent=new Intent (MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);*/
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent,Image_pick_camera_code);
    }

    private boolean check_storage_permission(){
        boolean result= ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermission(){
        ActivityCompat.requestPermissions(getActivity(),storagePermissions,Storage_Request_code);

    }


    private boolean check_camera_permission() {
        boolean result= ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.CAMERA)==(PackageManager.PERMISSION_GRANTED);
        boolean result1=ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }


    private void updateProfile() {
        JsonObject object = new JsonObject();


        object.addProperty("EmployeeID",getActivity().getSharedPreferences("LoginDetails",Context.MODE_PRIVATE).getString("EmployeeId","") );
        object.addProperty("EmployeeName",contentProfileBinding.etName.getText().toString() );
        object.addProperty("DOB",contentProfileBinding.etDob.getText().toString() );
        object.addProperty("Gender",contentProfileBinding.etGender.getText().toString() );
        object.addProperty("MobileNo",contentProfileBinding.txtMobile.getText().toString() );
        object.addProperty("PerAddress",contentProfileBinding.etAddress.getText().toString() );
        object.addProperty("PerState",contentProfileBinding.etState.getText().toString() );
        object.addProperty("PerPinCode",contentProfileBinding.etPinCode.getText().toString() );
        object.addProperty("PerCity",contentProfileBinding.etCity.getText().toString() );
        object.addProperty("Email",contentProfileBinding.etEmail.getText().toString() );
        object.addProperty("LocAddress",contentProfileBinding.etAddress.getText().toString() );
        object.addProperty("DateOfJoining",contentProfileBinding.etDob.getText().toString() );
        object.addProperty("Pan",contentProfileBinding.etPan.getText().toString() );
        object.addProperty("BankAccountNo",contentProfileBinding.etAcc.getText().toString() );
        object.addProperty("BankName",contentProfileBinding.etBank.getText().toString() );
        object.addProperty("BankBranchName",contentProfileBinding.etBranch.getText().toString() );
        object.addProperty("IFSCCOde",contentProfileBinding.etIfsc.getText().toString() );
        object.addProperty("PostedFile",Url );
//        callMultipart();


        object.addProperty("LeaveID","" );
        LoggerUtil.logItem(object);
        Call<CommonResponse> call = apiServices.UpdateEmployeeProfile(object);

        call.enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
                if(response.isSuccessful()){
                    Toast.makeText(getActivity(), response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    contentProfileBinding.etName.setEnabled(false);

                    contentProfileBinding.etFatherName.setEnabled(false);


                    contentProfileBinding.txtMobile.setEnabled(false);


                    contentProfileBinding.etEmail.setEnabled(false);


                    contentProfileBinding.etDob.setEnabled(false);


                    contentProfileBinding.etAddress.setEnabled(false);

                    contentProfileBinding.etCity.setEnabled(false);

                    contentProfileBinding.etPinCode.setEnabled(false);

                    contentProfileBinding.etState.setEnabled(false);

                    contentProfileBinding.etPan.setEnabled(false);


                    contentProfileBinding.etAcc.setEnabled(false);


                    contentProfileBinding.etBank.setEnabled(false);


                    contentProfileBinding.etBranch.setEnabled(false);


                    contentProfileBinding.etIfsc.setEnabled(false);

                    binding.imgAdd.setVisibility(View.GONE);

                    contentProfileBinding.icUpdate.setVisibility(View.GONE);
                    contentProfileBinding.icAdd.setVisibility(View.VISIBLE);
                    getProfile();
                }
            }

            @Override
            public void onFailure(Call<CommonResponse> call, Throwable t) {

            }
        });
    }

    private void callMultipart() {

        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("text/plain");
        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("EmployeeID","2")
                .addFormDataPart("EmployeeName","Sarfraz Ahmad Khan")
                .addFormDataPart("DOB","20/07/2000")
                .addFormDataPart("Gender","Male")
                .addFormDataPart("MobileNo","8787000491")
                .addFormDataPart("PerAddress","Deoria")
                .addFormDataPart("PerState","Utter")
                .addFormDataPart("PerPinCode","274407")
                .addFormDataPart("PerCity","Tarkulwa")
                .addFormDataPart("Email","sarfraj@gmail.com")
                .addFormDataPart("LocAddress","Lucknow")
                .addFormDataPart("DateOfJoining","12/12/2023")
                .addFormDataPart("Pan","1234567")
                .addFormDataPart("BankAccountNo","121212121212")
                .addFormDataPart("BankName","SBI")
                .addFormDataPart("BankBranchName","TarKulwa")
                .addFormDataPart("IFSCCOde","SBIN00011201")
                .addFormDataPart("PostedFile","/C:/Users/DELL/Downloads/attendance_report.png",
                        RequestBody.create(MediaType.parse("application/octet-stream"),
                                new File("/C:/Users/DELL/Downloads/attendance_report.png")))
                .build();
        Request request = new Request.Builder()
                .url("http://demo4.afluex.com/WebAPI/UpdateEmployeeProfile")
                .method("POST", body)
                .build();


        try {
            okhttp3.Response response = client.newCall(request).execute();
            if(response.isSuccessful()==true){
                Toast.makeText(getActivity(), response.toString(), Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void requestCameraPermission(){
        ActivityCompat.requestPermissions(getActivity(),cameraPermissions,Camera_Request_code);

    }

    private void getProfile() {
        JsonObject object = new JsonObject();
        object.addProperty("PK_EmployeeID",getActivity().getSharedPreferences("LoginDetails",Context.MODE_PRIVATE).getString("EmployeeId","") );


        object.addProperty("LeaveID","" );
        LoggerUtil.logItem(object);
        Call<ResponseProfile> call = apiServices.EmployeeProfile(object);
        call.enqueue(new Callback<ResponseProfile>() {
            @Override
            public void onResponse(Call<ResponseProfile> call, Response<ResponseProfile> response) {
                if(response.isSuccessful()){
                    contentProfileBinding.etName.setText(response.body().getEmployeeName());
                    contentProfileBinding.etFatherName.setText(response.body().getFatherName());
                    contentProfileBinding.txtMobile.setText(response.body().getMobileNo());
                    contentProfileBinding.etEmail.setText(response.body().getEmail());
                    contentProfileBinding.etDob.setText(response.body().getDob());
                    contentProfileBinding.etAddress.setText(response.body().getAddress());
                    contentProfileBinding.etCity.setText(response.body().getCity());
                    contentProfileBinding.etPinCode.setText(response.body().getPinCode());
                    contentProfileBinding.etState.setText(response.body().getState());
                    contentProfileBinding.etPan.setText(response.body().getPanNo());
                    contentProfileBinding.etAcc.setText(response.body().getAccountNo());
                    contentProfileBinding.etBank.setText(response.body().getBankName());
                    contentProfileBinding.etBranch.setText(response.body().getBankBranch());
                    contentProfileBinding.etIfsc.setText(response.body().getIFSCCOde());
                    contentProfileBinding.etGender.setText(response.body().getGender());
                }
            }

            @Override
            public void onFailure(Call<ResponseProfile> call, Throwable t) {

            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (resultCode == RESULT_OK) {
            if (requestCode == Image_pick_gallery_code) {
                image_uri = data.getData();
                Bitmap bitmap=null;
                try {

                    bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), image_uri);
                    ProfilePhoto = bitmap;
                    binding.imgProfile.setImageBitmap(ProfilePhoto);
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                    byte[] data1 = bytes.toByteArray();
//                    uploadToStorage(data1);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //iv_logo.setImageURI(image_uri);


            } else if (requestCode == Image_pick_camera_code) {
                Log.e("uri", String.valueOf(data.getData()));
                //image_uri=data.getData();
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                ProfilePhoto = imageBitmap;
                binding.imgProfile.setImageBitmap(ProfilePhoto);

                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                byte[] data1 = bytes.toByteArray();
//                uploadToStorage(data1);

            }
           
            super.onActivityResult(requestCode, resultCode, data);
        }


    }

    private void uploadToStorage(byte[] data1) {
        String filePathAndName = "Profile/" +getActivity().getSharedPreferences("LoginDetails",Context.MODE_PRIVATE)
                .getString("LoginID","");

        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
        storageReference.putBytes(data1).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful()) ;

//                         refreshGallery(LoginPic.this,timestamp,image_uri);
                Uri downloadImageUri = uriTask.getResult();

                Url=String.valueOf(downloadImageUri);
                Log.e("StorageFirebase",Url);




            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Log.e("StorageFirebase",e.getMessage());

                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        ((DashboardActivity)getActivity()).setTitle( "Profile");
    }
}