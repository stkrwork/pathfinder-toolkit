package com.lateensoft.pathfinder.toolkit.patching;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.AsyncTask;
import android.util.Log;

import com.google.inject.Injector;
import com.lateensoft.pathfinder.toolkit.BaseApplication;
import com.lateensoft.pathfinder.toolkit.db.repository.CharacterRepository;
import com.lateensoft.pathfinder.toolkit.db.repository.PartyRepository;
import com.lateensoft.pathfinder.toolkit.deprecated.v1.PTUserPrefsManager;
import com.lateensoft.pathfinder.toolkit.model.character.PathfinderCharacter;
import com.lateensoft.pathfinder.toolkit.pref.GlobalPrefs;
import com.lateensoft.pathfinder.toolkit.pref.Preferences;
import roboguice.RoboGuice;
import roboguice.util.RoboContext;

public class UpdatePatcher {
	private static final String TAG = UpdatePatcher.class.getSimpleName();

    private Context context;
    private Preferences preferences;
    private PackageInfo packageInfo;

    public UpdatePatcher(Context context) {
        this.context = context;
        Injector injector = RoboGuice.getInjector(context);
        preferences = injector.getInstance(Preferences.class);
        packageInfo = injector.getInstance(PackageInfo.class);
    }

	public boolean isPatchRequired() {
		return ( getPreviousVersion() < packageInfo.versionCode );
	}

    public boolean updateLastUsedVersion(){
        return preferences.put(GlobalPrefs.LAST_USED_VERSION, packageInfo.versionCode);
    }

    public int getPreviousVersion(){
        return preferences.get(GlobalPrefs.LAST_USED_VERSION, -1);
    }
	
	/**
	 * Applies all necessary patches to the app.
	 * @return false if was not 100% successful, and the user can expect some data loss.
	 */
	public boolean applyUpdatePatches() {
		Log.i(TAG, "Applying update patches...");
		int prevVer = getPreviousVersion();
		boolean completeSuccess = true;
		
		if (prevVer != -1) {
            // Apply each patch in order, as required.
			if (prevVer < 6) {
                // These are non-forward compatible, and will need to be updated.
                if (prevVer < 5) {
                    applyPreV5Patch();
                }
				if (!applyV5ToCurrentPatch()) {
					completeSuccess = false;
				}
			} else {
                // These should be mostly forward compatible patches from now on.
                if (prevVer < 10) {
                    if (!applyV9ToV10Patch()) {
                        completeSuccess = false;
                    }
                }
            }
		}
		
		updateLastUsedVersion();
		Log.i(TAG, "Patching complete!");
		if (!completeSuccess) {
			Log.e(TAG, "Failures occurred during patch!");
		}
		return completeSuccess;
	}

    /**
     * TODO - update party table to not have isencounterparty col
     * TODO - create encounter, partymembership, encounterparticipation tables
     * TODO - migrate partymembers to characters with party membership, and delete partymembers table
     * TODO - convert single encounter in party database to an encounter, then remove
     *
     * TODO - move Name field from fluff to character table
     * All this should be done while avoiding the use of our datamodels to provide forward compatibility.
     * This may be unavoidable though with the transfer from partymember to character
     */
    private boolean applyV9ToV10Patch() {

        return false;
    }
	
