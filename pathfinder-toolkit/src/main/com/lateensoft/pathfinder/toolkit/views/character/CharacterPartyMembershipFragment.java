package com.lateensoft.pathfinder.toolkit.views.character;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;
import com.lateensoft.pathfinder.toolkit.R;
import com.lateensoft.pathfinder.toolkit.adapters.SimpleSelectableListAdapter;
import com.lateensoft.pathfinder.toolkit.db.repository.LitePartyRepository;
import com.lateensoft.pathfinder.toolkit.db.repository.PartyMembershipRepository;
import com.lateensoft.pathfinder.toolkit.model.IdNamePair;
import com.lateensoft.pathfinder.toolkit.views.MultiSelectActionModeCallback;
import com.lateensoft.pathfinder.toolkit.views.MultiSelectActionModeController;
import com.lateensoft.pathfinder.toolkit.views.picker.PickerUtil;

import java.util.Collections;
import java.util.List;

import static com.lateensoft.pathfinder.toolkit.db.repository.PartyMembershipRepository.Membership;

public class CharacterPartyMembershipFragment extends AbstractCharacterSheetFragment {

    private static final String TAG = CharacterPartyMembershipFragment.class.getSimpleName();
    public static final int GET_NEW_PARTIES_CODE = 33091;

    private ListView m_partyListView;
    private Button m_addButton;

    private LitePartyRepository m_partyRepo;
    private List<IdNamePair> m_parties;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        m_partyRepo = new LitePartyRepository();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        setRootView(inflater.inflate(R.layout.character_membership_fragment,
                container, false));

        m_addButton = (Button) getRootView().findViewById(R.id.button_add);
        m_addButton.setOnClickListener(new OnClickListener() {
            @Override public void onClick(View v) {
                showPartyPicker();
            }
        });

        m_partyListView = (ListView) getRootView()
                .findViewById(R.id.lv_parties);
        m_partyListView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        m_partyListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (!actionModeController.isActionModeStarted()) {
                    actionModeController.startActionModeWithInitialSelection(position);
                    return true;
                }
                return false;
            }
        });
        m_partyListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (actionModeController.isActionModeStarted()) {
                    actionModeController.toggleListItemSelection(position);
                }
            }
        });

        return getRootView();
    }

    private MultiSelectActionModeController actionModeController = new MultiSelectActionModeController() {
        @Override public Activity getActivity() {
            return CharacterPartyMembershipFragment.this.getActivity();
        }

        @Override public int getActionMenuResourceId() {
            return R.menu.membership_action_mode_menu;
        }

        @Override public ListView getListView() {
            return m_partyListView;
        }

        @Override public boolean onActionItemClicked(MultiSelectActionModeController controller, MenuItem item) {
            if (item.getItemId() == R.id.mi_remove) {
                showRemoveCharacterFromPartyDialog(getSelectedItems(m_parties));
                controller.finishActionMode();
                return true;
            }
            return false;
        }
    };

    private void refreshPartiesListView() {
        if (actionModeController.isActionModeStarted()) {
            actionModeController.finishActionMode();
        }

        Collections.sort(m_parties);

        SimpleSelectableListAdapter adapter = new SimpleSelectableListAdapter<IdNamePair>(getActivity(),
                 m_parties, new SimpleSelectableListAdapter.DisplayStringGetter<IdNamePair>() {
                    @Override public String getDisplayString(IdNamePair object) {
                        return object.getName();
                    }
                });
        adapter.setItemSelectionGetter(new SimpleSelectableListAdapter.ItemSelectionGetter() {
            @Override public boolean isItemSelected(int position) {
                return actionModeController.isListItemSelected(position);
            }
        });

        m_partyListView.setAdapter(adapter);
    }

    public void showRemoveCharacterFromPartyDialog(final List<IdNamePair> parties) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.membership_remove_dialog_title)
                .setMessage(String.format(getString(R.string.membership_remove_dialog_msg), parties.size()));

        builder.setPositiveButton(R.string.ok_button_text, new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    removeCharacterFromParties(parties);
                }
            }
        })
                .setNegativeButton(R.string.cancel_button_text, null)
                .show();
    }

    private void removeCharacterFromParties(List<IdNamePair> parties) {
        PartyMembershipRepository memberRepo = m_partyRepo.getMembersRepo();
        for (IdNamePair party : parties) {
            int dels = memberRepo.delete(new Membership(party.getId(), currentCharacterID));
            if (dels > 0) {
                m_parties.remove(party);
            }
            refreshPartiesListView();
        }
    }

    public void showPartyPicker() {
        List<IdNamePair> parties = m_partyRepo.queryIdNameList();
        parties.removeAll(m_parties);
        PickerUtil.Builder builder = new PickerUtil.Builder(getActivity());
        builder.setTitle(R.string.membership_party_picker_title)
                .setSingleChoice(false)
                .setPickableParties(parties);
        Intent pickerIntent = builder.build();
        startActivityForResult(pickerIntent, GET_NEW_PARTIES_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GET_NEW_PARTIES_CODE && resultCode == Activity.RESULT_OK &&
                data != null) {
            PickerUtil.ResultData resultData = new PickerUtil.ResultData(data);
            List<IdNamePair> partiesToAdd = resultData.getParties();
            if (partiesToAdd != null) {
                PartyMembershipRepository membersRepo = m_partyRepo.getMembersRepo();
                for (IdNamePair party : partiesToAdd) {
                    long id = membersRepo.insert(new Membership(party.getId(), currentCharacterID));
                    if (id >= 0) {
                        m_parties.add(party);
                    } else {
                        Log.e(TAG, "Database returned " + id + " while adding character "
                                + currentCharacterID + " to " + party);
                    }
                }
                refreshPartiesListView();
            }
        }
    }

    @Override
    public void updateFragmentUI() {
        refreshPartiesListView();
    }
    
    @Override
    public String getFragmentTitle() {
        return getString(R.string.tab_character_membership);
    }

    @Override
    public void updateDatabase() {
        // Done dynamically
    }

    @Override
    public void loadFromDatabase() {
        m_parties = m_partyRepo.queryPartyNamesForCharacter(getCurrentCharacterID());
    }

}
