package com.richitec.chinesetelephone.assist;

import com.richitec.chinesetelephone.R;
import com.richitec.chinesetelephone.R.layout;
import com.richitec.chinesetelephone.R.menu;
import com.richitec.chinesetelephone.account.SettingActivity;
import com.richitec.chinesetelephone.alipay.MobileSecurePayHelper;
import com.richitec.chinesetelephone.customcomponent.TelephoneBarButtonItem;
import com.richitec.chinesetelephone.customcomponent.TelephoneNavigationActivity;
import com.richitec.commontoolkit.customcomponent.BarButtonItem.BarButtonItemStyle;
import com.richitec.commontoolkit.user.UserManager;
import com.richitec.commontoolkit.utils.MyToast;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.NavUtils;

public class RemainMoneyActivity extends TelephoneNavigationActivity {
	public static String BALANCE = "balance";
	private MobileSecurePayHelper mspHelper= null;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remain_money);
        
        mspHelper = new MobileSecurePayHelper(RemainMoneyActivity.this);
        
        this.setRightBarButtonItem(new TelephoneBarButtonItem(this,BarButtonItemStyle.LEFT_BACK,"充值",chargeBtnListener));
        setTitle(R.string.get_remain_money_title);
        
        double balance = getIntent().getDoubleExtra(BALANCE, 0.0);
        //MyToast.show(this, "balance:"+balance, Toast.LENGTH_SHORT);
        
        String username = UserManager.getInstance().getUser().getName();
        
        double callTime = balance*10;
        
        double smsCount = balance*10;
        
        double backcallTime = balance*6;
        
        ((TextView)findViewById(R.id.uername)).setText(username);
        ((TextView)findViewById(R.id.remain_money)).setText(String.valueOf(balance)+getString(R.string.yuan));
        ((TextView)findViewById(R.id.direct_call)).setText(String.valueOf(callTime)+getString(R.string.minute));
        ((TextView)findViewById(R.id.back_call)).setText(String.valueOf(backcallTime)+getString(R.string.minute));
        ((TextView)findViewById(R.id.sms_count)).setText(String.valueOf(smsCount)+getString(R.string.tiao));
    }
    
    private OnClickListener chargeBtnListener = new OnClickListener(){

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			boolean isMobile_spExist = mspHelper.detectMobile_sp();
			if (!isMobile_spExist)
				return;
		}
    	
    };
}
