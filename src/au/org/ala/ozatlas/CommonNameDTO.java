package au.org.ala.ozatlas;

public class CommonNameDTO {
	
	String infoSourceName;
	String infoSourceURL;
	String identifier;
	String guid;
	String nameString;
	Boolean isPreferred;
	
	public String getInfoSourceName() {
		return infoSourceName;
	}
	public void setInfoSourceName(String infoSourceName) {
		this.infoSourceName = infoSourceName;
	}
	public String getInfoSourceURL() {
		return infoSourceURL;
	}
	public void setInfoSourceURL(String infoSourceURL) {
		this.infoSourceURL = infoSourceURL;
	}
	public String getIdentifier() {
		return identifier;
	}
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	public String getGuid() {
		return guid;
	}
	public void setGuid(String guid) {
		this.guid = guid;
	}
	public String getNameString() {
		return nameString;
	}
	public void setNameString(String nameString) {
		this.nameString = nameString;
	}
	public Boolean getIsPreferred() {
		return isPreferred;
	}
	public void setIsPreferred(Boolean isPreferred) {
		this.isPreferred = isPreferred;
	}
}
