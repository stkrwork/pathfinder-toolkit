package com.lateensoft.pathfinder.toolkit.views.party;

import com.lateensoft.pathfinder.toolkit.PTSharedPreferences;
import com.lateensoft.pathfinder.toolkit.R;
import com.lateensoft.pathfinder.toolkit.db.IDNamePair;
import com.lateensoft.pathfinder.toolkit.db.repository.PTPartyMemberRepository;
import com.lateensoft.pathfinder.toolkit.db.repository.PTPartyRepository;
import com.lateensoft.pathfinder.toolkit.model.party.PTParty;
import com.lateensoft.pathfinder.toolkit.model.party.PTPartyMember;
import com.lateensoft.pathfinder.toolkit.views.PTBasePageFragment;
import com.lateensoft.pathfinder.toolkit.views.character.PTCharacterSpellEditActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

public class PTPartyManagerFragment extends PTBasePageFragment implements
		OnClickListener, OnItemClickListener {
	private static final String TAG = PTPartyManagerFragment.class.getSimpleName();
	
	private final int MENU_ITEM_PARTY_LIST = 0;
	private final int MENU_ITEM_ADD_MEMBER = 1;
	private final int MENU_ITEM_NEW_PARTY = 2;
	private final int MENU_ITEM_DELETE_PARTY = 3;
	private final int DIALOG_MODE_ADD_MEMBER = 4;

	public PTParty m_party;

	private int m_dialogMode;
	private long m_partyIDSelectedInDialog;

	private EditText m_partyNameEditText;
	private ListView m_partyMemberList;
	
	private int m_partyMemberIndexSelectedForEdit;
	
	private PTPartyRepository m_partyRepo;
	private PTPartyMemberRepository m_memberRepo;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		m_partyRepo = new PTPartyRepository();
		m_memberRepo = new PTPartyMemberRepository();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		setRootView(inflater.inflate(R.layout.fragment_party_manager,
				container, false));
		setTitle(R.string.title_activity_party_manager);
		setSubtitle(null);

		m_partyNameEditText = (EditText) getRootView()
				.findViewById(R.id.editTextPartyName);

		m_partyMemberList = (ListView) getRootView()
				.findViewById(R.id.listViewPartyMembers);
		m_partyMemberList.setOnItemClickListener(this);

		loadCurrentParty();
		return getRootView();
	}
	
	@Override
	public void onPause() {
		updateDatabase();
		super.onPause();
	}

	/**
	 * Load the currently set party in shared prefs If there is no party set in
	 * user prefs, it automatically generates a new one.
	 */
	private void loadCurrentParty() {
		long currentPartyID = PTSharedPreferences.getInstance().getLong(
				PTSharedPreferences.KEY_LONG_SELECTED_PARTY_ID, -1);

		if (currentPartyID == -1) {
			// There was no current party set in shared prefs
			addNewParty();
		} else {
			m_party = m_partyRepo.query(currentPartyID);
			if (m_party == null) {
				// Recovery for some kind of catastrophic failure.
				IDNamePair[] ids = m_partyRepo.queryList();
				for (int i = 0; i < ids.length; i++) {
					m_party = m_partyRepo.query(ids[i].getID());
					if (m_party != null) {
						PTSharedPreferences.getInstance().putLong(
								PTSharedPreferences.KEY_LONG_SELECTED_PARTY_ID, m_party.getID());
						break;
					}
				}
				if (m_party == null) {
					addNewParty();
				}
			}
			refreshPartyView();

		}
	}

	/**
	 * Generates a new party and sets it to the current party.
	 */
	private void addNewParty() {
		m_party = new PTParty("New Party");
		m_partyRepo.insert(m_party);
		PTSharedPreferences.getInstance().putLong(
				PTSharedPreferences.KEY_LONG_SELECTED_PARTY_ID, m_party.getID());
		refreshPartyView();
	}

	/**
	 * Deletes the current party and loads the first in the list, or creates a
	 * new blank one, if there was only one.
	 */
	private void deleteCurrentParty() {
		int currentPartyIndex = 0;
		long currentPartyID = m_party.getID();
		IDNamePair[] partyIDs = m_partyRepo.queryList();

		for (int i = 0; i < partyIDs.length; i++) {
			if (currentPartyID == partyIDs[i].getID())
				currentPartyIndex = i;
		}

		if (partyIDs.length == 1) {
			addNewParty();
		} else if (currentPartyIndex == 0) {
			PTSharedPreferences.getInstance().putLong(
					PTSharedPreferences.KEY_LONG_SELECTED_PARTY_ID, partyIDs[1].getID());
			loadCurrentParty();
		} else {
			PTSharedPreferences.getInstance().putLong(
					PTSharedPreferences.KEY_LONG_SELECTED_PARTY_ID, partyIDs[0].getID());
			loadCurrentParty();
		}

		m_partyRepo.delete(currentPartyID);
	}

	private void updateDatabase() {
		m_party.setName(m_partyNameEditText.getText().toString());
		m_partyRepo.update(m_party);
	}

	private void refreshPartyView() {
		m_partyNameEditText.setText(m_party.getName());
		String[] memberNames = m_party.getPartyMemberNames();
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_list_item_1, memberNames);
		m_partyMemberList.setAdapter(adapter);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		updateDatabase();

		switch (item.getItemId()) {
		case MENU_ITEM_PARTY_LIST: // Tapped party list button
			m_dialogMode = MENU_ITEM_PARTY_LIST;
			showPartyDialog();
			break;
		case MENU_ITEM_ADD_MEMBER:
			m_dialogMode = DIALOG_MODE_ADD_MEMBER;
			showPartyDialog();
			break;
		case MENU_ITEM_NEW_PARTY:
			// Add new party
			m_dialogMode = MENU_ITEM_NEW_PARTY;
			showPartyDialog();
			break;
		case MENU_ITEM_DELETE_PARTY:
			// Delete party
			m_dialogMode = MENU_ITEM_DELETE_PARTY;
			showPartyDialog();
			break;
		}

		super.onOptionsItemSelected(item);
		return true;

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		MenuItem partyListItem = menu.add(Menu.NONE, MENU_ITEM_PARTY_LIST,
				Menu.NONE, R.string.menu_item_party_list);
		partyListItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

		MenuItem addMemberListItem = menu.add(Menu.NONE, MENU_ITEM_ADD_MEMBER,
				Menu.NONE, R.string.menu_item_party_list);
		addMemberListItem.setIcon(R.drawable.ic_action_new);
		addMemberListItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

		MenuItem newPartyItem = menu.add(Menu.NONE, MENU_ITEM_NEW_PARTY,
				Menu.NONE, R.string.menu_item_new_party);
		newPartyItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

		MenuItem deletePartyItem = menu.add(Menu.NONE, MENU_ITEM_DELETE_PARTY,
				Menu.NONE, R.string.menu_item_delete_party);
		deletePartyItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

		super.onCreateOptionsMenu(menu);
		return true;
	}

	private void showPartyDialog() {
		m_partyIDSelectedInDialog = m_party.getID(); // actual current party

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		switch (m_dialogMode) {
		case MENU_ITEM_PARTY_LIST:
			builder.setTitle("Select Party");

			IDNamePair[] partyIDs = m_partyRepo.queryList();
			String[] partyList = IDNamePair.toNameArray(partyIDs);
			int currentPartyIndex = 0;

			for (int i = 0; i < partyIDs.length; i++) {
				if (m_partyIDSelectedInDialog == partyIDs[i].getID())
					currentPartyIndex = i;
			}

			builder.setSingleChoiceItems(partyList, currentPartyIndex, this)
					.setPositiveButton(R.string.ok_button_text, this)
					.setNegativeButton(R.string.cancel_button_text, this);
			break;

		case MENU_ITEM_NEW_PARTY:
			builder.setTitle(getString(R.string.menu_item_new_party));
			builder.setMessage("Create new party?")
					.setPositiveButton(R.string.ok_button_text, this)
					.setNegativeButton(R.string.cancel_button_text, this);
			break;

		case MENU_ITEM_DELETE_PARTY:
			builder.setTitle(getString(R.string.menu_item_delete_party));
			builder.setMessage(
					getString(R.string.delete_character_dialog_message_1)
							+ m_party.getName()
							+ getString(R.string.delete_character_dialog_message_2))
					.setPositiveButton(R.string.delete_button_text, this)
					.setNegativeButton(R.string.cancel_button_text, this);
			break;

		case DIALOG_MODE_ADD_MEMBER:
			builder.setTitle(getString(R.string.new_party_member_dialog_title));
			builder.setMessage(getString(R.string.new_party_member_dialog_text))
					.setPositiveButton(R.string.ok_button_text, this)
					.setNegativeButton(R.string.cancel_button_text, this);
			break;

		}
		AlertDialog alert = builder.create();
		alert.show();
	}

	// Click method for the party selection dialog
	@Override
	public void onClick(DialogInterface dialogInterface, int selection) {
		switch (selection) {
		case DialogInterface.BUTTON_POSITIVE:
			performPositiveDialogAction();
			break;
		case DialogInterface.BUTTON_NEGATIVE:
			break;
		default:
			// Set the currently selected party in the dialog
			m_partyIDSelectedInDialog = m_partyRepo.queryList()[selection].getID();
			break;

		}
	}

	/**
	 * Called when dialog positive button is tapped
	 */
	private void performPositiveDialogAction() {
		switch (m_dialogMode) {
		case MENU_ITEM_PARTY_LIST:
			// Check if "currently selected" party is the same as saved one
			if (m_partyIDSelectedInDialog != m_party.getID()) {
				updateDatabase(); // Ensures any data changed on the party
										// in the current fragment is saved
				PTSharedPreferences.getInstance().putLong(
						PTSharedPreferences.KEY_LONG_SELECTED_PARTY_ID, m_partyIDSelectedInDialog);
				loadCurrentParty();
			}
			break;

		case MENU_ITEM_NEW_PARTY:
			updateDatabase();
			addNewParty();
			break;

		case MENU_ITEM_DELETE_PARTY:
			deleteCurrentParty();
			break;

		case DIALOG_MODE_ADD_MEMBER:
			m_partyMemberIndexSelectedForEdit = -1;
			showPartyMemberEditor(null);
			break;

		}

	}

	// Party member in list was clicked
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		m_partyMemberIndexSelectedForEdit = position;
		showPartyMemberEditor(m_party.getPartyMember(position));

	}

	private void showPartyMemberEditor(PTPartyMember member) {
		Intent intent = new Intent(getActivity(),
				PTPartyMemberEditorActivity.class);
		intent.putExtra(
				PTCharacterSpellEditActivity.INTENT_EXTRAS_KEY_EDITABLE_PARCELABLE, member);
		startActivityForResult(intent, 0);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (resultCode) {
		case Activity.RESULT_OK:
			PTPartyMember member = data.getExtras().getParcelable(
					PTPartyMemberEditorActivity.INTENT_EXTRAS_KEY_EDITABLE_PARCELABLE);
			Log.v(TAG, "Add/edit member OK: " + member.getName());
			if(m_partyMemberIndexSelectedForEdit < 0) {
				Log.v(TAG, "Adding a member");
				if(member != null) {
					member.setPartyID(m_party.getID());
					if (m_memberRepo.insert(member) != -1) {
						m_party.addPartyMember(member);
						refreshPartyView(); 
					}
				}
			} else {
				Log.v(TAG, "Editing a member");
				if (m_memberRepo.update(member) != 0) {
					m_party.setPartyMember(m_partyMemberIndexSelectedForEdit, member);
					refreshPartyView();
				}
			}
			
			break;
		
		case PTPartyMemberEditorActivity.RESULT_DELETE:
			Log.v(TAG, "Deleting a member");
			if (m_memberRepo.delete(m_party.getPartyMember(m_partyMemberIndexSelectedForEdit)) != 0) {
				m_party.deletePartyMember(m_partyMemberIndexSelectedForEdit);
				refreshPartyView();
			}
			break;
		
		case Activity.RESULT_CANCELED:
			break;
		
		default:
			break;
		}
		updateDatabase();
		super.onActivityResult(requestCode, resultCode, data);
	}
}