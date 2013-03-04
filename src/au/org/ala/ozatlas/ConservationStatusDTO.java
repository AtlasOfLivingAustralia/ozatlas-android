package au.org.ala.ozatlas;

public class ConservationStatusDTO {
	
	String infoSourceName;
	String infoSourceURL;
	String rawStatus;
	String status;
	String rawCode;
	String system;
	String region;
	
	public String getSystem() {
		return system;
	}
	public void setSystem(String system) {
		this.system = system;
	}
	public String getRegion() {
		return region;
	}
	public void setRegion(String region) {
		this.region = region;
	}	
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
	public String getRawStatus() {
		return rawStatus;
	}
	public void setRawStatus(String rawStatus) {
		this.rawStatus = rawStatus;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getRawCode() {
		return rawCode;
	}
	public void setRawCode(String rawCode) {
		this.rawCode = rawCode;
	}
}