	/**
	 * In version 6, the database was overhauled, so old values must be extracted from that database, and migrated
	 * to the new one, using the old deprecated data models. The old database must be deleted once complete.
	 * 
	 * ID's in shared preferences were changed from int to long, so those must be deleted and recreated.
	 * 
	 * Encounter party in shared preference is now in database
	 * 
	 * Delete last tab in shared prefs
	 * 
	 * @return false if was not 100% successful, and the user can expect some data loss.
	 */
	private boolean applyV5ToCurrentPatch() {
		Log.i(TAG, "Applying v5 patches...");
		Context appContext = context.getApplicationContext();
		CharacterRepository characterRepo = new CharacterRepository();
		PartyRepository partyRepo = new PartyRepository();
		PTUserPrefsManager oldPrefsManager = new PTUserPrefsManager(appContext);
		com.lateensoft.pathfinder.toolkit.deprecated.v1.db.PTDatabaseManager oldDBManager = new com.lateensoft.pathfinder.toolkit.deprecated.v1.db.PTDatabaseManager(appContext);
		boolean completeSuccess = true;
		
		int oldSelectedCharacterID;
        try {
            oldSelectedCharacterID = oldPrefsManager.getSelectedCharacter();
        } catch (ClassCastException e) {
            // Cases in which this has become a long in preferences for unknown reason.
            oldSelectedCharacterID =
                    Long.valueOf(preferences.getLong(PTUserPrefsManager.KEY_SHARED_PREFS_SELECTED_CHARACTER, -1)).intValue();
        }

        int oldSelectedPartyID;
        try {
            oldSelectedPartyID = oldPrefsManager.getSelectedParty();
        } catch (ClassCastException e) {
            oldSelectedPartyID =
                    Long.valueOf(preferences.getLong(PTUserPrefsManager.KEY_SHARED_PREFS_SELECTED_PARTY, -1)).intValue();
        }
		
		// Delete, because need to convert to long later
		oldPrefsManager.remove(PTUserPrefsManager.KEY_SHARED_PREFS_SELECTED_CHARACTER);
		oldPrefsManager.remove(PTUserPrefsManager.KEY_SHARED_PREFS_SELECTED_PARTY);
		
		// Give user a week before they are asked to rate again.
		preferences.remove(GlobalPrefs.LAST_RATE_PROMPT_TIME);
		
		int[] oldCharIDs = oldDBManager.getCharacterIDs();
		com.lateensoft.pathfinder.toolkit.deprecated.v1.model.character.PTCharacter oldChar;
		PathfinderCharacter newChar;
		for (int id : oldCharIDs) {
			oldChar = oldDBManager.getCharacter(id);
			newChar = CharacterConverter.convertCharacter(oldChar);
			
			if (characterRepo.insert(newChar) == -1){
				completeSuccess = false;
				Log.e(TAG, "Error migrating character "+oldChar.getName());
			} else if (id == oldSelectedCharacterID) {
				preferences.put(GlobalPrefs.SELECTED_CHARACTER_ID, newChar.getId());
			}
		}

        // TODO convert party members to characters
//		int[] oldPartyIDs = oldDBManager.getPartyIDs();
//		com.lateensoft.pathfinder.toolkit.deprecated.v1.model.party.PTParty oldParty;
//		CampaignParty newParty;
//		for (int id : oldPartyIDs) {
//			oldParty = oldDBManager.getParty(id);
//			newParty = PartyConverter.convertParty(oldParty);
//			if (partyRepo.insert(newParty) == -1) {
//				completeSuccess = false;
//				Log.e(TAG, "Error migrating party "+oldParty.getName());
//			} if (id == oldSelectedPartyID) {
//				newSharedPrefs.putLong(AppPreferences.KEY_LONG_SELECTED_PARTY_ID, newParty.getId());
//			}
//		}

        // TODO use new EncounterParticipant classes
//		oldParty = null;
//		oldParty = oldPrefsManager.getEncounterParty();
//		if (oldParty != null) {
//			CampaignParty newEncParty = PartyConverter.convertParty(oldParty);
//			if (partyRepo.insert(newEncParty, true) == -1) {
//				completeSuccess = false;
//				Log.e(TAG, "Error migrating encounter party "+oldParty.getName());
//			}
//		}
		
		// Final cleanup
		oldPrefsManager.remove(com.lateensoft.pathfinder.toolkit.deprecated.v1.PTUserPrefsManager.KEY_SHARED_PREFS_ENCOUNTER_PARTY);
		oldPrefsManager.remove(com.lateensoft.pathfinder.toolkit.deprecated.v1.PTUserPrefsManager.KEY_SHARED_PREFS_LAST_TAB);
		
		appContext.deleteDatabase(com.lateensoft.pathfinder.toolkit.deprecated.v1.db.PTDatabaseManager.dbName);
		
		Log.i(TAG, "v5 patch complete");
		return completeSuccess;
	}
	
	private void applyPreV5Patch() {
		Log.i(TAG, "Applying pre v5 patch...");
		Context appContext = context.getApplicationContext();
		com.lateensoft.pathfinder.toolkit.deprecated.v1.db.PTDatabaseManager oldDBManager = new com.lateensoft.pathfinder.toolkit.deprecated.v1.db.PTDatabaseManager(appContext);
		oldDBManager.performUpdates(appContext);
		Log.i(TAG, "Pre v5 patch complete");
	}
	
	public static class PatcherTask extends AsyncTask<PatcherListener, Void, Boolean> {

		private PatcherListener m_listener;
        private UpdatePatcher patcher;

        public PatcherTask(UpdatePatcher patcher) {
            this.patcher = patcher;
        }
		
		@Override
		protected Boolean doInBackground(PatcherListener... params) {
			if (params.length > 0) {
				m_listener = params[0];
			}
			return patcher.applyUpdatePatches();
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (m_listener != null) {
				m_listener.onPatchComplete(result);
			}
			super.onPostExecute(result);
		}	
	}
	
	public static interface PatcherListener {
		public void onPatchComplete(boolean completeSuccess);
	}
}
