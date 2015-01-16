package org.zenu.home;

import android.os.Parcel;
import android.os.Parcelable;
import android.location.Location;


public class ObjectParcelable<T> implements Parcelable
{
	public T Data = null;

	public ObjectParcelable(T in)
	{
		Data = in;
	}

	@Override
	public int describeContents()
	{
		return(0);
	}

	@Override
	public void writeToParcel(Parcel out, int flags)
	{
		out.writeValue(Data);
	}

	 public static final Parcelable.Creator<ObjectParcelable> CREATOR =
	 	new Parcelable.Creator<ObjectParcelable>()
		{
			public ObjectParcelable createFromParcel(Parcel in)
			{
				return new ObjectParcelable(in.readValue(Location.class.getClassLoader()));
			}

			public ObjectParcelable[] newArray(int size)
			{
				return new ObjectParcelable[size];
			}
		};
}
