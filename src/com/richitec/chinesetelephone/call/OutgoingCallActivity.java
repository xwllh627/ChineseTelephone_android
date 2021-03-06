package com.richitec.chinesetelephone.call;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.richitec.chinesetelephone.R;
import com.richitec.chinesetelephone.sip.listeners.SipInviteStateListener;
import com.richitec.chinesetelephone.sip.services.BaseSipServices;
import com.richitec.chinesetelephone.tab7tabcontent.DialTabContentActivity;

public class OutgoingCallActivity extends Activity implements
		SipInviteStateListener {

	private static final String LOG_TAG = "OutgoingCallActivity";

	// keyboard gridView keys
	public static final String KEYBOARD_BUTTON_CODE = "keyboard_button_code";
	public static final String KEYBOARD_BUTTON_IMAGE = "keyboard_button_image";
	public static final String KEYBOARD_BUTTON_BGRESOURCE = "keyboard_button_background_resource";
	public static final String KEYBOARD_BUTTON_ONCLICKLISTENER = "keyboard_button_onClickListener";

	// sip services
	private static BaseSipServices _smSipServices;

	// outgoing call activity onCreate param key
	public static final String OUTGOING_CALL_PHONE = "outgoing_call_phone";
	public static final String OUTGOING_CALL_OWNERSHIP = "outgoing_call_ownership";

	// sound pool
	private static final SoundPool SOUND_POOL = new SoundPool(1,
			AudioManager.STREAM_MUSIC, 0);

	// outgoing call phone number
	private String _mCalleePhone;

	// audio manager
	private AudioManager _mAudioManager;

	// call duration timer
	private final Timer CALLDURATION_TIMER = new Timer();

	// call duration time and set default value is 0
	private Long _mCallDutation = 0L;

	// update call duration time handle
	private final Handler UPDATE_CALLDURATIONTIME_HANDLE = new Handler();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// keep outgoing call activity screen on
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		// set content view
		setContentView(R.layout.outgoing_call_activity_layout);

		// get the intent parameter data
		Bundle _data = getIntent().getExtras();

		// check the data bundle and get call phone
		if (null != _data) {
			// init outgoing call phone and set callee textView text
			if (null != _data.getString(OUTGOING_CALL_PHONE)) {
				_mCalleePhone = _data.getString(OUTGOING_CALL_PHONE);

				((TextView) findViewById(R.id.callee_textView))
						.setText(null != _data
								.getString(OUTGOING_CALL_OWNERSHIP) ? _data
								.getString(OUTGOING_CALL_OWNERSHIP)
								: _mCalleePhone);
			}

			// check sip services
			if (null != _smSipServices) {
				// set sip services sip invite state listener
				_smSipServices.setSipInviteStateListener(this);
			} else {
				Log.e(LOG_TAG, "Get sip services error, sip services is null");
			}
		}

		// init audio manager
		_mAudioManager = (AudioManager) this
				.getSystemService(Context.AUDIO_SERVICE);

		// set wallpaper as outgoing call background
		((ImageView) findViewById(R.id.outgoingcall_background_imageView))
				.setImageDrawable(getWallpaper());

		// get call controller gridView
		GridView _callControllerGridView = (GridView) findViewById(R.id.callController_gridView);

		// set call controller gridView adapter
		_callControllerGridView.setAdapter(generateCallControllerAdapter());

		// set call controller gridView on item click listener
		_callControllerGridView
				.setOnItemClickListener(new CallControllerGridViewOnItemClickListener());

		// set keyboard gridView adapter
		((GridView) findViewById(R.id.keyboard_gridView))
				.setAdapter(generateKeyboardAdapter());

		// bind hangup outgoing call button on click listener
		((ImageButton) findViewById(R.id.hangup_button))
				.setOnClickListener(new HangupOutgoingCallBtnOnClickListener());

		// bind hide keyboard button on click listener
		((ImageButton) findViewById(R.id.hideKeyboard_button))
				.setOnClickListener(new HideKeyboardBtnOnClickListener());
	}

	@Override
	public void onBackPressed() {
		// nothing to do
	}

	@Override
	public void onCallInitializing() {
		// update call state textView text
		((TextView) findViewById(R.id.callState_textView))
				.setText(R.string.outgoing_call_trying);
	}

	@Override
	public void onCallEarlyMedia() {
		// update call state textView text
		((TextView) findViewById(R.id.callState_textView))
				.setText(R.string.outgoing_call_earlyMedia7RemoteRing);
	}

	@Override
	public void onCallRemoteRinging() {
		// update call state textView text
		((TextView) findViewById(R.id.callState_textView))
				.setText(R.string.outgoing_call_earlyMedia7RemoteRing);
	}

	@Override
	public void onCallSpeaking() {
		// check current sip voice call using loudspeaker
		if (_smSipServices.isSipVoiceCallUsingLoudspeaker()) {
			// set current sip voice call loudspeaker
			// set mode
			_mAudioManager.setMode(AudioManager.MODE_NORMAL);

			// open speaker
			_mAudioManager.setSpeakerphoneOn(true);
		}

		// increase call duration time per second using timer task
		CALLDURATION_TIMER.schedule(new TimerTask() {

			@Override
			public void run() {
				// increase call duration time
				_mCallDutation += 1L;

				// update call duration time
				UPDATE_CALLDURATIONTIME_HANDLE.post(new Runnable() {

					// 60 seconds per minute
					Integer SECONDS_PER_MINUTE = 60;

					@Override
					public void run() {
						// get call duration minutes and seconds
						Long _durationMinutes = _mCallDutation
								/ SECONDS_PER_MINUTE;
						Integer _durationSeconds = (int) (_mCallDutation % SECONDS_PER_MINUTE);

						// format call duration
						StringBuilder _callDurationTimeFormat = new StringBuilder();
						_callDurationTimeFormat
								.append(_durationMinutes <= 9 ? "0"
										+ _durationMinutes : _durationMinutes)
								.append(":")
								.append(_durationSeconds <= 9 ? "0"
										+ _durationSeconds : _durationSeconds);

						// update call state textView text using call duration
						((TextView) findViewById(R.id.callState_textView))
								.setText(_callDurationTimeFormat);
					}
				});
			}
		}, 0, 1000);
	}

	@Override
	public void onCallFailed() {
		// update call state textView text
		((TextView) findViewById(R.id.callState_textView))
				.setText(R.string.outgoing_call_failed);

		// sip voice call terminated
		onCallTerminated();
	}

	@Override
	public void onCallTerminating() {
		// update call state textView text
		((TextView) findViewById(R.id.callState_textView))
				.setText(R.string.end_outgoing_call);

		onCallTerminated();
	}

	@Override
	public void onCallTerminated() {
		// terminate current sip voice call
		terminateSipVoiceCall(SipVoiceCallTerminatedType.PASSIVE);
	}

	// init outgoing call activity sip services
	public static void initSipServices(BaseSipServices sipServices) {
		_smSipServices = sipServices;
	}

	// generate call controller adapter
	private ListAdapter generateCallControllerAdapter() {
		// call controller item adapter data key
		final String CALL_CONTROLLER_ITEM_BACKGROUND = "call_controller_item_background";
		final String CALL_CONTROLLER_ITEM_ICON = "call_controller_item_icon";
		final String CALL_CONTROLLER_ITEM_LABEL = "call_controller_item_label";

		// define call controller gridView content
		final int[][] _callControllerGridViewContentArray = new int[][] {
				{ R.drawable.callcontroller_contactitem6keyboard_1btn_bg,
						R.drawable.img_callcontroller_contactitem_normal,
						R.string.callController_contactItem_text },
				{ R.drawable.callcontroller_keyboarditem6keyboard_3btn_bg,
						R.drawable.img_callcontroller_keyboarditem_normal,
						R.string.callController_keyboardItem_text },
				{ R.drawable.callcontroller_muteitem6keyboard_9btn_bg,
						R.drawable.img_callcontroller_muteitem_normal,
						R.string.callController_muteItem_text },
				{ R.drawable.callcontroller_handfreeitem6keyboard_poundbtn_bg,
						R.drawable.img_callcontroller_handfreeitem_normal,
						R.string.callController_handfreeItem_text } };

		// set call controller data list
		List<Map<String, ?>> _callControllerDataList = new ArrayList<Map<String, ?>>();

		for (int i = 0; i < _callControllerGridViewContentArray.length; i++) {
			// generate data
			Map<String, Object> _dataMap = new HashMap<String, Object>();

			// put value
			_dataMap.put(CALL_CONTROLLER_ITEM_BACKGROUND,
					_callControllerGridViewContentArray[i][0]);
			_dataMap.put(CALL_CONTROLLER_ITEM_ICON,
					_callControllerGridViewContentArray[i][1]);
			_dataMap.put(CALL_CONTROLLER_ITEM_LABEL,
					_callControllerGridViewContentArray[i][2]);

			// add data to list
			_callControllerDataList.add(_dataMap);
		}

		return new OutgoingCallControllerAdapter(
				this,
				_callControllerDataList,
				R.layout.call_controller_item,
				new String[] { CALL_CONTROLLER_ITEM_BACKGROUND,
						CALL_CONTROLLER_ITEM_ICON, CALL_CONTROLLER_ITEM_LABEL },
				new int[] { R.id.callController_item_relativeLayout,
						R.id.callController_item_iconImgView,
						R.id.callController_item_labelTextView });
	}

	// generate keyboard adapter
	private ListAdapter generateKeyboardAdapter() {
		// keyboard adapter data key
		final String KEYBOARD_BUTTON = "keyboard_button";

		// define keyboard gridView image resource content
		final int[] _keyboardGridViewImgResourceContentArray = {
				R.drawable.img_dial_1_btn, R.drawable.img_dial_2_btn,
				R.drawable.img_dial_3_btn, R.drawable.img_dial_4_btn,
				R.drawable.img_dial_5_btn, R.drawable.img_dial_6_btn,
				R.drawable.img_dial_7_btn, R.drawable.img_dial_8_btn,
				R.drawable.img_dial_9_btn, R.drawable.img_dial_star_btn,
				R.drawable.img_dial_0_btn, R.drawable.img_dial_pound_btn };

		// set keyboard button data list
		List<Map<String, ?>> _keyboardButtonDataList = new ArrayList<Map<String, ?>>();

		for (int i = 0; i < _keyboardGridViewImgResourceContentArray.length; i++) {
			// generate data
			Map<String, Object> _dataMap = new HashMap<String, Object>();

			// value map
			Map<String, Object> _valueMap = new HashMap<String, Object>();
			_valueMap.put(KEYBOARD_BUTTON_CODE, i);
			_valueMap.put(KEYBOARD_BUTTON_IMAGE,
					_keyboardGridViewImgResourceContentArray[i]);
			switch (i) {
			case 0:
				// set top left keyboard button background
				_valueMap.put(KEYBOARD_BUTTON_BGRESOURCE,
						R.drawable.callcontroller_contactitem6keyboard_1btn_bg);
				break;

			case 2:
				// set top right keyboard button background
				_valueMap
						.put(KEYBOARD_BUTTON_BGRESOURCE,
								R.drawable.callcontroller_keyboarditem6keyboard_3btn_bg);
				break;

			case 9:
				// set bottom left keyboard button background
				_valueMap.put(KEYBOARD_BUTTON_BGRESOURCE,
						R.drawable.callcontroller_muteitem6keyboard_9btn_bg);
				break;

			case 11:
				// set bottom right keyboard button background
				_valueMap
						.put(KEYBOARD_BUTTON_BGRESOURCE,
								R.drawable.callcontroller_handfreeitem6keyboard_poundbtn_bg);
				break;

			default:
				// set normal keyboard button background
				_valueMap.put(KEYBOARD_BUTTON_BGRESOURCE,
						R.drawable.keyboard_btn_bg);
				break;
			}
			_valueMap.put(KEYBOARD_BUTTON_ONCLICKLISTENER,
					new KeyboardBtnOnClickListener());

			// put value
			_dataMap.put(KEYBOARD_BUTTON, _valueMap);

			// add data to list
			_keyboardButtonDataList.add(_dataMap);
		}

		return new OutgoingCallKeyboardAdapter(this, _keyboardButtonDataList,
				R.layout.keyboard_btn_layout, new String[] { KEYBOARD_BUTTON },
				new int[] { R.id.keyboardBtn_imageBtn });
	}

	// show or hide keyboard
	private void show6hideKeyboard(boolean isShowKeyboard) {
		// get hide keyboard image button, keyboard gridView, call controller
		// gridView and dtmf textView text
		ImageButton _hideKeyboardImgBtn = (ImageButton) findViewById(R.id.hideKeyboard_button);
		GridView _keyboardGridView = (GridView) findViewById(R.id.keyboard_gridView);
		GridView _callControllerGridView = (GridView) findViewById(R.id.callController_gridView);
		TextView _dtmfTextView = (TextView) findViewById(R.id.dtmf_textView);

		// check is show keyboard
		if (isShowKeyboard) {
			// show hide keyboard image button
			_hideKeyboardImgBtn.setVisibility(View.VISIBLE);

			// show keyboard gridView and hide call controller gridView
			_keyboardGridView.setVisibility(View.VISIBLE);
			_callControllerGridView.setVisibility(View.GONE);

			// reset hangup image button source image
			((ImageButton) findViewById(R.id.hangup_button))
					.setImageResource(R.drawable.img_hangup_btn_short);

			// clear dtmf textView text
			_dtmfTextView.setText("");
		} else {
			// hide hide keyboard image button
			_hideKeyboardImgBtn.setVisibility(View.GONE);

			// hide keyboard gridView and show call controller gridView
			_keyboardGridView.setVisibility(View.GONE);
			_callControllerGridView.setVisibility(View.VISIBLE);

			// reset hangup image button source image
			((ImageButton) findViewById(R.id.hangup_button))
					.setImageResource(R.drawable.img_hangup_btn_long);

			// show callee textView and hide dtmf textView
			((TextView) findViewById(R.id.callee_textView))
					.setVisibility(View.VISIBLE);
			_dtmfTextView.setVisibility(View.GONE);
		}
	}

	// terminate sip voice call
	private void terminateSipVoiceCall(SipVoiceCallTerminatedType terminatedType) {
		// check sip voice call terminated type
		switch (terminatedType) {
		case INITIATIVE:
			// hangup current sip voice call
			if (!_smSipServices.hangupSipVoiceCall(_mCallDutation)) {
				// cancel call duration timer
				CALLDURATION_TIMER.cancel();

				// force finish outgoing call activity
				finish();

				// return immediately
				return;
			} else {
				// update call state textView text
				((TextView) findViewById(R.id.callState_textView))
						.setText(R.string.end_outgoing_call);
			}

			break;

		case PASSIVE:
		default:
			// update call log call duration time
			_smSipServices.updateSipVoiceCallLog(_mCallDutation);

			break;
		}

		// cancel call duration timer
		CALLDURATION_TIMER.cancel();

		// delayed 0.4 second to terminating
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				// update call state textView text
				((TextView) findViewById(R.id.callState_textView))
						.setText(R.string.outgoing_call_ended);

				// delayed 0.8 second to back
				new Handler().postDelayed(new Runnable() {

					@Override
					public void run() {
						// finish outgoing call activity
						finish();
					}
				}, 800);
			}
		}, 400);
	}

	// inner class
	// call controller gridView on item click listener
	class CallControllerGridViewOnItemClickListener implements
			OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// check call controller gridView item on clicked
			switch (position) {
			case 0:
				// show contacts list
				Log.d(LOG_TAG, "Show contacts list, not implement now");

				break;

			case 1:
				// show keyboard gridView and hide keyboard image button
				show6hideKeyboard(true);

				break;

			case 2:
				// mute or unmute current sip voice call
				_smSipServices.setSipVoiceCallUsingEarphone();
				// _smSipServices.muteSipVoiceCall();
				// _smSipServices.unmuteSipVoiceCall();

				break;

			case 3:
			default:
				// set current sip voice call loudspeaker or earphone
				_smSipServices.setSipVoiceCallUsingLoudspeaker();
				// _smSipServices.setSipVoiceCallUsingEarphone();

				break;
			}
		}
	}

	// keyboard button on click listener
	class KeyboardBtnOnClickListener implements OnClickListener {

		// define keyboard button value data
		private final String[] _keyboardPhoneButtonValueData = new String[] {
				"1", "2", "3", "4", "5", "6", "7", "8", "9", "*", "0", "#" };

		@Override
		public void onClick(View v) {
			// get callee and dtmf textView
			TextView _calleeTextView = (TextView) findViewById(R.id.callee_textView);
			TextView _dtmfTextView = (TextView) findViewById(R.id.dtmf_textView);

			// check callee and dtmf textView visible
			if (_calleeTextView.isShown()) {
				// hide callee textView
				_calleeTextView.setVisibility(View.GONE);
			}
			if (!_dtmfTextView.isShown()) {
				// show dtmf textView
				_dtmfTextView.setVisibility(View.VISIBLE);
			}

			// get clicked phone number
			String _clickedPhoneNumber = _keyboardPhoneButtonValueData[(Integer) v
					.getTag()];

			// define keyboard phone string builder
			StringBuilder _keyboardPhoneStringBuilder = new StringBuilder(
					_dtmfTextView.getText());

			// dial phone
			_keyboardPhoneStringBuilder.append(_clickedPhoneNumber);

			// reset dtmf textView text
			_dtmfTextView.setText(_keyboardPhoneStringBuilder);

			// play keyboard phone button dtmf sound
			// get volume
			float _volume = _mAudioManager
					.getStreamVolume(AudioManager.STREAM_MUSIC);

			// play dial phone button dtmf sound with index
			SOUND_POOL.play(DialTabContentActivity
					.getDialPhoneBtnDTMFSoundPoolMap()
					.get((Integer) v.getTag()), _volume, _volume, 0, 0, 1f);

			// send dtmf
			_smSipServices.sentDTMF(_clickedPhoneNumber);
		}
	}

	// hangup outgoing call button on click listener
	class HangupOutgoingCallBtnOnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// terminate current sip voice call
			terminateSipVoiceCall(SipVoiceCallTerminatedType.INITIATIVE);
		}

	}

	// hide keyboard button on click listener
	class HideKeyboardBtnOnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// hide keyboard gridView and hide keyboard image button
			show6hideKeyboard(false);
		}

	}

	// sip voice call terminated type
	enum SipVoiceCallTerminatedType {
		// initiative or passive
		INITIATIVE, PASSIVE
	}

}
