package au.org.ala.ozatlas;

import java.util.List;

import android.location.Address;

public interface GeocodeCallback {
	public void geocodeCallback(List<Address> address);
}
