package com.richitec.chinesetelephone.tab7tabcontent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Bundle;
import android.view.Menu;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.richitec.chinesetelephone.R;
import com.richitec.commontoolkit.activityextension.NavigationActivity;
import com.richitec.commontoolkit.addressbook.AddressBookManager;

public class CallRecordHistoryListTabContentActivity extends NavigationActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// set content view
		setContentView(R.layout.call_record_history_list_tab_content_activity_layout);

		// set title
		setTitle(R.string.call_record_history_list_nav_title);

		// get call record history listView
		ListView _callRecordHistoryListView = (ListView) findViewById(R.id.callRecordHistoryList_listView);

		// set call record history listView adapter
		_callRecordHistoryListView
				.setAdapter(generateCallRecordHistoryListItemAdapter());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(
				R.menu.call_record_history_list_tab_content_activity_layout,
				menu);
		return true;
	}

	// generate call record history list item adapter
	private ListAdapter generateCallRecordHistoryListItemAdapter() {
		// call record history list item adapter data keys
		final String CALL_RECORD_DISPLAYNAME = "call_record_displayName";
		final String CALL_RECORD_PHONE = "call_record_phone";
		final String CALL_RECORD_INITIATETIME = "call_record_initiateTime";

		// set call record history list view data list
		List<Map<String, ?>> _callRecordHistoryDataList = new ArrayList<Map<String, ?>>();

		// get address book manager reference
		AddressBookManager _addressBookManager = AddressBookManager
				.getInstance();

		// test by ares
		// define list view content
		String[] listViewPhoneContentArr = { "13770662051", "13456231234",
				"+86-025-66083096-801", "13382794516", "13813005146", "1234" };
		String[] listViewInitiateContentArr = { "12-10-10\n12:12:10",
				"12-10-10\n12:22:10", "12-10-10\n14:12:10",
				"12-10-11\n12:12:10", "12-10-11\n19:12:10",
				"12-10-12\n11:12:10" };

		for (int i = 0; i < listViewPhoneContentArr.length; i++) {
			// generate data
			Map<String, Object> _dataMap = new HashMap<String, Object>();

			// put value
			// get dial phone
			String _dialPhone = listViewPhoneContentArr[i];

			// check dial phone has ownership
			Long _dialPhoneOwnershipId = _addressBookManager
					.isContactWithPhoneInAddressBook(_dialPhone);
			if (null == _dialPhoneOwnershipId) {
				_dataMap.put(
						CALL_RECORD_DISPLAYNAME,
						getResources().getString(
								R.string.unknown_dial_phoneNumber));
			} else {
				_dataMap.put(CALL_RECORD_DISPLAYNAME, _addressBookManager
						.getContactByAggregatedId(_dialPhoneOwnershipId)
						.getDisplayName());
			}
			_dataMap.put(CALL_RECORD_PHONE, listViewPhoneContentArr[i]);
			_dataMap.put(CALL_RECORD_INITIATETIME,
					listViewInitiateContentArr[i]);

			// add data to list
			_callRecordHistoryDataList.add(_dataMap);
		}

		return new CallRecordHistoryListItemAdapter(this,
				_callRecordHistoryDataList,
				R.layout.call_record_historylist_item, new String[] {
						CALL_RECORD_DISPLAYNAME, CALL_RECORD_PHONE,
						CALL_RECORD_INITIATETIME }, new int[] {
						R.id.record_displayName_textView,
						R.id.record_phone_textView,
						R.id.record_initiateTime_textView });
	}

}
