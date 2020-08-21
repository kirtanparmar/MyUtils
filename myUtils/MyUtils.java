package com.iconflux.brokingbulls.myUtils;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.Settings;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.github.aakira.expandablelayout.ExpandableLinearLayout;
import com.google.android.flexbox.FlexboxLayout;
import com.google.gson.Gson;
import com.iconflux.brokingbulls.R;
import com.iconflux.brokingbulls.activity.createUpdateProject.fragments.OnResponseReceived;
import com.iconflux.brokingbulls.apis.APIObject;
import com.iconflux.brokingbulls.apis.RequestParameter;
import com.iconflux.brokingbulls.models.PropertyProjectPermission;
import com.iconflux.brokingbulls.models.propertyInfoModel.ImageAPIModel;
import com.iconflux.brokingbulls.models.raiseEnquiryModels.MyCity;
import com.iconflux.brokingbulls.models.raiseEnquiryModels.MyState;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.DOWNLOAD_SERVICE;

public final class MyUtils {

    public static final String API_CALL_FAIL = "FAILED_TO_ACCESS_API";
    public static final int MAX_BHK = 15;
    private static ProgressDialog progressDialog;

    public static void setSpnrFromAPINumberString(Spinner spnr, String text) {
        if (text == null || text.trim().equals("") || text.trim().equals("-")) return;
        int val;
        try {
            val = Integer.parseInt(text);
        } catch (Exception e) {
            return;
        }
        if (val < 0 || val > spnr.getCount()) return;
        spnr.setSelection(val);
    }

    public static void setToggleButtonFromAPIString(@NonNull ArrayList<ToggleButton> buttons, String text) {
        if (text == null || text.trim().equals("") || text.trim().equals("-")) {
            buttons.get(0).setChecked(true);
            return;
        }
        for (ToggleButton t : buttons) {
            if (Integer.parseInt(t.getTag().toString()) == Integer.parseInt(text)) {
                t.setChecked(true);
                break;
            }
        }
    }

    public synchronized static void setEDTTextFromAPIString(EditText edt, String text) {
        edt.setText(text == null || text.equalsIgnoreCase("-") ? "" : text);
    }

    public static void setImagesInFlexLayout(Context context, @NonNull FlexboxLayout layout, @NonNull ArrayList<ImageAPIModel> models) {
        layout.removeAllViews();
        for (ImageAPIModel model : models) {
            ImageView imageView = new ImageView(context);
            imageView.setLayoutParams(new LinearLayout.LayoutParams(getDisplayWidth20Percent(), getDisplayWidth20Percent()));
            Picasso.get().load(model.getImage()).placeholder(R.drawable.ic_place_holder_property).into(imageView);
            layout.addView(imageView);
        }
    }

