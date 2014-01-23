package com.lateensoft.pathfinder.toolkit.views.character;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.lateensoft.pathfinder.toolkit.R;
import com.lateensoft.pathfinder.toolkit.db.repository.PTAbilityRepository;
import com.lateensoft.pathfinder.toolkit.db.repository.PTArmorRepository;
import com.lateensoft.pathfinder.toolkit.db.repository.PTCombatStatRepository;
import com.lateensoft.pathfinder.toolkit.db.repository.PTSaveRepository;
import com.lateensoft.pathfinder.toolkit.model.character.stats.PTAbilitySet;
import com.lateensoft.pathfinder.toolkit.model.character.stats.PTCombatStatSet;
import com.lateensoft.pathfinder.toolkit.model.character.stats.PTSave;
import com.lateensoft.pathfinder.toolkit.model.character.stats.PTSaveSet;

public class PTCharacterCombatStatsFragment extends PTCharacterSheetFragment
		implements OnFocusChangeListener, OnEditorActionListener {

	@SuppressWarnings("unused")
	private static final String TAG = PTCharacterCombatStatsFragment.class.getSimpleName();

	static final int FORT_KEY = 0;
	static final int REF_KEY = 1;
	static final int WILL_KEY = 2;
	
	private static enum EAbilityMod { INIT, AC, CMB, CMD, FORT, REF, WILL };
	private EAbilityMod m_abilityModSelectedForEdit;

	private TextView m_currentHPTextView;
	private EditText m_totalHPEditText;
	private EditText m_damageReductEditText;
	private EditText m_woundsEditText;
	private EditText m_nonLethalDmgEditText;

	private EditText m_baseSpeedEditText;

	private TextView m_initTextView;
	private TextView m_initAbilityTv;
	private EditText m_initMiscEditText;

	private TextView m_ACTextView;
	private EditText m_armourBonusEditText;
	private EditText m_shieldBonusEditText;
	private TextView m_ACAbilityTv;
	private EditText m_ACSizeEditText;
	private EditText m_naturalArmourEditText;
	private EditText m_deflectEditText;
	private EditText m_ACMiscEditText;
	private TextView m_ACTouchTextView;
	private TextView m_ACFFTextView;
	private EditText m_spellResistEditText;

	private EditText m_BABPrimaryEditText;
	private EditText m_BABSecondaryEditText;
	private TextView m_CMBTextView;
	private EditText m_CmbBABEditText;
	private TextView m_CMBAbilityTv;
	private EditText m_CMBSizeEditText;
	private TextView m_CMDTextView;
	private TextView m_CMDAbilityTv;
	private EditText m_CMDMiscModEditText;

	private TextView m_fortTextView;
	private EditText m_fortBaseEditText;
	private EditText m_fortAbilityModEditText;
	private EditText m_fortMagicModEditText;
	private EditText m_fortMiscModEditText;
	private EditText m_fortTempModEditText;

	private TextView m_refTextView;
	private EditText m_refBaseEditText;
	private EditText m_refAbilityModEditText;
	private EditText m_refMagicModEditText;
	private EditText m_refMiscModEditText;
	private EditText m_refTempModEditText;

	private TextView m_willTextView;
	private EditText m_willBaseEditText;
	private EditText m_willAbilityModEditText;
	private EditText m_willMagicModEditText;
	private EditText m_willMiscModEditText;
	private EditText m_willTempModEditText;
	
	private OnAbilityTextClickListener m_abilityTextListener;
	
	private PTCombatStatRepository m_statsRepo;
	private PTSaveRepository m_saveRepo;
	private PTAbilityRepository m_abilityRepo;
	private PTArmorRepository m_armorRepo;
	
	private PTCombatStatSet m_combatStats;
	private PTSaveSet m_saveSet;
	private PTAbilitySet m_abilitySet;
	private int m_maxDex;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		m_statsRepo = new PTCombatStatRepository();
		m_saveRepo = new PTSaveRepository();
		m_abilityRepo = new PTAbilityRepository();
		m_armorRepo = new PTArmorRepository();
		
		m_abilityTextListener = new OnAbilityTextClickListener();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		setRootView(inflater.inflate(
				R.layout.character_combat_stats_fragment, container, false));
		setupViews(getRootView());

		return getRootView();
	}

	private void updateAllViews() {
		updateHPViews();
		updateSpeedViews();
		updateInitiativeViews();
		updateACViews();
		updateBABViews();
		updateCombatManeuverViews();
		updateSaveViews();
	}
	
	private void updateAbilityView(TextView abilityTv) {
		int abilityKey = -1;
		if (abilityTv == m_initAbilityTv) {
			abilityKey = m_combatStats.getInitAbilityKey();
		} else if (abilityTv == m_ACAbilityTv) {
			abilityKey = m_combatStats.getACAbilityKey();
		} else if (abilityTv == m_CMBAbilityTv) {
			abilityKey = m_combatStats.getCMBAbilityKey();
		} else if (abilityTv == m_CMDAbilityTv) {
			abilityKey = m_combatStats.getCMDAbilityKey();
		}
		
		// TODO add saves
		
		if (abilityKey != -1) {
			PTAbilitySet.getAbilityShortNameMap();
			abilityTv.setText(PTAbilitySet.getAbilityShortNameMap().get(abilityKey)
					+ " (" + m_abilitySet.getTotalAbilityMod(abilityKey, m_maxDex) + ")");
		}
	}

	/**
	 * Updates all stats for HP
	 */
	private void updateHP() {
		m_combatStats.setTotalHP(getEditTextInt(m_totalHPEditText));
		m_combatStats.setWounds(getEditTextInt(m_woundsEditText));
		m_combatStats.setNonLethalDamage(getEditTextInt(m_nonLethalDmgEditText));
		m_combatStats.setDamageReduction(getEditTextInt(m_damageReductEditText));
		updateHPViews();
	}

	/**
	 * Updates all views for HP
	 */
	private void updateHPViews() {
		setIntText(m_currentHPTextView, m_combatStats.getCurrentHP());
		setIntText(m_totalHPEditText, m_combatStats.getTotalHP());
		setIntText(m_woundsEditText, m_combatStats.getWounds());
		setIntText(m_nonLethalDmgEditText, m_combatStats.getNonLethalDamage());
		setIntText(m_damageReductEditText, m_combatStats.getDamageReduction());
	}

	/**
	 * Updates all stats for speed
	 */
	private void updateSpeed() {
		m_combatStats.setBaseSpeed(getEditTextInt(m_baseSpeedEditText));
	}

	/**
	 * Updates all views for speed
	 */
	private void updateSpeedViews() {
		setIntText(m_baseSpeedEditText, m_combatStats.getBaseSpeed());
	}

	/**
	 * Updates all stats for initiative
	 */
	private void updateInitiative() {
		m_combatStats.setInitiativeMiscMod(getEditTextInt(m_initMiscEditText));
		updateInitiativeViews();
	}

	/**
	 * Updates all views for initiative
	 */
	private void updateInitiativeViews() {
		setIntText(m_initTextView, m_combatStats.getInitiativeMod(m_abilitySet, m_maxDex));
		updateAbilityView(m_initAbilityTv);
		setIntText(m_initMiscEditText, m_combatStats.getInitiativeMiscMod());
	}

	/**
	 * Updates all stats for AC
	 */
	private void updateAC() {
		m_combatStats.setACArmourBonus(getEditTextInt(m_armourBonusEditText));
		m_combatStats.setACShieldBonus(getEditTextInt(m_shieldBonusEditText));
		m_combatStats.setSizeModifier(getEditTextInt(m_ACSizeEditText));
		m_combatStats.setNaturalArmour(getEditTextInt(m_naturalArmourEditText));
		m_combatStats.setDeflectionMod(getEditTextInt(m_deflectEditText));
		m_combatStats.setACMiscMod(getEditTextInt(m_ACMiscEditText));
		m_combatStats.setSpellResistance(getEditTextInt(m_spellResistEditText));
		updateACViews();
	}

	/**
	 * Updates all views for ac
	 */
	private void updateACViews() {
		setIntText(m_ACTextView, m_combatStats.getTotalAC(m_abilitySet, m_maxDex));
		setIntText(m_ACTouchTextView, m_combatStats.getTouchAC(m_abilitySet, m_maxDex));
		setIntText(m_ACFFTextView, m_combatStats.getFlatFootedAC(m_abilitySet, m_maxDex));
		setIntText(m_armourBonusEditText, m_combatStats.getACArmourBonus());
		setIntText(m_shieldBonusEditText, m_combatStats.getACShieldBonus());
		updateAbilityView(m_ACAbilityTv);
		updateSizeModViews();
		setIntText(m_naturalArmourEditText, m_combatStats.getNaturalArmour());
		setIntText(m_deflectEditText, m_combatStats.getDeflectionMod());
		setIntText(m_ACMiscEditText, m_combatStats.getACMiscMod());
		setIntText(m_spellResistEditText, m_combatStats.getSpellResist());
	}

	/**
	 * Updates all stats for BAB
	 */
	private void updateBAB() {
		m_combatStats.setBABPrimary(getEditTextInt(m_BABPrimaryEditText));
		m_combatStats.setBABSecondary(m_BABSecondaryEditText.getText().toString());
		updateBABViews();
	}

	private void updateBABViews() {
		updateCombatManeuverViews();
		m_BABSecondaryEditText.setText(m_combatStats
				.getBABSecondary());
	}

	/**
	 * Updates all stats for combat maneuvers
	 */
	private void updateCombatManeuvers() {
		m_combatStats.setBABPrimary(getEditTextInt(m_CmbBABEditText));
		m_combatStats.setSizeModifier(getEditTextInt(m_CMBSizeEditText));
		m_combatStats.setCMDMiscMod(getEditTextInt(m_CMDMiscModEditText));
		updateCombatManeuverViews();
	}

	/**
	 * Updates all stats for saves
	 */
	private void updateSaves() {
		updateFort();
		updateRef();
		updateWill();
	}

	private void updateFort() {
		m_saveSet.getSave(0).setBase(getEditTextInt(m_fortBaseEditText));
		m_saveSet.getSave(0).setAbilityMod(getEditTextInt(m_fortAbilityModEditText));
		m_saveSet.getSave(0).setMagicMod(getEditTextInt(m_fortMagicModEditText));
		m_saveSet.getSave(0).setMiscMod(getEditTextInt(m_fortMiscModEditText));
		m_saveSet.getSave(0).setTempMod(getEditTextInt(m_fortTempModEditText));
	}

	private void updateRef() {
		m_saveSet.getSave(1).setBase(getEditTextInt(m_refBaseEditText));
		m_saveSet.getSave(1).setAbilityMod(getEditTextInt(m_refAbilityModEditText));
		m_saveSet.getSave(1).setMagicMod(getEditTextInt(m_refMagicModEditText));
		m_saveSet.getSave(1).setMiscMod(getEditTextInt(m_refMiscModEditText));
		m_saveSet.getSave(1).setTempMod(getEditTextInt(m_refTempModEditText));
	}

	private void updateWill() {
		m_saveSet.getSave(2).setBase(getEditTextInt(m_willBaseEditText));
		m_saveSet.getSave(2).setAbilityMod(getEditTextInt(m_willAbilityModEditText));
		m_saveSet.getSave(2).setMagicMod(getEditTextInt(m_willMagicModEditText));
		m_saveSet.getSave(2).setMiscMod(getEditTextInt(m_willMiscModEditText));
		m_saveSet.getSave(2).setTempMod(getEditTextInt(m_willTempModEditText));
	}

	/**
	 * Updates all views for combat maneuvers
	 */
	private void updateCombatManeuverViews() {
		setIntText(m_CMBTextView, m_combatStats.getCombatManeuverBonus(m_abilitySet, m_maxDex));
		updatePrimaryBABViews();
		updateAbilityView(m_CMBAbilityTv);
		updateSizeModViews();
		setIntText(m_CMDTextView, m_combatStats.getCombatManeuverDefense(m_abilitySet, m_maxDex));
		updateAbilityView(m_CMDAbilityTv);
		setIntText(m_CMDMiscModEditText, m_combatStats.getCMDMiscMod());
	}

	/**
	 * Updates the BAB edit texts in BAB and CMB
	 */
	private void updatePrimaryBABViews() {
		setIntText(m_BABPrimaryEditText, m_combatStats.getBABPrimary());
		setIntText(m_CmbBABEditText, m_combatStats.getBABPrimary());
	}

	/**
	 * Updates the size mod edit texts in AC and CMB
	 */
	private void updateSizeModViews() {
		setIntText(m_ACSizeEditText, m_combatStats.getSizeModifier());
		setIntText(m_CMBSizeEditText, m_combatStats.getSizeModifier());
	}

	private void setIntText(TextView textView, int number) {
		textView.setText(Integer.toString(number));
	}

	private void updateSaveViews() {
		updateFortSaveViews();
		updateRefSaveViews();
		updateWillSaveViews();
	}

	private void updateFortSaveViews() {
		setIntText(m_fortTextView, m_saveSet.getSave(0).getTotal());
		setIntText(m_fortBaseEditText, m_saveSet.getSave(0).getBase());
		setIntText(m_fortAbilityModEditText, m_saveSet.getSave(0).getAbilityMod());
		setIntText(m_fortMagicModEditText, m_saveSet.getSave(0).getMagicMod());
		setIntText(m_fortMiscModEditText, m_saveSet.getSave(0).getMiscMod());
		setIntText(m_fortTempModEditText, m_saveSet.getSave(0).getTempMod());
	}

	private void updateRefSaveViews() {
		setIntText(m_refTextView, m_saveSet.getSave(1).getTotal());
		setIntText(m_refBaseEditText, m_saveSet.getSave(1).getBase());
		setIntText(m_refAbilityModEditText, m_saveSet.getSave(1).getAbilityMod());
		setIntText(m_refMagicModEditText, m_saveSet.getSave(1).getMagicMod());
		setIntText(m_refMiscModEditText, m_saveSet.getSave(1).getMiscMod());
		setIntText(m_refTempModEditText, m_saveSet.getSave(1).getTempMod());
	}

	private void updateWillSaveViews() {
		setIntText(m_willTextView, m_saveSet.getSave(2).getTotal());
		setIntText(m_willBaseEditText, m_saveSet.getSave(2).getBase());
		setIntText(m_willAbilityModEditText, m_saveSet.getSave(2).getAbilityMod());
		setIntText(m_willMagicModEditText, m_saveSet.getSave(2).getMagicMod());
		setIntText(m_willMiscModEditText, m_saveSet.getSave(2).getMiscMod());
		setIntText(m_willTempModEditText, m_saveSet.getSave(2).getTempMod());
	}

	/**
	 * 
	 * @param editText
	 * @return the value in the edit text. Returns Integer.MAX_VALUE if the
	 *         parse failed
	 */
	private int getEditTextInt(EditText editText) {
		try {
			return Integer.parseInt(editText.getText().toString());
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	// Sets edittext listeners
	private void setEditTextListeners(EditText editText) {
		editText.setOnFocusChangeListener(this);
		editText.setOnEditorActionListener(this);
	}
	
	private void setAbilityTextViewListener(TextView tv) {
		tv.setOnClickListener(m_abilityTextListener);
	}
	
	private class OnAbilityTextClickListener implements OnClickListener {

		@Override public void onClick(View v) {
			int defaultAbilityKey = -1;
			int currentAbility = PTAbilitySet.KEY_DEX;
			if (v == m_initAbilityTv) {
				m_abilityModSelectedForEdit = EAbilityMod.INIT;
				defaultAbilityKey = PTCombatStatSet.DEFUALT_INIT_ABILITY_KEY;
				currentAbility = m_combatStats.getInitAbilityKey();
			} else if (v == m_ACAbilityTv) {
				m_abilityModSelectedForEdit = EAbilityMod.AC;
				defaultAbilityKey = PTCombatStatSet.DEFUALT_AC_ABILITY_KEY;
				currentAbility = m_combatStats.getACAbilityKey();
			} else if (v == m_CMBAbilityTv) {
				m_abilityModSelectedForEdit = EAbilityMod.CMB;
				defaultAbilityKey = PTCombatStatSet.DEFUALT_CMB_ABILITY_KEY;
				currentAbility = m_combatStats.getCMBAbilityKey();
			} else if (v == m_CMDAbilityTv) {
				m_abilityModSelectedForEdit = EAbilityMod.CMD;
				defaultAbilityKey = PTCombatStatSet.DEFUALT_CMD_ABILITY_KEY;
				currentAbility = m_combatStats.getCMDAbilityKey();
			}
			
			if (defaultAbilityKey != -1) {
				PTAbilitySelectionDialog dialog = 
						new PTAbilitySelectionDialog(getActivity(), currentAbility, defaultAbilityKey);
				dialog.setOnAbilitySelectedListener(new AbilityDialogListener());
				dialog.show();
			}
		}
		
	}
	
	private class AbilityDialogListener implements PTAbilitySelectionDialog.OnAbilitySelectedListener {

		@Override public void onAbilitySelected(int abilityKey) {
			if (abilityKey != 0) {
				int viewID = -1;
				switch (m_abilityModSelectedForEdit) {
				case AC:
					m_combatStats.setACAbilityKey(abilityKey);
					viewID = m_ACAbilityTv.getId();
					break;
				case CMB:
					m_combatStats.setCMBAbilityKey(abilityKey);
					viewID = m_CMBAbilityTv.getId();
					break;
				case CMD:
					m_combatStats.setCMDAbilityKey(abilityKey);
					viewID = m_CMDAbilityTv.getId();
					break;
				case FORT: // TODO
					break;
				case INIT:
					m_combatStats.setInitAbilityKey(abilityKey);
					viewID = m_initAbilityTv.getId();
					break;
				case REF: // TODO
					break;
				case WILL: // TODO
					break;
				default:
					return;
				}
				if (viewID != -1) {
					finishedEditing(viewID);
				}
			}
		}
		
	}

	// Sets up all the text and edit texts
	private void setupViews(View fragmentView) {
		m_currentHPTextView = (TextView) fragmentView
				.findViewById(R.id.textViewCurrentHP);
		m_totalHPEditText = (EditText) fragmentView
				.findViewById(R.id.editTextTotalHP);
		setEditTextListeners(m_totalHPEditText);

		m_damageReductEditText = (EditText) fragmentView
				.findViewById(R.id.editTextDamageReduction);
		setEditTextListeners(m_damageReductEditText);

		m_woundsEditText = (EditText) fragmentView
				.findViewById(R.id.editTextWounds);
		setEditTextListeners(m_woundsEditText);

		m_nonLethalDmgEditText = (EditText) fragmentView
				.findViewById(R.id.editTextNonLethalDmg);
		setEditTextListeners(m_nonLethalDmgEditText);

		m_baseSpeedEditText = (EditText) fragmentView
				.findViewById(R.id.editTextBaseSpeed);
		setEditTextListeners(m_baseSpeedEditText);

		m_initTextView = (TextView) fragmentView
				.findViewById(R.id.textViewInitiative);
		m_initAbilityTv = (TextView) fragmentView
				.findViewById(R.id.tvInitAbility);
		setAbilityTextViewListener(m_initAbilityTv);

		m_initMiscEditText = (EditText) fragmentView
				.findViewById(R.id.editTextInitMiscMod);
		setEditTextListeners(m_initMiscEditText);

		m_ACTextView = (TextView) fragmentView.findViewById(R.id.textViewAC);
		m_armourBonusEditText = (EditText) fragmentView
				.findViewById(R.id.editTextArmourBonus);
		setEditTextListeners(m_armourBonusEditText);

		m_shieldBonusEditText = (EditText) fragmentView
				.findViewById(R.id.editTextShieldBonus);
		setEditTextListeners(m_shieldBonusEditText);

		m_ACAbilityTv = (TextView) fragmentView
				.findViewById(R.id.tvACAbility);
		setAbilityTextViewListener(m_ACAbilityTv);

		m_ACSizeEditText = (EditText) fragmentView
				.findViewById(R.id.editTextACSizeMod);
		setEditTextListeners(m_ACSizeEditText);

		m_naturalArmourEditText = (EditText) fragmentView
				.findViewById(R.id.editTextNaturalArmour);
		setEditTextListeners(m_naturalArmourEditText);

		m_deflectEditText = (EditText) fragmentView
				.findViewById(R.id.editTextDeflectionMod);
		setEditTextListeners(m_deflectEditText);

		m_ACMiscEditText = (EditText) fragmentView
				.findViewById(R.id.editTextACMiscMod);
		setEditTextListeners(m_ACMiscEditText);

		m_ACTouchTextView = (TextView) fragmentView
				.findViewById(R.id.textViewTouchAC);
		m_ACFFTextView = (TextView) fragmentView
				.findViewById(R.id.textViewFlatFootedAC);
		m_spellResistEditText = (EditText) fragmentView
				.findViewById(R.id.editTextSpellResist);
		setEditTextListeners(m_spellResistEditText);

		m_BABPrimaryEditText = (EditText) fragmentView
				.findViewById(R.id.editTextBABPrimary);
		setEditTextListeners(m_BABPrimaryEditText);

		m_BABSecondaryEditText = (EditText) fragmentView
				.findViewById(R.id.editTextBABSecondary);
		setEditTextListeners(m_BABSecondaryEditText);

		m_CMBTextView = (TextView) fragmentView.findViewById(R.id.textViewCMB);
		m_CmbBABEditText = (EditText) fragmentView
				.findViewById(R.id.editTextCmbBAB);
		setEditTextListeners(m_CmbBABEditText);

		m_CMBAbilityTv = (TextView) fragmentView
				.findViewById(R.id.tvCMBAbility);
		setAbilityTextViewListener(m_CMBAbilityTv);

		m_CMBSizeEditText = (EditText) fragmentView
				.findViewById(R.id.editTextCMBSizeMod);
		setEditTextListeners(m_CMBSizeEditText);

		m_CMDTextView = (TextView) fragmentView.findViewById(R.id.textViewCMD);
		m_CMDAbilityTv = (TextView) fragmentView
				.findViewById(R.id.tvCMDAbility);
		m_CMDMiscModEditText = (EditText) fragmentView
				.findViewById(R.id.editTextCMDMiscMod);
		setAbilityTextViewListener(m_CMDAbilityTv);
		setEditTextListeners(m_CMDMiscModEditText);

		m_fortTextView = (TextView) fragmentView.findViewById(R.id.tvFort);
		m_fortBaseEditText = (EditText) fragmentView
				.findViewById(R.id.etSaveFortBase);
		m_fortAbilityModEditText = (EditText) fragmentView
				.findViewById(R.id.etSaveFortAbilityMod);
		m_fortMagicModEditText = (EditText) fragmentView
				.findViewById(R.id.etSaveFortMagicMod);
		m_fortMiscModEditText = (EditText) fragmentView
				.findViewById(R.id.etSaveFortMiscMod);
		m_fortTempModEditText = (EditText) fragmentView
				.findViewById(R.id.etSaveFortTempMod);
		setEditTextListeners(m_fortBaseEditText);
		setEditTextListeners(m_fortAbilityModEditText);
		setEditTextListeners(m_fortMagicModEditText);
		setEditTextListeners(m_fortMiscModEditText);
		setEditTextListeners(m_fortTempModEditText);

		m_refTextView = (TextView) fragmentView.findViewById(R.id.tvRef);
		m_refBaseEditText = (EditText) fragmentView
				.findViewById(R.id.etSaveRefBase);
		m_refAbilityModEditText = (EditText) fragmentView
				.findViewById(R.id.etSaveRefAbilityMod);
		m_refMagicModEditText = (EditText) fragmentView
				.findViewById(R.id.etSaveRefMagicMod);
		m_refMiscModEditText = (EditText) fragmentView
				.findViewById(R.id.etSaveRefMiscMod);
		m_refTempModEditText = (EditText) fragmentView
				.findViewById(R.id.etSaveRefTempMod);
		setEditTextListeners(m_refBaseEditText);
		setEditTextListeners(m_refAbilityModEditText);
		setEditTextListeners(m_refMagicModEditText);
		setEditTextListeners(m_refMiscModEditText);
		setEditTextListeners(m_refTempModEditText);

		m_willTextView = (TextView) fragmentView.findViewById(R.id.tvWill);
		m_willBaseEditText = (EditText) fragmentView
				.findViewById(R.id.etSaveWillBase);
		m_willAbilityModEditText = (EditText) fragmentView
				.findViewById(R.id.etSaveWillAbilityMod);
		m_willMagicModEditText = (EditText) fragmentView
				.findViewById(R.id.etSaveWillMagicMod);
		m_willMiscModEditText = (EditText) fragmentView
				.findViewById(R.id.etSaveWillMiscMod);
		m_willTempModEditText = (EditText) fragmentView
				.findViewById(R.id.etSaveWillTempMod);
		setEditTextListeners(m_willBaseEditText);
		setEditTextListeners(m_willAbilityModEditText);
		setEditTextListeners(m_willMagicModEditText);
		setEditTextListeners(m_willMiscModEditText);
		setEditTextListeners(m_willTempModEditText);
	}

	/**
	 * Updates the view which has finished being edited
	 * 
	 * @param viewID
	 */
	private void finishedEditing(int viewID) {
		if (viewID == m_woundsEditText.getId()
				|| viewID == m_totalHPEditText.getId()
				|| viewID == m_nonLethalDmgEditText.getId()
				|| viewID == m_damageReductEditText.getId())
			updateHP();

		else if (viewID == m_baseSpeedEditText.getId())
			updateSpeed();

		else if (viewID == m_initAbilityTv.getId()
				|| viewID == m_initMiscEditText.getId())
			updateInitiative();

		else if (viewID == m_armourBonusEditText.getId()
				|| viewID == m_shieldBonusEditText.getId()
				|| viewID == m_ACAbilityTv.getId()
				|| viewID == m_ACSizeEditText.getId()
				|| viewID == m_naturalArmourEditText.getId()
				|| viewID == m_deflectEditText.getId()
				|| viewID == m_ACMiscEditText.getId()
				|| viewID == m_spellResistEditText.getId()) {
			updateAC();
			updateCombatManeuverViews();
		}

		else if (viewID == m_BABPrimaryEditText.getId()
				|| viewID == m_BABSecondaryEditText.getId())
			updateBAB();

		else if (viewID == m_CmbBABEditText.getId()
				|| viewID == m_CMBAbilityTv.getId()
				|| viewID == m_CMDAbilityTv.getId()
				|| viewID == m_CMBSizeEditText.getId()
				|| viewID == m_CMDMiscModEditText.getId()) {
			updateCombatManeuvers();
			updateACViews();
		}

		else if (viewID == m_fortBaseEditText.getId()
				|| viewID == m_fortAbilityModEditText.getId()
				|| viewID == m_fortMagicModEditText.getId()
				|| viewID == m_fortMiscModEditText.getId()
				|| viewID == m_fortTempModEditText.getId()) {
			updateFort();
			updateFortSaveViews();
		}

		else if (viewID == m_refBaseEditText.getId()
				|| viewID == m_refAbilityModEditText.getId()
				|| viewID == m_refMagicModEditText.getId()
				|| viewID == m_refMiscModEditText.getId()
				|| viewID == m_refTempModEditText.getId()) {
			updateRef();
			updateRefSaveViews();
		}

		else if (viewID == m_willBaseEditText.getId()
				|| viewID == m_willAbilityModEditText.getId()
				|| viewID == m_willMagicModEditText.getId()
				|| viewID == m_willMiscModEditText.getId()
				|| viewID == m_willTempModEditText.getId()) {
			updateWill();
			updateWillSaveViews();
		}

	}

	// TODO this should be an object
	public void onFocusChange(View view, boolean hasFocus) {
		if (!hasFocus) {
			finishedEditing(view.getId());
		}
	}

	// TODO this should be an object
	public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
		finishedEditing(view.getId());
		return false;
	}

	@Override
	public void updateFragmentUI() {
		updateAllViews();
	}
	
	@Override
	public String getFragmentTitle() {
		return getString(R.string.tab_character_combat_stats);
	}

	@Override
	public void updateDatabase() {
		updateHP();
		updateSpeed();
		updateInitiative();
		updateAC();
		updateBAB();
		updateCombatManeuvers();
		updateSaves();
		
		if (m_combatStats != null) {
			m_statsRepo.update(m_combatStats);
			PTSave[] saves = m_saveSet.getSaves();
			for(PTSave save : saves) {
				m_saveRepo.update(save);
			}
		}
	}

	@Override
	public void loadFromDatabase() {
		m_combatStats = m_statsRepo.query(getCurrentCharacterID());
		m_saveSet = new PTSaveSet(m_saveRepo.querySet(getCurrentCharacterID()));
		m_maxDex = m_armorRepo.getMaxDex(getCurrentCharacterID());
		m_abilitySet = new PTAbilitySet(m_abilityRepo.querySet(getCurrentCharacterID()));
	}

}
