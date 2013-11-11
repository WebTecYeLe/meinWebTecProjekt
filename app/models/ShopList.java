package models;

import java.util.ArrayList;
import java.util.List;

public class ShopList {

	public List<ShopListItem> items = new ArrayList<>();

	/**
	 * beispiel im speicher - würde normalerweise in DB gespeichert
	 */
	private static ShopList instance = new ShopList();
	
	public static ShopList get(){
		return instance;
	}

	public void addItem(ShopListItem item) {
		ShopListItem existingItem = findItemByName(item.name);
		if(null == existingItem){
			items.add(item);
		}else{
			existingItem.amount += item.amount;
		}
		
	}


	public void remove(String itemName) {
		ShopListItem itemToRemove = findItemByName(itemName);
		if(null != itemToRemove) items.remove(itemToRemove);
	}

	private ShopListItem findItemByName(String name){
		for(ShopListItem i : items){
			if(i.name.equals(name)){
				return i;
			}
		}
		return null;
	}
}
