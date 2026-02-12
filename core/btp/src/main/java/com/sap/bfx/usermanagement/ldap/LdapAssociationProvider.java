package com.sap.bfx.usermanagement.ldap;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.sap.bfx.usermanagement.utility.User;
import org.apache.commons.collections4.map.LRUMap;
import org.apache.commons.lang3.StringUtils;

/**
 * collects association id and userName to ldap-path.
 */
public final class LdapAssociationProvider {

	private static final LdapAssociationProvider INSTANCE = new LdapAssociationProvider();

	private final int LRUMAPSIZE = 150000; // no. of users before LRU mechanism starts (~10-20 MB heap)

	private final Map<String, String> idToPath;
	private final Map<String, String> userNameToPath;
	private final Map<String, String> idToUserName;

	private LdapAssociationProvider() {
		idToPath = Collections.synchronizedMap(new LRUMap<String, String>(LRUMAPSIZE));
		userNameToPath = Collections.synchronizedMap(new LRUMap<String, String>(LRUMAPSIZE));
		idToUserName = Collections.synchronizedMap(new LRUMap<String, String>(LRUMAPSIZE));
	}

	public static LdapAssociationProvider getInstance() {
		return INSTANCE;
	}

	public void setFromUser(User user) {
		String dn = user.getDn();
		String id = user.getId();
		String userName = user.getUserName();
		if (StringUtils.isNotEmpty(dn) && StringUtils.isNotEmpty(id) && StringUtils.isNotEmpty(userName)) {
			idToPath.put(id, dn);
			userNameToPath.put(userName, dn);
			idToUserName.put(id, userName);
		}
	}

	public String getPathFromId(String id) {
		return idToPath.get(id);
	}

	public void removeCacheEntryById(String id) {
		idToPath.remove(id);
		idToUserName.remove(id);
		if (idToUserName.containsKey(id)) {
			userNameToPath.remove(idToUserName.get(id));
		}
	}

	public Set<String> getUserNameFromId(Set<String> ids) {
		Set<String> result = new HashSet<String>();
		for (String aGpsID : ids) {
			String userName = idToUserName.get(aGpsID);
			if (!StringUtils.isEmpty(userName)) {
				result.add(userName);
			}
		}
		return result;
	}

	public String getPathFromUserName(String userName) {
		return userNameToPath.get(userName);
	}

	public void removeCacheEntryByUserName(String userName) {
		userNameToPath.remove(userName);
		if (idToUserName.containsValue(userName)) {
			Set<Entry<String, String>> entries = idToUserName.entrySet();
			for (Entry<String, String> entry : entries) {
				if (entry.getValue().matches(userName)) {
					idToPath.remove(entry.getKey());
					idToUserName.remove(entry.getKey());
					break;
				}
			}
		}
	}
}