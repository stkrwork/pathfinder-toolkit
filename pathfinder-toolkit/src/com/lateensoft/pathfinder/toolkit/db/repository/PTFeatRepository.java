package com.lateensoft.pathfinder.toolkit.db.repository;

import java.util.Hashtable;

import android.content.ContentValues;
import android.database.Cursor;

import com.lateensoft.pathfinder.toolkit.db.repository.PTTableAttribute.SQLDataType;
import com.lateensoft.pathfinder.toolkit.model.character.PTFeat;

public class PTFeatRepository extends PTBaseRepository<PTFeat> {
	public static final String TABLE = "Feat";
	public static final String ID = "feat_id";
	private final String NAME = "Name";
	private final String DESC = "Description";
	
	public PTFeatRepository() {
		super();
		PTTableAttribute id = new PTTableAttribute(ID, SQLDataType.INTEGER, true);
		PTTableAttribute characterId = new PTTableAttribute(CHARACTER_ID, SQLDataType.INTEGER);
		PTTableAttribute name = new PTTableAttribute(NAME, SQLDataType.TEXT);
		PTTableAttribute desc = new PTTableAttribute(DESC, SQLDataType.TEXT);
		PTTableAttribute[] attributes = {id, characterId, name, desc};
		m_tableInfo = new PTTableInfo(TABLE, attributes);
	}

	@Override
	protected PTFeat buildFromHashTable(Hashtable<String, Object> hashTable) {
		int id = ((Long) hashTable.get(ID)).intValue();
		int characterId = ((Long) hashTable.get(CHARACTER_ID)).intValue();
		String name = (String) hashTable.get(NAME);
		String desc = (String) hashTable.get(DESC);
		
		PTFeat feat = new PTFeat(id, characterId, name, desc);
		return feat;
	}

	@Override
	protected ContentValues getContentValues(PTFeat object) {
		ContentValues values = new ContentValues();
		if(isIDSet(object)) { 
			values.put(ID, object.getID());
		}
		values.put(CHARACTER_ID, object.getCharacterID());
		values.put(NAME, object.getName());
		values.put(DESC, object.getDescription());
		return values;
	}
	
	/**
	 * Returns all feats for the character with characterId
	 * @param characterId
	 * @return Array of PTFeat, ordered alphabetically by name
	 */
	public PTFeat[] querySet(long characterId) {
		String selector = CHARACTER_ID + "=" + characterId; 
		String orderBy = NAME + " ASC";
		String table = m_tableInfo.getTable();
		String[] columns = m_tableInfo.getColumns();
		Cursor cursor = getDatabase().query(true, table, columns, selector, 
				null, null, null, orderBy, null);
		
		PTFeat[] feats = new PTFeat[cursor.getCount()];
		cursor.moveToFirst();
		int i = 0;
		while (!cursor.isAfterLast()) {
			Hashtable<String, Object> hashTable =  getTableOfValues(cursor);
			feats[i] = buildFromHashTable(hashTable);
			cursor.moveToNext();
			i++;
		}
		return feats;
	}

}