    public static void setToggleWithExpandableLayout(final ToggleButton btn, final ExpandableLinearLayout layout, final Drawable btnIcon, final Drawable btnDropUpIcon, final Drawable btnDropDownIcon) {
        btn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) layout.expand();
                else layout.collapse();
                btn.setCompoundDrawablesRelativeWithIntrinsicBounds(btnIcon,
                        null,
                        isChecked ? btnDropUpIcon : btnDropDownIcon,
                        null);
            }
        });
    }

    public static void checkAndGetPermission(Context context, Activity activity) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.CAMERA},
                    1010);
        }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1012);
        }
    }

    public static boolean stringNotNullOrNotEmpty(String s) {
        if (s == null) return false;
        return !s.equals("");
    }

    public static void showProgress(Context context, String TITLE) {
        progressDialog = new ProgressDialog(context, R.style.AppTheme_LoadingDialogTheme);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Please wait...");
        progressDialog.setTitle(TITLE);
        progressDialog.show();
    }

    public static void dismissProgress() {
        if (progressDialog != null)
            progressDialog.dismiss();
    }

    public static void showCustomAlertDialog(@NonNull Context context, @Nullable String TITLE, @NonNull String MESSAGE, @Nullable String POSITIVE_TEXT, @NonNull DialogInterface.OnClickListener POSITIVE_CLICK_ACTION, @Nullable String NEGATIVE_TEXT, @Nullable DialogInterface.OnClickListener NEGATIVE_CLICK_ACTION) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AppTheme_AlertDialogueTheme);
        builder.setTitle(TITLE == null ? "Are You Sure?" : TITLE);
        builder.setMessage(MESSAGE);
        builder.setPositiveButton(POSITIVE_TEXT == null ? "Yes" : POSITIVE_TEXT, POSITIVE_CLICK_ACTION);
        builder.setNegativeButton(NEGATIVE_TEXT == null ? "No" : NEGATIVE_TEXT, NEGATIVE_CLICK_ACTION == null ? new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        } : NEGATIVE_CLICK_ACTION);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public static String getSavedUser(Context context) {
        Log.d("MY_UTILS:USER", "" + getMyAppSharedPref(context).getString("user", ""));
        return getMyAppSharedPref(context).getString("user", "");
    }

    public static String getDeviceID(Context context) {
        debugLog("MY_UTILS:DEVICEID", Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID) + "");
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public static String getEDTText(EditText editText) {
        if (editText == null) {
            return "";
        }
        return editText.getText().toString().trim();
    }

    public static boolean isValidEmail(String email) {
        return email.matches("^([a-zA-Z0-9_\\-.]+)@([a-zA-Z0-9_\\-.]+)\\.([a-zA-Z]{2,5})");
    }

    public static boolean isValidPhone(String phone) {
        return phone.matches("^[1-9][0-9]{9}");
    }

    public static boolean isValidLandline(String phone) {
        return phone.matches("^079[1-9][0-9]{6}");
    }

    public static boolean isValidName(String name) {
        return name.matches("^[A-z ]*");
    }

    public static MyState getStateFromJSONObject(JSONObject jsonObject) throws JSONException {
        return new MyState(jsonObject.getString("id"), jsonObject.getString("name"));
    }

    public static MyCity getCityFromJSONObject(JSONObject jsonObject) throws JSONException {
        return new MyCity(jsonObject.getString("id"), jsonObject.getString("name"));
    }

    public static void showMandatoryWithTextView(TextView tv, String s) {
        tv.setVisibility(View.VISIBLE);
        tv.setText(s + " is mandatory.");
    }

    public static void makeTextViewGone(TextView tv) {
        tv.setVisibility(View.GONE);
    }

    public static void showErrorWithTextView(TextView tv, String s) {
        tv.setVisibility(View.VISIBLE);
        tv.setText("Invalid " + s);
    }

    public static void setOnChangeEditText(EditText edt, final TextView tv) {
        edt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tv.setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    public static void showToastLong(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static boolean isResponse200(Response<String> response, String LOG_TITLE, Context context) {
        if (response.body() == null) {
            Log.d(LOG_TITLE, "EMPTY_RESPONSE_BODY");
            return false;
        }
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(response.body());
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(LOG_TITLE, "RESPONSE_BODY_NOT_JSON");
            return false;
        }
        try {
            Toast.makeText(context, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show();
            if (jsonObject.getInt("code") == 200) {
                return true;
            } else {
                Log.d(LOG_TITLE, "REQUEST_FAILED");
                return false;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(LOG_TITLE, "RESPONSE_CODE_NOT_FOUND");
            return false;
        }
    }

    public static void debugLog(String LOG_TITLE, String LOG_MESSAGE) {
        Log.d(LOG_TITLE, LOG_MESSAGE);
    }

    public static SharedPreferences getMyAppSharedPref(Context context) {
        return context.getSharedPreferences("com.iconflux.brokingbulls.OneTimeLogin", Context.MODE_PRIVATE);
    }

    public static SharedPreferences.Editor getMyAppSharedPrefEditor(SharedPreferences sharedPreferences) {
        return sharedPreferences.edit();
    }

    public static void removeStoredUser(Context context) {
        SharedPreferences.Editor editor = getMyAppSharedPrefEditor(getMyAppSharedPref(context));
        editor.clear();
        editor.apply();
    }

    public static String getUserAUK(Context context) {
        String AUK;
        String user = getSavedUser(context);
        if (!user.equals("")) {
            try {
                JSONObject jsonObject = new JSONObject(user);
                AUK = jsonObject.getString("auth_key");
                debugLog("MY_UTILS : AUK", "" + AUK);
                return AUK;
            } catch (JSONException e) {
                e.printStackTrace();
                return "";
            }
        } else {
            return "";
        }
    }

    public static int getRoleId(Context context) {
        int roleId = -1;
        String user = getSavedUser(context);
        if (!user.equals("")) {
            try {
                JSONObject jsonObject = new JSONObject(user);
                roleId = jsonObject.getInt("role_id");
                return roleId;
            } catch (JSONException e) {
                e.printStackTrace();
                return roleId;
            }
        } else return roleId;
    }

    public static String getCompanyType(Context context) {
        String companyType = "";
        String user = getSavedUser(context);
        if (!user.equals("")) {
            try {
                JSONObject jsonObject = new JSONObject(user);
                companyType = jsonObject.getString("company_type");
                return companyType;
            } catch (JSONException e) {
                e.printStackTrace();
                return companyType;
            }
        } else return companyType;
    }

    public static int getUserId(Context context) {
        int userId;
        String user = getSavedUser(context);
        if (!user.equals("")) {
            try {
                JSONObject jsonObject = new JSONObject(user);
                userId = jsonObject.getInt("id");
                debugLog("MY_UTILS : USER_ID", "" + userId);
                return userId;
            } catch (JSONException e) {
                e.printStackTrace();
                return 0;
            }
        } else {
            return 0;
        }
    }

    public static int getUserCompanyId(Context context) {
        int userCompanyId;
        String user = getSavedUser(context);
        if (!user.equals("")) {
            try {
                JSONObject jsonObject = new JSONObject(user);
                userCompanyId = jsonObject.getInt("company_id");
                debugLog("MY_UTILS : USER_COMPANY_ID", "" + userCompanyId);
                return userCompanyId;
            } catch (JSONException e) {
                e.printStackTrace();
                return 0;
            }
        } else {
            return 0;
        }
    }

    public static String getUserCityName(Context context) {
        String userCityName;
        String user = getSavedUser(context);
        if (!user.equals("")) {
            try {
                JSONObject jsonObject = new JSONObject(user);
                userCityName = jsonObject.getString("city_name");
                debugLog("MY_UTILS : USER_CITY_NAME", "" + userCityName);
                return userCityName;
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }

    public static String getCompanyTypeOfUser(Context context) {
        String useCompanyType;
        String user = getSavedUser(context);
        if (!user.equals("")) {
            try {
                JSONObject jsonObject = new JSONObject(user);
                useCompanyType = jsonObject.getString("company_type");
                debugLog("MY_UTILS : USER_COMPANY_TYPE", "" + useCompanyType);
                return useCompanyType;
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }

    public static int getDisplayHeight75Percent() {
        DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        return (int) (displayMetrics.heightPixels * 0.75);
    }

    public static int getDisplayHeightEighteenPercent() {
        DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        return (int) (displayMetrics.heightPixels * 0.18);
    }

    public static int getDisplayWidth20Percent() {
        DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        return (int) (displayMetrics.widthPixels * 0.25);
    }

    public static int getDisplay30Percent() {
        DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        return (int) (displayMetrics.heightPixels * 0.3);
    }

    public static int getDisplayWidth90Percent() {
        DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        return (int) (displayMetrics.widthPixels * 0.90);
    }

    public static FrameLayout.LayoutParams getMyCustomSizeForDialog() {
        return new FrameLayout.LayoutParams(getDisplayWidth90Percent(), getDisplayHeight75Percent());
    }

    public static FrameLayout.LayoutParams getMyCustomSizeForDialogWrapHeight() {
        return new FrameLayout.LayoutParams(getDisplayWidth90Percent(), FrameLayout.LayoutParams.WRAP_CONTENT);
    }

    public static FrameLayout.LayoutParams getMyCustomSizeForBottomSheet() {
        return new FrameLayout.LayoutParams(Resources.getSystem().getDisplayMetrics().widthPixels, FrameLayout.LayoutParams.WRAP_CONTENT);
    }

    public static String perfectDecimal(String str, int MAX_BEFORE_POINT, int MAX_DECIMAL) {
        if (str.charAt(0) == '.') str = "0" + str;
        int max = str.length();
        StringBuilder rFinal = new StringBuilder();
        boolean after = false;
        int i = 0, up = 0, decimal = 0;
        char t;
        while (i < max) {
            t = str.charAt(i);
            if (t != '.' && !after) {
                up++;
                if (up > MAX_BEFORE_POINT) return rFinal.toString();
            } else if (t == '.') {
                after = true;
            } else {
                decimal++;
                if (decimal > MAX_DECIMAL) return rFinal.toString();
            }
            rFinal.append(t);
            i++;
        }
        return rFinal.toString();
    }

    public static int getMonthFromString(String str) {
        switch (str.toLowerCase()) {
            case "jan":
                return Calendar.JANUARY;
            case "feb":
                return Calendar.FEBRUARY;
            case "mar":
                return Calendar.MARCH;
            case "apr":
                return Calendar.APRIL;
            case "may":
                return Calendar.MAY;
            case "jun":
                return Calendar.JUNE;
            case "jul":
                return Calendar.JULY;
            case "aug":
                return Calendar.AUGUST;
            case "sep":
                return Calendar.SEPTEMBER;
            case "oct":
                return Calendar.OCTOBER;
            case "nov":
                return Calendar.NOVEMBER;
            case "dec":
                return Calendar.DECEMBER;
        }
        return -1;
    }

    public static void dialPerson(String number, Context context) {
        Intent callIntent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", number, null));
        context.startActivity(callIntent);
    }

    public static int pxFromDp(Context context, int dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density);
    }

    public static void setTVWithEDTEmptyErr(EditText edt, final TextView tv) {
        edt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                tv.setVisibility(s.toString().trim().length() > 0 ? View.GONE : View.VISIBLE);
            }
        });
    }

    /**
     * @param list
     * @param model
     * @return int at which position model is removed. If not removed returns -1.
     */
    public static int removeFromListIfSame(@NonNull ArrayList<? extends MyListModel> list, MyListModel model) {
        for (int i = 0; i < list.size(); i++)
            if (list.get(i).getUniqueId() == model.getUniqueId()) {
                list.remove(i);
                return i;
            }
        return -1;
    }

    public static int getIntFromString(String s) {
        if (s == null || s.trim().equals("") || s.trim().equals("-") || !s.trim().matches("[0-9]+"))
            return -1;
        return Integer.parseInt(s);
    }

    public static void getPropertyProjectPermission(Context context, final String LOG_TITLE, OnPermissionReceived permissionReceived) {
        showProgress(context, "Checking validity");
        APIObject.getInstance().getPropertyPermission(MyUtils.getUserAUK(context), RequestParameter.getBodyForPermission(context)).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                dismissProgress();
                JSONObject jsonRes;
                try {
                    jsonRes = new JSONObject(response.body().string());
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                    debugLog(LOG_TITLE, "Response may null.");
                    return;
                }
                try {
                    if (jsonRes.getInt("code") == 200) {
                        PropertyProjectPermission model = new Gson().fromJson(jsonRes.getJSONObject("data").toString(), PropertyProjectPermission.class);
                        if (model != null) permissionReceived.postData(model);
                    } else
                        Toast.makeText(context, jsonRes.getString("msg"), Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                    debugLog(LOG_TITLE, "Response may not have CODE, MSG or DATA.");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                dismissProgress();
                Toast.makeText(context, "Server did not respond.", Toast.LENGTH_SHORT).show();
                debugLog(LOG_TITLE, "Server did not respond with error : " + t.getMessage());
            }
        });
    }

    public static void downloadPDFFromLink(Context context, String link, String fileName, OnResponseReceived responseReceived) {
        File futureStudioIconFile = new File(context.getExternalFilesDir(DOWNLOAD_SERVICE) + File.separator + fileName + ".pdf");
        if (futureStudioIconFile.exists()) {
            responseReceived.postResponse(true);
            return;
        }
        showProgress(context, "Downloading File");
        final String LOG_TITLE = "DOWNLOAD_FILE";
        APIObject.getInstance().getFile(link).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                dismissProgress();
                if (response.isSuccessful()) {
                    if (writeResponseBodyToDisk(response.body(), context, fileName))
                        responseReceived.postResponse(true);
                } else Toast.makeText(context, response.message(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                dismissProgress();
                Toast.makeText(context, "Server did not respond.", Toast.LENGTH_SHORT).show();
                debugLog(LOG_TITLE, "Server did not respond with error : " + t.getMessage());
            }
        });
    }

    private static boolean writeResponseBodyToDisk(ResponseBody body, Context context, String fileName) {
        try {
            File futureStudioIconFile = new File(context.getExternalFilesDir(DOWNLOAD_SERVICE) + File.separator + fileName + ".pdf");
            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                byte[] fileReader = new byte[4096];
                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;
                inputStream = body.byteStream();
                outputStream = new FileOutputStream(futureStudioIconFile);

                while (true) {
                    int read = inputStream.read(fileReader);
                    if (read == -1) break;
                    outputStream.write(fileReader, 0, read);
                    fileSizeDownloaded += read;
                }

                outputStream.flush();
                return true;
            } catch (IOException e) {
                return false;
            } finally {
                if (inputStream != null) inputStream.close();
                if (outputStream != null) outputStream.close();
            }
        } catch (IOException e) {
            return false;
        }
    }

    public static class PercentageInputFilter implements InputFilter {
        private float min;
        private float max;

        public PercentageInputFilter(float min, float max) {
            this.min = min;
            this.max = max;
        }

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            try {
                // Get input
                String stringToMatch = dest.toString() + source.toString();
                float input = Float.parseFloat(stringToMatch);

                // Check if the input is in range.
                if (isInRange(min, max, input)) {
                    // return null to accept the original replacement in case the format matches and text is in range.
                    return null;
                }
            } catch (NumberFormatException nfe) {
            }
            return "";
        }

        private boolean isInRange(float min, float max, float input) {
            return input >= min && input <= max;
        }
    }

}
