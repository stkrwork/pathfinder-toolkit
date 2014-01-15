package com.lateensoft.pathfinder.toolkit.views;

import com.lateensoft.pathfinder.toolkit.PTMainActivity;

import android.app.Activity;
import android.app.Fragment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class PTBasePageFragment extends Fragment{

	private View m_rootView;

	@Override
	public void onResume() {
		super.onResume();
		Activity a = getActivity();
		if (a instanceof PTMainActivity) {
			((PTMainActivity) a).hideKeyboardDelayed(100);
		}
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {		
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		return false;
	}
	
	protected void setTitle(String title) {
		getActivity().getActionBar().setTitle(title);
	}
	
	protected void setTitle(int resId) {
		getActivity().getActionBar().setTitle(resId);
	}
	
	protected void setSubtitle(String subtitle) {
		getActivity().getActionBar().setSubtitle(subtitle);
	}
	
	protected View getRootView() {
		return m_rootView;
	}
	
	protected void setRootView(View rootView) {
		m_rootView = rootView;
	}
}
