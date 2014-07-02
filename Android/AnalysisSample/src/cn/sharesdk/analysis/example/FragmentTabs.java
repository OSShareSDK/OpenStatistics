/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.sharesdk.analysis.example;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import cn.sharesdk.analysis.MobclickAgent;

/**
 * The demo shows how to integrate analytic SDK into Application based on
 * 'Fragment'. PageView ( like Fragment or viewgroup) can be tracked 
 */
public class FragmentTabs extends FragmentActivity {
	private FragmentTabHost mTabHost;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.sharesdk_example_fragment_tabs);
		mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
		mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);

		mTabHost.addTab(mTabHost.newTabSpec("simple").setIndicator("Simple"), FragmentSimple.class, null);
		mTabHost.addTab(mTabHost.newTabSpec("contacts").setIndicator("Contacts"), FragmentContacts.class, null);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	public static class FragmentSimple extends Fragment {
		private final String mPageName = "FragmentSimple";

		static FragmentSimple newInstance(int num) {
			FragmentSimple f = new FragmentSimple();

			// Supply num input as an argument.
			Bundle args = new Bundle();
			args.putInt("num", num);
			f.setArguments(args);

			return f;
		}

		@Override
		public void onPause() {
			super.onPause();
			MobclickAgent.onPageEnd(mPageName);
		}

		@Override
		public void onResume() {
			super.onResume();
			MobclickAgent.onPageStart(mPageName);
		}

		/**
		 * The Fragment's UI is just a simple text view showing its instance
		 * number.
		 */
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			TextView tv = new TextView(getActivity());
			tv.setText("Fragment Simple");
			return tv;
		}
	}

	public static class FragmentContacts extends Fragment {
		private final String mPageName = "FragmentContacts";

		static FragmentSimple newInstance(int num) {
			FragmentSimple f = new FragmentSimple();

			Bundle args = new Bundle();
			args.putInt("num", num);
			f.setArguments(args);

			return f;
		}

		/**
		 * The Fragment's UI is just a simple text view showing its instance
		 * number.
		 */
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			TextView tv = new TextView(getActivity());
			tv.setText("Fragment Contacts");
			return tv;
		}

		@Override
		public void onPause() {
			super.onPause();
			MobclickAgent.onPageEnd(mPageName);
		}

		@Override
		public void onResume() {
			super.onResume();
			MobclickAgent.onPageStart(mPageName);
		}
	}
}