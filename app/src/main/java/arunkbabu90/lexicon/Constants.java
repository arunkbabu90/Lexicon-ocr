package arunkbabu90.lexicon;

public class Constants
{
    public static final int EDGE_FOREGROUND_SERVICE_ID = 101;
    public static final String EDGE_CHANNEL_ID = "edge-screen-channel";
    public static final String EDGE_NOTIFICATION_CHANNEL_NAME = "notification_edge_channel_name";

    public static final int RECTANGLE_CROP_MODE = 200;
    public static final int OVAL_CROP_MODE = 201;
    public static final int FULLSCREEN_MODE = 202;

    public static final int REQUEST_CODE_SAF = 1;
    public static final int REQUEST_CODE_CAMERA_MAIN = 2;
    public static final int REQUEST_CODE_CAMERA_EXTRACT = 3;
    public static final int REQUEST_CODE_SAF_EXTRACT = 4;
    public static final int REQUEST_CODE_EDGE_SERVICE_PENDING_INTENT = 5;
    public static final int REQUEST_CODE_CAST_PERMISSION = 6;
    public static final int REQUEST_CODE_CAMERA_EDGE = 7;
    public static final int REQUEST_CODE_DRAWOVER_PERMISSION = 8;
    public static final int REQUEST_CODE_WIDGET_PENDING_INTENT = 300;

    public static final String RESULT_CODE_MEDIA_PROJECTION = "media_projection_request_code";
    public static final String RESULT_INTENT_MEDIA_PROJECTION = "media_projection_result_intent";

    public static final String CAMERA_ERROR_DIALOG_TAG = "CameraErrorDialog";
    public static final String ERROR_DIALOG_TAG = "error_dialog_tag";
    public static final String EXTRACTING_TEXT_DIALOG_TAG = "ExtractingTextDialogTag";
    public static final String EXTRACTED_TEXT_DIALOG_TAG = "ExtractedTextTagDialog";
    public static final String CLIPBOARD_TEXT_LABEL = "extracted_text";
    public static final String SAVED_TEXT_DIALOG_TAG = "saved_text_tag_dialog";

    public static final String FILE_PROVIDER_AUTHORITY = "arunkbabu90.lexicon.fileprovider";

    public static final String IMAGE_URI_KEY = "sel_img_key";
    public static final String OPENED_FROM_SAF_KEY = "saf_launch_key";
    public static final String PHOTO_URI_KEY = "cam_uri_key";
    public static final String CAPTURED_FROM_CAMERA_KEY = "cam_launch_key";
    public static final String CAPTURED_FILE_PATH_KEY = "file_name_captured_key";
    public static final String CAPTURED_FROM_SCREEN_KEY = "is_captured_from_screen_key";
    public static final String SCR_DENSITY_KEY = "screen_density_intent_key";
    public static final String SCR_HEIGHT_KEY = "screen_height_intent_key";
    public static final String SCR_WIDTH_KEY = "screen_width_intent_key";
    public static final String SCR_CAPTURED_IMG_URI_KEY = "scr_capture_img_uri_key";
    public static final String SCR_CAPTURED_IMG_ABS_PATH_KEY = "scr_capture_photo_path_key";
    public static final String IS_TUTORIAL_MODE_KEY = "tutorial_mode";
    public static final String WIDGET_SAVED_TEXT_LIST_KEY = "saved_text_list_key";

    public static final String TEXT_CACHE_KEY = "extracted_text_cache";
    public static final String OPEN_BUTTON_TEXT_CACHE_KEY = "open_button_text_cache";

    public static final String NULL = "_NuLl_";

    public static final String VIRTUAL_DISPLAY_NAME = "screen_capture";

    public static final String PREF_FILE_NAME = "Lex Preferences";
    public static final String PREF_EDGE_BLINK = "BlinkEdge";
    public static final String PREF_EDGE_SWITCH_STATE = "EdgeSwitchEnabled";
    public static final String PREF_SHOW_CAST_IMG_DISTORTION_WARNING = "ShowCastDistortionWarning";
    public static final String PREF_TUTORIAL_COMPLETED = "TutorialCompleted";

    public static final String IMAGE_FORMAT_PNG = ".png";

}
