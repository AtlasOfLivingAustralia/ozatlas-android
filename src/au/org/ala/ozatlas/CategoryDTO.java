package au.org.ala.ozatlas;

public class CategoryDTO {

	String infoSourceUid;
	String infoSourceName;
	String identifier;
	String stateProvince;
	String category;
	String authority;
	String categoryRemarks;
	
	public String getInfoSourceUid() {
		return infoSourceUid;
	}
	public void setInfoSourceUid(String infoSourceUid) {
		this.infoSourceUid = infoSourceUid;
	}
	public String getInfoSourceName() {
		return infoSourceName;
	}
	public void setInfoSourceName(String infoSourceName) {
		this.infoSourceName = infoSourceName;
	}
	public String getIdentifier() {
		return identifier;
	}
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	public String getStateProvince() {
		return stateProvince;
	}
	public void setStateProvince(String stateProvince) {
		this.stateProvince = stateProvince;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String getAuthority() {
		return authority;
	}
	public void setAuthority(String authority) {
		this.authority = authority;
	}
	public String getCategoryRemarks() {
		return categoryRemarks;
	}
	public void setCategoryRemarks(String categoryRemarks) {
		this.categoryRemarks = categoryRemarks;
	}	
}
