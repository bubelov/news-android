package de.luhmer.owncloudnewsreader;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import de.luhmer.owncloudnewsreader.authentication.AccountGeneral;
import de.luhmer.owncloudnewsreader.helper.ThemeChooser;


public class SyncIntervalSelectorActivity extends AppCompatActivity {

    private PlaceholderFragment mFragment;
    private String[] items_values;
    protected @BindView(R.id.toolbar) Toolbar toolbar;
    protected @Inject SharedPreferences mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((NewsReaderApplication) getApplication()).getAppComponent().injectActivity(this);

        ThemeChooser.chooseTheme(this);
        super.onCreate(savedInstanceState);
        ThemeChooser.afterOnCreate(this);

        setContentView(R.layout.activity_sync_interval_selector);

        ButterKnife.bind(this);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        items_values = getResources().getStringArray(R.array.array_sync_interval_values);

        if (savedInstanceState == null) {
            mFragment = new PlaceholderFragment();

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, mFragment)
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.sync_interval_selector, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically StartYoutubePlayer clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id == R.id.action_save) {
            int checkedPosition = mFragment.lvItems.getCheckedItemPosition();

            Integer minutes = Integer.parseInt(items_values[checkedPosition]);

            mPrefs.edit().putInt(SYNC_INTERVAL_IN_MINUTES_STRING, minutes).commit();

            setAccountSyncInterval(this, mPrefs);

            finish();
        }


        return super.onOptionsItemSelected(item);
    }


    public static void setAccountSyncInterval(Context context, SharedPreferences mPrefs) {
        int minutes = mPrefs.getInt(SYNC_INTERVAL_IN_MINUTES_STRING, 0);

        AccountManager mAccountManager = AccountManager.get(context);
        Account[] accounts = mAccountManager.getAccountsByType(AccountGeneral.ACCOUNT_TYPE);
        for (Account account : accounts) {
            if (minutes != 0) {
                long SYNC_INTERVAL = minutes * SECONDS_PER_MINUTE;
                ContentResolver.setSyncAutomatically(account, AccountGeneral.ACCOUNT_TYPE, true);

                Bundle bundle = new Bundle();
                ContentResolver.addPeriodicSync(
                        account,
                        AccountGeneral.ACCOUNT_TYPE,
                        bundle,
                        SYNC_INTERVAL);

            } else {
                ContentResolver.setSyncAutomatically(account, AccountGeneral.ACCOUNT_TYPE, false);
            }
        }
    }


    /**
     * A placeholder fragment containing a simple view.
     */

    // Sync interval constants
    public static final long MILLISECONDS_PER_SECOND = 1000L;
    public static final long SECONDS_PER_MINUTE = 60L;
    //public static final long SYNC_INTERVAL_IN_MINUTES = 60L;
    public static final String SYNC_INTERVAL_IN_MINUTES_STRING = "SYNC_INTERVAL_IN_MINUTES_STRING";

    public static class PlaceholderFragment extends Fragment {

        private ListView lvItems;
        protected @Inject SharedPreferences mPrefs;

        public PlaceholderFragment() {
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            ((NewsReaderApplication) getActivity().getApplication()).getAppComponent().injectFragment(this);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_sync_interval_selector, container, false);

            String[] items = getResources().getStringArray(R.array.array_sync_interval);

            lvItems = rootView.findViewById(R.id.lv_sync_interval_items);
            lvItems.setChoiceMode(ListView.CHOICE_MODE_SINGLE);


            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                    android.R.layout.simple_list_item_single_choice, android.R.id.text1, items);


            lvItems.setAdapter(adapter);

            if(!mPrefs.contains(SYNC_INTERVAL_IN_MINUTES_STRING))
                lvItems.setItemChecked(items.length - 1, true);//The last item is 24hours. This is the default value!
            else {
                int position = 0;
                int minutes = mPrefs.getInt(SYNC_INTERVAL_IN_MINUTES_STRING, 0);
                for(String item : ((SyncIntervalSelectorActivity)getActivity()).items_values) {
                    if(Integer.parseInt(item) == minutes)
                        break;
                    position++;
                }
                lvItems.setItemChecked(position, true);//The last item is 24hours. This is the default value!
            }

            return rootView;
        }
    }

}
